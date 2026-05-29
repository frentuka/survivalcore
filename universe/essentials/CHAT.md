# Chat Essential Module (ChatEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/chat/`

## 🎯 Main Purpose
Manages server-wide chat message routing, channel subscriptions (Global, Staff, Personal), and multi-page interactive chat screen overlays that capture player chat inputs.

## 🔗 Connections & Dependencies
* **Lifecycle Events:** Intercepts `PlayerDataRegisterEvent` to activate default channels and restore a player's chat history on login. Purges players and active screens on `PlayerDataUnregisterEvent`.
* **Consumer App:** Provides command routing and screen templates for the user-facing `ChatManagerApp`.\n