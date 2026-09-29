# Capabilities

| capability | owner | live path? | scenario | status | last evidence |
|---|---|---|---|---|---|
| Nether portal bucket build | TenorClef (ConstructNetherPortalBucketTask) | yes (speedrun PORTAL phase) | `@pathbench portal 3`, seed 12345, sky pad y=120 with 5x5 lava pool, natural lava wiped within 64 | partial | 1/3 (GOAL 1479t; DIED burned; TIMEOUT frame unreachable, pool drained). Before f11e38bb: 1/3, with one S211 false stall mid-build; after: 0 S211 fires in 3 reps |
| BlockScanner reset during background rescan | TenorClef (BlockScanner) | yes (every task set) | `@pathbench portal 3` | unverified | ddcc34a2: 0 ConcurrentModificationException in 3 reps; historically 4 of 70 bench logs had one, so too rare to call fixed. Portal in same run 0/3 (DIED, TIMEOUT, DIED) |
