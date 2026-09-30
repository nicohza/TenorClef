# Bench tooling

Scripts that drive TenorClef's `pathbench` command
(`src/main/java/.../PathBench.java`) against the Ostinato / Baritone jar in
`libs/`, plus a standalone elytra bench for Ostinato's 1.21.11 line.

Layout: `TenorClef/` and `Ostinato/` side by side.

| Pairing | Ostinato checkout | Staged jar |
| --- | --- | --- |
| Primary | `Ostinato` on `main` (Minecraft **1.21.4**) | `libs/baritone-unoptimized-fabric-1.21.4.jar` |
| Legacy | `Ostinato` on branch `1.16.1` | `libs/baritone-unoptimized-fabric-1.16.1.jar` |
| Experimental elytra | `Ostinato` on branch `1.21.11` | `Ostinato/dist/baritone-unoptimized-fabric-ostinato-1.21.11.jar` |

Ostinato `main` is 1.21.4. Do not treat `main` as a 1.21.11 tree.

## 1.16.1 benches (xvfb, headless)

    OUT=/tmp/bench bash tools/bench/suite.sh     # swim, wreck, column, flow, boat, cliff
    OUT=/tmp/bench bash tools/bench/swim.sh      # one bench; see each script

Each script rewrites `autoRunCommand` in
`versions/1.16.1/run/altoclef/altoclef_settings.json`, launches
`./gradlew --offline :1.16.1:runClient` with
`-Dtenorclef.seed=12345 -Dtenorclef.warp=N -Dtenorclef.pathbench.exit=true`,
waits for the CSV in `versions/1.16.1/run/pathbench/`, then prints it.
`suite.sh` runs `pkill -9 java` between benches: don't run anything else Java
at the time. `sum.sh` gives a compact summary of the latest wreck run.

Rebuild the 1.16.1 jar (Java 8, Gradle 4.9) in the 1.16.1 checkout:

    JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 gradle build -x test
    cp build/libs/baritone-unoptimized-fabric-1.16.5.jar ../TenorClef/libs/baritone-unoptimized-fabric-1.16.1.jar

## Elytra bench (1.21.11, Ostinato branch `1.21.11`)

TenorClef's 1.21.11 target does not compile yet, so `elytrabench/` is its own
Fabric mod. Ostinato `main` is 1.21.4; use branch `1.21.11` for this bench.

1. In an Ostinato `1.21.11` checkout: `./gradlew :fabric:remapJar`, copy
   `fabric/build/libs/baritone-fabric-*.jar` to
   `Ostinato/dist/baritone-unoptimized-fabric-ostinato-1.21.11.jar`
   (or pass `-DostinatoJar=path`).
2. Make a world named `bench` in `elytrabench/run/saves/` (e.g. run the vanilla
   1.21.11 server once with `level-seed=12345` and copy its `world/`).
3. In `run/options.txt` set `onboardAccessibility:false` (the onboarding
   screen blocks quick play) and a low `renderDistance`.
4. `xvfb-run -a -s "-screen 0 640x360x24" ./gradlew runClient`

The bench equips an elytra and 64 rockets at y=200, flies to four goals
1000–1700 blocks away with `getElytraProcess().pathTo`, and writes
`run/pathbench/pathbench_elytra_*.csv` (result, ticks, rockets used, health).
Grep the log for `ELYTRABENCH`.

## Vibe coding / AI use

Large parts of this repository were written or edited with AI assistants
(Claude, Grok, and similar). That is vibe coding: a person set the
direction; a model produced a lot of the text. A green CI run or a
commit message is not proof that a human understood every line.

Read the diff before you run or merge it. Do not treat this as audited
software. File bugs. Do not assume the model already considered your
case.
