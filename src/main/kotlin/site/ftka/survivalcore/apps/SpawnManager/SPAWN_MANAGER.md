# Spawn Manager App Documentation

The `SpawnManagerApp` is a Tier 4 administrative application designed to control, scan, and manage safe chunk spawn points within the SurvivalCore framework.

It acts as the primary user-facing and administrator interface for `SpawnFinderService` (Tier 3) and coordinates with `TerritoryService` to guarantee safe and isolated spawning zones.

## Features and Subsystems

### 1. Unified Command (`/spawnmanager` or `/sm`)
- **`/spawnmanager analyse <radius>`**: Scans chunks within a specific radius for safe biomes and resources, and pre-caches valid locations.
- **`/spawnmanager cancel`**: Halts any active scanning and cleans up scheduler tasks and boss bar overlays.
- **`/spawnmanager randomtp`**: Teleports the executing player to a random validated safe chunk spawn from the active cache pool.

### 2. Backward Compatibility
To support seamless migration, the following command aliases are registered and fully functional:
- `/sm`
- `/spawn`
- `/randomspawn`

### 3. Interaction with Services
The application coordinates operations across different tiers:
- **`SpawnFinderService`**: Exposes algorithms via `SpawnFinder_AlgorithmSubservice` to run Folia region-scheduler tasks, load/generate safe chunks, and build the in-memory pool of verified coordinates.
- **`TerritoryService`**: Automatically prunes coordinates from the spawn pool that become invalid due to players claiming chunks (via radial sweep listener).

## Permissions

The app features granular permission check mechanisms:
*   `survivalcore.admin.spawnmanager` (or `survivalcore.spawnmanager`): Grants complete access to administrative operations:
    *   `/spawnmanager analyse <radius>` (chunk scanning/pre-caching).
    *   `/spawnmanager cancel` (terminating scans).
    *   `/spawnmanager randomtp` (teleportation).
    *   Tab completion of all administrative subcommands.
*   `survivalcore.spawnmanager.randomtp`: Grants standard user access to run player-facing commands:
    *   `/spawnmanager randomtp` (teleportation).
    *   Tab completion limited only to `randomtp` and `help`.

