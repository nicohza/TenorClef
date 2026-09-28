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
