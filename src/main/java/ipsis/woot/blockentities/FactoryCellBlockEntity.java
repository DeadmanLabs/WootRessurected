package ipsis.woot.blockentities;

import ipsis.woot.blocks.FactoryCellBlock;
import ipsis.woot.farmblocks.FactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlueProvider;
import ipsis.woot.power.FactoryEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory Cell Block Entity - Power Storage
 * Stores RF/FE energy for the multiblock factory
 * Phase 1: Basic structure, will be expanded in Phase 2 with heart aggregation
 */
public class FactoryCellBlockEntity extends BlockEntity implements IFactoryGlueProvider {

    // Factory glue for communication
    private final FactoryGlue factoryGlue = new FactoryGlue(IFactoryGlue.FactoryBlockType.CELL);

    // Cell tier levels with their energy capacities
    public static final int TIER_I_CAPACITY = 100000;      // 100K RF
    public static final int TIER_II_CAPACITY = 500000;     // 500K RF
    public static final int TIER_III_CAPACITY = 2500000;   // 2.5M RF
    public static final int TIER_IV_CAPACITY = 10000000;   // 10M RF

    // Energy storage
    private FactoryEnergyStorage energyStorage;

    // Cell tier (1-4)
    private int tier = 1;

    // Reference to the factory heart (will be set during structure validation in Phase 2)
    @Nullable
    private BlockPos heartPos = null;

    // Client-side tracking of formed state for visual updates
    private boolean isClientFormed = false;

    public FactoryCellBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, 1); // Default to tier 1
    }

    public FactoryCellBlockEntity(BlockPos pos, BlockState state, int tier) {
        super(WootBlockEntities.FACTORY_CELL.get(), pos, state);
        this.tier = tier;
        this.energyStorage = createEnergyStorage(tier);
    }

    /**
     * Create energy storage based on tier
     */
    private FactoryEnergyStorage createEnergyStorage(int tier) {
        int capacity = getCapacityForTier(tier);
        int maxTransfer = Math.min(10000, capacity / 10); // 10% of capacity or 10K RF/t
        return new FactoryEnergyStorage(capacity, maxTransfer, maxTransfer); // Can receive and extract
    }

    /**
     * Get the capacity for a specific tier
     */
    public static int getCapacityForTier(int tier) {
        return switch (tier) {
            case 1 -> TIER_I_CAPACITY;
            case 2 -> TIER_II_CAPACITY;
            case 3 -> TIER_III_CAPACITY;
            case 4 -> TIER_IV_CAPACITY;
            default -> TIER_I_CAPACITY;
        };
    }

    /**
     * Server tick - main update loop
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, FactoryCellBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        // Update formed state in blockstate if it changed
        boolean currentFormed = state.getValue(FactoryCellBlock.FORMED);
        boolean shouldBeFormed = blockEntity.isFormed();

        if (currentFormed != shouldBeFormed) {
            level.setBlock(pos, state.setValue(FactoryCellBlock.FORMED, shouldBeFormed), 3);
            blockEntity.sync();
        }

        // Phase 2 will add: heart communication, energy aggregation
    }

    /**
     * Get the cell tier (1-4)
     */
    public int getTier() {
        return tier;
    }

    /**
     * Set the cell tier and recreate energy storage
     */
    public void setTier(int tier) {
        if (this.tier != tier) {
            int oldEnergy = energyStorage.getEnergyStored();
            this.tier = tier;
            this.energyStorage = createEnergyStorage(tier);

            // Try to preserve energy when changing tier
            if (oldEnergy > 0) {
                energyStorage.setEnergy(Math.min(oldEnergy, energyStorage.getMaxEnergyStored()));
            }

            setChanged();
        }
    }

    /**
     * Get the energy storage capability
     */
    @Nonnull
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
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
        if (level == null) {
            return null;
        }

        // Use the FactoryGlue's master position (set by FarmStructure)
        BlockPos masterPos = factoryGlue.getMaster();
        if (masterPos == null) {
            // Fallback to heartPos for backwards compatibility
            masterPos = heartPos;
        }

        if (masterPos == null) {
            return null;
        }

        BlockEntity be = level.getBlockEntity(masterPos);
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

    /**
     * Get energy stored
     */
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    /**
     * Get max energy capacity
     */
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    /**
     * Get fill percentage (0.0 to 1.0)
     */
    public float getFillPercentage() {
        return energyStorage.getFillPercentage();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("Tier", tier);
        tag.putInt("Energy", energyStorage.getEnergyStored());

        // Save heart position
        if (heartPos != null) {
            tag.putLong("HeartPos", heartPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Tier")) {
            this.tier = tag.getInt("Tier");
            this.energyStorage = createEnergyStorage(tier);
        }

        if (tag.contains("Energy")) {
            energyStorage.setEnergy(tag.getInt("Energy"));
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

    @Override
    @Nonnull
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("IsFormed", isFormed());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        return tag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains("IsFormed")) {
            isClientFormed = tag.getBoolean("IsFormed");
        }
        if (tag.contains("Energy")) {
            energyStorage.setEnergy(tag.getInt("Energy"));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
