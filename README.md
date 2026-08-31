# Apex Ballistics

A **Minecraft 1.21.1 Forge** mod that adds missiles, a handheld launcher, a target designator, and a redstone launch pad. Everything ships in **one jar** — no GeckoLib or other extra mods.

Placeholder 16×16 item textures and a simple in-code missile model are included so the mod is playable now. You can drop in real models, textures, and `.ogg` sounds later without changing the Java.

## Install

1. Install [Minecraft Forge 1.21.1](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.1.html) (52.1.16 or newer).
2. Build or grab `apexballistics-1.0.0.jar` from `build/libs`.
3. Put that **one jar** in your `mods` folder.

Build from source (Java 21):

```bash
./gradlew build
```

The file you want is `build/libs/apexballistics-1.0.0.jar`. Ignore any `-slim` / extra jars if they appear.

## What's in the mod

| Item | What it does |
| --- | --- |
| **Missile Launcher** | Right-click to fire a missile from your inventory (offhand first, then the rest). Homing rounds lock onto the mob you are looking at. |
| **HE Missile** | Standard high-explosive impact. |
| **Incendiary Missile** | Smaller blast that starts fires. |
| **Cluster Missile** | Splits into bomblets on impact. |
| **Homing Missile** | Steers toward a locked or nearby living target. |
| **Bunker Missile** | Heavy blast that punches downward. |
| **Rocket Fuel** | Crafting ingredient. |
| **Target Designator** | Right-click a block to mark it, then right-click a launch pad to program that target. |
| **Launch Pad** | Load a missile, optional designator target, then right-click / use a launcher / pulse redstone to fire. Sneak-right-click ejects the missile. |

Missiles leave a smoke/flame trail and explode on impact or after a few seconds of flight.

Blast size and whether explosions break blocks are in `config/apexballistics-common.toml`.

## Crafting

- **Rocket Fuel** — gunpowder + coal/charcoal + blaze powder (makes 3)
- **HE Missile** — iron, TNT, rocket fuel, redstone
- **Incendiary** — HE missile + fire charge
- **Cluster** — HE missile + 2 firework stars
- **Homing** — HE missile + eye of ender + redstone
- **Bunker** — HE missile + obsidian + TNT
- **Launcher** — iron, dispenser, blaze rod, lever
- **Designator** — copper, redstone, 2 spyglasses
- **Launch Pad** — iron blocks, dispenser, observer, copper

All items are also in the **Apex Ballistics** creative tab.

## Replacing assets (no coding)

When you have better art or sound, overwrite these paths inside the jar (or in a resource pack with namespace `apexballistics`):

**Item textures (16×16 PNG)**  
`assets/apexballistics/textures/item/`

- `missile_launcher.png`
- `target_designator.png`
- `rocket_fuel.png`
- `he_missile.png`
- `incendiary_missile.png`
- `cluster_missile.png`
- `homing_missile.png`
- `bunker_missile.png`

**Block texture (16×16 PNG)**  
`assets/apexballistics/textures/block/launch_pad.png`

**Missile body (64×64 PNG)**  
`assets/apexballistics/textures/entity/` — same names as the missiles, plus `bomblet.png`

**Sounds**  
Right now `sounds.json` points at vanilla firework / explode / orb files so the events already play. To use your own `.ogg` files:

1. Put files in `assets/apexballistics/sounds/` named for example `missile_launch.ogg`, `missile_explode.ogg`, `silo_fire.ogg`, `target_lock.ogg`
2. Change each event in `assets/apexballistics/sounds.json` from a `minecraft:...` path to `apexballistics:missile_launch` (no `.ogg`)

Send those files and they can be dropped in without touching the Java.

Mod logo: `logo.png` at the root of the jar.

## Config

`config/apexballistics-common.toml`

- `griefing` — explosions break blocks
- `powerMultiplier` — global blast scale
- `launcherCooldownTicks` — delay between launcher shots
- `maxLifetimeTicks` — airburst timer
- `homingRange` — how far homing missiles search
