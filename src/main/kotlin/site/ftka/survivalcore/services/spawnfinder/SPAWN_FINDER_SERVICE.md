# Spawn Finder Service

The `SpawnFinderService` provides a highly optimized, Folia-compliant chunk scanning framework for discovering and caching valid player spawn locations. It guarantees that new players spawn in resource-rich environments while remaining isolated from other players' territories.

## Architecture

| Subservice | Responsibility |
|---|---|
| `SpawnFinderService` | Manages the active memory pool of validated spawn chunks (`validSpawns`). Listens to `ChunkClaimedEvent` to continuously prune spawn locations that become invalid due to territory expansion. |
| `SpawnFinder_StorageSubservice` | Asynchronously serializes the valid spawns pool to `valid_spawns.json`, ensuring the cached spawns survive server restarts without needing costly re-scans. |
| `SpawnFinder_AlgorithmSubservice` | Executes the grid-search based chunk analysis algorithm on Folia's `regionScheduler`. |

## The Algorithm
Instead of blindly checking chunks randomly, the algorithm uses an optimized grid-search approach:
1. Steps outwardly in intervals of 4 chunks (`step = 4`).
2. Automatically skips any coordinates within 48 chunks of a player's territory claim.
3. If an unchecked chunk is deemed safe based on distance, it dispatches a task to the region scheduler.
4. Pre-checks the center biome using `world.getBiome(...)` directly. If the biome is blacklisted (e.g., Oceans, Mushroom Fields, Rivers), it discards the chunk instantly **before** loading or generating it.
5. If the biome is valid, it calls `world.getChunkAtAsync(x, z)` to safely load/generate the chunk and scans it for specific block-based food sources (`HAY_BLOCK`, `MELON`, `SWEET_BERRY_BUSH`, etc.) and `*_LOG` blocks.
6. The analysis is cancellable at any time by running `/randomspawn cancel`.
7. **Pruning & Duplicate Prevention**: The command dynamically manages the `validSpawns` pool. If a previously valid coordinate is re-scanned and found to be no longer valid (e.g. its resources were cleared or harvested), it is automatically removed from the pool. It also prevents duplicates if a coordinate is scanned multiple times.

## Cancellation & Active State
Only one analysis can be active at a time. The algorithm class keeps track of the active state, progress BossBar, and the command sender who initiated it. When cancellation is requested via `/randomspawn cancel`, the remaining task dispatch loop breaks immediately, the BossBar is hidden from the player, and any in-flight region tasks are cleanly discarded upon callback without registering new spawn coordinates.

## Events Integration
When a player claims a chunk (via `TerritoryService`), the `ChunkClaimedEvent` is fired. The `SpawnFinderService` intercepts this and performs an immediate radial sweep. Any chunk inside the `validSpawns` pool that is now within 48 chunks of the new claim is deleted, ensuring spawn integrity over time.
