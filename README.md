# Simple Factions

[![Version](https://img.shields.io/badge/version-0.1.0-blue)](#)
[![Spigot API](https://img.shields.io/badge/spigot%20api-1.21.x-orange)](#)
[![Java](https://img.shields.io/badge/java-21-red)](#)
[![License](https://img.shields.io/badge/license-unspecified-lightgrey)](#license)

A lightweight, self-contained factions plugin for modern Spigot/Paper servers — land claiming,
rank-based permissions, faction homes and storage, a scheduled multi-phase raid system, an
in-game GUI for browsing commands, and optional [BlueMap](https://bluemap.bluecolored.de/)
integration for rendering claims on the web map.

No external dependencies at runtime beyond the server itself and (optionally) BlueMap — data is
stored in plain JSON files under the plugin's data folder, no database required.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [The Faction Menu (GUI)](#the-faction-menu-gui)
- [How Raids Work](#how-raids-work)
- [BlueMap Integration](#bluemap-integration)
- [Configuration](#configuration)
- [Building From Source](#building-from-source)
- [Project Structure](#project-structure)
- [Known Limitations](#known-limitations)
- [License](#license)

## Features

**Factions**
- Create, invite, join, leave, kick, and disband factions
- A power system that scales claim capacity with membership
- Custom, gradient-colored faction tab-list prefixes
- Shared 54-slot faction storage inventory

**Land Claims**
- Hard claims (fully protected land) up to your faction's power
- Weak claims beyond that, raidable and configurable on/off
- Per-claim-type toggles for block break/place, interaction, PvP, entity damage, fire spread, and TNT

**Ranks & Permissions**
- Fully custom ranks per faction, each with its own set of `simplefactions.*` permissions
- Default `OWNER` and `MEMBER` ranks are created automatically and can't be deleted
- Add/remove members and permissions per rank, or inspect any rank's setup

**Raids**
- Schedule a raid against another faction's weak claims for a specific date/time
- A three-phase live event: preparation → hold the grounds → capture the flag
- Automatically pauses (and eventually cancels) if either side doesn't have enough players

**In-Game GUI**
- Run `/faction` with no arguments to open an interactive menu of every command you can access
- Only shows what you actually have permission (and faction membership, where relevant) to use

**Map Integration**
- Optional BlueMap addon: claims render as extruded, faction-colored columns with a ground border outline

## Requirements

- Java **21**
- A Spigot or Paper server on **1.21.x**
- Optional: [BlueMap](https://bluemap.bluecolored.de/) if you want claims rendered on the web map

## Installation

1. Build the plugin (see [Building From Source](#building-from-source)) or grab a prebuilt JAR.
2. Drop the JAR into your server's `plugins/` folder.
3. Start the server once — this generates `plugins/Simple-Factions/config.yml`.
4. Adjust the config to taste (see [Configuration](#configuration)).
5. Restart the server.

Commands are registered directly against the server's command map at startup, so there's nothing
to add to `plugin.yml` or a `commands.yml` — `/faction`, `/f`, and `/fac` all work out of the box.

## Commands

All subcommands live under `/faction` (aliases: `/f`, `/fac`). Arguments in `<angle brackets>` are
required, `[square brackets]` are optional.

### Faction

| Command | Description |
|---|---|
| `/faction help` | Show a quick command overview |
| `/faction info` | Show your faction's power, members, claims, and home |
| `/faction create <name>` | Create a new faction with you as owner |
| `/faction invite <player>` | Invite a player to your faction |
| `/faction join <faction>` | Accept a pending invite |
| `/faction leave confirm` | Leave your faction (owners must disband instead) |
| `/faction kick <player> confirm` | Remove a member from your faction |
| `/faction disband confirm` | Permanently delete your faction |
| `/faction claim` | Claim the chunk you're standing in |
| `/faction unclaim` | Release the chunk you're standing in |
| `/faction prefix <name> <color> [color2..5]` | Set your faction's tab-list prefix (hex colors, up to 5 for a gradient) |
| `/faction storage` | Open your faction's shared storage |

### Home

| Command | Description |
|---|---|
| `/faction home` | Teleport to your faction home |
| `/faction home set` | Set the home to your current location (must be in a hard claim) |

### Ranks

| Command | Description |
|---|---|
| `/faction rank info` | List all ranks in your faction |
| `/faction rank create <rank>` | Create a new rank |
| `/faction rank delete <rank> confirm` | Delete a rank (members fall back to `MEMBER`) |
| `/faction rank manage info <rank>` | Show a rank's member/permission counts |
| `/faction rank manage player add <rank> <player>` | Add a member to a rank |
| `/faction rank manage player remove <rank> <player> confirm` | Remove a member from a rank |
| `/faction rank manage player list <rank>` | List a rank's members |
| `/faction rank manage permissions add <rank> <perm...>` | Grant one or more permissions to a rank |
| `/faction rank manage permissions remove <rank> <perm...>` | Revoke one or more permissions from a rank |
| `/faction rank manage permissions list <rank>` | List a rank's permissions |

### Raids

| Command | Description |
|---|---|
| `/faction raid select here` | Add the weak claim you're standing on to your raid selection |
| `/faction raid <faction> <date> confirm` | Declare a raid against a faction's selected chunks |

`date` uses `DD-MM-YYYY:HHMM` in UTC, e.g. `30-04-2026:2130`.

### Admin

| Command | Description |
|---|---|
| `/faction admin summary` | Server-wide faction/claim/raid totals |
| `/faction admin factions` | List every faction with power/members/claims |
| `/faction admin info <faction>` | Inspect one faction in detail |
| `/faction admin members <faction>` | List a faction's members |
| `/faction admin claims <faction>` | List a faction's hard/weak claims |
| `/faction admin inspect [player]` | Inspect a player (or yourself) and their current chunk |
| `/faction admin claim <faction> <x> <z> [hard\|weak]` | Force-claim a chunk for a faction |
| `/faction admin unclaim <x> <z>` | Force-unclaim a chunk, regardless of owner |
| `/faction admin power <faction> <amount>` | Set a faction's power directly |
| `/faction admin disband <faction> confirm` | Force-delete a faction |
| `/faction admin raids` | List all waiting and active raids |
| `/faction admin save` | Force a data save |

## Permissions

Commands with no permission node (`create`, `join`, `help`, `info`, `leave`, `home`, `storage`,
`rank info`) are open to any player by default — access to them is instead governed by faction
membership where relevant. Everything else requires the node below.

When a faction is created, its `OWNER` rank is automatically granted every faction-management
permission (everything except `simplefactions.admin`), and the default `MEMBER` rank starts with
just `simplefactions.invite` and `simplefactions.home`. From there, ranks and their permissions
are entirely up to each faction.

| Permission | Grants |
|---|---|
| `simplefactions.admin` | The entire `/faction admin` command group |
| `simplefactions.claim` | `/faction claim` |
| `simplefactions.unclaim` | `/faction unclaim` |
| `simplefactions.invite` | `/faction invite` |
| `simplefactions.kick` | `/faction kick` |
| `simplefactions.disband` | `/faction disband` |
| `simplefactions.prefix` | `/faction prefix` |
| `simplefactions.home.set` | `/faction home set` |
| `simplefactions.raid` | `/faction raid` |
| `simplefactions.raid.select` | `/faction raid select` |
| `simplefactions.rank.create` | `/faction rank create` |
| `simplefactions.rank.delete` | `/faction rank delete` |
| `simplefactions.rank.manage.info` | `/faction rank manage info` |
| `simplefactions.rank.manage.player.add` | `/faction rank manage player add` |
| `simplefactions.rank.manage.player.remove` | `/faction rank manage player remove` |
| `simplefactions.rank.manage.player.list` | `/faction rank manage player list` |
| `simplefactions.rank.manage.permissions.add` | `/faction rank manage permissions add` |
| `simplefactions.rank.manage.permissions.remove` | `/faction rank manage permissions remove` |
| `simplefactions.rank.manage.permissions.list` | `/faction rank manage permissions list` |

## The Faction Menu (GUI)

Running `/faction` with no arguments opens an interactive menu instead of printing a usage error.

- Every command you're shown is one you can actually use right now — it's filtered by both
  permission and, for commands that operate on "your faction," whether you're currently in one.
- Clicking a command **with subcommands** always navigates into it, showing the next level down.
- Clicking a command **with no subcommands** either runs it immediately (if it needs nothing more
  than what you clicked through) or closes the menu and sends you its usage in chat, so you can
  type the rest yourself (e.g. a player name, a rank name, or a `confirm`).

## How Raids Work

1. **Select targets** — stand on an enemy faction's weak claim and run `/faction raid select here`
   for each chunk you want to include (capped by your faction's power).
2. **Declare** — `/faction raid <faction> <date> confirm` schedules the raid for that date/time.
   Declarations against the same defender need at least 30 minutes apart, and your faction has a
   1-hour cooldown between declarations.
3. **Preparation (2 minutes)** — once the scheduled time arrives, a random online defender in the
   contested area is handed a Raid Core item. They can place it themselves to start the next phase
   early, or it gets placed automatically at a random spot when the timer runs out.
4. **Hold the Grounds** — attackers need to outnumber defenders inside the contested chunks
   continuously for 1 minute (within a 2-minute window) to advance.
5. **Capture the Flag (2 minutes)** — attackers must find and destroy the Raid Core block before
   time runs out. Destroying it ends the raid in the attackers' favor and converts the contested
   chunks to their claims; running out the clock leaves the defenders in possession.

If either side drops below the minimum player count (2 attackers / 1 defender) mid-raid, it pauses
for up to a minute waiting for reinforcements before ending automatically.

## BlueMap Integration

When `enable-bluemap-addon` is `true` and the [BlueMap](https://bluemap.bluecolored.de/) plugin is
present, claimed chunks are rendered as extruded markers — solid, translucent columns spanning the
full height of the world, outlined both on the ground and up through the air — colored to match
whatever color the owning faction is currently using for its tab-list prefix.

## Configuration

`config.yml` (generated on first run):

| Key | Default | Description |
|---|---|---|
| `faction.object.base-faction-power` | `2` | Starting power for a newly created faction |
| `faction.object.base-faction-power-per-member` | `1` | Power gained per member who joins |
| `faction.object.weak-claims-enabled` | `true` | Whether weak (raidable) claims are allowed at all |
| `faction.object.weak-amount-coefficient` | `1.5` | Weak claim cap = this × current power |
| `faction.powerToChunkSelectionCoefficient` | `1.25` | Max raid chunk selection = this × defender's power |
| `faction.hard-claim.*` | see below | Interaction toggles for hard (fully protected) claims |
| `faction.weak-claim.*` | see below | Interaction toggles for weak (raidable) claims |
| `enable-bluemap-addon` | `true` | Enables the BlueMap claim overlay |
| `debug.wipe-factions-on-start` | `false` | **Debug only** — wipes all faction data from memory on every startup, for repeatedly testing from a clean slate. Do not leave enabled on a live server. |

Both `hard-claim` and `weak-claim` share the same set of toggles, each defaulting to `false`
(protected) except `interact`, `pvp`, and `entity-damage` on weak claims, which default to `true`:

`block-break`, `block-place`, `interact`, `pvp`, `entity-damage`, `fire-spread`, `tnt-explosion`

## Building From Source

```bash
mvn clean package
```

Requires Java 21 and Maven. Output goes to `target/` — `simple-factions-<version>-shaded.jar` is
the one to deploy, it bundles the plugin's own dependencies (Adventure, Gson via Spigot, etc.).

## Project Structure

```
com.gus.simpleFactions
├── Commands/           Command tree (CommandInterface + one class per (sub)command)
│   └── Faction/Sub/...  home, rank, raid, admin, etc.
├── EventListeners/      Bukkit event handlers (claim protection, join/quit, the GUI)
├── FactionHandlers/
│   ├── Objects/          FactionObject, FactionRankObject
│   └── FactionObjectServices/  Membership, land, ranks, formatting, BlueMap rendering
├── Json/                 Persistence (Gson-backed wrapper classes + JsonHandler)
├── RaidHandlers/         Raid state machine, timers, and helpers
└── Miscellaneous/        Teleportation, the Faction Command GUI
```

## Known Limitations

This is an early, actively-developed project (`0.1.0`) — a few things to be aware of:

- Everything currently assumes a single world named `world`; claims and homes in other worlds
  aren't supported yet.
- Commands are registered by reflecting into the server's internal command map rather than
  through `plugin.yml`, which works today but is tied to the current Spigot/Paper implementation.

## License

No license has been chosen yet for this project — until one is added, all rights are reserved by
the author. If you intend to distribute or reuse this code, please reach out first.
