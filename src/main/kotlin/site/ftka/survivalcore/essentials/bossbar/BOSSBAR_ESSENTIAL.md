# 🛡️ BossBar Essential Module (BossBarEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/bossbar/`

## 🎯 Main Purpose
Provides a priority-based, multiplexed BossBar rendering engine. Like the Action Bar system, it handles simultaneous display requests from various server subsystems (e.g., combat loggers, region announcements, health bars, quest objectives) without creating persistent clutter.

---

## 🏗️ Architecture & Component Design

```text
+----------------------------------------------------------------------+
|                           BossBarAPI                                 |
|        (Exposes layer management: set, remove, clear)                |
|    (Configures display strategy: STACK, EXCLUSIVE, CAROUSEL)         |
+----------------------------------------------------------------------+
                                   │
                                   ▼
+----------------------------------------------------------------------+
|                 BossBarEssential_MessagingSubservice                 |
|  - playerLayers: Map<UUID, Map<String, BossBarLayer>>                |
|  - activeBars: Map<UUID, List<ActiveBossBar>>                        |
|  - Async Ticker Scheduler (Running every 100ms)                      |
+----------------------------------------------------------------------+
         │                                              │
         ▼                                              ▼
+-------------------------+                   +------------------------+
|     BossBarListener     |                   |      BossBarLayer      |
| (Cleans up player maps  |                   | (Holds id, priority,   |
|   on PlayerQuitEvent)   |                   |  expiry, & providers)  |
+-------------------------+                   +------------------------+
```

### 1. `BossBarLayer`
A layer storing the reactive providers for an active BossBar requirement. It automatically executes functional variables (`() -> Component?`) upon every tick to animate the bar efficiently without static updating.

### 2. Multiplexing Strategies (`BossBarStrategy`)
- **`STACK` (Default)**: Renders up to `maxStackedBars` simultaneously stacked on the client. Over-the-limit bars are hidden but queued if a higher priority bar disappears.
- **`EXCLUSIVE`**: Hides everything else and displays only the absolute highest priority BossBar instance.
- **`CAROUSEL`**: Rotates through active priorities using `carouselIntervalMs` timing.

### 3. Rendering Engine
- Every 100ms tick, evaluates priorities.
- Synchronizes with `activeBars` which wrapper the pure `net.kyori.adventure.bossbar.BossBar` implementation.
- Executes `player.hideBossBar()` or `player.showBossBar()` transparently depending on layer invalidations.

---

## 🔗 Public API (`BossBarAPI`)

```kotlin
// Change the strategy dynamically
bossbarAPI.strategy = BossBarStrategy.STACK
bossbarAPI.maxStackedBars = 2

// 1. Player-Specific Layer (e.g. Combat)
bossbarAPI.setLayer(
    uuid = player.uniqueId,
    id = "combat",
    priority = 100,
    durationMs = 15_000L,
    color = { BossBar.Color.RED },
    progress = { getCombatTimeRemaining() / 15f }, // Automatically animated!
    title = { Component.text("⚔ In Combat") }
)

// 2. Global Server-Wide Layer (e.g. Server Restart)
// Overrides and displays for EVERYONE automatically!
bossbarAPI.setGlobalLayer(
    id = "restart_alert",
    priority = 1000,
    durationMs = null,
    color = { BossBar.Color.YELLOW },
    progress = { 1.0f },
    title = { Component.text("⚠️ Server Restarting in 5 Minutes!") }
)
```

## 🌟 Versatility Features
1. **Global BossBars**: Supports server-wide broadcast bars (`setGlobalLayer`) that automatically inject into every player's view alongside their personal layers.
2. **Graceful Hiding**: If a `titleProvider` ever returns `null` temporarily, the bar smoothly hides itself for that tick, rather than freezing on the player's screen!
3. **Player Overrides**: If a personal layer is registered with the exact same `id` as a global layer, the personal layer naturally overrides it for that specific player.

## 🛡️ Performance Profile
Memory structures utilize `ConcurrentHashMap` safely. Direct references to `net.kyori.adventure.bossbar.BossBar` are eliminated gracefully by hooking into `PlayerQuitEvent`.
