package ipsis.woot.blockentities;

import ipsis.woot.Woot;
import ipsis.woot.farmblocks.FactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlue;
import ipsis.woot.farmblocks.IFactoryGlueProvider;
import ipsis.woot.farmstructure.FarmSetup;
import ipsis.woot.farmstructure.FarmStructure;
import ipsis.woot.farmstructure.IFarmStructure;
import ipsis.woot.gui.FactoryHeartMenu;
import ipsis.woot.gui.data.FarmUIInfo;
import ipsis.woot.power.FactoryEnergyStorage;
import ipsis.woot.power.PowerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory Heart Block Entity - Main Controller
 * This is the core of the multiblock structure
 * Phase 2: Structure validation and formation
 */
public class FactoryHeartBlockEntity extends BlockEntity implements IFactoryGlueProvider, MenuProvider {

    // Factory glue for communication
    private final FactoryGlue factoryGlue = new FactoryGlue(IFactoryGlue.FactoryBlockType.HEART);

    // Structure validation
    private IFarmStructure farmStructure = null;
    private FarmSetup farmSetup = null;

    // Energy storage - aggregated from cells
    private FactoryEnergyStorage energyStorage;

    // Power recipe - defines requirements for spawning
    private PowerRecipe powerRecipe;

    // Progress tracking
    private long consumedPower = 0;
    private int tickCounter = 0;

    public FactoryHeartBlockEntity(BlockPos pos, BlockState state) {
        super(WootBlockEntities.FACTORY_HEART.get(), pos, state);

        // Default energy storage (will be updated from farmSetup)
        this.energyStorage = new FactoryEnergyStorage(100000, 1000, 0);

        // Default power recipe
        this.powerRecipe = PowerRecipe.createDefault();
    }

    /**
     * Server tick - main update loop
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, FactoryHeartBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        blockEntity.tickCounter++;

        // Initialize farm structure on first tick
        if (blockEntity.farmStructure == null) {
            blockEntity.farmStructure = new FarmStructure()
                .setWorld(level)
                .setPosition(pos);
            blockEntity.farmStructure.setStructureDirty();
        }

        // Tick the structure validator
        blockEntity.farmStructure.tick();

        // Update farm setup if structure changed
        if (blockEntity.farmStructure.hasChanged()) {
            blockEntity.updateFarmSetup();
            blockEntity.farmStructure.clearChanged();
        }

        // Debug logging
        if (blockEntity.tickCounter % 100 == 0) {
            boolean formed = blockEntity.isFormed();
            if (formed) {
                Woot.LOGGER.debug("Factory Heart at {} - Formed: {}, Tier: {}",
                    pos, formed, blockEntity.farmSetup != null ? blockEntity.farmSetup.getTier() : "N/A");
            }
        }

        // Phase 3 will add: power consumption, farming logic, loot generation
    }

    /**
     * Update the farm setup from the structure
     */
    private void updateFarmSetup() {
        if (farmStructure.isFormed()) {
            farmSetup = farmStructure.createSetup();
            if (farmSetup != null) {
                // Update energy storage from aggregated cells
                FactoryEnergyStorage aggregated = farmSetup.getAggregatedEnergy();
                if (aggregated != null) {
                    this.energyStorage = aggregated;
                }
                Woot.LOGGER.info("Farm setup updated: {}", farmSetup);
            }
        } else {
            farmSetup = null;
            // Reset to default energy
            this.energyStorage = new FactoryEnergyStorage(100000, 1000, 0);
        }
        setChanged();
    }

