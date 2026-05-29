# Proprietary Events Bus Module (ProprietaryEventsInitless)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/initless/proprietaryEvents/`

## 🎯 Main Purpose
A reflection-based, decoupled synchronous event bus (`ProprietaryEventsInitless`) that operates independently of Bukkit's standard event system. It enables components to subscribe to and publish internal events (`PropEvent`) via `@PropEventHandler` annotations with configurable priority levels.

## 🔗 Connections & Dependencies
* **Core Decoupler:** Eliminates compile-time circular dependencies between different tiers of the framework.
* **Critical Lifecycle Events:** Decouples core framework triggers including database disconnects, player data loads, permissions calculations, and channel purges.\n