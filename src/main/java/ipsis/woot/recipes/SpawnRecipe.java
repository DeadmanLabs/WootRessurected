package ipsis.woot.recipes;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents ingredient requirements for spawning a specific mob
 * Stores lists of ItemStacks and FluidStacks needed per spawn cycle
 * Based on original Woot's SpawnRecipe system
 */
public class SpawnRecipe {

    private final List<ItemStack> items;
    private final List<FluidStack> fluids;
    private final boolean efficiency; // Whether efficiency upgrades apply

    private SpawnRecipe(List<ItemStack> items, List<FluidStack> fluids, boolean efficiency) {
        this.items = new ArrayList<>(items);
        this.fluids = new ArrayList<>(fluids);
        this.efficiency = efficiency;
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
     * Check if efficiency upgrades apply to this recipe
     */
    public boolean hasEfficiency() {
        return efficiency;
    }

    /**
     * Check if recipe has no requirements
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
        return String.format("SpawnRecipe{items=%d, fluids=%d, efficiency=%s}",
            items.size(), fluids.size(), efficiency);
    }

    /**
     * Builder for creating SpawnRecipes
     */
    public static class Builder {
        private final List<ItemStack> items = new ArrayList<>();
        private final List<FluidStack> fluids = new ArrayList<>();
        private boolean efficiency = true; // Default: efficiency applies

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

        public Builder setEfficiency(boolean efficiency) {
            this.efficiency = efficiency;
            return this;
        }

        @Nonnull
        public SpawnRecipe build() {
            return new SpawnRecipe(items, fluids, efficiency);
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
