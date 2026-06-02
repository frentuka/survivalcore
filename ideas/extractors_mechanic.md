# Open-World Chunk Extractors Mechanic

## Concept Overview
The Extractor Mechanic is a dynamic, open-world alternative to Skyblock minions. Instead of magical generators creating items out of thin air, **Extractors physically mine the existing world**. 

Players explore the infinite survival world, "prospect" chunks to find rich ore veins, deploy heavy machinery, and defend it while it literally hollows out the chunk block-by-block over time. This creates a loop of exploration, base defense, and passive income that perfectly complements an open-world PvE environment.

---

## 1. The Gameplay Loop

### Step 1: Prospecting (Exploration)
To find rich chunks, players must craft and use a **Geological Scanner**. Scanning is an investment: scanners have limited durability, break when depleted, and must be configured before use.

**1. Crafting & Durability (The Empty Map Economy)**
The number of uses a scanner has is directly tied to its crafting recipe. Crafting a scanner requires **Empty Maps**. Higher tiers provide more uses but demand far more Empty Maps, creating a massive paper/compass sink.

**2. Depth Range & Scan Speeds**
A scanner's range is relative to the player's Y-level, not absolute world coordinates.
* **Tier I:** Takes ~30 seconds to scan. Scans 16 blocks downwards from the player. If you want to scan deepslate ores, you must physically dig down into a cave first!
* **Tier II:** Takes ~20 seconds to scan. Scans 32 blocks downwards.
* **Tier III:** Takes ~10 seconds to scan. Scans 64 blocks downwards.
* **Tier IV:** Takes ~3 seconds to scan. Endgame item. Scans the entire vertical chunk instantly regardless of where you stand.

**3. Material Samples (Calibration)**
Scanners don't magically know what to look for; you have to "teach" them. By sneak-right-clicking, a GUI opens (via `InventoryGUIService`).
* Inside the GUI, you place **Material Samples** (pure vanilla items like a Raw Iron, a Diamond, or a piece of Coal). No bloat items required!
* When activated, the scanner *only* pings for the materials slotted in its GUI. Higher tiers unlock more sample slots. A Tier I Scanner can only hold 1 sample type at a time. A Tier IV can hold 5 samples!

**4. The Output: Analysis Result Map**
When a scan finishes, the Scanner consumes 1 durability and drops an **Analysis Result Map** (a custom map item). This map contains the exact density data of the chunk for the scanned materials.

### Step 2: Deployment & Fuel Economy
Once a rich chunk is found, the player claims it and places an **Extractor Core** (a custom interactive block/hologram). 
* **Map Dependency:** An Extractor is useless on its own. To make it start operating, the player must insert the **Analysis Result Map** (generated in Step 1) into the Extractor's GUI. The Extractor reads the map to know what it is allowed to mine! (In reality, the extractor only reads "this chunk was analyzed with these materials, from layer Y=n to Y=n-m, so I can extract them", the extractor doesn't exactly rely on the map to extract the resources)
* **Fuel Economy:** Extractors are strictly fueled by custom Coal blocks. Players will likely need to build **Coal Extractors** specifically to generate **Compact Coal Blocks** to keep their higher-tier operations (like Diamond Extractors) running. This creates an interconnected resource economy!
* It features a GUI (utilizing your `InventoryGUIService`) for the Analysis Map, fuel, storage, and upgrades.

### Step 3: Physical Extraction
While fueled and active, the Extractor runs on a Folia Region Scheduler timer.
* If the chunk is loaded, every X seconds it randomly selects a target block (e.g., Iron Ore) within the chunk. 
* It **physically breaks the block** and deposits the item into its inventory.
* To simulate structural collapse, the extracted block is **replaced with Cobblestone** (if it was a standard stone-level ore) or **Cobbled Deepslate** (if it was a deepslate-level ore).
* Over days or weeks, players will find massive veins of unnatural cobblestone cutting through the earth where rich ore veins used to be, leaving a permanent visual scar of industrial depletion!
* If the extractor's chunk has not been loaded, upon loading, the extractor will simulate that it has been working by advancing with the extraction.

### Step 4: Depletion and Relocation
Eventually, the chunk runs dry. The Extractor GUI shows "Resources depleted". The player must pack up their machinery, refuel their scanner, and venture deeper into the open world to find a new claim. This solves the "endgame stagnation" problem by forcing players to continually explore.

---

## 2. Mechanic Depth & Features

