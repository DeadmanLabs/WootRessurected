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
 * Factory Importer Block Entity
 * Stores ingredient items that will be consumed by the factory
 */
public class ImporterBlockEntity extends BlockEntity implements Container {

    private static final int INVENTORY_SIZE = 27; // 3 rows of 9 slots
    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public ImporterBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.FACTORY_IMPORTER.get(), pos, state);
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
     * Try to consume an item from the inventory
     * Returns true if the item was found and consumed
     */
    public boolean consumeItem(@Nonnull ItemStack template, int count) {
        int remaining = count;

        for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
            ItemStack stack = inventory.get(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template)) {
                int toTake = Math.min(remaining, stack.getCount());
                stack.shrink(toTake);
                remaining -= toTake;

                if (stack.isEmpty()) {
                    inventory.set(slot, ItemStack.EMPTY);
                }
            }
        }

        if (remaining < count) {
            setChanged();
            return remaining == 0;
        }

        return false;
    }

    /**
     * Check if the importer has a specific item with at least the given count
     */
    public boolean hasItem(@Nonnull ItemStack template, int count) {
        int found = 0;

        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template)) {
                found += stack.getCount();
                if (found >= count) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, HolderLookup.@Nonnull Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, HolderLookup.@Nonnull Provider registries) {
        super.loadAdditional(tag, registries);
        inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, registries);
    }
}
