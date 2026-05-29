# Permissions Service Module (PermissionsService)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/services/permissions/`
* **Documentation:** [PERMISSIONS_SERVICE.md](file:///home/srleg/Projects/survivalcore/src/main/kotlin/site/ftka/survivalcore/services/permissions/PERMISSIONS_SERVICE.md)

## 🎯 Main Purpose
Resolves player rank inheritances, case-insensitive group updates, progressive left-to-right hierarchical wildcards (e.g., `survivalcore.chat.*`), and dynamic Spigot permissible attachments.

## 🔗 Connections & Dependencies
* **Profile Integration:** Stores permissions and group lists inside `PlayerData.permissions` profiles.
* **Join Listener:** Subscribes to `PlayerDataRegisterEvent` to compile a Paper-compatible `PermissionAttachment` bound to the active player thread on the primary server thread.\n