    /**
     * Mark the structure as needing validation
     */
    public void markStructureDirty() {
        if (farmStructure != null) {
            farmStructure.setStructureDirty();
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
     * Get the farm setup
     */
    @Nullable
    public FarmSetup getFarmSetup() {
        return farmSetup;
    }

    /**
     * Get the energy storage capability
     */
    @Nonnull
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    /**
     * Check if the multiblock is formed
     */
    public boolean isFormed() {
        return farmStructure != null && farmStructure.isFormed();
    }

    /**
     * Set the multiblock formed state
     * Used by blocks when broken to invalidate structure
     */
    public void setFormed(boolean formed) {
        if (!formed && farmStructure != null) {
            farmStructure.fullDisconnect();
            farmStructure.setStructureDirty();
        }
    }

    /**
     * Get the current power recipe
     */
    @Nullable
    public PowerRecipe getPowerRecipe() {
        return powerRecipe;
    }

    /**
     * Get consumed power for progress tracking
     */
    public long getConsumedPower() {
        return consumedPower;
    }

    /**
     * Get progress percentage (0-100)
     */
    public int getProgress() {
        if (powerRecipe == null) {
            return 0;
        }
        return powerRecipe.getProgress(consumedPower);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putLong("ConsumedPower", consumedPower);
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Energy")) {
            energyStorage.setEnergy(tag.getInt("Energy"));
        }
        if (tag.contains("ConsumedPower")) {
            consumedPower = tag.getLong("ConsumedPower");
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // Mark structure dirty when loaded to trigger validation
        if (!level.isClientSide() && farmStructure != null) {
            farmStructure.setStructureDirty();
        }
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

    // ========== GUI Data Methods ==========

    /**
     * Get mob count (for display)
     */
    public int getMobCount() {
        // Phase 3 will add mob count configuration
        // For now, default to 1 if programmed
        if (farmSetup != null && farmSetup.isProgrammed()) {
            return 1;
        }
        return 0;
    }

    /**
     * Get total recipe time in ticks
     */
    public int getRecipeTotalTime() {
        if (powerRecipe != null) {
            return powerRecipe.getTicks();
        }
        return 0;
    }

    /**
     * Get power consumed per tick during spawning
     */
    public int getRecipePowerPerTick() {
        if (powerRecipe != null) {
            return powerRecipe.getPowerPerTick();
        }
        return 0;
    }

    /**
     * Check if factory is currently running
     */
    public boolean isRunning() {
        // Will be implemented in Phase 3 (farming logic)
        return false;
    }

    /**
     * Check if missing required ingredients
     */
    public boolean hasMissingIngredients() {
        // Will be implemented in Phase 3 (farming logic)
        return false;
    }

    /**
     * Get stored power in RF
     */
    public int getPowerStored() {
        return energyStorage.getEnergyStored();
    }

    /**
     * Get maximum power capacity in RF
     */
    public int getPowerCapacity() {
        return energyStorage.getMaxEnergyStored();
    }

    /**
     * Get total power required for recipe
     */
    public long getRecipeTotalPower() {
        if (powerRecipe != null) {
            return powerRecipe.getTotalPower();
        }
        return 0;
    }

    /**
     * Check if the factory is valid and functional
     */
    public boolean isValid() {
        return isFormed() && farmSetup != null;
    }

    /**
     * Get UI information for GUI display
     * Called by network packet system
     */
    @Nonnull
    public FarmUIInfo getUIInfo() {
        FarmUIInfo info = new FarmUIInfo();

        if (farmSetup != null) {
            info.setTier(farmSetup.getTier());
            info.setMobCount(getMobCount()); // Use local method

            // Get programmed mob info
            if (farmSetup.getProgrammedMob() != null) {
                // EnderShardData is a record, use displayName() accessor
                info.setMobName(Component.literal(farmSetup.getProgrammedMob().displayName()));
            } else {
                info.setMobName(Component.literal("Not Programmed"));
            }

            // TODO Phase 3: Add ingredient and drop information
            // farmSetup.getRequiredIngredients().forEach(info::addIngredientItem);
            // farmSetup.getDrops().forEach(info::addDrop);
        } else {
            info.setTier(ipsis.woot.multiblock.EnumMobFactoryTier.TIER_I);
            info.setMobName(Component.literal("No Structure"));
        }

        // Recipe information
        if (powerRecipe != null) {
            info.setRecipeTotalPower(powerRecipe.getTotalPower());
            info.setRecipeTotalTime(powerRecipe.getTicks()); // Use getTicks() not getTotalTime()
            info.setRecipePowerPerTick(powerRecipe.getPowerPerTick());
        }

        // Current state
        info.setRunning(isRunning());
        info.setConsumedPower(consumedPower);
        info.setMissingIngredients(hasMissingIngredients());

        // Power storage
        info.setPowerStored(energyStorage.getEnergyStored());
        info.setPowerCapacity(energyStorage.getMaxEnergyStored());

        // Validity
        info.setValid(isValid());

        return info;
    }

    // ========== MenuProvider Implementation ==========

    @Override
    @Nonnull
    public Component getDisplayName() {
        return Component.translatable("container.woot.factory_heart");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory playerInventory, @Nonnull Player player) {
        return new FactoryHeartMenu(containerId, playerInventory, worldPosition, FactoryHeartMenu.createDataProvider(this));
    }
}
