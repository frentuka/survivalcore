# Permissions Manager App Documentation

The `PermissionsManagerApp` is a Tier 4 administrative application designed to configure, manage, and audit permission groups and player permissible parameters visually or via CLI within SurvivalCore.

It acts as the primary user-facing layer for `PermissionsService` (Tier 3) and coordinates with `PlayerDataService` and `UsernameTrackerEssential` to persist and mutate player permissions safely.

## Features and Subsystems

### 1. Unified Commands & Console Support
The application exposes two administrative command blocks with complete, cross-compatible support for both player senders and server console administrators.

#### 1.1 `/groups` (or `/g`, `/group`)
- **`/groups`** (Player): Opens the `GroupsListGUI` visual inventory interface.
- **`/groups list`**: Prints a textual list of loaded groups on the server with their primary colors.
- **`/groups create <name>`**: Creates a new group and initializes its JSON file.
- **`/groups delete <name>`**: Purges a group, cleaning up inheritances.
- **`/groups rename <old> <new>`**: Safely renames a group.
- **`/groups <group> info [category]`**: Inspects a group's metadata. Supports sections `inheritances`, `perms`, `onlineMembers`, or `members` (reads the offline players database asynchronously).
- **`/groups <group> settag <tag>`**: Updates chat prefix.
- **`/groups <group> setcolor <color>`**: Updates primary color.
- **`/groups <group> addperm/removeperm <node>`**: Manages direct permissions.
- **`/groups <group> addinh/removeinh <parent>`**: Manages inheritance.

#### 1.2 `/permissions` (or `/perms`, `/p`)
- **`/perms`** (Player): Opens the `GroupsListGUI` visual interface.
- **`/perms info <player>`**: Inspects a player's direct permissions, inherited permissions, display group, and joined groups.
- **`/perms <player> setdisplaygroup <group>`**: Set player's primary display group.
- **`/perms <player> addgroup/removegroup <group>`**: Manages player group memberships.
- **`/perms <player> addperm/removeperm <node>`**: Manages direct player permissions.

---

### 2. Premium Inventory GUIs
When executed by players in-game, `/groups` opens an interactive, paginated, and context-safe graphical interface utilizing premium virtual anvil inputs for metadata edits, entirely bypassing the chat:
- **`GroupsListGUI`**: Displays wool items colored dynamically using group color mappings. Includes member/inheritance counters and an emerald block to create groups using a virtual Anvil prompt.
- **`GroupDetailGUI`**: Single-group dashboard displaying core metadata, an editor for permissions, a shield for inheritance management, a TNT block for group deletion, and a painting button redirecting to the visual configurations submenu.
- **`GroupVisualsGUI`**: Dedicated submenu for styling, providing three interactive buttons to visually set chat tags, primary colors, and secondary colors.
- **`GroupColorPickerGUI`**: Provides a hand-picked palette of vibrant named colors and a custom RGB input button, allowing admins to set standard colors or type custom hex codes (e.g. `"#FF55AA"`).
- **`GroupDeleteConfirmGUI`**: A safety confirmation screen prompting the admin with split Cancel (Red Concrete) and Confirm (Green Concrete) blocks to prevent accidental deletions.
- **`GroupPermsGUI`**: A paginated layout listing a group's permission nodes with simple click removal and an advanced virtual Anvil editor for permission additions.
- **`GroupInheritanceGUI`**: A checklist grid displaying other groups. Inheritances are represented by lime dyes (enabled) and gray dyes (disabled); clicking instantly toggles inheritance.

---

### 3. Context-Aware Tab Completions
Both commands register a custom tab completer class providing premium, permission-aware parameter suggestions:
- **Subcommand Filter:** Dynamically hides administrative options from unauthorized players.
- **Group Suggestion:** Completes group names based on actual loaded groups.
- **Player Suggestion:** Matches online player names or offline player databases via `UsernameTracker`.
- **Direct Perms for Removal:** `/groups <group> removeperm` and `/perms <player> removeperm` suggest only permissions currently owned by the target, preventing typos.
- **Inheritances Mapping:** `/groups <group> addinh` suggests groups not currently inherited, and `/groups <group> removeinh` suggests only inherited ones.

---

### 4. Robust Chat Interceptor
For legacy workflows or general input safety, the custom `PermissionsManager_ChatInputInterceptor` runs at `LOWEST` and `MONITOR` priorities. This order of execution prevents intercepted input messages from leaking into the global chat, ensuring a seamless staff experience.

---

## Permissions

- `survivalcore.admin.permissions`: Grants complete access to manage permissions, open GUIs, execute console-safe commands, and perform profile audits.

