# Design Critique & Open Questions

A thorough analysis of the Extractor Mechanic design — identifying gaps, inconsistencies, missing definitions, and architectural concerns.

> **Legend:**
> - 🔴 **Critical** — Must be resolved before implementation
> - 🟡 **Important** — Needs a clear answer but won't block initial work
> - 🟢 **Minor** — Polish item or edge case
> - ✅ **Resolved** — Addressed by design decisions

---

## 1. Fundamental Design Gaps

### ✅ 1.1 — Chunk Claiming System *(Resolved)*

~~The design says "the player claims it and places an Extractor Core" — but there is no claim system defined.~~

**Resolution:** There is no separate "claim" system. Extractors are simply placed inside a chunk and fed an Analysis Result Map from that chunk's scan. The extractor belongs to the chunk it's placed in. The "chunklock" idea is a separate prototype that won't be implemented. Any amount of extractors can be placed in the same chunk (even by different players), but they will blindly compete and collide for the same blocks. Map sharing exploits are prevented because the Map is consumed upon insertion.

### ✅ 1.2 — Extractor Placement *(Resolved)*

**Resolution:**
- **In inventory:** Custom player head (unique skin per type/tier, like Hypixel Skyblock minions)
- **In world:** Places as a ~3×3×5 controlled structure with particles/armor stands/holograms
- **Interaction:** Right-click the structure to open the GUI
- **Cannot be broken normally:** Blocks re-appear if broken, but breaking them deals virtual damage to the extractor's health
- **Pickup:** Via "Disassembly" button in the GUI (blocked if damaged)
- **Placement requirements:** Enough space, not too close to chunk border

### ✅ 1.3 — Catch-Up Extraction *(Resolved)*

**Resolution:** The extractor uses a cooldown-based cycle. When offline/unloaded:
- The cooldown is **longer** than the "loaded" cooldown (reduced efficiency)
- Items gained are **limited by real block availability** — it physically checks and replaces blocks
- This naturally throttles offline extraction without needing an artificial cap

### ✅ 1.4 — No Extractor Limit Per Player *(Resolved)*

**Resolution:** The default limit is **4 extractors per player**. Higher limits may be unlocked later via a custom level system, but the baseline hard cap is 4.

---

## 2. Scanner Design Issues

### ✅ 2.1 — Scanner Tier V Value Proposition *(Resolved)*

**Resolution:** Each scanner tier restricts which materials can be scanned:

| Tier | Scannable Materials |
|------|-------------------|
| I | Coal, Copper |
| II | + Iron, Gold |
| III | + Diamond, Lapis, Redstone |
| IV | + Emerald, Amethyst (all Overworld) |
| V | + Quartz, Ancient Debris (Nether-exclusive) |

Tier V is the **only scanner capable of detecting Ancient Debris**, making it essential for endgame Nether operations. The fewer uses (48 vs 128) are balanced by targeting the most valuable material in the game.

### ✅ 2.2 — Analysis Result Map: Authorization Token vs. Resource Manifest *(Resolved)*

The map's role has been clarified as an "authorization token" — it tells the extractor what materials and Y-range to target, not a manifest of exact ore counts.

**Resolution:** The Map is **consumed** the moment it is placed inside an extractor and remains stuck until replaced. This prevents map duplication exploits (e.g., giving a map to a friend so both can extract the same chunk for free).

### ✅ 2.3 — What Happens When You Scan a Chunk With No Target Ores? *(Resolved)*

**Resolution:** The scan still completes, consumes 1 durability, and generates a map showing "0 Density." Players can theoretically insert this into an extractor, but it will just swing and miss forever, wasting fuel. It is the player's responsibility to check the density before investing fuel and time.

### 🟡 2.4 — Scanner Depth Relative to Player Y Creates Exploits

Still open: no feedback to the player about whether their scan range intersects with ore-generating terrain. A scan at Y=200 is a total waste but the player won't know until after burning durability.

---

## 3. Extractor Design Issues

### ✅ 3.1 — Fuel Economy Numbers Are Missing *(Resolved)*

