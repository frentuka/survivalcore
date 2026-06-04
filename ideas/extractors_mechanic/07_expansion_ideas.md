# Expansion Ideas (Future-Proofing)

Potential extensions to the core Extractor Mechanic for later development phases.

---

## Liquid Extractors

Pumps that suck up lava lakes in the Nether to power advanced furnaces.

- Could introduce a "Thermal Fuel" as an alternative to Compact Coal Blocks
- Natural synergy with Nether-tier extractors
- Lava lakes are finite — same depletion loop as ore extractors

---

## Arboreal Extractors (Lumberjacks)

Surface-level machines that automatically chop down trees within a radius.

- Different mechanic: radius-based rather than chunk-based
- Wood is renewable (trees regrow), so needs a different depletion/balance model
- Could require "Sapling Refills" as a fuel equivalent

---

## Network Nodes

Endgame players can craft "Item Pipes" or teleportation nodes to send extracted items directly from their wilderness claims back to their main base.

- Creates a logistics gameplay layer
- High crafting cost to prevent early-game trivialization
- Could integrate with a potential "Base Chunk" system

---

## Specialized Amethyst Extractor

> **Design Status:** Confirmed for future development. Amethyst has been removed from the standard extractor material pool specifically to accommodate this dedicated mechanic.

Amethyst geodes cannot be modeled under the standard depletion framework because **Budding Amethyst blocks regrow clusters over time** — the chunk is self-renewing, not finite. Rather than shoehorn it into the depletion loop, the Specialized Amethyst Extractor is a fundamentally different machine that harvests *regrowth* rather than *deposits*.

### Core Design Difference

| Standard Extractor | Amethyst Extractor |
|-------------------|-------------------|
| Targets finite ore blocks | Targets Amethyst Clusters (regrowth) |
| Chunk depletes permanently | Chunk produces indefinitely |
| Higher output = fewer cycles until depletion | Higher output = harvest faster than regrowth rate |
| Fueled by Compact Coal Blocks | Fueled by a different resource (TBD — potentially Compact Quartz) |

### Harvesting Model

The Amethyst Extractor does **not** break Budding Amethyst blocks. Instead:

1. It periodically scans for **fully-grown Amethyst Clusters** (the final growth stage) within the geode
2. It harvests only those clusters — leaving Small Buds, Medium Buds, and Large Buds alone
3. Harvested clusters are replaced with air (the Budding Amethyst block naturally grows a new cluster over time)
4. The **effective yield** depends on how fast clusters regrow vs. how often the extractor harvests

### Balance Challenge

Unlike standard extractors, this machine is **theoretically infinite** — the chunk never runs out. Balance levers to prevent exploitation:

- The extractor can only harvest clusters that have reached **full maturity** — rushing harvest yields nothing
- The geode's natural cluster regrowth rate (vanilla: ~20 minutes per stage, 3 stages after harvest) creates a hard cap on extraction frequency
- A **maximum clusters-per-harvest** limit prevents Fortune-module stacking from breaking the economy
- Compact Amethyst Blocks have **limited use-cases** to cap demand

### Placement Requirements (Distinct from Standard)

- Must be placed **inside the geode structure** — requires the interior space; cannot be placed externally
- Requires an Analysis Map equivalent: a **Geode Survey Crystal** (custom item), crafted using the geode's Calcite as material
- Can coexist with other players' geode access — no exclusive chunk-lock (geodes are cooperative, not contested)

### Future Scope

The Specialized Amethyst Extractor design is intentionally left incomplete here. Full design should be done as a separate mechanic document when development is ready, as it requires its own:
- Crafting recipe design
- Geode Survey Crystal tool design
- Harvesting algorithm specification
- Balance numbers and economy role

