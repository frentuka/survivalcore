# SurvivalCore

A comprehensive Minecraft Paper server plugin framework written in Kotlin, providing a modular foundation for survival servers. SurvivalCore handles chat management, player data persistence, permissions, inventory GUIs, and server administration through a clean three-tier architecture.

- **Author:** srleg
- **Language:** Kotlin 2.1.0 (JVM 21)
- **Target API:** Paper MC 1.21.10
- **Database:** Redis (via Lettuce)

---

## Architecture Overview

SurvivalCore is organized into four tiers that initialize in order:

```
Initless  →  Essentials  →  Services  →  Apps
(bootstrap)  (core infra)  (advanced)  (user-facing)
```

Each component exposes a dedicated `*API` class for safe cross-component access and delegates complex logic to focused `*Subservice` classes.

---

## Tier 1 — Initless (Bootstrap Infrastructure)

These systems start before everything else and have no inter-dependencies.

### LoggingInitless
Centralized logging system with color-coded console output and JSON file dumps.
- Provides `LoggingInstance` to any component that needs logging
- Supports log-level filtering and size-managed log files
- Located at: [initless/logging/](src/main/kotlin/site/ftka/survivalcore/initless/logging/)

### ProprietaryEventsInitless
A custom reflection-based event bus independent of the Bukkit event system.
- Listeners are annotated with `@PropEventHandler`
- Supports priority levels: `FIRST`, `HIGH`, `NORMAL`, `LOW`, `MONITOR`
- Enables decoupled inter-component communication
- Located at: [initless/proprietaryEvents/](src/main/kotlin/site/ftka/survivalcore/initless/proprietaryEvents/)

---

## Tier 2 — Essentials (Core Infrastructure)

Foundational services that all higher-tier components depend on.

### ConfigsEssential
Manages plugin configuration files with automatic versioning and hot-reloading.
- Handles three configuration types: `GeneralConfig`, `PlayerDataConfig`, `ChatConfig`
- GSON-based serialization/deserialization
- Located at: [essentials/configs/](src/main/kotlin/site/ftka/survivalcore/essentials/configs/)

### DatabaseEssential
Async Redis client using Lettuce with connection pooling and health monitoring.
- Provides: `ping()`, `exists()`, `get()`, `set()` via `DatabaseAPI`
- Shuts the server down safely on connection failure
- Located at: [essentials/database/](src/main/kotlin/site/ftka/survivalcore/essentials/database/)

### ChatEssential
Full chat infrastructure with channels, interactive screens, and message routing.

| Subservice | Responsibility |
|---|---|
| `ChatEssential_ChannelsSubservice` | Manages global, staff, and per-player chat channels |
| `ChatEssential_ScreensSubservice` | Manages interactive chat-based UI screens |
| `ChatEssential_MessagingSubservice` | Routes and logs messages |

Commands: `/s`, `/exitscreen`, `/backscreen`
Located at: [essentials/chat/](src/main/kotlin/site/ftka/survivalcore/essentials/chat/)

### ActionBarEssential
Handles sending action bar messages to players.
- `ActionBarAPI` exposes messaging operations
- Located at: [essentials/actionbar/](src/main/kotlin/site/ftka/survivalcore/essentials/actionbar/)

### UsernameTrackerEssential
Tracks username change history across sessions for auditing purposes.
- Located at: [essentials/usernameTracker/](src/main/kotlin/site/ftka/survivalcore/essentials/usernameTracker/)

---

## Tier 3 — Services (Advanced Functionality)

Complex systems built on top of Essentials.

### PlayerDataService
Complete player data lifecycle management backed by Redis.

| Subservice | Responsibility |
|---|---|
| `PlayerData_InputOutputSubservice` | Read/write player data to Redis |
| `PlayerData_BackupSubservice` | Periodic backup of player data |
| `PlayerData_IntegritySubservice` | Data validation and repair |
| `PlayerData_RegistrationSubservice` | Handle login/logout data flow |
| `PlayerData_EmergencySubservice` | Emergency data dump on crashes |
| `PlayerData_CachingSubservice` | In-memory caching layer |

Located at: [services/playerdata/](src/main/kotlin/site/ftka/survivalcore/services/playerdata/)

### PermissionsService
Hierarchical permission group management with Redis-backed persistence.
- Supports permission groups with assignment to players
- Subservices for permissions, players, groups, and I/O
- Located at: [services/permissions/](src/main/kotlin/site/ftka/survivalcore/services/permissions/)

