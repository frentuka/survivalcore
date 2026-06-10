# Custom Crafting Subservice

SurvivalCore extractors require highly specific crafting recipes using custom items (e.g., Compact Blocks) as ingredients. 

The `CustomItem_CraftingSubservice` handles registering and validating these recipes securely.

## 1. Recipe Definition

We wrap Bukkit's native `ShapedRecipe` and `ShapelessRecipe` to allow for custom items as ingredients.

```kotlin
interface RecipeIngredient {
    fun test(item: ItemStack): Boolean
}

class VanillaIngredient(val material: Material) : RecipeIngredient { ... }
class CustomIngredient(val typeId: String) : RecipeIngredient { ... }
```

**API Example:**
```kotlin
craftingSubservice.registerShapedRecipe(
    key = "diamond_extractor_tier_1",
    result = CustomItemType("extractor_diamond_tier_1"),
    shape = arrayOf(
        "CCC",
        "C C",
        "CCC"
    ),
    ingredients = mapOf(
        'C' to CustomIngredient("compact_diamond_block")
    )
)
```

## 2. Validation & Exploit Prevention

Bukkit's recipe system only understands vanilla materials. If we register a recipe requiring `COAL_BLOCK` (as a stand-in for Compact Coal Block), Bukkit will allow the player to craft the item using regular Vanilla Coal Blocks.

**The Solution:** 

**Phase 1: `PrepareItemCraftEvent` Interception**
1. When a player places items in a crafting grid, Bukkit fires `PrepareItemCraftEvent`.
2. We check if the resulting item matches a custom recipe.
3. If it does, we iterate through the crafting matrix and test every item against our `RecipeIngredient` definitions.
4. If a custom ingredient is required (e.g., `compact_coal_block`), we verify the PDC tag `survivalcore:custom_item_type` matches.
5. If the player used a vanilla item instead, we **clear the result slot**, visually preventing the craft.

**Phase 2: `CraftItemEvent` Strict Validation (Exploit Prevention)**
Client desyncs or malicious packets can sometimes bypass the prepare phase and trigger a direct craft.
1. When `CraftItemEvent` fires, we perform the exact same validation check as Phase 1.
2. If any ingredient in the matrix fails the `RecipeIngredient` test at the moment of crafting, we aggressively **cancel the event**.
3. This creates a secure, impenetrable wall against spoofed crafting packets.

## 3. Instance Generation on Craft

When the custom item is successfully crafted (via `CraftItemEvent`), the result is generated through the Registry Subservice's `createInstance()` method. 

This ensures:
- The newly crafted item receives a unique UUID (if tracked).
- The UUID is logged in Redis at the exact moment of creation.
- Owner tags are set (if the item is player-bound).
