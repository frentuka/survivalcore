# Balance & Numbers — Prototype

> **Status:** Prototype — all values are initial estimates subject to playtesting and adjustment  
> **Last Updated:** 2026-06-02

This document contains all numeric values for the Extractor Mechanic. It is the **single source of truth** for balance tuning.

---

## 1. Extraction Cooldowns

The extraction cycle is: **Cooldown → Attempt → Success/Fail → Cooldown**.

| Tier | Loaded Cooldown | Offline Cooldown (3×) |
|------|----------------|----------------------|
| **I** | 600s (10 min) | 1,800s (30 min) |
| **II** | 420s (7 min) | 1,260s (21 min) |
| **III** | 300s (5 min) | 900s (15 min) |
| **IV** | 180s (3 min) | 540s (9 min) |
| **V** | 90s (1.5 min) | 270s (4.5 min) |

> **Offline multiplier:** 3× — when the chunk is unloaded, the cooldown is three times longer. This means offline extraction runs at ~33% of loaded speed.

### With Drill Speed Modules

| Module | Cooldown Reduction | Fuel Penalty |
|--------|-------------------|-------------|
| Drill Speed I | -20% cooldown | +20% fuel consumption |
| Drill Speed II | -35% cooldown | +35% fuel consumption |

**Example:** Tier III (300s) + Drill Speed II = 195s cooldown, but fuel burns 35% faster.

---

## 2. Fuel Economy

Fuel: **Compact Coal Blocks** (1 Compact Coal Block = 9 Coal Blocks = 81 Coal)

### Fuel Duration (hours per 1 Compact Coal Block)

| Tier | Base Duration | With Drill Speed I | With Drill Speed II | With Furnace Module |
|------|--------------|-------------------|--------------------|--------------------|
| **I** | 48h | 40h | 35.6h | 38.4h |
| **II** | 24h | 20h | 17.8h | 19.2h |
| **III** | 12h | 10h | 8.9h | 9.6h |
| **IV** | 6h | 5h | 4.4h | 4.8h |
| **V** | 3h | 2.5h | 2.2h | 2.4h |

### Fuel Capacity (Compact Coal Blocks the fuel slot can hold)

| Tier | Fuel Slot Capacity | Max Continuous Runtime (base) |
|------|-------------------|------------------------------|
| **I** | 4 | 8 days |
| **II** | 8 | 8 days |
| **III** | 16 | 8 days |
| **IV** | 32 | 8 days |
| **V** | 64 | 8 days |

> Fuel capacity scales so that all tiers can hold ~8 days of fuel at base consumption. This reduces how often players need to refuel.

### Daily Fuel Consumption (24h continuous loaded operation)

| Tier | Base | With Drill Speed II | With Furnace |
|------|------|--------------------| -------------|
| **I** | 0.5 CCB/day | 0.67 CCB/day | 0.63 CCB/day |
| **II** | 1 CCB/day | 1.35 CCB/day | 1.25 CCB/day |
| **III** | 2 CCB/day | 2.70 CCB/day | 2.50 CCB/day |
| **IV** | 4 CCB/day | 5.45 CCB/day | 5.00 CCB/day |
| **V** | 8 CCB/day | 10.91 CCB/day | 10.00 CCB/day |

*(CCB = Compact Coal Blocks)*

---

## 3. Ore Density Reference

Approximate ore block counts per chunk in vanilla Minecraft, by scanner scan depth.

### Overworld Ores

| Material | Tier I (16 layers) | Tier II (32 layers) | Tier III (64 layers) | Tier IV (full chunk) |
|----------|-------------------|--------------------|--------------------|---------------------|
| **Coal** | 15–25 | 30–50 | 60–100 | 200–300 |
| **Copper** | 5–12 | 10–20 | 15–30 | 20–40 |
| **Iron** | 8–15 | 20–35 | 40–75 | 75–120 |
| **Gold** | 1–3 | 2–5 | 4–8 | 8–15 |
| **Redstone** | 2–5 | 5–10 | 10–20 | 15–30 |
| **Lapis** | 1–3 | 2–5 | 3–8 | 5–12 |
| **Diamond** | — | — | 3–5 | 4–8 |
| **Emerald** | — | — | 1–3 | 2–5 |
| **Amethyst** | — | — | 0–10 *(geode)* | 0–30 *(geode)* |

### Nether Ores (Tier V Scanner only)

| Material | Blocks/Chunk (full) |
|----------|-------------------|
| **Quartz** | 80–150 |
| **Nether Gold** | 15–30 |
| **Ancient Debris** | 1–4 |

> **Note:** These are estimates based on vanilla Minecraft world generation. Actual values vary per seed and chunk. The scanner reveals exact counts for the scanned range.

### What Counts as a "Rich" Chunk?

