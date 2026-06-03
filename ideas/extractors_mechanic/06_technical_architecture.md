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
| `Extractor_MiningSubservice` | The extraction engine: finds target blocks, replaces them, deposits items |
| `Extractor_FuelSubservice` | Fuel consumption tracking and Compact Coal economy |
| `Extractor_InfestationSubservice` | Heat calculation, mob spawning, siege orchestration |
| `Extractor_ClaimSubservice` | Chunk claiming and ownership validation |
| `Extractor_CatchupSubservice` | Offline extraction simulation on chunk load |
| `Extractor_HealthSubservice` | Extractor health, damage from mobs, repair |

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
    var maxExtractorsPerPlayer: Int = 3
    var fuelPerCompactCoalBlock: Int = 100
    var catchupMaxHours: Int = 24
    // ...
    fun toJson(): String = GsonBuilder().setPrettyPrinting().create().toJson(this)
}
```

Add the enum entry, field, and loading logic to `ConfigsEssential`.

---

## Data Model (Redis)

The existing `DatabaseEssential` uses **flat string keys with JSON string values**. API: `get(key)`, `set(key, value)`, `exists(key)`, `del(key)`.

**Proposed key structure:**

```
extractor:{extractorUuid}                → Extractor state JSON
extractor:chunk:{world}:{chunkX}:{chunkZ} → Extractor UUID at this chunk
```

Per-player extractor ownership should live inside `PlayerData` (serialized as part of the player's JSON blob), not as separate Redis keys.

### Extractor State Object (Draft)

```json
{
  "uuid": "extractor-uuid",
  "ownerUuid": "player-uuid",
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