**Resolution:** All fuel consumption durations, capacities, and costs are fully defined in [10_balance_numbers.md](10_balance_numbers.md). Coal extractors are explicitly profitable.

### ✅ 3.2 — Extractor Material Types Are Incomplete *(Resolved)*

**Resolution:** All material types are fully supported as distinct extractors. Quartz and Nether Gold are supported with the same patterns. Ancient Debris requires a Tier IV or Tier V extractor to be processed properly, and Amethyst will pull shards from geode clusters. Each material type is its own specific crafted Extractor Core.

### ✅ 3.3 — Compact Blocks Infrastructure *(Resolved)*

**Resolution:** A `CustomItemService` will be built as a prerequisite. See [09_custom_item_service_plan.md](09_custom_item_service_plan.md) for the full implementation plan. Compact Blocks will be registered as non-tracked custom items identified by PDC type tags.

### ✅ 3.4 — Furnace + Compactor Module Dependency *(Resolved)*

**Resolution:** The dependency is correct and logical:
- **Raw ores cannot be compacted** — there are no "Raw Ore Block" items in vanilla Minecraft
- Iron, Copper, and Gold must be **smelted into ingots first**, then compacted into blocks
- Pipeline: Mine Raw Iron → [Furnace] → Iron Ingot → [Compactor] → Iron Block → [Super Compactor] → Compact Iron Block
- For materials that don't need smelting (Diamond, Coal, etc.): Mine → [Compactor] → Block → [Super Compactor] → Compact Block

A new **Super Compactor** module has been added that converts blocks into Compact Blocks (requires Compactor as prerequisite).

### ✅ 3.5 — Extractor Health Pool Is Undefined *(Resolved)*

**Resolution:** Max health by tier and mob damage values are fully defined in [10_balance_numbers.md](10_balance_numbers.md). Extractors range from 100 HP (Tier I) to 1,600 HP (Tier V).

---

## 4. Infestation Event Gaps

### ✅ 4.1 — "Heat" Is Mentioned But Never Formalized *(Resolved)*

**Resolution:** Heat calculations (Base Material + Tier Modifier + Modules) and resulting threat levels are fully defined in [10_balance_numbers.md](10_balance_numbers.md).

### ✅ 4.2 — Light Level Threshold *(Resolved)*

**Resolution:** Mob spawning minimum light level raised to **10**. A torch produces light 14, dropping 1 per block. At threshold 10, torches only cover ~4-block radius effectively. In a 30-block extractor radius, this requires dense torch grids or creative lighting (lava, glowstone, sea lanterns). This makes base defense genuinely challenging.

### ✅ 4.3 — Mob Targeting *(Resolved)*

**Resolution:** Mobs now target both the **Extractor Core AND nearby players**, prioritizing **whichever is closest**. Players face real combat risk when defending, but mobs won't chase players across the map.

### ✅ 4.4 — Infestation Timing Is Undefined *(Resolved)*

**Resolution:** Infestation frequency (from "Every 4 hours" to "Every 15 minutes"), warning times (30s), duration, and wave counts are fully defined in [10_balance_numbers.md](10_balance_numbers.md).

### 🟢 4.5 — Charged Creeper 16-Block Detonation Range

Still needs testing and calibration.

---

## 5. Economy & Balance Concerns

### ✅ 5.1 — No Extraction Rate Numbers *(Resolved)*

**Resolution:** Cooldowns by tier (Loaded vs Offline), module impacts, ore density averages, and depletion time estimates are fully modeled in [10_balance_numbers.md](10_balance_numbers.md).

### ✅ 5.2 — Compact Block Economy Could Spiral *(Resolved)*

**Resolution:** Break-even analysis and fleet economics are modeled in [10_balance_numbers.md](10_balance_numbers.md). A Tier III Diamond extractor takes ~108 chunks / 36 hours of active loaded extraction to break even.

### ✅ 5.3 — No Anti-Duplication Safeguards *(Resolved)*

**Resolution:** 
1. The [Custom Item Service plan](09_custom_item_service_plan.md) addresses item duplication.
2. The Analysis Map itself is **consumed upon insertion** into the extractor, entirely preventing players from sharing maps to double-mine a chunk.

