# Chat Essential Module (ChatEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/chat/`

## 🎯 Main Purpose
Manages server-wide chat message routing, channel subscriptions (Global, Staff, Personal), and multi-page interactive chat screen overlays that capture player chat inputs.

## 🔗 Connections & Dependencies
* **Lifecycle Events:** Intercepts `PlayerDataRegisterEvent` to activate default channels and restore a player's chat history on login. Purges players and active screens on `PlayerDataUnregisterEvent`.
* **Consumer App:** Provides command routing and screen templates for the user-facing `ChatManagerApp`.

## 🛡️ Audit & Remediation (2026-05-29)
* Refactored core structures (`channelsMap`, `playersActiveChannels`, `playersInsideScreens`) to utilize `ConcurrentHashMap` and thread-safe collections.
* Resolved `ConcurrentModificationException` during iteration over screens.
* Fixed inverse log retrieval: Chat history now correctly loads the newest messages first using a `ConcurrentSkipListMap` with reversed ordering.
* Redraw spam optimization: `sendActiveFrame` now evaluates the layout and only transmits clear-chat updates if the screen frame dynamically mutated.\n