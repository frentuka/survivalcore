# Core Architecture

The Custom Item Service is broken into four distinct subservices to maintain clean separation of concerns.

## 1. Subservice Layout

```
Services (Tier 3)
└── CustomItemService
    ├── CustomItem_RegistrySubservice      — Item type definitions & instance tracking
    ├── CustomItem_CachingSubservice       — In-memory mirror of tracked UUIDs
    ├── CustomItem_SerializationSubservice — PDC read/write & item (de)serialization
    ├── CustomItem_ValidationSubservice    — Anti-dupe checks & item integrity (See 02_anti_duplication.md)
    └── CustomItem_CraftingSubservice      — Custom recipe registration (See 03_custom_crafting.md)
```

## 2. Core Data Model

### Item Type Definition

Every custom item is defined by a `CustomItemType`. This is the blueprint.

```kotlin
data class CustomItemType(
    val id: String,                      // e.g., "compact_diamond_block", "scanner_tier_3"
    val displayName: Component,          // Kyori Adventure component
    val material: Material,              // Base vanilla material (e.g., PLAYER_HEAD, COAL_BLOCK)
    val lore: List<Component>,           // Item lore lines
    val customModelData: Int?,           // Optional custom model data for resource packs
    val maxStackSize: Int = 64,          // 1 for unique items (extractors), 64 for bulk items
    val trackInstances: Boolean = false, // If true, every instance gets a UUID and is tracked
    val playerBound: Boolean = false,    // If true, only the owner can use/move it
    val headSkin: String? = null         // Base64 skin texture (for PLAYER_HEAD items)
) {
    init {
        require(!trackInstances || maxStackSize == 1) { 
            "Tracked custom items must have a maxStackSize of 1 to ensure unique UUID integrity." 
        }
    }
}
```

### Persistent Data Container (PDC) Tags

Every custom item instance in the game carries these tags in its PDC. This is how the server identifies them.

| NamespacedKey | Type | Description | Required |
|---------------|------|-------------|----------|
| `survivalcore:custom_item_type` | STRING | The item type ID (e.g., "scanner_tier_3") | Always |
| `survivalcore:custom_item_uuid` | STRING | Unique instance UUID | Only if `trackInstances = true` |
| `survivalcore:custom_item_owner` | STRING | Owner player UUID | Only if `playerBound = true` |
| `survivalcore:custom_item_data` | STRING | JSON blob for type-specific metadata (e.g., heat level, installed modules) | Optional |

## 3. The Registry Subservice

**Responsibility:** Manage item type definitions and track individual item instances in Redis.

**Redis Storage Structure:**
```
customitem:types                    → JSON array of all registered item type IDs
customitem:instance:{instanceUuid}  → JSON with item state (type, owner, creation time)
```
*Note: Bulk items (like Compact Coal Blocks) where `trackInstances = false` do not get individual Redis entries. They are identified solely by their PDC type tag.*

**Caching & Synchronization:**
To prevent stalling Folia region threads during synchronous inventory events, the `CustomItem_CachingSubservice` maintains a local, in-memory mirror of the instance registry. 
- When an inventory or chunk is loaded, relevant UUIDs are pulled from Redis into the cache.
- Redis Pub/Sub broadcasts creation/destruction events to keep all server regions synchronized instantaneously.

**Key Behaviors:**
- `createInstance(type)` constructs the `ItemStack`, writes the PDC tags, registers the UUID in Redis, and pushes it to the local cache.
- `destroyInstance(uuid)` removes the instance from Redis, instantly invalidating any copies that might exist in the world via Pub/Sub cache invalidation.

## 4. The Serialization Subservice

**Responsibility:** Safe, centralized read/write operations for PDC tags.

**Key Behaviors:**
- All PDC operations go through this subservice — absolutely no raw PDC access in other classes.
- Handles `ItemMeta` acquisition and modification safely.
- Serializes and deserializes the `custom_item_data` JSON blob for things like Extractor metadata.
