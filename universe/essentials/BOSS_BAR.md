# 🛡️ BossBar Essential Module (BossBarEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/bossbar/`

## 🎯 Main Purpose
Provides a priority-based, multiplexed BossBar rendering engine. Like the Action Bar system, it handles simultaneous display requests from various server subsystems without creating persistent clutter.

## 🔗 Connections & Dependencies
* **Utility Facade:** Extends high-level APIs (`BossBarAPI`) for Apps and Services (like Quest trackers, Combat tags, Regional events) to deliver persistent UI elements.
* **EssentialsFramework:** Initialized directly alongside other core services in `EssentialsFramework.kt`.
* **Kyori Adventure:** Directly depends on the native `net.kyori.adventure.bossbar.BossBar` instances for client-side rendering.

*(For detailed usage, architecture, and method signatures, see the exhaustive module documentation at `src/main/kotlin/site/ftka/survivalcore/essentials/bossbar/BOSSBAR_ESSENTIAL.md`)*
