# Server Administration App Module (ServerAdministrationApp)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/apps/ServerAdministration/`

## 🎯 Main Purpose
Administrative dashboard (`/server`) for live config reloads, database connection testing, and server performance auditing.

## 🔗 Connections & Dependencies
* **System Actions:** Invokes `ConfigsEssential` to trigger live reloads, and audits active connection states in `DatabaseEssential`.\n