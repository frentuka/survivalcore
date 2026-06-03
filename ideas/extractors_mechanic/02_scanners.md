# Geological Scanners

Geological Scanners are the entry point to the Extractor Mechanic. They allow players to prospect chunks for ore density before committing expensive machinery.

---

## Crafting & Durability (The Empty Map Economy)

The number of uses a scanner has is directly tied to its crafting recipe. Crafting a scanner requires **Empty Maps**. Higher tiers provide more uses but demand far more Empty Maps, creating a massive paper/compass sink.

---

## Tier Specifications

| Tier | Uses | Scan Time | Scan Depth | Material Slots | Special |
|------|------|-----------|------------|----------------|---------|
| **I** | 5 | ~30s | 16 blocks below player | 1 | — |
| **II** | 10 | ~20s | 32 blocks below player | 2 | — |
| **III** | 25 | ~10s | 64 blocks below player | 3 | — |
| **IV** | 128 | ~3s | Entire vertical chunk | 5 | Endgame. Scans regardless of Y position |
| **V** | 48 | ~3s | Entire vertical chunk | 5 | Nether-only. The ONLY scanner capable of detecting Ancient Debris |

> **Depth is relative**: A Tier I Scanner scans 16 blocks *downward from the player*. To scan deepslate ores, you must physically dig down into a cave first!

---

## Material Restrictions by Tier

Scanner tiers restrict which materials can be used as **Material Samples**. Each tier inherits all materials from lower tiers — low-tier scanners **cannot** scan for expensive ores.

| Tier | Scannable Materials |
|------|--------------------|
| **I** | Coal, Copper |
| **II** | All Tier I + Iron, Gold |
| **III** | All Tier II + Diamond, Lapis, Redstone |
| **IV** | All Tier III + Emerald, Amethyst (everything overworld) |
| **V** | All Tier IV + Quartz, Ancient Debris (Nether-exclusive) |

> **Key differentiator:** Tier V is the **only** scanner that can detect Ancient Debris. This makes it the single most valuable prospecting tool in the game, despite its lower durability (48 uses vs Tier IV's 128).

---

## Material Samples (Calibration)

Scanners don't magically know what to look for — you have to "teach" them.

**How it works:**
1. **Sneak + Right-Click** opens the scanner's calibration GUI (via `InventoryGUIService`)
2. Place **Material Samples** into the GUI slots (pure vanilla items: Raw Iron, Diamond, Coal, etc.)
3. When activated, the scanner *only* pings for the materials slotted in its GUI
4. Higher tiers unlock more sample slots

**No bloat items required** — standard vanilla items serve as calibration samples.

---

## The Output: Analysis Result Map

When a scan completes:

1. The scanner consumes **1 durability**
2. It produces an **Analysis Result Map** (a custom map item)
3. The map contains:
   - The chunk coordinates
   - The scanned material types
   - The Y-range that was scanned (from player Y to player Y minus scan depth)
   - Ore density data for each material (e.g., "Poor", "Average", "Rich")

**Empty Chunk Behavior:** If the scan finds zero target ores in the specified range, it **still consumes 1 durability** and produces a map with "0 Density". The player can technically feed this map to an extractor, but the extractor will just swing and miss continuously, wasting fuel. It is the player's responsibility to read the map and determine if the chunk is worth extracting.

> **Important clarification:** The extractor reads the map to understand "this chunk was analyzed with these materials, from layer Y=n to Y=n-m, so I can extract them." The extractor doesn't depend on the map data for actual extraction — it physically scans the chunk at runtime. The map serves as an **authorization token**, not a resource manifest.

---

## Crafting Recipes

> See [05_crafting_reference.md](05_crafting_reference.md#b-geological-scanners) for full recipes.
