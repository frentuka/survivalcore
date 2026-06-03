# Open-World Chunk Extractors Mechanic

> **Status:** Design Phase — Not Yet Implemented  
> **Author:** srleg  
> **Last Updated:** 2026-06-02

## Concept Overview

The Extractor Mechanic is a dynamic, open-world alternative to Skyblock minions. Instead of magical generators creating items out of thin air, **Extractors physically mine the existing world**.

Players explore the infinite survival world, "prospect" chunks to find rich ore veins, deploy heavy machinery, and defend it while it literally hollows out the chunk block-by-block over time. This creates a loop of exploration, base defense, and passive income that perfectly complements an open-world PvE environment.

### Core Design Pillars

| Pillar | Description |
|--------|-------------|
| **Exploration-Driven** | Rich chunks must be discovered — not spawned |
| **Physically Grounded** | Blocks are actually removed from the world |
| **Emergent Economy** | Coal extractors fuel diamond extractors, creating supply chains |
| **PvE Pressure** | Profitable machines attract hostile infestations |
| **Anti-Stagnation** | Chunks deplete, forcing continued exploration |

---

## Document Index

| Document | Contents |
|----------|----------|
| [01_gameplay_loop.md](01_gameplay_loop.md) | The four-step gameplay loop: Prospect → Deploy → Extract → Relocate |
| [02_scanners.md](02_scanners.md) | Geological Scanner system: tiers, calibration, material samples |
| [03_extractors.md](03_extractors.md) | Extractor Cores: types, tiers, modules, fuel economy |
| [04_infestation_events.md](04_infestation_events.md) | PvE base defense: mob sieges, light mitigation, breaker mobs |
| [05_crafting_reference.md](05_crafting_reference.md) | Complete crafting recipes for all items |
| [06_technical_architecture.md](06_technical_architecture.md) | SurvivalCore integration: services, subservices, data model |
| [07_expansion_ideas.md](07_expansion_ideas.md) | Future-proofing: liquid extractors, lumberjacks, networks |
| [08_critique_and_gaps.md](08_critique_and_gaps.md) | Design critique: gaps, inconsistencies, and open questions (with resolution status) |
| [09_custom_item_service_plan.md](09_custom_item_service_plan.md) | Implementation plan for the Custom Item Service prerequisite |
| [10_balance_numbers.md](10_balance_numbers.md) | Extractor numeric balancing: cooldowns, fuel, ore density, and ROI models |

---

## Summary

This mechanic fulfills all design criteria:

1. It is heavily inspired by Skyblock Minions (passive income, upgrades).
2. It mandates and rewards **open world exploration** (finding rich chunks).
3. It provides endless **PvE / base-building** motivation (defending machines).
4. It avoids artificial limits — you aren't "locked" in a chunk; you just *want* to stay there because your machinery is there!
