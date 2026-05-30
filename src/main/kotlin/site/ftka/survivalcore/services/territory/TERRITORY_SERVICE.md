# Territory Service

The `TerritoryService` is a robust Tier 3 tracking system designed to manage and persist player chunk claims. It acts as the definitive global registry for territory ownership across the server.

## Architecture

| Subservice | Responsibility |
|---|---|
| `TerritoryService` | Exposes the core `claimChunk`, `unclaimChunk`, and `getOwner` API. Keeps an active `ConcurrentHashMap` of all claims in memory for O(1) lookups. Fires proprietary events (`ChunkClaimedEvent`, `ChunkUnclaimedEvent`) upon territory modifications. |
| `Territory_StorageSubservice` | Handles asynchronous JSON persistence (`territory_claims.json`). Loads the global registry entirely into RAM on server startup. Saves asynchronously whenever a territory modification occurs to guarantee data integrity without impacting the main thread. |

## Data Flow
When a chunk is claimed via `claimChunk(uuid, x, z)`:
1. Validates the chunk is not already claimed.
2. Updates the global concurrent registry.
3. Triggers an asynchronous save via `storage_ss`.
4. Fires the `ChunkClaimedEvent` (which `SpawnFinderService` listens to).
5. Asynchronously updates the player's personal `PlayerData` by appending to their `unlockedChunks` list, ensuring player data perfectly correlates with global claims.
