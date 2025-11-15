package ipsis.woot.recipes;

import ipsis.woot.multiblock.EnumMobFactoryTier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents ingredient requirements and drop configuration for spawning a specific mob
 * Stores lists of ItemStacks and FluidStacks needed per spawn cycle
 * Also stores configured drops for mobs (overrides loot table if specified)
 * Also stores required factory tier for spawning this mob
 * Based on original Woot's SpawnRecipe system
 */
public class SpawnRecipe {

    private final List<ItemStack> items;
    private final List<FluidStack> fluids;
    private final List<ItemStack> drops; // Configured drops (empty = use loot table)
    private final boolean efficiency; // Whether efficiency upgrades apply
    private final EnumMobFactoryTier requiredTier; // Minimum factory tier required (null = will be calculated)

    private SpawnRecipe(List<ItemStack> items, List<FluidStack> fluids, List<ItemStack> drops, boolean efficiency, EnumMobFactoryTier requiredTier) {
        this.items = new ArrayList<>(items);
        this.fluids = new ArrayList<>(fluids);
        this.drops = new ArrayList<>(drops);
        this.efficiency = efficiency;
        this.requiredTier = requiredTier;
    }

    /**
     * Get required item ingredients
     */
    @Nonnull
    public List<ItemStack> getItems() {
        return items;
    }

    /**
     * Get required fluid ingredients
     */
    @Nonnull
    public List<FluidStack> getFluids() {
        return fluids;
    }

    /**
     * Get configured drops (empty if using loot table)
     */
    @Nonnull
    public List<ItemStack> getDrops() {
        return drops;
    }

    /**
     * Check if this recipe has configured drops (overrides loot table)
     */
    public boolean hasConfiguredDrops() {
        return !drops.isEmpty();
    }

    /**
     * Check if efficiency upgrades apply to this recipe
     */
    public boolean hasEfficiency() {
        return efficiency;
    }

    /**
     * Get required factory tier (null if should be auto-calculated)
     */
    @Nullable
    public EnumMobFactoryTier getRequiredTier() {
        return requiredTier;
    }

    /**
     * Check if tier is explicitly configured
     */
    public boolean hasTierOverride() {
        return requiredTier != null;
    }

    /**
     * Check if recipe has no ingredient requirements
     */
    public boolean isEmpty() {
        return items.isEmpty() && fluids.isEmpty();
    }

    /**
     * Get total number of ingredients
     */
    public int getIngredientCount() {
        return items.size() + fluids.size();
    }

    @Override
    public String toString() {
        return String.format("SpawnRecipe{items=%d, fluids=%d, drops=%d, tier=%s, efficiency=%s}",
            items.size(), fluids.size(), drops.size(),
            requiredTier != null ? requiredTier : "auto", efficiency);
    }

    /**
     * Builder for creating SpawnRecipes
     */
    public static class Builder {
        private final List<ItemStack> items = new ArrayList<>();
        private final List<FluidStack> fluids = new ArrayList<>();
        private final List<ItemStack> drops = new ArrayList<>();
        private boolean efficiency = true; // Default: efficiency applies
        private EnumMobFactoryTier requiredTier = null; // Default: auto-calculate from mob health

        public Builder addItem(@Nonnull ItemStack stack) {
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
            return this;
        }

        public Builder addFluid(@Nonnull FluidStack stack) {
            if (!stack.isEmpty()) {
                fluids.add(stack.copy());
            }
            return this;
        }

        public Builder addDrop(@Nonnull ItemStack stack) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
            return this;
        }

        public Builder setEfficiency(boolean efficiency) {
            this.efficiency = efficiency;
            return this;
        }

        public Builder setRequiredTier(@Nullable EnumMobFactoryTier tier) {
            this.requiredTier = tier;
            return this;
        }

        @Nonnull
        public SpawnRecipe build() {
            return new SpawnRecipe(items, fluids, drops, efficiency, requiredTier);
        }

        /**
         * Create an empty recipe (no requirements)
         */
        @Nonnull
        public static SpawnRecipe empty() {
            return new Builder().build();
        }
    }
}
