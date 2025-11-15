package ipsis.woot.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory Importer Block Entity
 * Scans adjacent blocks for storage containers and uses their contents as ingredients
 * Does NOT have internal storage - acts as a proxy to adjacent inventories
 */
public class ImporterBlockEntity extends BlockEntity {

    public ImporterBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.FACTORY_IMPORTER.get(), pos, state);
    }

    /**
     * Get all adjacent item handlers (excluding other importers)
     * Checks all 6 faces for containers
     */
    @Nonnull
    public List<IItemHandler> getAdjacentItemHandlers() {
        List<IItemHandler> handlers = new ArrayList<>();

        if (level == null) {
            return handlers;
        }

        // Check all 6 faces
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity adjacentBE = level.getBlockEntity(adjacentPos);

            // Skip if it's another importer
            if (adjacentBE instanceof ImporterBlockEntity) {
                continue;
            }

            // Check for IItemHandler capability
            if (adjacentBE != null) {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, adjacentPos, direction.getOpposite());
                if (handler != null) {
                    handlers.add(handler);
                }
            }
        }

        return handlers;
    }

    /**
     * Check if adjacent containers have a specific item with at least the given count
     */
    public boolean hasItem(@Nonnull ItemStack template, int count) {
        if (level == null) {
            return false;
        }

        int found = 0;
        List<IItemHandler> handlers = getAdjacentItemHandlers();

        for (IItemHandler handler : handlers) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template)) {
                    found += stack.getCount();
                    if (found >= count) {
                        return true;
                    }
                }
            }
        }

        return found >= count;
    }

    /**
     * Try to consume an item from adjacent containers
     * Returns true if the full amount was consumed
     */
    public boolean consumeItem(@Nonnull ItemStack template, int count) {
        if (level == null) {
            return false;
        }

        int remaining = count;
        List<IItemHandler> handlers = getAdjacentItemHandlers();

        for (IItemHandler handler : handlers) {
            if (remaining <= 0) {
                break;
            }

            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (remaining <= 0) {
                    break;
                }

                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template)) {
                    // Try to extract items from this slot
                    int toExtract = Math.min(remaining, stack.getCount());
                    ItemStack extracted = handler.extractItem(slot, toExtract, false);

                    if (!extracted.isEmpty()) {
                        remaining -= extracted.getCount();
                    }
                }
            }
        }

        return remaining == 0;
    }

    /**
     * Check if this importer has access to any adjacent containers
     */
    public boolean hasAdjacentContainers() {
        return !getAdjacentItemHandlers().isEmpty();
    }

    /**
     * Get total count of a specific item in all adjacent containers
     */
    public int getItemCount(@Nonnull ItemStack template) {
        if (level == null) {
            return 0;
        }

        int found = 0;
        List<IItemHandler> handlers = getAdjacentItemHandlers();

        for (IItemHandler handler : handlers) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template)) {
                    found += stack.getCount();
                }
            }
        }

        return found;
    }
}
