# Apex Ballistics

A **Minecraft 1.21.1 Forge** mod that adds missiles, a handheld launcher, a target designator, a redstone launch pad, and a two-block **cruise launcher**. Everything ships in **one jar** — no GeckoLib or other extra mods.

Placeholder 16×16 item textures and a simple in-code missile model are included so the mod is playable now. You can drop in real models, textures, and `.ogg` sounds later without changing the Java.

## The jar (this is the file you install)

Download **[`jars/apexballistics-1.0.0.jar`](jars/apexballistics-1.0.0.jar)** from this repo and put it in your Minecraft `mods` folder (Forge 1.21.1). That is the only file you need.

To rebuild from source (Java 21):

```bash
./gradlew build
```

That writes `build/libs/apexballistics-1.0.0.jar` (also copied to `jars/`).

## Install

1. Install [Minecraft Forge 1.21.1](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.1.html) (**52.1.14** or newer).
2. Download **[`jars/apexballistics-1.0.0.jar`](jars/apexballistics-1.0.0.jar)** (or run `./gradlew build` and take `build/libs/apexballistics-1.0.0.jar`).
3. Put that **one jar** in your `mods` folder.

Build from source (Java 21):

```bash
./gradlew build
```

The file you want is `build/libs/apexballistics-1.0.0.jar`. Ignore any `-slim` / extra jars if they appear.

## What's in the mod

| Item | What it does |
| --- | --- |
| **Missile Launcher** | Right-click to open the load GUI if empty, or to fire a missile that is loaded in that GUI. Sneak-right-click always opens the GUI. Does not fire unless a real missile is in the launcher. |
| **HE Missile** | Standard high-explosive impact. |
| **Incendiary Missile** | Smaller blast that starts fires. |
| **Cluster Missile** | Splits into bomblets on impact. |
| **Homing Missile** | Steers toward a locked or nearby living target. |
| **Bunker Missile** | Heavy blast that punches downward. |
| **Cruise Missile** | Large missile that sits on the cruise launcher, nose toward the front. Climbs high, flies over, then dives on the target. |
| **Rocket Fuel** | Crafting ingredient. |
| **Target Designator** | Right-click a block to mark it, then put it in a launch pad or cruise launcher. |
| **Coordinate Tool** | Right-click to open a GUI with X, Y, and Z boxes. Save, then put it in a cruise launcher or use it on a launch pad. |
| **Launch Pad** | Load a missile, optional designator target, then right-click / pulse redstone to fire. Sneak-right-click ejects the missile. |
| **Cruise Launcher** | 2 blocks long, 1 block high. **Right-click the placed block** to open the GUI, put in a cruise missile, set a location, then Launch or pulse redstone. |

Missiles leave a smoke/flame trail and explode on impact or after a few seconds of flight.

Blast size and whether explosions break blocks are in `config/apexballistics-common.toml`.

## Crafting

- **Rocket Fuel** — gunpowder + coal/charcoal + blaze powder (makes 3)
- **HE Missile** — iron, TNT, rocket fuel, redstone
- **Incendiary** — HE missile + fire charge
- **Cluster** — HE missile + 2 firework stars
- **Homing** — HE missile + eye of ender + redstone
- **Bunker** — HE missile + obsidian + TNT
- **Cruise Missile** — bunker missile, TNT, iron, rocket fuel
- **Launcher** — iron, dispenser, blaze rod, lever
- **Designator** — copper, redstone, 2 spyglasses
- **Coordinate Tool** — compass, paper, copper, redstone
- **Launch Pad** — iron blocks, dispenser, observer, copper
- **Cruise Launcher** — launch pad, 2 dispensers, iron, copper

All items are also in the **Apex Ballistics** creative tab.

## Replacing assets (no coding)

When you have better art or sound, overwrite these paths inside the jar (or in a resource pack with namespace `apexballistics`):

**Item textures (16×16 PNG)**  
`assets/apexballistics/textures/item/`

- `missile_launcher.png`
- `target_designator.png`
- `coord_tool.png`
- `rocket_fuel.png`
- `he_missile.png`
- `incendiary_missile.png`
- `cluster_missile.png`
- `homing_missile.png`
- `bunker_missile.png`
- `cruise_missile.png`
- `cruise_launcher.png`

**Block texture (16×16 PNG)**  
`assets/apexballistics/textures/block/launch_pad.png`  
`assets/apexballistics/textures/block/cruise_launcher.png`

**Missile body (64×64 PNG)**  
`assets/apexballistics/textures/entity/` — same names as the missiles, plus `bomblet.png` and `cruise_missile.png`

**Cruise launcher GUI (256×256 PNG, 176×212 used)**  
`assets/apexballistics/textures/gui/cruise_launcher.png`

**Coordinate tool GUI (256×256 PNG, 176×108 used)**  
`assets/apexballistics/textures/gui/coord_tool.png`

**Missile launcher GUI (256×256 PNG, 176×166 used)**  
`assets/apexballistics/textures/gui/missile_launcher.png`

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
- `cruiseExplosionPower` — cruise missile blast size
- `cruiseMaxLifetimeTicks` — cruise missile airburst timer
- `cruiseAltitudeBonus` — how high the missile climbs before flying to the target