### Extractor Types, Tiers & Crafting (The Progression Grind)
Extractors are **material-specific** (e.g., Diamond Extractor, Coal Extractor) and each type can be upgraded through distinct Tiers.
* **Tier I-III:** Overworld extractors. Upgrade linearly and require massive amounts of Compact Blocks.
* **Tier IV:** Overworld endgame extractor. Can mine above its y-level (~10 blocks) and is capable of very slowly mining netherite from the overworld.
* **Tier V:** Nether-exclusive extractor. Required for harvesting advanced nether resources natively.
* **Crafting Cost:** Crafting an extractor requires massive amounts of the material it mines. For example, a Diamond Extractor requires **Compact Diamond Blocks** (where 1 Compact Block = 9 Vanilla Diamond Blocks).
* **Tier Scaling:** Higher tiers are exponentially faster and hold more items. Additionally, they are technologically superior: a Tier III extractor runs much cooler and quieter than a Tier I, naturally reducing the risk of Infestations.
* **Risk by Material:** Rare materials inherently generate more noise. A Tier I Diamond Extractor creates massive heat compared to a Tier I Coal Extractor. Upgrading to a Tier III Diamond Extractor is crucial to *reduce* that immense noise and make it safer to operate!

Players can further slot tiered modules into the Extractor GUI:
* **Drill Speed Module (Tier I & II):** Reduces the tick delay between block breaks, reduces fuel efficiency of the extractor. Tier II is heavily gated by compact gold/redstone.
* **Fortune Module (Tier I & II):** Adds a % chance to double the ore drops.
* **Furnace Module:** The gained materials are cooked (only available for the Iron, Copper and Gold extractors). Consumes more fuel to cook the items.
* **Compactor module:** Converts the extracted items into block variants. If the extractor supports a furnace, then a furnace MUST exist in order for the compactor module to be compatible.
* **Storage Module (Tier I & II):** Unlocks more storage space for extracted resources.

### PvE "Base Defense" Events (Non-Forced PvP)
Mid-to-high tier Extractors are loud and generate immense heat, triggering an **Infestation Event**. Here is how the refined event works:w
* **The Beacon Effect:** The Extractor attempts to spawn mobs in a 30-block radius. 
* **Light Mitigation:** Players can actively defend their machines by lighting up the area. **If the light level is 5 or higher, mobs cannot spawn there.** This makes torch placement and base design critical.
* **The Siege:** If valid dark spots exist, waves of mobs spawn and pathfind **directly to the Extractor Core**, ignoring players entirely. 
* **Rare "Breaker" Mobs:** Custom or rare vanilla mobs spawn to break through player defenses. For example, a **Charged Creeper** might spawn during a high-tier extraction. If it gets within 16 blocks of the Extractor but gets stuck behind a wall or trench, it will self-detonate to blow open the player's defenses, allowing the rest of the horde inside!
* If the Extractor's health reaches zero, it shuts down, drops its internal inventory, and requires an expensive repair kit to restart.
* **Why it's fun:** It scales difficulty directly with profit. Cheap extractors are safe and slow. Expensive extractors are highly profitable but require the player to build intricate, well-lit, heavily fortified bunkers to survive the sieges!

---

## 3. Technical Implementation (SurvivalCore Architecture)

This system fits perfectly into your **Tier 3 (Services)** and **Tier 4 (Apps)** architecture.

### 1. `ExtractorService` (Tier 3)
* **`Extractor_IOSubservice`**: Saves extractor locations, fuel levels, inventories, and upgrade slots to Redis. Since Folia unloads chunks, we need to know where extractors are.
* **`Extractor_ScanningSubservice`**: Handles the asynchronous block counting when a player uses the Geological Scanner. Uses Folia's `RegionScheduler` to avoid main-thread lag.
* **`Extractor_MiningSubservice`**: The engine that physically replaces blocks. It queries the chunk snapshot, finds the coordinate of a target block, schedules a block update, and updates the inventory.

### 2. `ExtractorApp` (Tier 4)
* Provides the user-facing commands (e.g., `/extractor list` to see coordinates of all owned extractors).
* Manages the interactive GUIs built using `InventoryGUIService`.
* Handles the craft recipes for the scanner and modules.

---

## 4. Expansion Ideas (Future-Proofing)

* **Liquid Extractors:** Pumps that suck up lava lakes in the Nether to power advanced furnaces.
* **Arboreal Extractors (Lumberjacks):** Surface-level machines that automatically chop down trees within a radius.
* **Network Nodes:** Endgame players can craft "Item Pipes" or teleportation nodes to send extracted items directly from their wilderness claim back to their main base.

