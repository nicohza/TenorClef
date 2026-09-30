# TenorClef 0.20.0

First TenorClef release. Every build runs on [Ostinato](https://github.com/vexrypt-rgb/Ostinato) (the Baritone fork), not upstream Baritone.

## Downloads

| Minecraft | TenorClef jar | Ostinato |
|---|---|---|
| 1.21.4 | `tenorclef-mc1.21.4-v0.20.0.jar` | Ostinato v1.0.0 `ostinato-mc1.21.4-*.jar` |
| 1.16.1 | `tenorclef-mc1.16.1-v0.20.0.jar` | Ostinato v1.0.0 `ostinato-mc1.16.1-*.jar` |

Install Fabric Loader, Fabric API, the TenorClef jar and the matching Ostinato jar. Do not add a second Baritone jar.

## Highlights

- Ostinato only: builds link against Ostinato; the upstream Baritone fallback is gone.
- Swarm: bots running Ostinato can form a signed, whisper-based link, share status, and split a schematic build across the group with `#swarm build`.
- 1.21.11 is experimental and 26.3 is not supported yet; neither is part of this release.
