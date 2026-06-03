# SurvivalCore Development Universe

Welcome, AI Coding Agent! This directory serves as the centralized knowledge base for understanding the architecture, mechanics, and integration paths across all modules of `SurvivalCore`.

---

## ⚠️ THE TWO GOLDEN RULES FOR AGENTS

To ensure system stability, architectural consistency, and trace-friendly codebases, you **MUST** adhere to these two principles without exception:

### 1. READ BEFORE CODING
Before editing, refactoring, or touching any code inside any module, you **MUST** locate and thoroughly read the dedicated `.md` documentation file located inside that module's package.
* These files detail internal class roles, thread-safety mechanics (like coroutine locks), custom subservice divisions, and external connection vectors.
* Additionally, check the corresponding brief overview file inside the `universe/` folder for rapid structural mapping.

### 2. KEEP DOCUMENTATION SYNCHRONIZED
After modifying or adding any feature to a module, you **MUST** immediately update its local `.md` documentation file.
* Keep schemas, troubleshooting matrices, event listings, and Mermaid diagrams perfectly in sync with your changes.
* Failure to keep documentation updated will lead to documentation drift, causing future agents to make flawed assumptions.

---

## 🗺️ Workspace Map & Module Documentation Index

Below is the absolute directory map of `SurvivalCore` modules. Each module links to its **brief universe purpose** and its **detailed codebase documentation**:

### 1. [universe/initless/](file:///home/srleg/Projects/survivalcore/universe/initless/) — Bootstrap Tier
Bootstrap components with zero internal dependencies that initialize before all other systems.

