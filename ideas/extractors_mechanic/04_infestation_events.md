# Infestation Events (PvE Base Defense)

Mid-to-high tier Extractors are loud and generate immense heat, triggering **Infestation Events** — waves of hostile mobs that siege the extractor.

---

## Design Philosophy

> **"Difficulty scales directly with profit."**
> 
> Cheap extractors are safe and slow. Expensive extractors are highly profitable but require the player to build intricate, well-lit, heavily fortified bunkers to survive the sieges.

This is **Non-Forced PvP** — the threat comes from PvE, not other players.

---

## How Infestations Work

### 1. The Beacon Effect

- The Extractor attempts to spawn mobs in a **30-block radius** around itself
- Spawn attempts happen at intervals tied to the extractor's **heat level** (material rarity × tier modifier)

### 2. Light Mitigation

Players can actively defend their machines by lighting up the area:

- **If the light level is ≥ 10, mobs cannot spawn there**
- A torch produces light level 14, dropping 1 per block of distance — at threshold 10, a torch only effectively covers ~4 blocks radius
- Within a 30-block extractor radius, this requires **dense torch grids** or creative lighting solutions (lava channels, glowstone ceilings, sea lanterns)
- This makes base design genuinely challenging — haphazard torch placement won't cut it

### 3. The Siege

If valid dark spots exist within the 30-block radius:

- Waves of mobs spawn and **pathfind directly to the Extractor Core**
- Mobs target both the Extractor Core AND nearby players, **prioritizing whichever is closest**
- Players face real combat risk when defending, but mobs won't chase players across the map
- This means players must build physical defenses *and* be prepared to fight

### 4. Rare "Breaker" Mobs

Custom or rare vanilla mobs spawn to break through player defenses:

- **Charged Creeper:** If it reaches within 16 blocks of the Extractor but gets stuck behind a wall or trench, it **self-detonates** to blow open the player's defenses, allowing the rest of the horde inside
- Additional breaker mobs could include:
  - Silverfish that burrow through walls
  - Endermen that teleport past defenses
  - Custom "Drill Worms" that tunnel through solid blocks

---

## Extractor Damage & Repair

- Mobs deal damage to the Extractor Core's health pool
- If the Extractor's health reaches **zero**:
  1. It **shuts down**
  2. It **drops its internal inventory** on the ground
  3. It **cannot be moved (disassembled)** — the GUI shows: *"Moving the extractor in these conditions would completely destroy it"*
  4. It requires a **Repair Kit** plus crafting materials proportional to the damage level to restart

### Repair Cost Scaling

- **Light damage** → Repair Kit + Tier I and/or Tier II extractor parts
- **Heavy damage** → Repair Kit + Tier IV and/or Tier V extractor parts
- Since higher-tier extractors inherit components from all lower tiers, the repair cost scales with both **damage severity** and **extractor tier**
- A Tier V extractor with heavy damage is *extremely* expensive to repair, creating real stakes during infestations

> See [05_crafting_reference.md](05_crafting_reference.md#e-repair-kit) for Repair Kit recipe.

---

## Heat & Noise System

| Factor | Effect on Heat |
|--------|---------------|
| Material rarity (Coal vs Diamond) | Rare materials = more heat |
| Extractor tier | Higher tiers = *less* heat (better technology) |
| Drill Speed Module | Increases heat |
| Time running continuously | Heat may accumulate over time (TBD) |

The interplay between material rarity and tier creates a natural progression:
- **Early game:** Coal extractors are nearly silent. Safe to operate undefended.
- **Mid game:** Iron/Gold extractors start drawing attention. Basic lighting required.
- **Late game:** Diamond extractors at low tiers are incredibly dangerous. Upgrading to Tier III+ is essential for manageable heat levels.
