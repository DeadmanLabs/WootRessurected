package ipsis.woot.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block Entity for the Stygian Iron Anvil
 * Stores a single base item used in anvil crafting
 */
public class AnvilBlockEntity extends BlockEntity {

    private ItemStack baseItem = ItemStack.EMPTY;

    public AnvilBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.ANVIL.get(), pos, state);
    }

    /**
     * Get the current base item on the anvil
     */
    public ItemStack getBaseItem() {
        return baseItem;
    }

    /**
     * Set the base item on the anvil
     */
    public void setBaseItem(ItemStack stack) {
        this.baseItem = stack.copy();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Check if the anvil has a base item
     */
    public boolean hasBaseItem() {
        return !baseItem.isEmpty();
    }

    /**
     * Clear the base item from the anvil
     */
    public void clearBaseItem() {
        this.baseItem = ItemStack.EMPTY;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!baseItem.isEmpty()) {
            tag.put("BaseItem", baseItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("BaseItem")) {
            this.baseItem = ItemStack.parseOptional(registries, tag.getCompound("BaseItem"));
        } else {
            this.baseItem = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
