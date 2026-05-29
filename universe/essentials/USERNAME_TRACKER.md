# Username Tracker Essential Module (UsernameTrackerEssential)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/essentials/usernameTracker/`

## 🎯 Main Purpose
Logs username history mappings (`UUID` -> Case-Sensitive Username) upon player logins, providing a historical registry for username audits.

## 🔗 Connections & Dependencies
* **Offline Modifier Bridge:** Extremely critical for offline player mutations. Allows the `PermissionsService` and `PlayerDataService` to resolve target UUIDs by executing offline name queries, ensuring safe transactional saves.\n