# Open Questions & Design Gaps

Before development of the Custom Item Service begins, the following questions need to be resolved.

| # | Question | Impact | Current Recommendation |
*All design questions have been resolved.*

## Resolved Decisions

| # | Question | Resolution |
|---|----------|------------|
| 1 | CustomModelData vs Vanilla Fallbacks | **Use CustomModelData.** It's a 100% vanilla feature. We will implement a dual-system: players with the server resource pack see beautiful 3D models/textures. Players who decline the pack see the base vanilla item (e.g., a Coal Block) with a glowing enchantment glint, custom name, and lore. |
| 2 | Folia Scheduling for Inventory Audits | **Per-region tasks.** The system will use a global coordinator scheduler that triggers per-region tasks. Each region thread will safely iterate over the players currently inside its bounds, ensuring strict Folia thread safety without cross-thread lag. |
| 3 | Persistence vs. Startup Rebuild | **Redis is the absolute source of truth.** Item UUIDs must survive server restarts in Redis. This ensures immediate validation against dupes the second a player interacts with an item post-restart, closing vulnerability windows. |
| 4 | Nested Container Scanning Depth | **Hard cap at Depth 3** (e.g., Inventory → Shulker → Bundle). Scanning deeper than 3 levels causes performance risks. Players will be natively blocked from inserting tracked items into containers that exceed this nesting depth. |
| 5 | Tracked Items in Hoppers | **Strictly blacklisted via fast-fail checks.** High-value tracked items (Extractors, Scanners) are prevented from moving through Hoppers, Droppers, or Dispensers. The system uses a highly optimized `InventoryMoveItemEvent` check that looks exclusively for the `survivalcore:custom_item_type` PDC tag. If present, the transfer is instantly cancelled without touching Redis. Note: Bulk untracked items like Compact Blocks *can* still go through hoppers. |