| Richness | Rule of Thumb |
|----------|--------------|
| **Poor** | Below average density for the material |
| **Average** | Middle of the ranges above |
| **Rich** | Top 20% — at or above the high end of the range |
| **Exceptional** | 1.5× the high end or more (rare ore clusters, lucky generation) |

The scanner reveals exact numbers, so players can judge whether a chunk is worth deploying an extractor to.

---

## 4. Depletion Time Estimates

How long until a chunk runs out of a given material, assuming **average ore density** and **continuous loaded operation**.

### Without Modules (Base Cooldown)

| Material | Avg Ores (Tier III scan) | Tier I | Tier II | Tier III | Tier IV |
|----------|--------------------------|--------|---------|----------|---------|
| **Coal** | 80 | 13.3h | 9.3h | 6.7h | 4.0h |
| **Iron** | 55 | 9.2h | 6.4h | 4.6h | 2.8h |
| **Gold** | 6 | 1.0h | 42m | 30m | 18m |
| **Diamond** | 4 | 40m | 28m | 20m | 12m |
| **Redstone** | 15 | 2.5h | 1.75h | 1.25h | 45m |
| **Lapis** | 5 | 50m | 35m | 25m | 15m |
| **Emerald** | 2 | 20m | 14m | 10m | 6m |

### With Drill Speed II (-35% cooldown)

| Material | Avg Ores (Tier III scan) | Tier I | Tier III | Tier IV |
|----------|--------------------------|--------|----------|---------|
| **Coal** | 80 | 8.7h | 4.3h | 2.6h |
| **Iron** | 55 | 5.9h | 3.0h | 1.8h |
| **Diamond** | 4 | 26m | 13m | 7.8m |

> **Key takeaway:** Common ores (Coal, Iron) sustain extractors for hours. Rare ores (Diamond, Emerald) deplete in minutes, driving constant exploration. This is by design — **rare materials demand active play**.

---

## 5. Module Effects Summary

| Module | Effect | Fuel Impact | Prerequisite |
|--------|--------|-------------|-------------|
| **Drill Speed I** | -20% cooldown | +20% fuel consumption | — |
| **Drill Speed II** | -35% cooldown | +35% fuel consumption | — |
| **Fortune I** | 1.5× average drops | — | — |
| **Fortune II** | 2.0× average drops | — | — |
| **Furnace** | Smelts raw ores (Iron, Copper, Gold) | +25% fuel consumption | — |
| **Compactor** | Auto-compacts items into Blocks (9 items → 1 Block) | — | Furnace *(if extractor type supports smelting)* |
| **Super Compactor** | Auto-compacts Blocks into Compact Blocks (9 Blocks → 1 Compact Block) | — | Compactor |
| **Storage I** | +9 storage slots | — | — |
| **Storage II** | +18 storage slots | — | — |
| **Remote Monitor I** | Critical event notifications (chat) | — | — |
| **Remote Monitor II** | + Periodic status reports + early warnings | — | — |

---

## 6. Heat & Infestation System

### Heat Calculation

```
Heat = BaseMaterialHeat × TierModifier + ModuleHeat
```

#### Base Material Heat

| Material | Base Heat (at Tier I) |
|----------|----------------------|
| Coal | 5 |
| Copper | 8 |
| Iron | 15 |
| Redstone | 20 |
| Lapis | 18 |
| Gold | 25 |
| Emerald | 45 |
| Diamond | 50 |
| Quartz | 30 |
| Ancient Debris | 80 |

#### Tier Modifier (higher tier = cooler technology)

| Tier | Heat Multiplier |
|------|----------------|
| I | ×1.0 |
| II | ×0.8 |
| III | ×0.6 |
| IV | ×0.45 |
| V | ×0.35 |

#### Module Heat Additions

| Module | Heat Added |
|--------|-----------|
| Drill Speed I | +10 |
| Drill Speed II | +20 |
| All other modules | +0 |

### Heat Examples

| Setup | Calculation | Heat | Threat Level |
|-------|-----------|------|-------------|
| Tier I Coal | 5 × 1.0 = 5 | 5 | 🟢 Safe |
| Tier I Iron | 15 × 1.0 = 15 | 15 | 🟡 Low |
| Tier I Diamond | 50 × 1.0 = 50 | 50 | 🔴 Dangerous |
| Tier III Diamond | 50 × 0.6 = 30 | 30 | 🟠 Moderate |
| Tier III Diamond + Drill II | 50 × 0.6 + 20 = 50 | 50 | 🔴 Dangerous |
| Tier I Diamond + Drill II | 50 × 1.0 + 20 = 70 | 70 | 🔴🔴 Extreme |
| Tier IV Diamond | 50 × 0.45 = 22.5 | 23 | 🟡 Low |
| Tier I Ancient Debris | 80 × 1.0 = 80 | 80 | 🔴🔴🔴 Suicidal |
| Tier V Ancient Debris | 80 × 0.35 = 28 | 28 | 🟠 Moderate |

