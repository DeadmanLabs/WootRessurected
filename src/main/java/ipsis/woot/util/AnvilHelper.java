package ipsis.woot.util;

import ipsis.woot.Woot;
import ipsis.woot.crafting.AnvilRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for anvil crafting operations
 */
public class AnvilHelper {

    /**
     * Get all item entities within a 2x2x2 area around the anvil position
     * @param level The world/level
     * @param pos The anvil position
     * @return List of ItemEntity objects in the area
     */
    @Nonnull
    public static List<ItemEntity> getItems(Level level, BlockPos pos) {
        List<ItemEntity> items = new ArrayList<>();
        AABB aabb = AABB.ofSize(pos.getCenter(), 2.0, 2.0, 2.0);
        List<ItemEntity> entityItemList = level.getEntitiesOfClass(ItemEntity.class, aabb);

        for (ItemEntity itemEntity : entityItemList) {
            ItemStack itemStack = itemEntity.getItem();
            if (itemStack.isEmpty() || itemEntity.isRemoved()) {
                continue;
            }
            items.add(itemEntity);
        }

        return items;
    }

    /**
     * Check if the anvil is "hot" (has a magma block below it)
     * @param level The world/level
     * @param pos The anvil position
     * @return true if there's a magma block directly below the anvil
     */
    public static boolean isAnvilHot(@Nonnull Level level, @Nonnull BlockPos pos) {
        BlockState blockState = level.getBlockState(pos.below());
        return blockState.is(Blocks.MAGMA_BLOCK);
    }

    /**
     * Find a matching anvil recipe for the given base item and ingredients
     * @param level The world/level
     * @param baseItem The item placed on the anvil
     * @param ingredients List of item stacks collected from around the anvil
     * @return The matching recipe, or null if none found
     */
    public static AnvilRecipe findRecipe(@Nonnull Level level, @Nonnull ItemStack baseItem, @Nonnull List<ItemStack> ingredients) {
        if (level.isClientSide()) {
            return null;
        }

        // Get all anvil recipes using getAllRecipesFor
        var anvilRecipes = level.getRecipeManager().getAllRecipesFor(Woot.ANVIL_RECIPE_TYPE.get());
        Woot.LOGGER.debug("Found {} anvil recipes in RecipeManager", anvilRecipes.size());

        for (RecipeHolder<AnvilRecipe> recipeHolder : anvilRecipes) {
            AnvilRecipe anvilRecipe = recipeHolder.value();

            Woot.LOGGER.debug("Checking anvil recipe: {} - Base: {}", recipeHolder.id(), anvilRecipe.getBaseItem());

            // Check if base item matches
            if (anvilRecipe.isMatchingBase(baseItem)) {
                Woot.LOGGER.debug("  Base item matches! Checking ingredients...");
                // Check if ingredients match
                if (anvilRecipe.matchesIngredients(ingredients)) {
                    Woot.LOGGER.debug("Found matching recipe: {}", recipeHolder.id());
                    return anvilRecipe;
                } else {
                    Woot.LOGGER.debug("  Ingredients don't match. Required: {}", anvilRecipe.getAnvilIngredients());
                }
            } else {
                Woot.LOGGER.debug("  Base item doesn't match");
            }
        }

        Woot.LOGGER.debug("No matching recipe found. Total anvil recipes loaded: {}", anvilRecipes.size());
        return null;
    }

    /**
     * Consume ingredients from the item entities based on the recipe
     * @param entityItems The list of item entities around the anvil
     * @param recipe The recipe being crafted
     */
    public static void consumeIngredients(List<ItemEntity> entityItems, AnvilRecipe recipe) {
        for (var ingredient : recipe.getAnvilIngredients()) {
            boolean consumed = false;

            for (ItemEntity itemEntity : entityItems) {
                ItemStack stack = itemEntity.getItem();
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    // Consume one item from this stack
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    }
                    consumed = true;
                    break;
                }
            }

            if (!consumed) {
                Woot.LOGGER.warn("Failed to consume ingredient: {}", ingredient);
            }
        }
    }
}
