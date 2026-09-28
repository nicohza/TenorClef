# Small-objective runs (1.16.1)

Fresh world each run (seed 12345, empty inventory, `movementbackend baritone`, autorun via
`altoclef_settings.json`). Evidence column says what was actually observed in `latest.log`.

| Date | Command | Result | Time | Evidence |
|---|---|---|---|---|
| 2026-09-28 | `get cobblestone 3` | finished | 30.8 s | vanilla advancement *Stone Age* (cobblestone obtained); count of 3 not independently checked |
| 2026-09-28 | `get cobblestone 3` (after Butler fix) | finished | 63.9 s | *Stone Age*; 0 whisper-loop lines (was 37) |
| 2026-09-28 | `get iron_pickaxe` | finished | 115.2 s | *Stone Age* → *Getting an Upgrade* → *Acquire Hardware* → *Isn't It Iron Pick* |
| 2026-09-28 | `get water_bucket` | finished | 122.3 s | *Acquire Hardware* (iron for the bucket); bucket fill is **task-reported only** (no vanilla advancement exists for it) |

| 2026-09-28 | `goto 87 -5 -115` (below bedrock, unreachable) | aborted, as intended | 68 s | ALTERNATE_PATH 1/3 → RETRY 2/3 → RETRY 3/3 → "Progress retries exhausted, aborting goal"; after the verified fix: `outcome=FAILED reason=TIMEOUT verified=false` |

Recovery exercised once (the unreachable goto). Before the fix its TRAVEL line mislabelled the abort ARRIVED.
Single run per objective; timings are not a benchmark.