### Infestation Frequency by Heat Level

| Heat Range | Threat Level | Infestation Frequency | Mob Composition |
|------------|-------------|----------------------|-----------------|
| **0–10** | 🟢 Safe | None | — |
| **11–25** | 🟡 Low | Every ~4 hours | Zombies, Skeletons (3–5 per wave) |
| **26–40** | 🟠 Moderate | Every ~2 hours | + Spiders, Creepers (5–8 per wave) |
| **41–60** | 🔴 Dangerous | Every ~1 hour | + Endermen, Charged Creepers (8–12 per wave) |
| **61–80** | 🔴🔴 Extreme | Every ~30 minutes | + Breaker mobs, Iron Golems (10–15 per wave) |
| **81–100** | 🔴🔴🔴 Suicidal | Every ~15 minutes | Maximum mob variety and count (12–20 per wave) |

### Infestation Event Structure

| Parameter | Value |
|-----------|-------|
| **Warning time** | 30 seconds (rumbling sound, dark particles around extractor) |
| **Event duration** | 3–5 minutes |
| **Waves per event** | 2–3 |
| **Time between waves** | 45–60 seconds |
| **Mob spawn radius** | 30 blocks from extractor |
| **Minimum light level to block spawns** | 10 |

---

## 7. Health & Damage

### Extractor Max Health by Tier

| Tier | Max Health | Description |
|------|-----------|-------------|
| **I** | 100 | Fragile — a single Charged Creeper is devastating |
| **II** | 200 | Moderate |
| **III** | 400 | Sturdy |
| **IV** | 800 | Very durable |
| **V** | 1,600 | Built to survive the Nether |

### Mob Damage to Extractors

| Mob | Damage per Hit | Notes |
|-----|---------------|-------|
| Zombie | 5 | Melee, slow |
| Skeleton | 3 | Ranged |
| Spider | 4 | Fast, can climb |
| Creeper | 30 | Explosion (single hit) |
| Charged Creeper | 100 | Devastating AoE |
| Enderman | 8 | Teleports past walls |
| Breaker Mob (custom) | 15 | Tunnels/breaks defenses |

### Repair Cost Tiers

| Health Remaining | Damage Level | Materials Required |
|-----------------|-------------|-------------------|
| 50–99% | Light | Repair Kits only (quantity scales linearly with damage taken) |
| 1–49% | Heavy | Repair Kits + the extractor's own crafting materials (quantity proportional to damage) |
| 0% (Destroyed) | Total | Full Reconstruction: all tier materials from Tier I to the extractor's own tier, plus many Repair Kits — OR manually destroy wreckage to salvage a Degraded Core (restores to Tier I state) |

> Reminder: A damaged extractor **cannot be disassembled** until fully repaired. While damaged, extraction speed is reduced proportionally to missing health.

---

## 8. Storage Capacity

| Tier | Base Slots | + Storage I | + Storage II | Max Possible |
|------|-----------|------------|-------------|-------------|
| **I** | 9 (1 row) | 18 | 36 | 36 |
| **II** | 18 (2 rows) | 27 | 45 | 45 |
| **III** | 27 (3 rows) | 36 | 54 | 54 |
| **IV** | 36 (4 rows) | 45 | 54 | 54 |
| **V** | 45 (5 rows) | 54 | 54 | 54 |

> **Maximum capacity:** 54 slots (full double-chest equivalent). When storage is full, the extractor **pauses extraction** until items are removed.

---

## 9. Economy Modeling

### Coal Extractor Profitability (The Fuel Engine)

The core question: **Do Coal Extractors produce more Compact Coal Blocks than they consume?**

#### Tier III Coal Extractor — Full Module Stack
*Setup: Tier III Scanner (64 layers), Fortune II, Compactor, Super Compactor*

| Metric | Value |
|--------|-------|
| Coal ore in scan range | ~80 (average) |
| Fortune II (2× drops) | 160 coal |
| Compactor: 160 ÷ 9 | 17.7 Coal Blocks |
| Super Compactor: 17.7 ÷ 9 | **1.97 Compact Coal Blocks produced** |
| Depletion time | ~6.7 hours |
| Fuel consumed (12h/CCB base) | **0.56 Compact Coal Blocks** |
| **Net profit per chunk** | **+1.41 Compact Coal Blocks** |

#### Tier IV Coal Extractor — Maximum Throughput
*Setup: Tier IV Scanner (full chunk), Fortune II, Compactor, Super Compactor*