### LanguageService
Multi-language support with per-player language preferences.
- `LanguagePack` objects define message strings per locale
- Located at: [services/language/](src/main/kotlin/site/ftka/survivalcore/services/language/)

### InventoryGUIService
Framework for creating custom inventory-based GUIs.
- Components implement the `InventoryGUIOwner` interface
- Handles click, close, and navigation events via listeners
- Located at: [services/inventorygui/](src/main/kotlin/site/ftka/survivalcore/services/inventorygui/)

### SingulaService
A facade/shortcut layer for unified cross-service access patterns.
- Located at: [services/singula/](src/main/kotlin/site/ftka/survivalcore/services/singula/)

---

## Tier 4 — Apps (User-Facing Applications)

High-level plugins built on Services providing commands and UIs.

### ChatManagerApp
Staff-facing chat administration interface.
- Command: `/chat`
- Manage channels and interactive screens
- Located at: [apps/ChatManager/](src/main/kotlin/site/ftka/survivalcore/apps/ChatManager/)

### PermissionsManagerApp
Interactive permissions administration with inventory GUI support.
- Commands: `/permissions` (aliases: `/p`, `/perms`), `/groups` (aliases: `/g`, `/group`)
- Located at: [apps/PermissionsManager/](src/main/kotlin/site/ftka/survivalcore/apps/PermissionsManager/)

### ServerAdministrationApp
Server control and plugin management interface.
- Command: `/server` (alias: `/sv`)
- Located at: [apps/ServerAdministration/](src/main/kotlin/site/ftka/survivalcore/apps/ServerAdministration/)

---

## Commands Reference

| Command | Aliases | Description |
|---|---|---|
| `/chat` | — | Chat administration (staff) |
| `/s` | — | Interact with a chat screen |
| `/exitscreen` | — | Exit the current chat screen |
| `/backscreen` | — | Go back to the previous screen |
| `/server` | `/sv` | Server administration panel |
| `/permissions` | `/p`, `/perms` | Manage player permissions |
| `/groups` | `/g`, `/group` | Manage permission groups |

---

## Project Structure

```
src/main/kotlin/site/ftka/survivalcore/
├── MClass.kt                    # Plugin entry point
├── initless/
│   ├── logging/                 # LoggingInitless, LoggingInstance
│   └── proprietaryEvents/       # Custom event bus
├── essentials/
│   ├── EssentialsFramework.kt
│   ├── configs/                 # Config management
│   ├── database/                # Redis client
│   ├── chat/                    # Chat channels & screens
│   ├── actionbar/               # Action bar messaging
│   └── usernameTracker/         # Username history
├── services/
│   ├── ServicesFramework.kt
│   ├── playerdata/              # Player data persistence
│   ├── permissions/             # Permission groups
│   ├── language/                # Multi-language support
│   ├── inventorygui/            # Inventory GUI system
│   └── singula/                 # Cross-service facade
├── apps/
│   ├── AppsFramework.kt
│   ├── ChatManager/             # /chat
│   ├── PermissionsManager/      # /permissions, /groups
│   └── ServerAdministration/    # /server
└── utils/
    ├── base64Utils.kt
    ├── dateUtils.kt
    ├── numericUtils.kt
    ├── textUtils.kt
    ├── objectsSizeUtils.kt
    └── serializers/
```

---

## Building

Requires **Java 21** and a running **Redis** instance.

```bash
./gradlew build
```

The compiled JAR is automatically copied to the configured server plugins directory.

### Key Dependencies

| Library | Version | Purpose |
|---|---|---|
| Paper API | 1.21.10 | Minecraft server API |
| Kotlin | 2.1.0 | Language runtime |
| Lettuce | 6.5.3 | Async Redis client |
| Kotlinx Coroutines | 1.7.3 | Async/concurrent operations |
| GSON | 2.10.1 | JSON serialization |
| Kyori Adventure | 4.14.0 | Rich text components |
| Apache Commons Pool 2 | 2.4.3 | Connection pooling |

---

## Design Patterns

- **Three-Tier Framework** — Initless → Essentials → Services → Apps with ordered lifecycle (`init`, `restart`, `stop`)
- **API/Facade Pattern** — Each component exposes a `*API` class; internals are hidden
- **Subservice Delegation** — Complex components split responsibility across focused `*Subservice` classes
- **Custom Event Bus** — `ProprietaryEventsInitless` decouples components without Bukkit coupling
- **Async-First** — Kotlin Coroutines and Lettuce async operations throughout