### ✅ 5.4 — Repair Kit Scaling *(Resolved)*

**Resolution:** Repair now requires a Repair Kit PLUS crafting materials proportional to damage:
- Light damage → Tier I/II parts
- Heavy damage → Tier III+ parts
- Destroyed → Parts spanning all tiers up to the extractor's tier
- Broken extractors **cannot be disassembled** until fully repaired

---

## 6. Technical Architecture Concerns

### ✅ 6.1 — Custom Item Framework *(Resolved)*

**Resolution:** A `CustomItemService` will be built as a prerequisite. See [09_custom_item_service_plan.md](09_custom_item_service_plan.md) for the full implementation plan including anti-duplication, custom crafting, and PDC-based identification.

> ⚠️ **Constraint remains:** Vanilla blocks don't have `PersistentDataContainer`. Since the extractor is a multi-block structure, tracking uses location-based maps in memory/Redis. The armor stands / holograms used for decoration CAN carry PDC tags.

### ✅ 6.2 — Subservices *(Resolved)*

**Resolution:** The architecture now defines 8 subservices:
`IOSubservice`, `ScanningSubservice`, `MiningSubservice`, `FuelSubservice`, `InfestationSubservice`, `ClaimSubservice`, `CatchupSubservice`, `HealthSubservice`

### 🟡 6.3 — Event Definitions Are Missing

The design should define which `ProprietaryEvents` it will fire. Suggested list:

- `ExtractorPlacedEvent`, `ExtractorRemovedEvent`, `ExtractorDepletedEvent`
- `ExtractorDamagedEvent`, `ExtractorDestroyedEvent`
- `InfestationStartEvent`, `InfestationEndEvent`
- `ChunkScannedEvent`, `ExtractorFuelDepletedEvent`

### 🟢 6.4 — Redis Key Namespacing

Consider prefixing: `survivalcore:extractor:{uuid}` instead of `extractor:{uuid}`.

---

## 7. Player Experience Gaps

### ✅ 7.1 — Notification System *(Resolved)*

**Resolution:** A **Remote Monitor Module** (Tier I & II) has been designed:
- **Tier I:** Chat notifications for critical events (fuel depleted, under siege, damaged, destroyed, depleted)
- **Tier II:** + Periodic status reports + early infestation warnings

This is a module, not a baseline feature — players must invest in monitoring their remote extractors.

### 🟡 7.2 — No Extractor Visual Feedback

Still open: particles, sounds, animations, hologram status text while the extractor is running. The 3×3×5 structure is defined but its visual/audio behavior during operation is not.

### 🟢 7.3 — The Scanner "Teaching" UX Is Unclear

Still needs onboarding design. Clear item lore and GUI labels would go a long way.

---

## 8. Summary of Required Decisions

### Resolved ✅

