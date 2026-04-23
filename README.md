# Simple Factions (Spigot Plugin)

A lightweight factions plugin for modern Spigot servers with land claiming, rank-based permissions, faction homes, storage, and scheduled raid events.

## Status

Current version: `0.1.0`  
Target API: Spigot `1.21.x` (`api-version: 1.21`)

## Features

- Faction creation, invites, joining, leaving, kicking, and disbanding
- Chunk claiming with hard claims (normal protected claims)
- Optional weak claims beyond base power
- Claim interaction controls (block break/place, interact, PvP, entity damage, TNT, fire spread)
- Faction power system
- Faction home system (`/faction home`, `/faction home set`)
- Shared faction storage (`6x9` inventory)
- Faction prefix/team integration
- Rank system with custom rank permissions and member assignment
- Scheduled raids with three phases: preparation, hold grounds, and capture the flag (core)
- Optional BlueMap integration for claimed land rendering
- Command aliases: `/faction`, `/f`, `/fac`

## Requirements

- Java `21`
- Spigot/Paper server compatible with `1.21.x`
- Optional: BlueMap (for map visualization addon)

## Installation

1. Build or download the plugin JAR.
2. Put the JAR in your server `plugins/` folder.
3. Start the server once to generate `plugins/Simple-Factions/config.yml`.
4. Edit config values as needed.
5. Restart the server.

## Build From Source

```bash
mvn clean package
```

Artifacts are generated in `target/` (including a shaded JAR).

## Commands

### Core

- `/faction help`
- `/faction info`
- `/faction create <factionName>`
- `/faction invite <playerName>`
- `/faction join <factionName>`
- `/faction leave confirm`
- `/faction kick <playerName> confirm`
- `/faction disband confirm`
- `/faction claim`
- `/faction unclaim`
- `/faction toggle <factionName> <weak|hard|all> <enable|disable>`
- `/faction prefix <prefixName> <color1> [color2] [color3] [color4] [color5]`
- `/faction storage`

### Home

- `/faction home`
- `/faction home set`

### Raid

- `/faction raid <factionName> <raidDate> <confirm>`
- `/faction raid select here`

`raidDate` format is: `dd-MM-yyyy:HHmm`  
Example: `30-04-2026:2130`

### Ranks

- `/faction rank info`
- `/faction rank create <rankName>`
- `/faction rank delete <rankName> confirm`
- `/faction rank manage info <rankName>`
- `/faction rank manage player add <rankName> <playerName>`
- `/faction rank manage player remove <rankName> <playerName> confirm`
- `/faction rank manage player list <rankName>`
- `/faction rank manage permissions add <rankName> <permission1> <permission2> ...`
- `/faction rank manage permissions remove <rankName> <permission1> <permission2> ...`
- `/faction rank manage permissions list <rankName>`

### Admin

- `/faction admin <options>` (admin/internal tools)

## Permission Nodes

- `simplefactions.admin`
- `simplefactions.claim`
- `simplefactions.disband`
- `simplefactions.home.set`
- `simplefactions.invite`
- `simplefactions.kick`
- `simplefactions.prefix`
- `simplefactions.raid`
- `simplefactions.raid.select`
- `simplefactions.rank.create`
- `simplefactions.rank.delete`
- `simplefactions.rank.manage.permissions.add`
- `simplefactions.rank.manage.permissions.list`
- `simplefactions.rank.manage.permissions.remove`
- `simplefactions.rank.manage.player.add`
- `simplefactions.rank.manage.player.list`
- `simplefactions.rank.manage.player.remove`
- `simplefactions.toggle`
- `simplefactions.unclaim`

## Configuration

Config file: `src/main/resources/config.yml`

Main sections:

- `faction.object`
- `faction.object.base-faction-power`
- `faction.object.base-faction-power-per-member`
- `faction.object.weak-claims-enabled`
- `faction.object.weak-amount-coefficient`
- `faction.hard-claim` (behavior toggles in hard claims)
- `faction.weak-claim` (behavior toggles in weak claims)
- `enable-bluemap-addon`

## Notes

- This project is currently early-stage (`0.1.0`).
- BlueMap support is optional and controlled by config.
