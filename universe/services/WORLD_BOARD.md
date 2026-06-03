# WorldBoard Service Module (WorldBoardService)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/services/worldboard/`
* **API Entry:** `WorldBoardAPI.kt`

## 🎯 Main Purpose
A zero-lag, Folia-compliant framework for deploying 3D holographic text displays into the world using modern Minecraft `TextDisplay` entities, completely abstracted from thread management complexities.

## 🔗 Connections & Dependencies
* **Consumer Modules**: Built to be consumed by apps like `ChunkLock` to visually mark claims or territory borders with floating, animated signboards.
* **Engine Cleanup**: Relies on `ServicesFramework`'s strict `stop()` sequence to despawn entities and prevent orphaned ghost models during reloads.

## 📊 Core Architecture
1. **Thread Abstraction**: `WorldBoardInstance` wraps the entity. By encapsulating Folia's `RegionScheduler` for spawning and `EntityScheduler` for mutation, consumers can update board text or trigger Matrix scaling animations synchronously without triggering `ThreadStateException` crashes.
2. **Race Condition Prevention**: Incorporates `AtomicBoolean` logic to ensure async spawning operations yield gracefully if the board is abruptly removed via the API prior to completion.
