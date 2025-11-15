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
        ipsis.woot.Woot.LOGGER.info("[{}] setBaseItem called with: {}",
            level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN", stack);
        this.baseItem = stack.copy();
        setChanged();
        if (level != null && !level.isClientSide) {
            ipsis.woot.Woot.LOGGER.info("[SERVER] Sending block update for setBaseItem");
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
        ipsis.woot.Woot.LOGGER.info("[{}] clearBaseItem called! BaseItem before: {}",
            level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN", this.baseItem);
        this.baseItem = ItemStack.EMPTY;
        setChanged();
        if (level != null && !level.isClientSide) {
            ipsis.woot.Woot.LOGGER.info("[SERVER] Sending block update for clearBaseItem");
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        ipsis.woot.Woot.LOGGER.info("[{}] clearBaseItem done! BaseItem after: {}",
            level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN", this.baseItem);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ipsis.woot.Woot.LOGGER.debug("saveAdditional called - BaseItem: {}", baseItem);

        // ALWAYS save a boolean flag to indicate if there's an item
        // This ensures client receives updates when item is cleared (tag always has data)
        boolean hasItem = !baseItem.isEmpty();
        tag.putBoolean("HasBaseItem", hasItem);

        // Only save the ItemStack data if it's not empty (can't encode empty ItemStacks)
        if (hasItem) {
            tag.put("BaseItem", baseItem.save(registries));
            ipsis.woot.Woot.LOGGER.debug("Saved BaseItem to tag");
        } else {
            ipsis.woot.Woot.LOGGER.debug("BaseItem is empty, saved HasBaseItem=false flag");
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Check the HasBaseItem flag (always present in new saves)
        if (tag.contains("HasBaseItem")) {
            boolean hasItem = tag.getBoolean("HasBaseItem");
            ipsis.woot.Woot.LOGGER.info("[{}] loadAdditional called - HasBaseItem: {}",
                level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN", hasItem);

            if (hasItem) {
                this.baseItem = ItemStack.parseOptional(registries, tag.getCompound("BaseItem"));
                ipsis.woot.Woot.LOGGER.info("[{}] Loaded BaseItem from tag: {}",
                    level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN", this.baseItem);
            } else {
                this.baseItem = ItemStack.EMPTY;
                ipsis.woot.Woot.LOGGER.info("[{}] HasBaseItem=false, setting to EMPTY",
                    level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN");
            }
        } else {
            // Fallback for old saves that used the old format
            ipsis.woot.Woot.LOGGER.info("[{}] loadAdditional called - old save format, tag contains BaseItem: {}",
                level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN",
                tag.contains("BaseItem"));

            if (tag.contains("BaseItem")) {
                this.baseItem = ItemStack.parseOptional(registries, tag.getCompound("BaseItem"));
                ipsis.woot.Woot.LOGGER.info("[{}] Loaded BaseItem from tag (old format): {}",
                    level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN", this.baseItem);
            } else {
                this.baseItem = ItemStack.EMPTY;
                ipsis.woot.Woot.LOGGER.info("[{}] No data in tag, setting to EMPTY",
                    level != null ? (level.isClientSide ? "CLIENT" : "SERVER") : "UNKNOWN");
            }
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
