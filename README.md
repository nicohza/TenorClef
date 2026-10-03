> [!WARNING]
> **Only the Minecraft 26.3 build has been tested** (manually, in-game). The 26.3 port is experimental.
> Other versions (1.21.4, 1.21.11, 1.16.1) were **not** rebuilt or tested after the 26.3 mixin changes.
> The changes are wrapped in `//#if MC >= 260000` blocks, so older targets should be unaffected, but this is unverified.

---

## Minecraft 26.3 (experimental)

The 26.3 module is opt-in: it is only included in the Gradle build with `-Pwith26`, so it cannot
break the other targets. It uses Mojang names (Minecraft 26.x is unobfuscated) and Java 25.

### Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5
- Fabric API `0.161.0+26.3`
- Ostinato jar built from the Ostinato branch `26.3` (see the [wiring guide](docs/OSTINATO_WIRING.md)).
  It is **not stored in this repository** (`*.jar` is gitignored); build or download it separately and name it
  `baritone-unoptimized-fabric-ostinato-26.3.jar`.
- Java 25 to run the game

### Install (26.3)

Put exactly these three jars in your `mods` folder:

1. `fabric-api-0.161.0+26.3.jar`
2. `altoclef-26.3-<version>.jar` (the full jar, **not** the `-slim` one)
3. `baritone-unoptimized-fabric-ostinato-26.3.jar`

Do not install a second Baritone or another TenorClef jar. Start with a single-player world.

### Build (26.3)

1. Place the Ostinato 26.3 jar at `libs/baritone-unoptimized-fabric-ostinato-26.3.jar`
   (the build fails with "Missing Ostinato 26.3 Baritone jar" otherwise).
2. Run:

```bat
gradlew.bat :26.3:build -Pwith26 "-Pmod_version=0.23.3" -x ":26.3:test"
```

### Known limitations (26.3)

- `MixinLocalPlayer` is skipped silently on 26.3 (`require = 0`). A replacement for `getPitch`/`getYaw`
  (`getViewXRot`/`getViewYRot`) is not implemented yet.