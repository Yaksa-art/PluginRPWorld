# Rpstrana Minecraft Plugin

Rpstrana is a robust Minecraft plugin that manages RP countries, diplomacy, and roles. It fetches country data from an external JSON API and integrates with LuckPerms for chat prefixes/suffixes.

## Features

- **Country Management**: Players can join countries, manage roles, and interact with other nations
- **Role System**: Four distinct roles (PRESIDENT, VICE_PRESIDENT, MAYOR, RESIDENT) with different permissions
- **Diplomacy System**: Countries can form alliances, declare wars, or remain neutral
- **Invitation System**: Players can invite others with clickable accept/deny buttons
- **LuckPerms Integration**: Automatically sets prefixes and suffixes based on country and role
- **GUI Interface**: Interactive menus for country management and diplomacy

## Commands

- `/rp` - Opens the main menu GUI
- `/rp invite <player>` - Invite a player to your country (President, VP, Mayor only)
- `/rp join <country_id>` - Join a country
- `/rp promote <player>` - Promote a player (President only)
- `/rp demote <player>` - Demote a player (President only)
- `/rp leave` - Leave your current country
- `/rp countryload` - Admin command to load country data from API
- `/rp help` - Show help message

## Permissions

- `rpstrana.admin` - Required for `/rp countryload` command (default: op)

## Installation

1. Place the compiled JAR file in your server's `plugins` folder
2. Ensure LuckPerms is installed for prefix/suffix functionality
3. Restart or reload your server
4. Use `/rp countryload` to fetch country data from the API

## Configuration

The plugin automatically creates the following files in the `plugins/Rpstrana/` directory:

- `countries.json` - Stores country data fetched from the API
- `players.yml` - Stores player country memberships, roles, and diplomatic relations

## Technical Details

- **Java Version**: Java 21
- **Minecraft Version**: 1.21.x
- **Build System**: Gradle with Kotlin DSL
- **Dependencies**: 
  - Spigot API 1.21
  - LuckPerms API
  - Gson for JSON handling

## Development

To build the project:

```bash
./gradlew build
```

The resulting JAR file will be in the `build/libs/` directory.