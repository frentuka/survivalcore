# Configs Essential Module (ConfigsEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/configs/`
* **Documentation:** [CONFIG_ESSENTIAL.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/essentials/configs/CONFIG_ESSENTIAL.md)

## 🎯 Main Purpose
Manages system configuration files (`chat.json`, `general.json`, `playerdata.json`) utilizing GSON serialization. It automates file generation, validates schema fields, and supports on-the-fly live config hot-reloads.

## 🔗 Connections & Dependencies
* **Bootstrap Hook:** Relies on `LoggingInitless` to report config loading status.
* **Infrastructure Provider:** Powers parameters for `DatabaseEssential` (connection properties), `PlayerData_CachingSubservice` (TTL config), and `ChatEssential` (channel maps).\n