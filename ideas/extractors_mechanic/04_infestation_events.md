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

> See [03_extractors.md — Repair System](03_extractors.md) for the full repair cost table and Degraded Core salvage rules.

- **Light damage (50–99% health)** → Repair Kits only (quantity proportional to damage)
- **Heavy damage (1–49% health)** → Repair Kits + the extractor's own crafting materials
- **Destroyed (0% health)** → Full Reconstruction (all tier materials) or salvage as Degraded Core

---

## Offline Infestations

Extractors **can be sieged while their owner is offline or the chunk is unloaded.** This is intentional — it creates genuine stakes and motivates players to invest in automated defense systems (planned for a future implementation phase).

### Behavior While Offline

- Infestation events are **scheduled and tracked server-side** regardless of chunk load state
- When an infestation triggers on an unloaded chunk, the chunk is **temporarily force-loaded** for the duration of the event (3–5 minutes), then released
- Mobs spawn, pathfind, and deal damage normally during this window
- If the extractor's health reaches zero during an offline infestation, it is **destroyed** and its inventory is **dropped on the ground**
- Damage dealt during offline infestations is applied identically to online infestations — no special scaling

### Mitigation

| Mitigation | Effect |
|------------|--------|
| **Defense systems** *(future)* | Automated turrets, traps, and walls fight infestations without the player present |
| **Remote Monitor II** | Sends chat notifications even to offline players; they can log in and rush to defend |
| **Reduce Heat** | Upgrade to higher tiers and avoid Drill Speed modules to lower infestation frequency |
| **Tier III+ health pools** | 400–1,600 HP means most low/moderate infestations cannot destroy the extractor in one event |

> ⚠️ **Design Intent:** The risk of offline destruction is the primary motivation for building automated defenses and for being strategic about *when* and *where* you deploy high-tier extractors. Low-tier coal extractors are nearly immune to offline destruction; high-tier diamond/ancient debris extractors demand active defense planning.

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
