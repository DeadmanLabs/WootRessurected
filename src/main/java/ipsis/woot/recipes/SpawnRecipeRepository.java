package ipsis.woot.recipes;

import ipsis.woot.Woot;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Central registry for spawn recipes
 * Maps mob entity IDs to their spawn ingredient requirements
 * Based on original Woot's SpawnRecipeRepository pattern
 */
public class SpawnRecipeRepository {

    // Mob entity key -> SpawnRecipe mapping
    private final Map<String, SpawnRecipe> recipes = new HashMap<>();

    // Default recipe for mobs without specific requirements
    private SpawnRecipe defaultRecipe = SpawnRecipe.Builder.empty();

    /**
     * Maximum number of item ingredients per recipe (from original Woot)
     */
    public static final int MAX_ITEM_INGREDIENTS = 6;

    /**
     * Maximum number of fluid ingredients per recipe (from original Woot)
     */
    public static final int MAX_FLUID_INGREDIENTS = 6;

    /**
     * Register a spawn recipe for a specific mob
     * @param mobKey Mob entity key (e.g., "minecraft:creeper")
     * @param recipe The spawn recipe
     */
    public void register(@Nonnull String mobKey, @Nonnull SpawnRecipe recipe) {
        if (recipe.getItems().size() > MAX_ITEM_INGREDIENTS) {
            Woot.LOGGER.warn("Spawn recipe for {} has too many items ({} > {}), trimming",
                mobKey, recipe.getItems().size(), MAX_ITEM_INGREDIENTS);
        }
        if (recipe.getFluids().size() > MAX_FLUID_INGREDIENTS) {
            Woot.LOGGER.warn("Spawn recipe for {} has too many fluids ({} > {}), trimming",
                mobKey, recipe.getFluids().size(), MAX_FLUID_INGREDIENTS);
        }

        recipes.put(mobKey, recipe);
        Woot.LOGGER.debug("Registered spawn recipe for {}: {}", mobKey, recipe);
    }

    /**
     * Register a spawn recipe using ResourceLocation
     */
    public void register(@Nonnull ResourceLocation mobId, @Nonnull SpawnRecipe recipe) {
        register(mobId.toString(), recipe);
    }

    /**
     * Get spawn recipe for a specific mob
     * Returns default recipe if mob has no specific requirements
     * @param mobKey Mob entity key
     * @return SpawnRecipe (never null)
     */
    @Nonnull
    public SpawnRecipe get(@Nonnull String mobKey) {
        return recipes.getOrDefault(mobKey, defaultRecipe);
    }

    /**
     * Get spawn recipe using ResourceLocation
     */
    @Nonnull
    public SpawnRecipe get(@Nonnull ResourceLocation mobId) {
        return get(mobId.toString());
    }

    /**
     * Check if a mob has a specific recipe (not using default)
     */
    public boolean hasRecipe(@Nonnull String mobKey) {
        return recipes.containsKey(mobKey);
    }

    /**
     * Set the default recipe for mobs without specific requirements
     */
    public void setDefaultRecipe(@Nonnull SpawnRecipe recipe) {
        this.defaultRecipe = recipe;
        Woot.LOGGER.info("Set default spawn recipe: {}", recipe);
    }

    /**
     * Get the default recipe
     */
    @Nonnull
    public SpawnRecipe getDefaultRecipe() {
        return defaultRecipe;
    }

    /**
     * Get all registered mob keys
     */
    @Nonnull
    public Set<String> getRegisteredMobs() {
        return recipes.keySet();
    }

    /**
     * Get total number of registered recipes
     */
    public int getRecipeCount() {
        return recipes.size();
    }

    /**
     * Clear all recipes (useful for reloading)
     */
    public void clear() {
        recipes.clear();
        defaultRecipe = SpawnRecipe.Builder.empty();
        Woot.LOGGER.info("Cleared spawn recipe repository");
    }

    /**
     * Remove a specific recipe
     */
    @Nullable
    public SpawnRecipe remove(@Nonnull String mobKey) {
        return recipes.remove(mobKey);
    }
}
