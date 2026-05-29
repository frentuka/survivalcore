# 🛡️ ActionBar Essential Module (ActionBarEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/actionbar/`

## 🎯 Main Purpose
Provides a clean, streamlined facade for registering, prioritizing, and multiplexing multiple concurrent text Action Bars for online players. Rather than having multiple game systems overwrite each other's messages, the ActionBar Essential module operates as a **Priority-Based Multiplexed Registry**.

---

## 🏗️ Architecture & Component Design

```
+----------------------------------------------------------------------+
|                           ActionBarAPI                               |
|        (Exposes layer management: set, remove, clear, send)          |
|    (Configures display strategy: CONCATENATION, EXCLUSIVE, CAROUSEL) |
+----------------------------------------------------------------------+
                                   │
                                   ▼
+----------------------------------------------------------------------+
|                ActionBarEssential_MessagingSubservice                |
|  - playerLayers: ConcurrentHashMap<UUID, Map<String, Layer>>         |
|  - Async Ticker Scheduler (Running every 100ms / 2 ticks equivalent)  |
+----------------------------------------------------------------------+
         │                                              │
         ▼                                              ▼
+-------------------------+                   +------------------------+
|    ActionBarListener    |                   |     ActionBarLayer     |
| (Cleans up player maps  |                   | (Holds id, priority,   |
|   on PlayerQuitEvent)   |                   |  expiry, & provider)   |
+-------------------------+                   +------------------------+
```

### 1. `ActionBarLayer`
A domain object that stores data for a specific layer:
- **`id`**: Unique string identifier (e.g., `"combat_tag"`, `"status_hud"`).
- **`priority`**: High numbers are sorted first or given display preference.
- **`expiresAt`**: System epoch timestamp (milliseconds) representing the lifetime of the layer (null = permanent).
- **`provider`**: A thread-safe functional interface `() -> Component?` yielding the Adventure component to render at each tick.

### 2. Multiplexing Strategies (`ActionBarStrategy`)
The module supports three interchangeable render strategies accessible via `actionBarAPI.strategy`:
1. **`CONCATENATION` (Default)**: Joins all active layers, sorted by priority, using a clean separator (`  |  `).
2. **`EXCLUSIVE`**: Only renders the layer with the absolute highest priority. All lower-priority layers are silently hidden until the high-priority layer expires or is removed.
3. **`CAROUSEL`**: Cycles through all active layers one-by-one based on the `actionBarAPI.carouselIntervalMs` setting (default: 2000ms / 2s).

### 3. Rendering Engine
Every 100ms, the asynchronous ticker task:
1. Iterates through all online players.
2. Filters out expired layers.
3. Sorts all active layers in descending priority order.
4. Processes the layers based on the selected `ActionBarStrategy`.
5. Transmits the consolidated message to the player's client.

---

## 🔗 Public API (`ActionBarAPI`)

The `ActionBarAPI` is accessible via the `ActionBarEssential` component:

```kotlin
// Change rendering strategy globally
actionBarAPI.strategy = ActionBarStrategy.EXCLUSIVE
actionBarAPI.carouselIntervalMs = 3000L // 3 seconds per carousel slide

// Registers or updates a layer (durationMs = null creates a permanent layer)
actionBarAPI.setLayer(
    uuid = player.uniqueId,
    id = "quest_hud",
    priority = 20,
    durationMs = null
) {
    Component.text("📜 Quest: Defeat 5 Zombies")
}

// Manually removes an active layer
actionBarAPI.removeLayer(player.uniqueId, "quest_hud")

// Clears all active layers
actionBarAPI.clearLayers(player.uniqueId)

// Compatibility/Legacy helper (displays a high-priority layer that expires after 3 seconds)
actionBarAPI.sendActionBar(player.uniqueId, Component.text("⚠️ Danger!"))
```

---

## 🛡️ Thread-Safety & Optimization Guardrails

- **Thread Safety**: All state registries are backed by `ConcurrentHashMap` instances. Custom layer provider lambdas are queried safely inside an asynchronous ticker thread without interrupting Minecraft's primary server thread.
- **Automatic Garbage Collection**: An `ActionBarListener` hooks into Bukkit's `PlayerQuitEvent` and immediately invokes `clearLayers(player.uniqueId)` to eliminate lingering memory allocations.
- **Performance Ticker**: Running the scheduler at a highly stable rate of 100ms minimizes payload footprint while retaining sub-second responsiveness for action bar animations.