| Metric | Value |
|--------|-------|
| Coal ore in full chunk | ~250 (average) |
| Fortune II (2× drops) | 500 coal |
| Compactor + Super Compactor | **6.17 Compact Coal Blocks produced** |
| Depletion time | ~12.5 hours |
| Fuel consumed (6h/CCB base) | **2.08 Compact Coal Blocks** |
| **Net profit per chunk** | **+4.09 Compact Coal Blocks** |

> ✅ **Coal Extractors are profitable at all tiers with Fortune + Compactor modules.** Tier III+ are the real fuel engines.

### Diamond Extractor ROI Analysis

#### Tier III Diamond Extractor — Crafting Cost

| Component | Diamond Cost |
|-----------|-------------|
| Tier I Extractor | 3 Compact Diamond Blocks = 243 diamonds |
| Tier II Extractor | +3 Compact Diamond Blocks = 243 diamonds |
| Tier III Extractor | +2 Compact Diamond Blocks = 162 diamonds |
| **Total** | **~648 diamonds (8 Compact Diamond Blocks)** |

*(Plus Nether Stars, Iron Blocks, Obsidian, etc.)*

#### Break-Even Analysis

*Setup: Tier III Scanner (64 layers), Fortune I (1.5× drops)*

| Metric | Value |
|--------|-------|
| Diamonds per chunk (average) | 4 |
| With Fortune I | 6 diamonds/chunk |
| Chunks to break even | 648 ÷ 6 = **108 chunks** |
| Time per chunk (5 min cooldown × 4 blocks) | ~20 minutes |
| Total extraction time | ~36 hours of active deployment |
| Fuel consumed (108 chunks × 20min × 2 CCB/day) | ~3 Compact Coal Blocks total |

**At 3 chunk deployments per play session (2 hours):**
- ~36 play sessions to break even
- **~2.5 weeks of daily play to ROI** *(assuming daily 2-hour sessions)*

After break-even, every chunk is pure profit. With 4 extractors running simultaneously and constant prospecting, diamond income compounds over time.

### Fleet Economics — Steady State Example

*A mid-game player running the maximum 4 extractors:*

| Extractor | Purpose | Chunks/Day | Net Output/Day |
|-----------|---------|-----------|----------------|
| Coal III | Fuel production | 3–4 | +4.2–5.6 CCB |
| Coal III | Fuel production | 3–4 | +4.2–5.6 CCB |
| Iron III | Resource income | 5–6 | ~275–330 iron (+ smelted/compacted) |
| Diamond III | High-value income | ~15–20 | ~60–80 diamonds |

**Daily fuel balance:**
- Produced: ~8.4–11.2 CCB from 2 Coal Extractors
- Consumed: ~2 CCB (Coal) + ~2 CCB (Iron) + ~2 CCB (Diamond) = ~6 CCB
- **Surplus: +2.4–5.2 CCB/day** ✅ Fuel-positive economy

> The player must actively prospect and deploy to ~25–35 different chunks per day across all extractors. This is the **core gameplay loop** — exploration drives the economy.

---

## 10. Player Extractor Limit

| Setting | Value |
|---------|-------|
| **Default maximum extractors per player** | 4 |
| **Expandable?** | Yes — future custom level system or progression mechanic (TBD) |

---

## 11. Tuning Knobs Reference

All values that server operators (or future playtesting) can adjust:

| Category | Parameter | Default | Location |
|----------|-----------|---------|----------|
| Cooldowns | Base cooldown per tier | See §1 | `ExtractorConfig` |
| Cooldowns | Offline multiplier | 3× | `ExtractorConfig` |
| Fuel | Duration per CCB per tier | See §2 | `ExtractorConfig` |
| Fuel | Drill Speed fuel penalty | 20% / 35% | `ExtractorConfig` |
| Fuel | Furnace fuel penalty | 25% | `ExtractorConfig` |
| Modules | Fortune multiplier | 1.5× / 2.0× | `ExtractorConfig` |
| Modules | Drill Speed cooldown reduction | 20% / 35% | `ExtractorConfig` |
| Heat | Base material heat values | See §6 | `ExtractorConfig` |
| Heat | Tier modifiers | See §6 | `ExtractorConfig` |
| Infestation | Frequency per heat range | See §6 | `ExtractorConfig` |
| Infestation | Mob counts per wave | See §6 | `ExtractorConfig` |
| Infestation | Warning time | 30s | `ExtractorConfig` |
| Infestation | Minimum light level | 10 | `ExtractorConfig` |
| Health | Max health per tier | See §7 | `ExtractorConfig` |
| Health | Mob damage values | See §7 | `ExtractorConfig` |
| Storage | Base slots per tier | See §8 | `ExtractorConfig` |
| Limits | Max extractors per player | 4 | `ExtractorConfig` |
