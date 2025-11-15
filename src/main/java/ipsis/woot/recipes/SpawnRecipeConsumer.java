package ipsis.woot.recipes;

import ipsis.woot.Woot;
import ipsis.woot.blockentities.ImporterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles checking and consuming spawn recipe ingredients from importer inventories
 * Based on original Woot's SpawnRecipeConsumer pattern
 */
public class SpawnRecipeConsumer {

    /**
     * Check if all required ingredients are available in the importers
     * @param level World
     * @param importerPositions List of importer block positions
     * @param recipe Spawn recipe with requirements
     * @param mobCount Number of mobs to spawn (scales ingredient requirements)
     * @return true if all ingredients are available
     */
    public static boolean hasIngredients(@Nonnull Level level,
                                          @Nonnull List<BlockPos> importerPositions,
                                          @Nonnull SpawnRecipe recipe,
                                          int mobCount) {
        if (recipe.isEmpty()) {
            return true; // No requirements
        }

        // Check all required items
        for (ItemStack required : recipe.getItems()) {
            int totalNeeded = required.getCount() * mobCount;
            if (!hasItem(level, importerPositions, required, totalNeeded)) {
                return false;
            }
        }

        // Check all required fluids
        for (FluidStack required : recipe.getFluids()) {
            int totalNeeded = required.getAmount() * mobCount;
            if (!hasFluid(level, importerPositions, required, totalNeeded)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Consume ingredients from importers
     * @param level World
     * @param importerPositions List of importer block positions
     * @param recipe Spawn recipe with requirements
     * @param mobCount Number of mobs to spawn (scales ingredient requirements)
     * @param simulate If true, only check without consuming
     * @return true if all ingredients were consumed (or would be if simulating)
     */
    public static boolean consume(@Nonnull Level level,
                                   @Nonnull List<BlockPos> importerPositions,
                                   @Nonnull SpawnRecipe recipe,
                                   int mobCount,
                                   boolean simulate) {
        if (recipe.isEmpty()) {
            return true; // No requirements
        }

        // First check if all ingredients are available
        if (!hasIngredients(level, importerPositions, recipe, mobCount)) {
            return false;
        }

        // If not simulating, consume the ingredients
        if (!simulate) {
            // Consume all required items
            for (ItemStack required : recipe.getItems()) {
                int totalNeeded = required.getCount() * mobCount;
                consumeItem(level, importerPositions, required, totalNeeded);
            }

            // Consume all required fluids
            for (FluidStack required : recipe.getFluids()) {
                int totalNeeded = required.getAmount() * mobCount;
                consumeFluid(level, importerPositions, required, totalNeeded);
            }

            Woot.LOGGER.debug("Consumed ingredients for {} mobs: {}", mobCount, recipe);
        }

        return true;
    }

    /**
     * Check if importers (via their adjacent containers) contain enough of a specific item
     */
    private static boolean hasItem(@Nonnull Level level,
                                    @Nonnull List<BlockPos> importerPositions,
                                    @Nonnull ItemStack template,
                                    int count) {
        int found = 0;

        for (BlockPos pos : importerPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ImporterBlockEntity importer) {
                // Importer now checks adjacent containers automatically
                int available = importer.getItemCount(template);
                found += available;
                if (found >= count) {
                    return true;
                }
            }
        }

        return found >= count;
    }

    /**
     * Consume a specific item from importers (via their adjacent containers)
     */
    private static void consumeItem(@Nonnull Level level,
                                     @Nonnull List<BlockPos> importerPositions,
                                     @Nonnull ItemStack template,
                                     int count) {
        int remaining = count;

        for (BlockPos pos : importerPositions) {
            if (remaining <= 0) {
                break;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ImporterBlockEntity importer) {
                // Importer now consumes from adjacent containers automatically
                if (importer.consumeItem(template, remaining)) {
                    return; // All consumed
                }

                // Consume what we can from this importer's adjacent containers
                int consumed = importer.getItemCount(template);
                if (consumed > 0) {
                    int toConsume = Math.min(remaining, consumed);
                    if (importer.consumeItem(template, toConsume)) {
                        remaining -= toConsume;
                    }
                }
            }
        }

        if (remaining > 0) {
            Woot.LOGGER.warn("Failed to consume all items: {} remaining of {}", remaining, template);
        }
    }

    /**
     * Check if importers contain enough of a specific fluid
     * Note: Fluid support not yet implemented - placeholder for future
     */
    private static boolean hasFluid(@Nonnull Level level,
                                     @Nonnull List<BlockPos> importerPositions,
                                     @Nonnull FluidStack template,
                                     int amount) {
        // TODO: Implement fluid tank support
        Woot.LOGGER.debug("Fluid checking not yet implemented: {} x {}", template, amount);
        return true; // Always succeed for now
    }

    /**
     * Consume a specific fluid from importers
     * Note: Fluid support not yet implemented - placeholder for future
     */
    private static void consumeFluid(@Nonnull Level level,
                                      @Nonnull List<BlockPos> importerPositions,
                                      @Nonnull FluidStack template,
                                      int amount) {
        // TODO: Implement fluid tank support
        Woot.LOGGER.debug("Fluid consumption not yet implemented: {} x {}", template, amount);
    }
}
