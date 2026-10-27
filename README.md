```text
   ___          _    __                 __   _     __ 
  / _ \___ ___ (_)__/ /__ ___  _______ / /  (_)__ / /_
 / , _/ -_|_-</ / _  / -_) _ \/ __/ -_) /__/ (_-</ __/
/_/|_|\__/___/_/\_,_/\__/_//_/\__/\__/____/_/___/\__/ 
```

README LANGUAGES [ [**English**](README.md) | [中文](README_CN.md)  ]

![CodeSize](https://img.shields.io/github/languages/code-size/ArtformGames/ResidenceList)
[![Download](https://img.shields.io/github/downloads/ArtformGames/ResidenceList/total)](https://github.com/ArtformGames/ResidenceList/releases)
[![Java CI with Maven](https://github.com/ArtformGames/ResidenceList/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/ArtformGames/ResidenceList/actions/workflows/maven.yml)
![Support](https://img.shields.io/badge/Minecraft-Java%201.16--Latest-green)

# **ResidenceList**

> Great **"Residence"** all **listed** !

List existing residence in server in a gui, and allow players to manage, teleport, and comment.

<img src=".doc/logo.png" width="300px"  height="300px" alt="Project logo" align="right">

## Features & Advantages

- Display all residences in a GUI!
- Allow players to edit residence's nickname, description, icon and public status!
- Allow players to pin their favourite residences!
- Allow players to rate & comment residences!
- Show each residence's unique visitor count and visit history, and sort residences by visitor count!
- Admin can manage all residences in a GUI!

## Screenshots

![LIST](.doc/images/LIST.png)
![RATE](.doc/images/RATE.png)

## Dependencies

- **[Necessary]** Residence part base on [Residence](https://www.zrips.net/residence/).
- **[Recommend]** Placeholders based on [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) .

For development dependencies, please
see  [Dependencies](https://github.com/ArtformGames/ResidenceList/network/dependencies) .

### Folia

Full Folia support is pending an EasyPlugin update: version 1.5.14 uses a non-thread-safe
GUI registry. Fix that registry upstream and update the dependency before using concurrent
GUIs across Folia regions. ResidenceList uses EasyPlugin's GUI class directly.

ResidenceList uses Folia's global, asynchronous and player schedulers, while retaining
the Bukkit scheduler on Spigot and Paper. Chat editing uses Paper's `AsyncChatEvent` when
available. Install Folia-compatible versions of Residence, CMILib and any placeholder
expansions you use; teleportation still goes through Residence's permission and teleport logic.
The bundled compile-time dependency Residence 5.1.3.0 still reads destination blocks during
teleport safety checks. This branch does not fix that upstream behavior; cross-region
teleportation needs verification with a Residence build that handles those checks on Folia.
All platforms reuse the same teleport item configuration. Folia menus mark the approximate
residence center coordinates with `≈` without loading destination chunks; the actual safe
teleport spot may differ.
Use a full server restart when installing or replacing the plugin. Hot unloading/reloading
plugins is not supported on Folia; `/reslistadmin reload` only reloads configuration.

When testing a Folia server, check startup with update checking and metrics enabled, then
use players in separate regions to open/page/sort lists, edit names/descriptions/comments,
cancel with `#cancel`, select an icon and teleport to another region. Also check quitting
during an edit, configuration reload, and saving data across a server restart. Repeat the
GUI and editing checks on Spigot to verify the legacy path.

## Commands

### Player commands

Main command is `/ResidenceList` or `/reslist`.

```text
# help
- Display all plugin commands

# open [player-name]
- Open the residence list gui

# info <residence-name>
- Display the residence info

# edit <residence-name>
- Open the residence edit gui
```

### Admin commands

Main command is `/ResidenceListAdmin` or `/reslistadmin`, with permission `residencelist.admin`.

```text
# open [player-name]
- Open the residence manage list gui

# edit <residence-name>
- Open the residence edit gui

# reload
- Reload the plugin configuration
```

## Placeholders

This function based on [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) .
You should install it before using placeholders in game.

```text
# %residencelist_status_<residence>%
- Display the residence public status

# %residencelist_name_<residence>%
- Display the residence alias name or the residence name(if alias not set)

# %residencelist_visitors_<residence>%
- Display the number of unique visitors, excluding the owner
```

## Configurations

You can find configuration translations at [ResidenceList-Translations](https://github.com/ArtformGames/ResidenceList-Translations).

### Plugin Configuration (`config.yml`)

Will be generated on the first boot up.

### Messages Configuration (`messages.yml`)

Will be generated on the first boot up.

## Permissions

```text
# ResidenceList.admin
- The permissions for all admin commands and functions.
```

## Statistics

[![bStats](https://bstats.org/signatures/bukkit/ResidenceList.svg)](https://bstats.org/plugin/bukkit/ResidenceList/19709)

## Open Source Licence

The source code of this project adopts the [GNU General Public License v3.0](https://opensource.org/licenses/GPL-3.0).

## Supports

This project is mainly developed by the [Artfrom Games](https://github.com/ArtformGames/) .

<img src="https://raw.githubusercontent.com/ArtformGames/.github/master/logo/logo_full.svg" width="317px" height="117px" alt="ArtformGames">

Many thanks to Jetbrains for kindly providing a license for us to work on this and other open-source projects.  

[![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=https://github.com/ArtformGames/ResidenceList)
