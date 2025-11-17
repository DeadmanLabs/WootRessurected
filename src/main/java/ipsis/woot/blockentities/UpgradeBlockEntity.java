package ipsis.woot.blockentities;

import ipsis.woot.blocks.UpgradeBlock;
import ipsis.woot.farmblocks.FactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlueProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * BlockEntity for upgrade blocks
 * Tracks linking to master factory and syncs formed state to client
 */
public class UpgradeBlockEntity extends BlockEntity implements IFactoryGlueProvider {

    private final FactoryGlue factoryGlue = new FactoryGlue(IFactoryGlue.FactoryBlockType.UPGRADE);

    public UpgradeBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.UPGRADE.get(), pos, state);
    }

    @Override
    @Nonnull
    public IFactoryGlue getFactoryGlue() {
        return factoryGlue;
    }

    /**
     * Check if this upgrade is part of a formed multiblock
     */
    public boolean isFormed() {
        return factoryGlue.hasValidMaster();
    }

    /**
     * Server tick - updates FORMED blockstate based on master connection
     */
    public static void serverTick(@Nonnull ServerLevel level, @Nonnull BlockPos pos,
                                   @Nonnull BlockState state, @Nonnull UpgradeBlockEntity blockEntity) {
        // Update blockstate FORMED property to match master link status
        boolean currentFormed = state.getValue(UpgradeBlock.FORMED);
        boolean shouldBeFormed = blockEntity.isFormed();

        if (currentFormed != shouldBeFormed) {
            BlockState newState = state.setValue(UpgradeBlock.FORMED, shouldBeFormed);
            // Use flag 11 to ensure client receives update and refreshes model
            // 11 = 1 (notify neighbors) | 2 (send to clients) | 8 (force re-render)
            level.setBlock(pos, newState, 11);
            // Explicitly sync BlockEntity data to client
            blockEntity.sync();
        }
    }

    /**
     * Sync block entity data to client
     */
    public void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
            requestModelDataUpdate();
        }
    }

    // ========== NBT Serialization ==========

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Save master position
        BlockPos master = factoryGlue.getMaster();
        if (master != null) {
            tag.put("master", NbtUtils.writeBlockPos(master));
        }
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Load master position
        if (tag.contains("master")) {
            BlockPos master = NbtUtils.readBlockPos(tag, "master").orElse(null);
            factoryGlue.setMaster(master);
        }
    }

    // ========== Client Sync ==========

    @Override
    @Nonnull
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("formed", isFormed());
        return tag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        // Update client-side blockstate to match the formed status from NBT
        if (level != null && level.isClientSide() && tag.contains("formed")) {
            boolean formedFromNBT = tag.getBoolean("formed");
            BlockState currentState = getBlockState();

            if (currentState.getBlock() instanceof ipsis.woot.blocks.UpgradeBlock) {
                boolean currentFormed = currentState.getValue(ipsis.woot.blocks.UpgradeBlock.FORMED);
                if (currentFormed != formedFromNBT) {
                    BlockState newState = currentState.setValue(ipsis.woot.blocks.UpgradeBlock.FORMED, formedFromNBT);
                    level.setBlock(worldPosition, newState, 11);
                    // Force chunk section to re-render
                    level.sendBlockUpdated(worldPosition, currentState, newState, 11);
                }
            }
            requestModelDataUpdate();
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@Nonnull net.minecraft.network.Connection net, @Nonnull ClientboundBlockEntityDataPacket pkt, @Nonnull HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        super.onDataPacket(net, pkt, registries);
        // Update client-side blockstate to match the formed status from packet
        if (level != null && level.isClientSide() && tag != null && tag.contains("formed")) {
            boolean formedFromNBT = tag.getBoolean("formed");
            BlockState currentState = getBlockState();

            if (currentState.getBlock() instanceof ipsis.woot.blocks.UpgradeBlock) {
                boolean currentFormed = currentState.getValue(ipsis.woot.blocks.UpgradeBlock.FORMED);
                if (currentFormed != formedFromNBT) {
                    BlockState newState = currentState.setValue(ipsis.woot.blocks.UpgradeBlock.FORMED, formedFromNBT);
                    level.setBlock(worldPosition, newState, 11);
                    // Force chunk section to re-render
                    level.sendBlockUpdated(worldPosition, currentState, newState, 11);
                }
            }
            requestModelDataUpdate();
        }
    }
}
