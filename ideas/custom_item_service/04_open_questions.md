# Open Questions & Design Gaps

Before development of the Custom Item Service begins, the following questions need to be resolved.

| # | Question | Impact | Current Recommendation |
|---|----------|--------|------------------------|
| 1 | Should Compact Blocks use `CustomModelData` for custom textures, or just use lore/name/enchantment-glint differentiation? | Requires resource pack if using custom models | Start with lore/glint. Add resource pack support later if needed. |
| 2 | Should the periodic inventory audit run on a Folia global scheduler or per-region? | Performance vs. thoroughness | Per-region, iterating through players within that region to respect Folia thread safety. |
| 3 | Should tracked items survive server restarts via Redis, or should the registry be rebuilt from player inventories on startup? | Data integrity vs. complexity | Redis is the source of truth. Items must survive in Redis. |
| 4 | How deep should the audit scan for nested items (e.g., Bundles inside Shulker Boxes inside other containers)? | Performance cost | Limit nesting depth natively, or scan recursively up to a sensible hard cap (e.g., depth 3). |
| 5 | Should tracked items be allowed inside Hoppers at all? | Duplication risk | **No.** High-value tracked items (Extractors, Modules) should be blacklisted from hoppers entirely. They are not bulk transport items. |
