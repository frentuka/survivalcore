# Language Service Module (LanguageService)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/services/language/`

## 🎯 Main Purpose
Manages multi-lingual text localization by mapping player settings to locale language packs (e.g., `"es"`, `"en"`), returning translated messages dynamically.

## 🔗 Connections & Dependencies
* **Profile Settings:** Reads language codes from `PlayerData.settings.language`.
* **Join Mapping:** Intercepts `PlayerDataRegisterEvent` to populate local memory cache maps with player-selected language preferences.\n