| # | Decision | Resolution |
|---|----------|------------|
| 1 | Chunk claiming | No separate claim — extractor placed in chunk + analysis map |
| 2 | Physical form | Custom player head → 3×3×5 structure with armor stands/holograms |
| 3 | Catch-up limits | Full algorithm in [06_technical_architecture.md](06_technical_architecture.md): fuel and storage accounted for, 50-block/tick batch cap, 24h max |
| 4 | Extraction rate numbers | Modeled in [10_balance_numbers.md](10_balance_numbers.md) |
| 5 | Fuel consumption numbers | Modeled in [10_balance_numbers.md](10_balance_numbers.md) |
| 6 | Max extractors | Default limit of **4** (config default corrected to 4) |
| 7 | Custom Item system | Implementation plan drafted → [09_custom_item_service_plan.md](09_custom_item_service_plan.md) |
| 8 | Furnace/Compactor | Dependency is correct — raw ores need smelting before compaction |
| 9 | Heat/noise formalization | Modeled in [10_balance_numbers.md](10_balance_numbers.md) |
| 10 | Light level | Raised to 10 |
| 11 | Infestation timing | Modeled in [10_balance_numbers.md](10_balance_numbers.md) |
| 12 | Scanner Tier V | Material restrictions by tier — Tier V is only Ancient Debris scanner |
| 13 | Missing materials | Extractor types for Quartz, Debris, Amethyst, and Nether Gold are confirmed |
| 14 | Notification system | Remote Monitor Module (Tier I & II) |
| 15 | Repair cost model | Revised: 50–99% = Repair Kits only; 1–49% = Kits + extractor materials; 0% = Full Reconstruction or Degraded Core salvage |
| 16 | Days-to-ROI model | Modeled in [10_balance_numbers.md](10_balance_numbers.md) |
| 17 | Extractor health per tier | Modeled in [10_balance_numbers.md](10_balance_numbers.md) |
| 18 | Scan-empty-chunk | Map consumes durability, shows 0 Density. Extractor will fail cycle if used. |
| 19 | GUI access control | Owner-only GUI access. Any player may break structure blocks (dealing damage). |
| 20 | Structure protection | Full protection table in [03_extractors.md](03_extractors.md): pistons, explosions, fluids, block placement all handled |
| 21 | Block locking (race conditions) | Detection + replacement must be atomic in the same tick; `ConcurrentHashMap<BlockKey, UUID>` lock; see [03_extractors.md](03_extractors.md) |
| 22 | Offline infestations | Yes — chunk force-loaded for each event. Damage applied normally. Defense systems planned for future. |
| 23 | Admin tools | `staff.extractors` permission; GUI warning banner on foreign extractors; `/extractor admin` subcommands; all actions logged |
| 24 | Analysis Result Map display | Smooth bilinear heatmap (MapRenderer API) + precision lore text. Spec in [02_scanners.md](02_scanners.md) |
| 25 | Player onboarding | Advancement tree + Prospector's Handbook + recipe progressive unlocking. Full spec in [11_player_experience.md](11_player_experience.md) |
| 26 | Visual/audio feedback | Full particle + sound + hologram spec tied to heat level. In [03_extractors.md](03_extractors.md) |
| 27 | Amethyst extraction | Moved out of standard mechanic into its own future Specialized Amethyst Extractor design (harvests regrowth, infinite). See [07_expansion_ideas.md](07_expansion_ideas.md) |
| 28 | Nether block replacement | Biome-native filler (Netherrack / Blackstone / Soul Sand). Thematic asymmetry: Overworld = landmarks, Nether = ghost runs. In [03_extractors.md](03_extractors.md) |
| 29 | Scanner Y-range feedback | Live action bar warning with per-material depth thresholds and color coding. In [02_scanners.md](02_scanners.md) |
| 30 | Heat value in GUI | Colored glass pane matching threat level + hover lore with math breakdown + thematic threat descriptor. In [03_extractors.md](03_extractors.md) |
| 31 | Repair Kit quantity numbers | Preview exact crafting items needed + kits, capped by recipe limits. Math formula specified in [03_extractors.md](03_extractors.md) |
| 32 | Module slot count | Scales by tier (2 / 3 / 3 / 4 / 5). Forced trade-offs early, everything late. Spec in [03_extractors.md](03_extractors.md) |
| 33 | Module fate on disassembly | Modules AND Extractor Core item drop to ground with animation for better physical feel and safety. Spec in [03_extractors.md](03_extractors.md) |
| 34 | Scanner calibration persistence | Yes, samples are persistent in PDC and items are returned to inventory when GUI closes. Spec in [02_scanners.md](02_scanners.md) |
| 35 | Fortune module behavior | Probabilistic, mirroring vanilla Fortune (50% for 1 extra, 25% for 2 extra). Spec in [03_extractors.md](03_extractors.md) |
| 36 | Structure block visual design | Extractor's own base/compact block forms the core spine. Shell materials upgrade by tier (stone to crying obsidian). Spec in [03_extractors.md](03_extractors.md) |
| — | Multi-extractor per chunk | Yes, but block locking prevents race conditions. Map is consumed on insertion. |
| — | Map anti-duplication | Map is consumed upon insertion, preventing duplication. |

### Still Open

*No open design gaps currently identified.*
