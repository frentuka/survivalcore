# Permissions Manager App Module (PermissionsManagerApp)

## 📌 Code Path
* **Directory:** `src/main/kotlin/site/ftka/survivalcore/apps/PermissionsManager/`

## 🎯 Main Purpose
Administrative command module (`/permissions`, `/groups`) rendering interactive GUI panels for mutating permission nodes and group inheritances.

## 🔗 Connections & Dependencies
* **GUI & Service Hooks:** Uses `InventoryGUIService` to render menus, and calls `PermissionsService` to safely write mutations to active group files and player states.\n