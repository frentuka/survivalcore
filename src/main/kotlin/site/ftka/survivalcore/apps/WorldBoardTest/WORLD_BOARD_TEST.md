# WorldBoardTest App Documentation

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/apps/WorldBoardTest/`

## 🎯 Main Purpose
A utility app strictly designed for developers and server administrators to test the functionality, rendering, and lifecycle of the Tier 3 `WorldBoardService` module in-game.

## 🔗 Commands
* `/wbtest`: Spawns an animated, gradient-colored holographic board exactly 3 blocks in front of the executor's eye location and simultaneously draws a matching, color-synchronized 3D particle chunk boundary cage using the `BorderService`. Both elements have an internal lifecycle timer that safely despawns them after exactly 20 seconds.

## 🛡️ Permissions
* `staff.admin`: Required to execute the test command.
