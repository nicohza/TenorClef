# Ostinato wiring

TenorClef always runs on Ostinato. It is only compiled for the Minecraft versions Ostinato
is built for; never load a jar built for one Minecraft version into another.

## Compatibility

| TenorClef module | Ostinato source | Status |
| --- | --- | --- |
| `1.21.4` | Ostinato `main` (MC 1.21.4), staged as `libs/baritone-unoptimized-fabric-1.21.4.jar` | Primary; compiles in CI |
| `1.16.1` | Ostinato branch `1.16.1`, staged as `libs/baritone-unoptimized-fabric-1.16.1.jar` | Legacy pairing; compiles in CI |
| `1.21.11` | Ostinato branch `1.21.11`, Fabric artifact (CI builds it) | Experimental |
| `1.21.1` … `1.16.5` | none | Preprocess-only: not compiled, packaged or tested |

The preprocess chain cannot be trimmed (see `settings.gradle.kts`), so the preprocess-only
modules still exist. The preprocessor resolves types on each of them while it remaps sources
down to 1.16.1, so they keep the matching upstream Baritone as a **compile-only** dependency for
that analysis. Nothing built from them is shipped or run.

## Build

Run `gradlew.bat :1.21.4:build` on Windows or `./gradlew :1.21.4:build` on macOS/Linux.

To refresh a staged jar, build the matching Ostinato branch and copy its
`dist/baritone-unoptimized-fabric-*.jar` over the file in `libs/`:

- `1.21.4`: JDK 21, `main`, `./gradlew :fabric:build`.
- `1.16.1`: JDK 8, `./gradlew build -Pbaritone.fabric_build`.
- `1.21.11`: JDK 21, build branch `1.21.11` with `./gradlew :fabric:build`, then place the jar in
  `../Ostinato/dist/` (or `libs/baritone-unoptimized-fabric-ostinato-*.jar`).

## Tungsten

Tungsten is optional and only affects travel/custom-goal movement on the modern target.
Build its Fabric jar from `vendor/tungsten` and place it in TenorClef's `libs/` folder,
or install it beside TenorClef and Ostinato in the Minecraft instance. If absent,
Ostinato falls back to Baritone travel.

## Publishing follow-up

Before making 1.21.11 a stable TenorClef target, finish its source port, publish a
tagged matching Ostinato Fabric artifact, and consume it through a pinned dependency
coordinate. That makes an incorrect pairing fail at dependency resolution rather than
at runtime.
