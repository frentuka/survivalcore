# PlayerDataManager App Documentation

The `PlayerDataManagerApp` is a Tier 4 interactive dashboard application built to manage user profiles securely within SurvivalCore. 

It provides an inventory-based GUI paired with dynamic chat text-input capabilities to interact deeply with `PlayerDataService`, `DatabaseEssential`, and `TerritoryService`.

## Features and Subsystems

### 1. Unified Command (`/playerdata` or `/pdata`)
- **`/pdata`**: Opens a global list of all currently online players via the `OnlineListGUI`.
- **`/pdata <username>`**: Opens the `MainGUI` dashboard directly for any player (online or offline), using `UsernameTrackerEssential` to resolve their UUID.
- **`/pdata delete <username>`**: Fast-track command-line alias to trigger the `DeleteConfirmGUI`.

### 2. Interactive Navigation (GUIs)
The app uses the memory-leak-safe `InventoryGUIOwner` structure.

*   `PlayerDataManager_OnlineListGUI`: Displays player heads dynamically mapped to online profiles. Includes a search button that opens a ChatScreen to query offline players by name.
*   `PlayerDataManager_MainGUI`: Shows core profile details like First Join, Health, Gamemode, and Unlocked Chunks in a dashboard format. Acts as a hub to edit or delete data.
*   `PlayerDataManager_EditorGUI`: Uses dynamic GSON serialization to create a nested, tree-based file browser of the `PlayerData` object. Clicking an object opens a sub-folder. Clicking a primitive value (like Health or FirstJoin timestamp) intercepts the player's next chat message to perform an asynchronous property replacement.
*   `PlayerDataManager_DeleteConfirmGUI`: An explicit dual-sided confirmation screen to prevent accidental deletions.

### 3. Total Data Wipe (Wipe Sequence)
When an administrator with the `staff.admin.playerdatamanager` permission confirms deletion inside the `DeleteConfirmGUI`, the following destructive operations run in sequence:
1.  **Eviction**: Kicks the player immediately if they are online.
2.  **Memory Unload**: Evicts their profile from `PlayerDataService` live memory (`removePlayerData`) and local cache.
3.  **Redis Database Purge**: Triggers `essentialsFwk.database.api.del` to physically delete their profile from the Redis database.
4.  **Dump Clear**: Erases any emergency file dumps via `PlayerData_EmergencySubservice`.
5.  **Territory Orphanage**: Iterates over `TerritoryService.claims` and fully unclaims every single chunk they owned, returning the land to the server.

## Event Hooking
The app hooks tightly into `ChatEssential` using `ChatScreen` and `ChatScreenPage` to temporarily blind standard chat operations, intercept typed property values or search queries, and resume GUI presentation.
