# Benchmarks

TenorClef records scenario / live-run metrics under `adris.altoclef.benchmark`.

## Phase 10 — offline harness

| Type | Role |
|------|------|
| `Scenario` / `ScenarioContext` | Named runnable |
| `BenchmarkResult` | name, success, durationMs, deaths/replans/pathFails, notes, counters |
| `BenchmarkCounters` | TaskResult / RecoveryAction / FailureReason / threat tallies |
| `BenchmarkHarness` | run / aggregate / JSON export |
| `BenchmarkJson` | Hand-rolled JSON (no Jackson) |
| `MockScenarios` | Offline fixtures |

Run offline unit tests: `BenchmarkAggregationTest`, `MockScenariosTest`.

## Post-phase-10 — live hooks

Hypothesis: a singleton optional `LiveBenchmarkSession` consulted from
`RecoveryManager` / `ThreatMonitor` / `PlanExecutor` / `Task` is enough; avoid
ticking every entity.

| Type | Role |
|------|------|
| `LiveBenchmarkSession` | start/stop around a named run; null when inactive (no-op hooks) |
| `BenchmarkFiles` | `<gameDir>/altoclef/bench/` (fallback `altoclef/bench` or `bench-out`) |
| `@bench` | `start <name>` / `stop` / `status` / `goal <item> [count]` |

### Recorded metrics

- durationMs, success/fail
- TaskResult tallies (succeed / fail / recovery apply)
- FailureReason counts
- RecoveryAction counts (via `RecoveryManager.apply`)
- ThreatLevel peak; HIGH pause + CRITICAL fail counts
- PlanExecutor replan count

### Commands

```
@bench start my-run
@bench status
@bench stop
@bench goal cobblestone 64
```

`@bench goal` starts a session, fires `AcquireItemGoal` via `PlanRunnerTask`, and
stops (writing JSON) when the goal completes or fails.

### Safety

When no session is active, all `LiveBenchmarkSession.note*` helpers return
immediately. Gameplay paths must not depend on an active bench session.

### JSON

Live exports use `BenchmarkJson.toLiveJson` →
`altoclef/bench/<timestamp>-<name>.json` with `type=live`, peak threat, pause/fail
counts, and a nested `result` (same shape as Phase 10 `BenchmarkResult`).

## Travel misses, 2026-09-28 (3 reps each, seed 12345)

Source: `pathbench_travel_baritone_20260928_205606.csv` (47/48) and
`pathbench_travel_kinematic_20260928_203533.csv` (46/48). CSVs are local, not committed.

| mover | goal | offset (dx,dz) | rep | result | ticks | endDist |
|---|---|---|---|---|---|---|
| baritone | 15 | 68,-68 | 0 | STALLED | 715 | 44.2 |
| kinematic | 12 | -96,0 | 0 | STALLED | 967 | 17.3 |
| kinematic | 14 | 0,-96 | 1 | STALLED | 704 | 47.7 |

Every miss is one rep of a 96-block goal; the other reps of the same goal reached it, and no goal
failed for both movers. So there is no repeat trouble spot in these runs. Those runs logged only the summary line, so
their stall causes are unknown.

Since then each travel trial also logs one line (`grep -a "PATHBENCH TRIAL" run/logs/latest.log`):

```
PATHBENCH TRIAL mover=kinematic goal=14 rep=0 result=GOAL ticks=338 end=53, 74, -217 endDist=1.9 bestDist=3.0 bestAt=335 lastMoveAt=337 firstMove=11 activeAtEnd=true
```

`end` is the final block position; `bestDist`/`bestAt` are the closest approach and when it happened
(only gains over 1 block count); `lastMoveAt` is the last tick with movement over 0.3 blocks;
`activeAtEnd` is whether the mover was still running before the bench cancelled it. A stall shows as
a large `bestDist` with `bestAt` well before `ticks`, and `end` gives the place to inspect.
Tested in-game on goals 12, 14, 15 (kinematic, 1 rep): 3/3 reached, one line per trial.

## Travel re-run with per-trial lines, 2026-09-28 21:30–22:05 (3 reps each)

| mover | goals reached | avg ticks (reached) | avg ticks to first move |
|---|---|---|---|
| baritone | 48/48 | 362 | 8.8 |
| kinematic | 46/48 | 393 | 10.5 |

Kinematic stalls, from the `PATHBENCH TRIAL` lines:

| goal | rep | end | endDist | bestDist | bestAt | lastMoveAt | ticks | reading |
|---|---|---|---|---|---|---|---|---|
| 11 (-68,68) | 0 | 57, 88, -27 | 68.4 | 66.2 | 546 | 925 | 949 | kept moving for ~380 ticks without getting closer: wandering, at y=88 (well above the ~70 start) |
| 13 | 2 | -4, 83, -171 | 11.4 | 11.9 | 660 | 664 | 1063 | stopped moving 11 blocks short and stayed put for ~400 ticks |

In both, `activeAtEnd=true`: the mover still reported itself active, so these are silent stalls, not
aborts. Goals 11 and 13 did not stall in the earlier 20:35 run, so neither is a repeat spot yet. The
client log has no kinematic-mover output around either trial. Kinematic movement changes are frozen,
so these are recorded, not fixed.

### Kinematic after merging Ostinato `origin/1.16.1` (2026-09-28 23:18)

Ostinato's `claude/blissful-keller-dyrn6h` merged 12 kinematic/jump/slime commits from `origin/1.16.1`.

- **Full run `pathbench travel kinematic 3`:** 42/48 goals, average 389 ticks. The previous result was 46/48.
  - Four trials stalled motionless, with the custom goal process still active and nothing logged:
    - goal 4, reps 1 and 2, both at 46,73,-102;
    - goal 6, rep 0;
    - goal 7, rep 0.
  - Two misses followed a player death: goal 11 rep 1 and goal 13 rep 1. In each, `Death position saved.` appeared about 20 s before the trial ended. The cause of death was not captured.
- **Subset rerun of goals 4, 6, 7, 11 and 13 (3 reps each):** 12/15.
  - Goals 4, 7, 11 and 13 reached the goal 3/3, so none of the stalls reproduced deterministically.
  - Goal 6 ended `STOPPED` 2.9 blocks from the goal in all 3 reps: the process finished, `activeAtEnd=false`.
- **Movement faults:** Ostinato's `movementFault` sink defaults to a no-op, so PathBench now logs each fault as a `PATHBENCH FAULT <code> <evidence>` line during travel runs. No faults fired in the subset rerun. The kinematic M01 watchdog (motionless for more than 20 ticks while the controller is driving) was therefore never triggered.
- **Open:** in these runs the silent stalls come from a path where the kinematic controller is not driving, since M01 never fired. That points at Baritone's own executor, or at the controller returning -1 every tick, not at the M01 watchdog. The runs do not yet show which.
