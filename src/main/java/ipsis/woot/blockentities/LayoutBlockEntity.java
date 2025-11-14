package ipsis.woot.blockentities;

import ipsis.woot.multiblock.EnumMobFactoryTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Block entity for Layout block
 * Stores the selected factory tier for ghost block rendering
 */
public class LayoutBlockEntity extends BlockEntity {

    private EnumMobFactoryTier selectedTier = EnumMobFactoryTier.TIER_I;

    public LayoutBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.LAYOUT.get(), pos, state);
    }

    /**
     * Get the currently selected tier
     */
    @Nonnull
    public EnumMobFactoryTier getSelectedTier() {
        return selectedTier;
    }

    /**
     * Set the selected tier
     */
    public void setSelectedTier(@Nonnull EnumMobFactoryTier tier) {
        this.selectedTier = tier;
        setChanged();

        // Sync to client for rendering
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Cycle to the next tier (wraps from IV back to I)
     */
    public void cycleNextTier() {
        int currentLevel = selectedTier.getLevel();
        int nextLevel = currentLevel + 1;
        if (nextLevel > 4) {
            nextLevel = 1; // Wrap back to Tier I
        }
        selectedTier = EnumMobFactoryTier.byLevel(nextLevel);
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Cycle to the previous tier
     */
    public void cyclePreviousTier() {
        int currentLevel = selectedTier.getLevel();
        int prevLevel = currentLevel - 1;
        if (prevLevel < 1) {
            prevLevel = 4;
        }
        selectedTier = EnumMobFactoryTier.byLevel(prevLevel);
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("SelectedTier", selectedTier.getLevel());
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("SelectedTier")) {
            selectedTier = EnumMobFactoryTier.byLevel(tag.getInt("SelectedTier"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("SelectedTier", selectedTier.getLevel());
        return tag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        loadAdditional(tag, registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LayoutBlockEntity blockEntity) {
        // No server-side ticking needed for layout block
    }
}
