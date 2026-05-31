# Gameplay Service

The `GameplayService` is a Tier 3 tracking system designed to manage core game rules, player onboarding, entity lifecycles, and mob spawning behavior across the server. It orchestrates custom survival mechanics for Folia.

## Architecture

| Subservice | Responsibility |
|---|---|
| `GameplayService` | Exposes core subservices and manages lifecycle initializations and restarts. |
| `Gameplay_FirstJoinSubservice` | Handles the first join experience. Teleports new players to a valid random chunk (via `SpawnFinderService`), claims it for them (via `TerritoryService`), provides the starter item, and sends welcome messages. If `SpawnFinderService` is empty, it uses a fallback chunk scanning algorithm. |
| `Gameplay_MobSpawningSubservice` | Enforces hostile mob spawning rules. Blocks hostile mob spawns outside of claimed chunks. Also blocks all hostile spawns inside claimed chunks if the owner is in their 15-minute grace period. Calculates and enforces custom mob caps dynamically using an O(1) concurrent tracker map. |
| `Gameplay_EntityFreezeSubservice` | Ensures that entities in unclaimed territory are completely frozen (`setAware(false)`). Automatically freezes entities on chunk loads or natural spawns (e.g. passive spawns) if the chunk is unclaimed. Re-enables AI when a chunk is successfully claimed via `ChunkClaimedEvent`. |
| `Gameplay_TerritoryBorderSubservice` | Automates chunk border creation and manages proximity-activated WorldBoard holograms at exactly +0.12f Y-offset. Uses a high-frequency (2-tick) proximity tracking task for extremely responsive native client-side interpolation of holograms. Supports dynamic cycling subtitles to show prices and taxes smoothly below the board. Listens to `BorderPunchEvent` to trigger the border expansion GUI. |

## Spawning Rules Details
- **Unclaimed Territory**: 
  - Natural/Default Spawns (Passive or Hostile) are strictly forbidden.
  - Chunk Generation Spawns (e.g. initial passive mobs like cows or sheep) are allowed but are immediately frozen.
- **Claimed Territory**:
  - Grace Period: No hostile mobs spawn for the first 15 minutes of a player's first connection.
  - Custom Mob Cap: Hostile mobs are capped per player according to: `min(70, 5 * player_unlocked_chunks)`.
  - Mobs are fully aware and AI is active.

## Events Integration
- Listens to `PlayerDataRegisterEvent` to catch `isFirstJoin == true`.
- Listens to `CreatureSpawnEvent` to intercept and analyze chunk ownership via `TerritoryService`.
- Listens to `ChunkLoadEvent` and `ChunkClaimedEvent` to toggle entity awareness states dynamically.
- Listens to `BorderPunchEvent` (via `BorderListener`) to open `Gameplay_BorderExpansionGUI` for purchasing territory.

## Dynamic Land Pricing System
The `PriceCalculator` implements a Hybrid Dynamic Pricing System that dynamically calculates multiple `ChunkPriceOption`s based on Biomes, Geometric Contiguity, and Time.
- **Multiple Payment Options**: Every chunk provides options to the user.
  - *Universal Cost*: A standard escalating cost based on player progression.
  - *Biome Fast-Track*: A cost mapped specifically to the chunk's biome (e.g. Prismarine for Oceans, Sand for Deserts). Costs 60% less base resources, but requires rare biome-specific materials.
- **Geometric Contiguity System**: The price scales dynamically based on the shape of the player's territory.
  - *Algorithmic Snaking Tax*: Calculates the Isoperimetric Quotient of the player's territory to estimate snaking severity. Squares and blobs incur **0%** tax, while sprawling 1-wide 1xN lines scale up drastically in cost (e.g. 5%, 20%, 75%+).
  - *Infill Discount*: If a player claims a chunk bordered by 3 or 4 of their own chunks, a **-50%** discount is applied to encourage squaring off bases.
- **Rotating Market Discounts**: A global market rotation occurs every 8 real-world hours. 
  - Grants a **-50%** discount to specific biome categories (Aquatic, Arid, Cold, Forest).
  - Actively updates on holographic WorldBoards and the Expansion GUI.
- **Progression Tiers**: The base universal items required scale with total chunk count (e.g. Cobblestone -> Iron -> Gold -> Diamonds), starting perfectly cheap at 1 item for new players.
- **Player Origin Distance Scaling**: The base quantities dynamically scale based on the distance from the player's very first claimed chunk (their personal territory origin), NOT the world spawn!
