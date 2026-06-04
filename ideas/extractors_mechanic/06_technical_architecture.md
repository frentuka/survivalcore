# Technical Architecture

How the Extractor Mechanic integrates with SurvivalCore's four-tier architecture, grounded in the actual codebase patterns.

---

## Placement within the Framework

```
Initless  →  Essentials  →  Services  →  Apps
                             ↑               ↑
                     CustomItemService   ExtractorApp
                     ExtractorService
```

> ⚠️ **Prerequisite:** `CustomItemService` must be built before `ExtractorService`. See [09_custom_item_service_plan.md](09_custom_item_service_plan.md) for the implementation plan.

### Physical Representation

- **In inventory:** Custom player head with unique skin per type/tier (like Hypixel Skyblock minions)
- **In world:** A ~3×3×5 controlled structure built from real blocks, enhanced with particles/armor stands/holograms
- **Blocks cannot be broken normally** — they re-appear, but breaking them deals virtual damage to the extractor
- **Pickup:** Via "Disassembly" button in the GUI (blocked if damaged)
- **Placement:** On the floor, with enough space, not too close to chunk borders

---

## Tier 3: `ExtractorService`

### Subservices

| Subservice | Responsibility |
|------------|----------------|
| `Extractor_IOSubservice` | Saves/loads extractor state to Redis |
| `Extractor_ScanningSubservice` | Async block counting when a player uses the Geological Scanner |
| `Extractor_MiningSubservice` | Extraction engine: finds target blocks, applies block locks, replaces blocks, deposits items |
| `Extractor_FuelSubservice` | Fuel consumption tracking and Compact Coal economy |
| `Extractor_InfestationSubservice` | Heat calculation, mob spawning, siege orchestration (online and offline) |
| `Extractor_ClaimSubservice` | Chunk ownership validation and structure block registry |
| `Extractor_CatchupSubservice` | Offline extraction simulation on chunk load (with fuel/storage accounting) |
| `Extractor_HealthSubservice` | Extractor health, damage from mobs and structure breaks, repair |

### Dependencies

| Dependency | Usage | Access Pattern |
|------------|-------|----------------|
| `DatabaseEssential` | Persist extractor data | `plugin.essentialsFwk.database` (internal) |
| `ConfigsEssential` | `ExtractorConfig` for tuning | `plugin.essentialsFwk.configs` (internal) |
| `ProprietaryEventsInitless` | Fire custom events | `plugin.propEventsInitless.fireEvent()` |
| `PlayerDataService` | Link extractors to player ownership | `plugin.servicesFwk.playerData.api` |
| `InventoryGUIService` | Extractor GUI and Scanner calibration GUI | `plugin.servicesFwk.inventoryGUI.api` |

---

## Tier 4: `ExtractorApp`

| Responsibility | Details |
|----------------|---------|
| Commands | `/extractor list` — View all owned extractors and coordinates |
| GUIs | Extractor management GUI, Scanner calibration GUI |
| Crafting | Register custom recipes for scanners, extractors, modules, compact blocks |

---

## Integration with Existing Systems

### InventoryGUIService Integration

The Extractor GUI and Scanner Calibration GUI must implement the `InventoryGUIOwner` interface:

```kotlin
interface InventoryGUIOwner : InventoryHolder {
    val ownerName: String
    fun openEvent(event: InventoryOpenEvent) {}
    fun closeEvent(event: InventoryCloseEvent) {}
    fun clickEvent(event: InventoryClickEvent) {}
    fun dragEvent(event: InventoryDragEvent) {}
}
```

**Pattern:**
1. Create a class implementing `InventoryGUIOwner`
2. In `init`, call `plugin.servicesFwk.inventoryGUI.api.createInventory(this, size, title)`
3. Implement `getInventory()` to return the inventory
4. Open via `player.openInventory(gui.inventory)`

Event routing is automatic — `InventoryGUIDetectionListener` casts `event.inventory.holder as? InventoryGUIOwner`.

### PlayerData Integration

Extractor ownership data should be added as a new module to the `PlayerData` model. The existing structure:

