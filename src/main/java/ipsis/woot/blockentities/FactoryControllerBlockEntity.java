package ipsis.woot.blockentities;

import ipsis.woot.Woot;
import ipsis.woot.items.data.EnderShardData;
import ipsis.woot.farmblocks.FactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlueProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory Controller Block Entity - Programming and UI Point
 * Players interact with this to program the factory with ender shards
 * Phase 1: Basic structure, will be expanded in Phase 6 with GUI
 */
public class FactoryControllerBlockEntity extends BlockEntity implements IFactoryGlueProvider {

    // Factory glue for communication
    private final FactoryGlue factoryGlue = new FactoryGlue(IFactoryGlue.FactoryBlockType.CONTROLLER);

    // Programmed mob data (from ender shard)
    @Nullable
    private EnderShardData programmedMob = null;

    // Reference to the factory heart (will be set during structure validation in Phase 2)
    @Nullable
    private BlockPos heartPos = null;

    public FactoryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.FACTORY_CONTROLLER.get(), pos, state);
    }

    /**
     * Server tick - main update loop
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, FactoryControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        // Phase 2 will add: heart communication, structure validation checks
    }

    /**
     * Check if the controller has been programmed with a mob
     */
    public boolean isProgrammed() {
        return programmedMob != null;
    }

    /**
     * Get the programmed mob data
     */
    @Nullable
    public EnderShardData getProgrammedMob() {
        return programmedMob;
    }

    /**
     * Get the mob display name for UI
     */
    @Nonnull
    public Component getMobDisplayName() {
        if (programmedMob == null) {
            return Component.translatable("woot.controller.no_mob");
        }
        return Component.literal(programmedMob.displayName());
    }

    /**
     * Program the controller with mob data from an ender shard
     * Returns true if programming was successful
     */
    public boolean programFromShard(@Nonnull EnderShardData shardData) {
        if (!shardData.isValid()) {
            return false;
        }

        this.programmedMob = shardData;
        setChanged();
        sync();

        Woot.LOGGER.debug("Controller at {} programmed with mob: {}",
            worldPosition, shardData.displayName());
        return true;
    }

    /**
     * Clear the programmed mob data
     */
    public void clearProgramming() {
        if (programmedMob != null) {
            Woot.LOGGER.debug("Controller at {} programming cleared", worldPosition);
            this.programmedMob = null;
            setChanged();
            sync();
        }
    }

    /**
     * Set the heart position (called during structure formation in Phase 2)
     */
    public void setHeartPos(@Nullable BlockPos pos) {
        this.heartPos = pos;
        setChanged();
    }

    /**
     * Get the heart position
     */
    @Nullable
    public BlockPos getHeartPos() {
        return heartPos;
    }

    /**
     * Get the heart block entity if available
     */
    @Nullable
    public FactoryHeartBlockEntity getHeart() {
        if (level == null || heartPos == null) {
            return null;
        }

        BlockEntity be = level.getBlockEntity(heartPos);
        if (be instanceof FactoryHeartBlockEntity heart) {
            return heart;
        }

        return null;
    }

    /**
     * Check if connected to a formed multiblock
     */
    public boolean isFormed() {
        FactoryHeartBlockEntity heart = getHeart();
        return heart != null && heart.isFormed();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        // Save programmed mob data
        if (programmedMob != null) {
            CompoundTag mobTag = new CompoundTag();
            mobTag.putString("EntityKey", programmedMob.entityKey());
            mobTag.putString("DisplayName", programmedMob.displayName());
            mobTag.putInt("DeathCount", programmedMob.deathCount());
            mobTag.putString("Tag", programmedMob.tag());
            tag.put("ProgrammedMob", mobTag);
        }

        // Save heart position
        if (heartPos != null) {
            tag.putLong("HeartPos", heartPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Load programmed mob data
        if (tag.contains("ProgrammedMob")) {
            CompoundTag mobTag = tag.getCompound("ProgrammedMob");
            String entityKey = mobTag.getString("EntityKey");
            String displayName = mobTag.getString("DisplayName");
            int deathCount = mobTag.getInt("DeathCount");
            String tagStr = mobTag.getString("Tag");
            this.programmedMob = new EnderShardData(entityKey, displayName, deathCount, tagStr);
        } else {
            this.programmedMob = null;
        }

        // Load heart position
        if (tag.contains("HeartPos")) {
            this.heartPos = BlockPos.of(tag.getLong("HeartPos"));
        } else {
            this.heartPos = null;
        }
    }

    /**
     * Get the factory glue instance
     */
    @Override
    @Nonnull
    public IFactoryGlue getFactoryGlue() {
        return factoryGlue;
    }

    /**
     * Mark dirty and sync to clients
     */
    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
