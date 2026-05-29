# PlayerData Service Module (PlayerDataService)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/services/playerdata/`
* **Documentation:** [PLAYER_DATA.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/services/playerdata/PLAYER_DATA.md)

## 🎯 Main Purpose
Orchestrates the entire player profile lifecycle (inventories, stats, coordinates, permissions, settings). Integrates thread-safe UUID-based `Mutex` locks to prevent race conditions during rapid login/logouts and handles database dropouts via local disk emergency dumps.

## 🔗 Connections & Dependencies
* **Data Core:** Backed by `DatabaseEssential` (Redis).
* **Cross-Component Hooks:**
  * Fires `PlayerDataRegisterEvent` to trigger permission compiler syncs, language mapping, and chat history restores.
  * Fires `PlayerDataPreUnregisterEvent` allowing other services to commit final states before the profile is flushed to Redis.\n