* **Logging System** (`LoggingInitless`): Establishes color-coded logging and rolling JSON file writes.
  * *Brief Overview:* [LOGGING.md](file:///home/srleg/Projects/survivalcore/universe/initless/LOGGING.md)
  * *Detailed Docs:* [LOGGING_INITLESS.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/initless/logging/LOGGING_INITLESS.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/initless/logging/`
* **Custom Event Bus** (`ProprietaryEventsInitless`): Reflection-based event bus for decoupled inter-component signaling.
  * *Brief Overview:* [PROPRIETARY_EVENTS.md](file:///home/srleg/Projects/survivalcore/universe/initless/PROPRIETARY_EVENTS.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/initless/proprietaryEvents/`

### 2. [universe/essentials/](file:///home/srleg/Projects/survivalcore/universe/essentials/) — Essentials Tier
Foundational utilities supplying vital capabilities to services and applications.

* **Configs Manager** (`ConfigsEssential`): Manages GSON-based configurations with auto-saving and live hot-reloads.
  * *Brief Overview:* [CONFIGS.md](file:///home/srleg/Projects/survivalcore/universe/essentials/CONFIGS.md)
  * *Detailed Docs:* [CONFIG_ESSENTIAL.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/essentials/configs/CONFIG_ESSENTIAL.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/essentials/configs/`
* **Redis Database Driver** (`DatabaseEssential`): Lettuce-powered async connection pools and health checks.
  * *Brief Overview:* [DATABASE.md](file:///home/srleg/Projects/survivalcore/universe/essentials/DATABASE.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/essentials/database/`
* **Chat Engine** (`ChatEssential`): Handles chat channels, routing, and interactive chat UIs.
  * *Brief Overview:* [CHAT.md](file:///home/srleg/Projects/survivalcore/universe/essentials/CHAT.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/essentials/chat/`
* **ActionBar Utility** (`ActionBarEssential`): Restricts and routes Bukkit action bar rendering.
  * *Brief Overview:* [ACTION_BAR.md](file:///home/srleg/Projects/survivalcore/universe/essentials/ACTION_BAR.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/essentials/actionbar/`
* **Username Auditing** (`UsernameTrackerEssential`): Logs connections to map case-sensitive names to UUIDs historically.
  * *Brief Overview:* [USERNAME_TRACKER.md](file:///home/srleg/Projects/survivalcore/universe/essentials/USERNAME_TRACKER.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/essentials/usernameTracker/`

### 3. [universe/services/](file:///home/srleg/Projects/survivalcore/universe/services/) — Services Tier
Complex services providing compound business logic built on bootstrap and essentials infrastructure.

* **PlayerData Manager** (`PlayerDataService`): Single source of truth for player states, inventory serialization, locations, coroutine locks, and emergency disk dumps.
  * *Brief Overview:* [PLAYER_DATA.md](file:///home/srleg/Projects/survivalcore/universe/services/PLAYER_DATA.md)
  * *Detailed Docs:* [PLAYER_DATA.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/services/playerdata/PLAYER_DATA.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/services/playerdata/`
* **Permissions Engine** (`PermissionsService`): Manages permission group inheritances, wildcard resolution, and dynamic Bukkit permissible attachments.
  * *Brief Overview:* [PERMISSIONS.md](file:///home/srleg/Projects/survivalcore/universe/services/PERMISSIONS.md)
  * *Detailed Docs:* [PERMISSIONS_SERVICE.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/services/permissions/PERMISSIONS_SERVICE.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/services/permissions/`
* **Language Support** (`LanguageService`): Controls multi-locale support and dynamically retrieves translation packs mapped to player preferences.
  * *Brief Overview:* [LANGUAGE.md](file:///home/srleg/Projects/survivalcore/universe/services/LANGUAGE.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/services/language/`
* **Inventory GUI** (`InventoryGUIService`): Framework to build dynamic inventory click UI owners.
  * *Brief Overview:* [INVENTORY_GUI.md](file:///home/srleg/Projects/survivalcore/universe/services/INVENTORY_GUI.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/services/inventorygui/`
* **World Board Engine** (`WorldBoardService`): High-performance, Folia-threaded holographic 3D text display system.
  * *Brief Overview:* [WORLD_BOARD.md](file:///home/srleg/Projects/survivalcore/universe/services/WORLD_BOARD.md)
  * *Detailed Docs:* [WORLD_BOARD.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/services/worldboard/WORLD_BOARD.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/services/worldboard/`
* **Unified Singula** (`SingulaService`): A facade layer that streamlines standard queries to other services.
  * *Brief Overview:* [SINGULA.md](file:///home/srleg/Projects/survivalcore/universe/services/SINGULA.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/services/singula/`

### 4. [universe/apps/](file:///home/srleg/Projects/survivalcore/universe/apps/) — Apps Tier
Higher-tier apps executing Minecraft commands and displaying interactive GUI screens.

* **Chat Manager** (`ChatManagerApp`): Staff control commands (`/chat`) and interactive paging.
  * *Brief Overview:* [CHAT_MANAGER.md](file:///home/srleg/Projects/survivalcore/universe/apps/CHAT_MANAGER.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/apps/ChatManager/`
* **Permissions Manager** (`PermissionsManagerApp`): Interactive rank editor GUIs (`/permissions`, `/groups`).
  * *Brief Overview:* [PERMISSIONS_MANAGER.md](file:///home/srleg/Projects/survivalcore/universe/apps/PERMISSIONS_MANAGER.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/apps/PermissionsManager/`
* **Server Administration** (`ServerAdministrationApp`): Core server utilities (`/server`).
  * *Brief Overview:* [SERVER_ADMINISTRATION.md](file:///home/srleg/Projects/survivalcore/universe/apps/SERVER_ADMINISTRATION.md)
  * *Codebase Path:* `src/main/kotlin/site/ftka/survivalcore/apps/ServerAdministration/`

---

## 📈 Standardized Audit & Search Process for Agents

When you receive a new development goal, proceed using this systematic approach:
1. **Locate the Target Package**: Check the directory layout in this file or `README.md` to identify the correct packages.
2. **Review documentation first**: Locate and view the corresponding `.md` documentation file before looking at the `.kt` code files.
3. **Trace integration channels**: Check the "Critical Cross-Module Connections & Dependencies" section inside the module's documentation to understand what other classes compile, invoke, or listen to this component.
4. **Implement modifications**: Adhere to the thread-safety, locking, and coroutine delegation strategies detailed in the documentation.
5. **Update documentation**: Perform contiguous or multi-replace edits to the module's `.md` file to document your adjustments.
