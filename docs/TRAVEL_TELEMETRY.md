# Travel telemetry (`TRAVEL` line)

Every `GetToBlockTask` run logs exactly one structured line when it stops
(`adris.altoclef.movement.TravelTrace`, emitted from `GetToBlockTask.onStop`):

```
TRAVEL goal=GetToBlock start=x,y,z target=x,y,z requested=<backend> executed=<backend> fallback=<bool>
       pathFoundTick=<n> ticks=<n> traveled=<blocks> remaining=<blocks> stallTicks=<n>
       outcome=ARRIVED|FAILED|STOPPED reason=<FailureReason|INTERRUPTED|-> verified=<bool>
```

| Field | Meaning |
|---|---|
| `requested` | Ostinato `movementBackend` preference at dispatch (`MovementEngineAdapter.requestedBackend()`) |
| `executed` | Mover that took the last dispatch: an engine backend (`BARITONE`, `TUNGSTEN`, ...), `CUSTOM_GOAL_PROCESS` (adapter fallback), or `TUNGSTEN_TASK` (GetToBlockTask's direct Tungsten leg) |
| `fallback` | Sticky: true if any dispatch ran on something other than `requested` |
| `pathFoundTick` | First task tick on which Baritone/engine reported pathing (-1 = never) |
| `traveled` | Summed per-tick movement; jumps ≥10 blocks (teleport/respawn) are excluded |
| `remaining` | Distance from the final position to the target **block centre**. `GetToBlock` accepts nearby positions, so an ARRIVED run can show 1–3 blocks here |
| `stallTicks` | Ticks with movement < 0.01 blocks (includes planning time) |
| `verified` | `GetToBlockTask.isFinished()`, a world-position check — not engine status (Ostinato's `HybridMovementEngine.status()` never reports ARRIVED) |

## Reproducing

`@goto x y z` with `movementbackend baritone|tungsten` in `run/baritone/settings.txt`, then
`grep -a "TRAVEL goal=" run/logs/latest.log` (the line appears twice: log + chat echo).

## Observed results (1.16.1, seed 12345, `goto 87 69 -115`, spawn ≈ 57–63,68–71,-110–-112)

See the table in the commit that added this file / the session report; only runs actually executed
are listed there. Nothing here is extrapolated.
