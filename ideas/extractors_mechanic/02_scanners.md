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
| **IV** | All Tier III + Emerald (all standard overworld ores) |
| **V** | All Tier IV + Quartz, Ancient Debris (Nether-exclusive) |

> ⚠️ **Amethyst is not scannable with any standard scanner tier.** Amethyst geodes operate on a fundamentally different regrowth mechanic that the standard Extractor cannot properly model. See [07_expansion_ideas.md — Specialized Amethyst Extractor](07_expansion_ideas.md) for the dedicated mechanic.

> **Key differentiator:** Tier V is the **only** scanner that can detect Ancient Debris. This makes it the single most valuable prospecting tool in the game, despite its lower durability (48 uses vs Tier IV's 128).

---

## Material Samples (Calibration)

Scanners don't magically know what to look for — you have to "teach" them.

**How it works:**
1. **Sneak + Right-Click** opens the scanner's calibration GUI (via `InventoryGUIService`)
2. Place **Material Samples** into the GUI slots (pure vanilla items: Raw Iron, Diamond, Coal, etc.)
3. When the GUI is closed, the samples are **persisted** to the scanner's PDC (Persistent Data Container) and the physical items are **returned to the player's inventory**.
4. The scanner "remembers" its calibration forever (until recalibrated), so players don't need to carry extra ores around just to scan.
5. When activated, the scanner *only* pings for the materials recorded in its PDC.
6. Higher tiers unlock more sample slots.

**No bloat items required** — standard vanilla items serve as calibration samples, and they are not consumed upon scanning.

---

## The Output: Analysis Result Map

When a scan completes:

1. The scanner consumes **1 durability**
2. It produces an **Analysis Result Map** (a custom `FILLED_MAP` item)
3. The map encodes:
   - The chunk coordinates
   - The scanned material type(s)
   - The Y-range scanned (from player Y down to scan depth)
   - Exact ore block count per material
   - Density rating per material: **None / Poor / Average / Rich / Exceptional**

**Empty Chunk Behavior:** If the scan finds zero target ores, it still consumes 1 durability and produces a map showing "0 Density." Players can technically insert this map into an extractor, but it will swing and miss continuously, wasting fuel. Reading the map before deploying is the player's responsibility.

> **Important clarification:** The map is an **authorization token**, not a resource manifest. The extractor reads it to know *what* to look for and *where* (Y-range), then physically scans the chunk at runtime. The actual ore count on the map is informational only.

### Rendered Heatmap Display

The Analysis Result Map renders a **smooth heatmap image** on the Minecraft map canvas (128×128 pixels) using Bukkit's `MapRenderer` API.

#### Rendering Algorithm

The chunk's 16×16 column grid is upscaled to the 128×128 canvas using **bilinear interpolation** — not hard pixel blocks. Each output pixel samples the four nearest column density values and blends between them, producing smooth gradients instead of sharp 8×8 squares.

```
For each output pixel (px, py) in [0, 128):
  sx = px / 8.0   // fractional source column (0.0–16.0)
  sy = py / 8.0
  x0, y0 = floor(sx), floor(sy)
  x1, y1 = min(x0+1, 15), min(y0+1, 15)
  tx = sx - x0     // horizontal blend factor [0.0, 1.0]
  ty = sy - y0     // vertical blend factor
  value = bilinear(density[x0][y0], density[x1][y0],
                   density[x0][y1], density[x1][y1], tx, ty)
  pixel[px][py] = thermalColor(value)
```

#### Thermal Color Palette

| Density (0.0–1.0) | Rating | Color |
|-------------------|--------|-------|
| 0.00 | None | `#1a1a2e` (near-black) |
| 0.15 | Poor | `#1a237e` (deep blue) |
| 0.40 | Average | `#00bcd4` (cyan) |
| 0.70 | Rich | `#ff9800` (orange) |
| 1.00 | Exceptional | `#ff1744` (bright red) |

Intermediate values are linearly interpolated between these stops, producing a continuous thermal gradient.

#### Map Overlay

Beyond the heatmap, the renderer overlays:
- **Chunk grid lines** — thin gray border around the 16×16 boundary
- **Compass rose** — top-right corner (8×8 px) showing North
- **Coordinate label** — bottom edge: `Chunk [X, Z]`
- **Material icon** — top-left corner, a small color-coded icon for the scanned material
- **Y-range label** — bottom-left: `Y: 16 → -48`

If multiple materials were scanned, the primary material (highest total count) is rendered. A **secondary material indicator** appears in the corner when applicable.

### Item Lore (Precision Data)

For players who want exact numbers rather than a visual read, the item's lore contains all raw data:

```
[Analysis Result Map]
Chunk: [6, -13]  World: world
Scanned: Y=16 → Y=-48

Diamond Ore:     4 blocks  [Rich]
Deepslate Diam:  2 blocks  [Average]

Scanned with: Tier III Scanner
Date: 2026-06-04
```

> 💡 **Dual-layer design:** The heatmap is the *emotional* hook — players can compare two maps at a glance by holding one in each hand. The lore is for the *min-maxer* who wants exact counts before committing fuel and machinery.

---

## Scan Range Feedback

To prevent players from wasting scanner durability on out-of-range scans, a **live action bar warning** is shown whenever a calibrated scanner is held.

### Behavior

- Triggers when the player's **main hand or offhand** contains a calibrated scanner (at least one Material Sample in its GUI slots)
- Updates every **40 ticks (2 seconds)** via a per-player repeating task
- **Stops automatically** when the scanner is put away

### Format

```
📡 Scan: Y=12 → Y=-52  |  Diamond: ✓ in range  |  Emerald: ⚠ needs Y ≤ -16
```

For each calibrated material, the feedback shows whether the player's **current Y-position** places the scan range within the ore-generating zone for that material.

### Material Depth Thresholds

| Material | Ore-generating zone (center of peak density) |
|----------|----------------------------------------------|
| Coal | Any depth — safe to scan at Y ≥ -60 |
| Copper | Y=47 to Y=-16 |
| Iron | Y=15 to Y=-63 |
| Gold | Y=31 to Y=-63 (+ Badlands surface) |
| Redstone | Y=15 to Y=-63 |
| Lapis | Y=0 to Y=-63 |
| Diamond | Y=-16 to Y=-63 |
| Emerald | Y=-16 to Y=-63 (Mountains biome only) |
| Amethyst | Geodes: Y=70 to Y=-63 |

### Color Coding

| Status | Color | Meaning |
|--------|-------|---------|
| ✓ `§a` Green | Scan range fully covers ore-generating zone | Good to scan now |
| ⚠ `§e` Yellow | Partial overlap — some ore-gen layers included | Acceptable but not optimal |
| ✗ `§c` Red | No overlap — this material won't be found here | Descend or re-calibrate |

---

## Crafting Recipes

> See [05_crafting_reference.md](05_crafting_reference.md#b-geological-scanners) for full recipes.
