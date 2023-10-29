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

## Features & Advantages

## Screenshots

## Dependencies

- **[Necessary]** Residence part base on [Residence](https://www.zrips.net/residence/).
- **[Recommend]** Placeholders based on [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) .

For development dependencies, please
see  [Dependencies](https://github.com/ArtformGames/ResidenceList/network/dependencies) .

## Commands

### Player commands

Main command is `/ResidenceList` or `/rl`.

```text
# help
- Display all plugin commands

# open
- Open the residence list gui

# info <residence-name>
- Display the residence info

# edit <residence-name>
- Open the residence edit gui
```

### Admin commands

Main command is `/ResidenceListAdmin` or `/rla`, with permission `residencelist.admin`.

```text
# edit <residence-name>
- Open the residence edit gui

# manage <player-name>
- Open the residence list gui of the player
```

## Placeholders

This function based on [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) .
You should install it before using placeholders in game.

```text
# %residencelist_status_<residence>%
- Display the residence public status

# %residencelist_name_<residence>%
- Display the residence alias name or the residence name(if alias not set)
```

## Configurations

### Plugin Configuration ([`config.yml`]())

Will be generated on the first boot up.

### Messages Configuration ([`messages.yml`]())

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

Many thanks to Jetbrains for kindly providing a license for us to work on this and other open-source projects.  
[![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=https://github.com/ArtformGames/ResidenceList)
