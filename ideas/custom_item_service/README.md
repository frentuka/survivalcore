# Custom Item Service

This module is a **Tier 3 (Service)** designed to provide a unified, secure, and extensible framework for all custom items in SurvivalCore. 

It is the core prerequisite for any mechanic that relies on custom items, most notably the **Extractors Mechanic**.

## Motivation

SurvivalCore needs custom items (Scanners, Extractors, Modules, Compact Blocks, Geode Crystals, etc.). Vanilla Minecraft provides `ItemStack` and `ItemMeta`, but managing custom items purely through metadata is fragile and prone to duplication exploits. 

This service provides:
- **Strong Identity:** Every item knows exactly what it is via Persistent Data Container (PDC) tags.
- **Traceability:** High-value items receive unique UUIDs and are tracked server-wide.
- **Anti-Duplication:** Multi-layered defense against dupes, intercepting vanilla mechanics (hoppers, creative mode, chunk unloads) that usually duplicate items.
- **Custom Crafting:** First-class support for custom shaped and shapeless recipes using custom item ingredients.

---

## Documentation Index

| File | Description |
|------|-------------|
| [01_core_architecture.md](01_core_architecture.md) | Data models, PDC tags, and the 4 subservices (Registry, Serialization, Validation, Crafting) |
| [02_anti_duplication.md](02_anti_duplication.md) | The multi-layered security model for preventing item duplication |
| [03_custom_crafting.md](03_custom_crafting.md) | How recipes are defined, validated, and how vanilla exploit vectors are handled |
| [04_open_questions.md](04_open_questions.md) | Design gaps and architectural decisions that need finalizing |

> **Note:** The original draft of this plan existed inside the Extractors Mechanic folder. It has been elevated to its own dedicated service design since its scope affects the entire server.
