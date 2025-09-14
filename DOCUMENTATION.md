# SpygChestShop Documentation

## User Guide

### Overview
SpygChestShop is a Minecraft plugin that allows players to create, manage, and interact with chest-based shops. It supports buying and selling items, player shop management, and integrates with Vault for economy support.

### Features
- Create and manage chest shops
- Add/remove players to shops
- Set buy/sell prices
- Shop GUIs for easy management
- Multi-language support (EN, HU, PL)
- Permissions for fine-grained control
- Hologram and inventory integration

### Installation
1. Download the plugin JAR and place it in your server's `plugins` folder.
2. Ensure you have [Vault](https://dev.bukkit.org/projects/vault) and a compatible economy plugin installed.
3. Start or reload your server. The plugin will generate default configuration and locale files.

### Commands
- `/spygchestshop list [page]` — List your shops (permission: `spygchestshop.use`)
- `/spygchestshop create <shop-name>` — Create a new shop (permission: `spygchestshop.use`)
- `/spygchestshop remove <shop-name>` — Remove a shop (permission: `spygchestshop.use`)
- `/spygchestshop rename <old-name> <new-name>` — Rename a shop (permission: `spygchestshop.use`)
- `/spygchestshop add <shop-name> <player>` — Add a player to your shop (permission: `spygchestshop.use`)
- `/spygchestshop removeplayer <shop-name> <player>` — Remove a player from your shop (permission: `spygchestshop.use`)
- `/spygchestshop reload` — Reload configuration (permission: `spygchestshop.admin.reload`)
- `/spygchestshop admin list <target> [page]` — List a target player's shops (permission: `spygchestshop.admin.list`)
- `/spygchestshop admin customer [target]` — Open your own shops as a customer (permission: `spygchestshop.admin.customermode`)

### Permissions
- `spygchestshop.use` — Use basic shop commands
- `spygchestshop.admin.*` — All admin permissions
- `spygchestshop.max.<group>` — Set max shop count for permission group
- `spygchestshop.list` — List shops

### Configuration
Edit `config.yml` to customize:
- Locale/language
- Shop creation price
- Minimum item durability
- Price formats
- Enable/disable decimals
- Shop name length
- Disabled worlds
- Max shops/players
- Hologram settings

Edit `guis.yml` to customize GUI titles, slots, and item appearances.

### Localization
Locale files are in `src/main/resources/locale/` (e.g., `en_US.yml`, `hu_HU.yml`, `pl_PL.yml`).
Set your preferred language in `config.yml` with the `locale` option.

### Shop GUI
- Open a shop to manage items, prices, and players via an interactive GUI.
- Use left/right click to set buy/sell prices.
- View shop info, inventory, and added players.

## Developer Guide

### Main Classes
- `ChestShop` — Main plugin class
- `Shop` — Represents a shop
- `ShopFile` — Handles shop data storage
- `ShopGui`, `DashboardGui`, `PlayersGui` — GUI classes
- `Config`, `GuiConfig`, `MessageConfig` — Configuration management

### Events
- `ShopCreateEvent` — Fired when a shop is created
- `ShopRemoveEvent` — Fired when a shop is removed

### Extending the Plugin
- Add new commands using LiteCommands annotations
- Extend GUIs by editing `GuiConfig` or implementing new GUI classes
- Listen to shop events for custom logic

### Dependencies
- Requires Vault for economy
- Uses [Spyg lib](https://github.com/SpyG-Studios/spyg-lib) for YAML/config management

### License
This plugin is licensed under the Apache License 2.0.

---
For more details, see the source code or contact the authors ([@ikoli](https://github.com/ikoliHU), [@Ris](https://github.com/RisDN)).