---
## Summary
This mechanic fulfills all your criteria:
1. It is heavily inspired by Skyblock Minions (passive income, upgrades).
2. It mandates and rewards **Open World exploration** (finding rich chunks).
3. It provides endless **PvE / Base-building** motivation (defending the machines).
4. It avoids artificial limits—you aren't "locked" in a chunk; you just *want* to stay there because your machinery is there!

---

## 5. Crafting & Items Reference

### A. Compact Blocks
**Recipe:** 3x3 grid filled with the vanilla block.
*E.g., 9 Blocks of Coal = 1 Compact Coal Block (which is 81 Coal).*
*Applies to: Coal, Iron, Gold, Diamond, Emerald, Redstone, Lapis, Copper.*
**Reversible:** 1 Compact Block in the crafting grid yields 9 of the regular block.

### B. Geological Scanners
**Tier I Scanner (5 Uses)**
* R R R (Redstone Dust)
* P C P (Paper, Compass, Paper)
* P P P (Paper)

**Tier II Scanner (10 Uses)**
* E S E (Empty Map, Scanner Tier I, Empty Map)
* M C M (Empty Map, Clock, Empty Map)
* M M M (Empty Map)

**Tier III Scanner (25 Uses)**
* M S M (Empty Map, Scanner Tier II, Empty Map)
* M L M (Empty Map, Sculk Sensor, Empty Map)
* M M M (Empty Map)

**Tier IV Scanner (128 Uses)** (Instantly scans entire vertical chunk)
* C S C (Calibrated Sculk Sensor, Scanner Tier III, Calibrated Sculk Sensor)
* D B D (Compact Diamond Block, Beacon, Compact Diamond Block)
* I I I (Compact Iron Block)

**Tier V Scanner (48 Uses)** (Nether-only, Locates Quartz & Ancient Debris)
* N S N (Netherite Scrap, Scanner Tier IV, Netherite Scrap)
* G B G (Compact Gold Block, Beacon, Compact Gold Block)
* N N N (Netherite Scrap)

### C. Extractor Cores (Material Specific)
*Note: "C" is the Compact Block of the specific material (e.g., Compact Diamond Block for a Diamond Extractor).*

**Tier I Extractor**
* C C C
* C P C (P = Diamond Pickaxe)
* I B I (I = Iron Block, B = Blast Furnace)

**Tier II Extractor**
* C D C (D = Compact Diamond Block)
* C E C (E = Extractor Tier I)
* O F O (O = Obsidian, F = Furnace)

**Tier III Extractor**
* C S C (S = Nether Star)
* C E C (E = Extractor Tier II)
* C C C 

**Tier IV Extractor** (Overworld, mines upwards 10 blocks, mines netherite very slowly)
* N E N (N = Netherite Scrap, E = Extractor Tier III)
* C S C (S = Nether Star)
* C C C 

**Tier V Extractor** (Nether Exclusive)
* N E N (N = Netherite Ingot, E = Extractor Tier IV)
* B S B (B = Beacon, S = Nether Star)
* C C C 

### D. Extractor Modules
**Drill Speed Module (Tier I)**
* R R R (Redstone Block)
* R P R (P = Diamond Pickaxe)
* R R R 

**Drill Speed Module (Tier II)**
* R G R (R = Compact Redstone Block)
* G S G (G = Compact Gold Block, S = Speed Module Tier I)
* R G R

**Fortune Module (Tier I)**
* L E L (L = Lapis Block)
* E P E (E = Emerald Block, P = Diamond Pickaxe)
* L E L

**Fortune Module (Tier II)**
* L E L (L = Compact Lapis Block)
* E F E (E = Compact Emerald Block, F = Fortune Module Tier I)
* L E L

**Furnace Module**
* I I I (I = Compact Iron Block)
* I F I (F = Blast Furnace)
* I I I

**Compactor Module**
* I P I (I = Iron Block)
* I R I (P = Piston, R = Redstone Block)
* I I I

**Storage Module (Tier I)**
* C C C (C = Chest)
* C H C (H = Hopper)
* C C C

**Storage Module (Tier II)**
* I I I (I = Iron Block)
* I S I (S = Storage Module Tier I)
* I I I

### E. Repair Kit
*(Used to restart an Extractor if it is destroyed during a siege).*
* I I I (I = Iron Ingot)
* I A I (A = Anvil)
* I I I
