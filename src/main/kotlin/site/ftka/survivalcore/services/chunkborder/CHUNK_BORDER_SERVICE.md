# Chunk Border Service

The `ChunkBorderService` provides physical block-based chunk borders for players, creating a "cage" around their unlocked chunks using Gray Stained Glass. When chunks are unlocked, it seamlessly restores the original vanilla world generation.

## Architecture

This service is a Tier 3 component (`Services`) and handles the entire lifecycle of building borders, caching block states, and restoring them.

### Subservices

| Subservice | Responsibility |
|---|---|
| `Border_GenerationSubservice` | Replaces naturally generated border blocks with Gray Stained Glass from Y=-64 to Y=319. |
| `Border_StorageSubservice` | Serializes and saves original `BlockData` (including NBT for Tile Entities like chests) to Redis. |
| `Border_RestorationSubservice` | Rebuilds the saved blocks smoothly when a chunk is unlocked. Sorts attachable blocks so they don't break. |

## Data Flow (Caching Option A)

1. **Chunk Locked:**
   - System identifies border chunks.
   - `StorageSubservice` caches natural blocks that are on the border edges.
   - `GenerationSubservice` replaces these blocks with Gray Stained Glass (applyPhysics = false).
2. **Chunk Unlocked:**
   - `RestorationSubservice` pulls cached data from Redis.
   - Solid blocks are placed first.
   - Attachable blocks (signs, torches) are placed last to prevent physics drops.
   - Tile entities are recreated with their full NBT (inventory contents, etc.).

## Protection

To prevent world boundary breaches and keep player areas fully protected, `BorderListener` implements robust block protection mechanics:
- **`BlockBreakEvent`**: Completely blocks any player from breaking gray stained glass blocks belonging to active border regions.
- **`EntityExplodeEvent` / `BlockExplodeEvent`**: Filters out gray stained glass border blocks from explosion lists (TNT, Creepers, Respawn Anchors, Beds) to keep the borders intact.
- **`BlockPistonExtendEvent` / `BlockPistonRetractEvent`**: Cancels piston movements if they attempt to push or pull any gray stained glass border blocks, preventing physical manipulation of territory walls.
- **`PlayerInteractEvent`**: Captures left/right clicks on glass border blocks to trigger proprietary `BorderPunchEvent`.

## Testing
- The `/wbtest` command executes a quick build and restore of a chunk border around the current chunk, validating caching and restoration without requiring a full player lifecycle.