```kotlin
data class PlayerData(val uuid: UUID) {
    var information:    PlayerInformation?  = PlayerInformation()
    var state:          PlayerState?        = PlayerState()
    var permissions:    PlayerPermissions?  = PlayerPermissions()
    var settings:       PlayerSettings?     = PlayerSettings()
    var unlockedChunks: MutableList<Pair<Int, Int>> = mutableListOf()
    // ...
}
```

> ⚠️ **GSON constraint:** Any new field MUST be nullable (`?`) with a default value, because GSON bypasses constructors on deserialization.

A new module like `var extractors: PlayerExtractors? = PlayerExtractors()` could hold owned extractor UUIDs and lightweight metadata.

### ProprietaryEvents Integration

Custom events follow this pattern:

```kotlin
class ExtractorPlacedEvent(
    val playerUuid: UUID,
    val extractorUuid: UUID,
    val chunkX: Int,
    val chunkZ: Int
) : PropEvent {
    override val name: String = "ExtractorPlacedEvent"
    override val async: Boolean = true
    override var cancelled: Boolean = false
}
```

> ⚠️ **Thread safety:** `fireEvent()` invokes handlers synchronously on the calling thread. Do not fire from async contexts if handlers touch Bukkit API.

### ConfigsEssential Integration

Create an `ExtractorConfig` following the existing pattern:

```kotlin
class ExtractorConfig {
    var version: Int = 1
    var extractionIntervalTicks: Int = 1200  // 60 seconds
    var maxExtractorsPerPlayer: Int = 4
    var fuelPerCompactCoalBlock: Int = 100
    var catchupMaxHours: Int = 24
    // ...
    fun toJson(): String = GsonBuilder().setPrettyPrinting().create().toJson(this)
}
```

Add the enum entry, field, and loading logic to `ConfigsEssential`.

---

## Catch-Up Algorithm (`Extractor_CatchupSubservice`)

When a chunk is loaded, each extractor in that chunk runs the following algorithm:

```
1. elapsed = now - lastActiveTimestamp
2. elapsed = min(elapsed, catchupMaxHours * 3600 seconds)   // cap
3. cycles  = floor(elapsed / offlineCooldownSeconds)
4. for each simulated cycle:
   a. If fuel <= 0: stop simulation (extractor ran dry mid-catchup)
   b. Deduct one cycle's worth of fuel from fuel pool
   c. If storage is full: skip item deposit but still consume fuel and break block
      (items are lost — the extractor "mined into a full hopper" and ore fell to the ground)
   d. Attempt to find an unlocked target block in the chunk (using block locking rules)
   e. If found: replace block, deposit item into storage (unless storage full), increment mined count
   f. If not found: swing-and-miss, continue to next cycle
5. Simulate offline infestations:
   a. Calculate how many infestation events should have fired during elapsed time
   b. For each event: apply damage directly to health pool without spawning actual mobs
      (mobs are force-loaded only for real-time events; catchup applies flat damage)
   c. If health reaches 0 during catchup simulation: extractor is destroyed, inventory drops
6. Update lastActiveTimestamp = now
```

### Performance Guardrails

- Catch-up block replacement is performed in **batches of max 50 blocks per tick** to avoid region-thread lag spikes
- If the total simulated cycles would exceed the batch budget, remaining cycles are deferred to subsequent ticks (spread across up to 5 ticks after chunk load)
- Catch-up runs **after** the normal chunk load sequence to avoid competing with world population

---

## Data Model (Redis)

The existing `DatabaseEssential` uses **flat string keys with JSON string values**. API: `get(key)`, `set(key, value)`, `exists(key)`, `del(key)`.

**Proposed key structure:**

```
extractor:{extractorUuid}                       → Extractor state JSON
extractor:chunk:{world}:{chunkX}:{chunkZ}       → List of extractor UUIDs active in this chunk
extractor:structure:{world}:{x}:{y}:{z}         → Extractor UUID that owns this structure block
```

