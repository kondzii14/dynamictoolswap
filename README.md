# Dynamic Tool Swap 🛠️

A Minecraft 1.21.1 Fabric QOL mod that automatically equips the best tool from your inventory when you start hitting a block or mob.

## Features

- ⛏️ Stone, ores, metals → best pickaxe
- 🪓 Logs, wood, leaves, chests → best axe
- 🪏 Dirt, sand, gravel, snow → best shovel
- ⚔️ Mobs → best sword
- 🌾 Hay, nylium, sponge → best hoe
- ⚠️ Warns you in chat if your pickaxe tier is too low for the ore
- Searches your **full inventory**, not just hotbar
- Triggers on click — won't interfere with normal gameplay

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `dynamictoolswap-1.0.0.jar` into your `mods/` folder
4. Launch Minecraft!

## Building from source

```bash
git clone https://github.com/kondzii14/dynamictoolswap.git
cd dynamictoolswap
./gradlew build
```

Output jar will be in `build/libs/`.

## Compatibility

- Minecraft 1.21.1
- Fabric Loader 0.16.5+
- Client-side only — works on any server!

## License

GPL-2.0
