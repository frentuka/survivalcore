# Database Essential Module (DatabaseEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/database/`

## 🎯 Main Purpose
Lettuce-driven asynchronous Redis engine (`DatabaseEssential`). It handles Redis connection pooling, executes async key-value queries (`get`, `set`, `exists`), and fires proprietary events on reconnection or database failures.

## 🔗 Connections & Dependencies
* **Critical Requisite:** Primary data persistence tier. 
* **State Saver:** Serves as the database backing for `PlayerDataService` profiles, `PermissionsService` groups, and username audit trails.
* **Server Health Watchdog:** Fires `DatabaseDisconnectEvent` immediately on Redis connection loss, commanding `PlayerDataListener` to execute local emergency dumps and safely shut down the Paper server to prevent data desynchronization.\n