Per-player extractor ownership should live inside `PlayerData` (serialized as part of the player's JSON blob), not as separate Redis keys.

### Extractor State Object (Draft)

```json
{
  "uuid": "extractor-uuid",
  "ownerUuid": "player-uuid",
  "trustedPlayers": [],
  "type": "DIAMOND",
  "tier": 3,
  "location": { "world": "world", "x": 100, "y": 40, "z": -200 },
  "chunkX": 6, "chunkZ": -13,
  "analysisMap": {
    "materials": ["DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE"],
    "yRangeTop": 16,
    "yRangeBottom": -48
  },
  "fuel": 42,
  "health": 100,
  "maxHealth": 100,
  "inventory": [],
  "modules": {
    "drillSpeed": 1,
    "fortune": 0,
    "furnace": false,
    "compactor": false,
    "storage": 0
  },
  "status": "ACTIVE",
  "lastActiveTimestamp": 1717372800000
}
```

---

## Folia Scheduling Patterns

Based on existing codebase patterns, the extractor should use:

### 1. `globalRegionScheduler.runAtFixedRate` — Main extraction tick loop

```kotlin
// Pattern from existing Gameplay_TerritoryBorderSubservice.kt:
extractionTask = plugin.server.globalRegionScheduler.runAtFixedRate(plugin, { _ ->
    tickAllExtractors()
}, 2L, 2L)  // initialDelay, period (in ticks)
```

Returns `ScheduledTask` which can be cancelled via `task.cancel()`.

### 2. `Bukkit.getRegionScheduler().execute` — Block operations

For physically breaking/replacing blocks, **must** execute on the correct region thread:

```kotlin
Bukkit.getRegionScheduler().execute(plugin, blockLocation) {
    chunk.getBlock(x, y, z).setType(Material.COBBLESTONE)
}
```

### 3. `player.scheduler.execute` — Player interactions

For opening GUIs or modifying player inventory:

```kotlin
player.scheduler.execute(plugin, {
    player.openInventory(extractorGui.inventory)
}, null, 1L)
```

---

## Admin Tools

Staff with the **`staff.extractors`** permission have elevated access to all extractors regardless of ownership.

### Permissions

| Permission | Effect |
|------------|--------|
| `staff.extractors` | Bypass owner check; open any extractor GUI; delete any extractor |

### Staff GUI Behavior

When a staff member opens an extractor they don't own, the GUI displays a **prominent warning banner** at the top:

> ⚠️ *You are viewing [PlayerName]'s [Diamond Extractor Tier III]. Actions taken here affect another player's property.*

This warning exists to prevent accidental modifications during routine admin work.

### Staff Commands

| Command | Effect |
|---------|--------|
| `/extractor admin list [player]` | List all extractors owned by a player with coordinates and status |
| `/extractor admin delete <uuid>` | Safely remove a stuck/bugged extractor and drop its contents |
| `/extractor admin inspect <uuid>` | Open the extractor GUI with the admin warning banner |
| `/extractor admin status` | Server-wide summary: total active extractors, total fuel consumed, heat distribution |

> All admin interactions are **logged** via the existing `LoggingInstance` system with player UUID, target extractor UUID, action type, and timestamp.

---

## Critical Technical Constraints

| Constraint | Detail |
|---|---|
| **No PDC on blocks** | Vanilla blocks don't have `PersistentDataContainer`. Extractor tracking must use location-based maps in memory/Redis, not PDC. PDC works on entities/items only. |
| **GSON nullability** | Any new `PlayerData` module field must be nullable (`?`) with a default value. |
| **Redis simplicity** | Flat string keys + JSON values only. No Redis hash/set structures. |
| **Event bus is synchronous** | `fireEvent()` runs handlers on the calling thread. |
| **Internal visibility** | `DatabaseEssential` and `ConfigsEssential` are `internal` — accessible within SurvivalCore via `plugin.essentialsFwk.*` |
| **No custom item system** | No existing `CustomModelData`, custom recipe registration, or custom item framework. **Must be built first.** |
| **Existing NamespacedKey patterns** | PDC tags follow `NamespacedKey(plugin, "tag_name")` with `PersistentDataType.STRING`. Used for `spawner_owner`, `virtual_anvil_input`, `worldboard`. |
