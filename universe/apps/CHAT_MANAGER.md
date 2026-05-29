# Chat Manager App Module (ChatManagerApp)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/apps/ChatManager/`

## 🎯 Main Purpose
Staff-facing command module (`/chat`) that coordinates chat screen activations, staff messaging toggles, and channel moderation.

## 🔗 Connections & Dependencies
* **Chat Integration:** Relies on `ChatEssential` to manipulate active screen sessions and route channel changes.

## 🛡️ Audit & Remediation (2026-05-29)
* **Critical Security Fix:** Corrected inverted logic gate in `/chat` execution that erroneously bypassed permissions.
* **Scope Fix:** `/chat <player> channels disable <channel>` correctly delegates muting to the target player rather than the staff member executing the command.\n