package ipsis.woot.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Factory Exporter Block Entity
 * Stores output items produced by the factory
 */
public class ExporterBlockEntity extends BlockEntity implements Container {

    private static final int INVENTORY_SIZE = 27; // 3 rows of 9 slots
    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public ExporterBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.FACTORY_EXPORTER.get(), pos, state);
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= inventory.size()) {
            return ItemStack.EMPTY;
        }
        return inventory.get(slot);
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) {
        if (slot >= 0 && slot < inventory.size()) {
            inventory.set(slot, stack);
            if (stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            setChanged();
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        inventory.clear();
        setChanged();
    }

    /**
     * Try to insert an item into the inventory
     * Returns the remaining stack that couldn't be inserted
     */
    @Nonnull
    public ItemStack insertItem(@Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stack.copy();

        // First pass: try to stack with existing items
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = inventory.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)) {
                int maxStack = Math.min(existing.getMaxStackSize(), getMaxStackSize());
                int canAdd = maxStack - existing.getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, remaining.getCount());
                    if (!simulate) {
                        existing.grow(toAdd);
                    }
                    remaining.shrink(toAdd);
                }
            }
        }

        // Second pass: try to insert into empty slots
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            if (inventory.get(slot).isEmpty()) {
                int maxStack = Math.min(remaining.getMaxStackSize(), getMaxStackSize());
                int toInsert = Math.min(remaining.getCount(), maxStack);
                if (!simulate) {
                    inventory.set(slot, remaining.split(toInsert));
                } else {
                    remaining.shrink(toInsert);
                }
            }
        }

        if (!simulate && remaining.getCount() != stack.getCount()) {
            setChanged();
        }

        return remaining;
    }

    /**
     * Check if there's space for an item
     */
    public boolean hasSpaceFor(@Nonnull ItemStack stack) {
        ItemStack result = insertItem(stack, true);
        return result.isEmpty();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, registries);
    }
}
