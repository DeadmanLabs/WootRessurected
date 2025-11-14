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
import ipsis.woot.util.LootHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private boolean isRunning = false;

    // Drop tracking for GUI (last spawn cycle)
    private List<ItemStack> lastDrops = new ArrayList<>();

    // Drop learning system (cumulative statistics)
    private int totalSamples = 0; // Total mobs spawned
    private Map<String, Integer> dropStatistics = new HashMap<>(); // Item registry name -> count

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

        // Update aggregated energy every 20 ticks for GUI display
        if (blockEntity.tickCounter % 20 == 0) {
            blockEntity.updateAggregatedEnergy();
        }

        // Debug logging
        if (blockEntity.tickCounter % 100 == 0) {
            boolean formed = blockEntity.isFormed();
            boolean canProcess = blockEntity.canProcess();
            if (formed) {
                Woot.LOGGER.info("Factory Heart at {} - Formed: {}, Programmed: {}, CanProcess: {}, Running: {}, Power: {}/{}, Energy: {}/{}",
                    pos, formed,
                    blockEntity.farmSetup != null && blockEntity.farmSetup.isProgrammed(),
                    canProcess,
                    blockEntity.isRunning,
                    blockEntity.consumedPower, blockEntity.powerRecipe.getTotalPower(),
                    blockEntity.energyStorage.getEnergyStored(), blockEntity.energyStorage.getMaxEnergyStored());

                // Extra debug if not processing
                if (!canProcess) {
                    if (blockEntity.farmSetup == null) {
                        Woot.LOGGER.warn("  -> Cannot process: farmSetup is null");
                    } else if (!blockEntity.farmSetup.isProgrammed()) {
                        Woot.LOGGER.warn("  -> Cannot process: not programmed with mob");
                    } else if (blockEntity.energyStorage.getEnergyStored() <= 0) {
                        Woot.LOGGER.warn("  -> Cannot process: no energy stored");
                    }
                }
            }
        }

        // Factory processing logic
        if (blockEntity.canProcess()) {
            blockEntity.process((ServerLevel) level);
        } else {
            blockEntity.stopProcessing();
        }
    }

    /**
     * Update aggregated energy from cells for GUI display
     */
    private void updateAggregatedEnergy() {
        if (farmSetup == null) {
            return;
        }

        int totalCapacity = 0;
        int totalStored = 0;

        for (BlockPos cellPos : farmSetup.getCellPositions()) {
            BlockEntity be = level.getBlockEntity(cellPos);
            if (be instanceof FactoryCellBlockEntity cell) {
                totalCapacity += cell.getMaxEnergyStored();
                totalStored += cell.getEnergyStored();
            }
        }

        // Recreate aggregated energy storage with current values
        if (totalCapacity > 0) {
            energyStorage = new FactoryEnergyStorage(totalCapacity, totalCapacity / 10, 0, totalStored);
        }
    }

    /**
     * Check if the factory can process
     */
    private boolean canProcess() {
        if (farmSetup == null || !farmSetup.isProgrammed()) {
            return false;
        }

        // Check if we have stored energy available
        return energyStorage.getEnergyStored() > 0;
    }

    /**
     * Process one tick of factory operation
     */
    private void process(ServerLevel level) {
        isRunning = true;

        // Try to consume power
        long totalPower = powerRecipe.getTotalPower();
        long remainingPower = totalPower - consumedPower;

        if (remainingPower > 0) {
            // Still need more power - consume per tick
            int powerPerTick = powerRecipe.getPowerPerTick();
            int extracted = extractEnergyFromCells(powerPerTick);

            if (extracted > 0) {
                consumedPower += extracted;
                setChanged();
            }
        }

        // Check if we've consumed enough power to complete a spawn cycle
        if (consumedPower >= totalPower) {
            completeSpawnCycle(level);
        }
    }

    /**
     * Extract energy from the physical cell blocks
     */
    private int extractEnergyFromCells(int amount) {
        if (farmSetup == null) {
            return 0;
        }

        int remaining = amount;
        List<BlockPos> cellPositions = farmSetup.getCellPositions();

        // Extract from cells until we have enough or run out
        for (BlockPos cellPos : cellPositions) {
            if (remaining <= 0) {
                break;
            }

            BlockEntity be = level.getBlockEntity(cellPos);
            if (be instanceof FactoryCellBlockEntity cell) {
                IEnergyStorage cellStorage = cell.getEnergyStorage();
                int extracted = cellStorage.extractEnergy(remaining, false);
                remaining -= extracted;

                if (extracted > 0) {
                    cell.setChanged();
                }
            }
        }

        return amount - remaining;
    }

    /**
     * Complete a spawn cycle - generate loot and output to exporters
     */
    private void completeSpawnCycle(ServerLevel level) {
        // Get the mob entity type
        EntityType<?> entityType = getEntityType();
        if (entityType == null) {
            Woot.LOGGER.warn("Cannot spawn - invalid entity type");
            resetProgress();
            return;
        }

        int mobCount = getMobCount();

        // Get and consume spawn ingredients
        ipsis.woot.recipes.SpawnRecipe recipe = getSpawnRecipe();
        if (recipe != null && !recipe.isEmpty()) {
            boolean consumed = ipsis.woot.recipes.SpawnRecipeConsumer.consume(
                level,
                farmSetup.getImporterPositions(),
                recipe,
                mobCount,
                false // Actually consume
            );

            if (!consumed) {
                Woot.LOGGER.warn("Failed to consume spawn ingredients, aborting spawn");
                resetProgress();
                return;
            }
        }

        // Generate loot from mob loot tables
        List<ItemStack> drops = LootHelper.generateLoot(level, entityType, 0, mobCount);
        List<ItemStack> mergedDrops = LootHelper.mergeItemStacks(drops);

        Woot.LOGGER.info("Factory completed spawn cycle: {} × {} mobs, {} unique drops",
            farmSetup.getProgrammedMob().displayName(), mobCount, mergedDrops.size());

        // Record drops to learning system (cumulative statistics)
        recordDrops(mergedDrops, mobCount);

        // Update GUI display to show cumulative averages
        lastDrops = calculateDropsWithPercentages();

        // Output drops to exporters
        outputDrops(mergedDrops);

        // Reset progress for next cycle
        resetProgress();
    }

    /**
     * Output drops to exporter blocks
     */
    private void outputDrops(List<ItemStack> drops) {
        if (farmSetup == null) {
            return;
        }

        List<BlockPos> exporterPositions = farmSetup.getExporterPositions();
        if (exporterPositions.isEmpty()) {
            Woot.LOGGER.warn("No exporters found - dropping items at heart");
            return;
        }

        // Try to insert items into exporters
        for (ItemStack drop : drops) {
            ItemStack remaining = drop.copy();

            for (BlockPos exporterPos : exporterPositions) {
                if (remaining.isEmpty()) {
                    break;
                }

                BlockEntity be = level.getBlockEntity(exporterPos);
                if (be instanceof ExporterBlockEntity exporter) {
                    remaining = exporter.insertItem(remaining, false);
                }
            }

            // If there's still items remaining, log a warning
            if (!remaining.isEmpty()) {
                Woot.LOGGER.warn("Could not output {} × {} - exporters full",
                    remaining.getCount(), remaining.getDisplayName().getString());
            }
        }

        setChanged();
    }

    /**
     * Record drops to the learning system
     * Tracks cumulative statistics for drop chance calculation
     */
    private void recordDrops(List<ItemStack> drops, int mobCount) {
        // Increment total samples by the number of mobs spawned
        totalSamples += mobCount;

        // Record each item drop
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }

            // Use item registry name as key for tracking
            String itemKey = BuiltInRegistries.ITEM.getKey(drop.getItem()).toString();

            // Increment drop count
            int currentCount = dropStatistics.getOrDefault(itemKey, 0);
            dropStatistics.put(itemKey, currentCount + drop.getCount());
        }

        setChanged();
    }

    /**
     * Calculate drop percentages based on cumulative statistics
     * Returns a list of ItemStacks with their calculated drop chances
     */
    private List<ItemStack> calculateDropsWithPercentages() {
        List<ItemStack> result = new ArrayList<>();

        if (totalSamples == 0) {
            return result;
        }

        // Convert drop statistics to ItemStacks with averaged counts
        for (Map.Entry<String, Integer> entry : dropStatistics.entrySet()) {
            String itemKey = entry.getKey();
            int totalDropped = entry.getValue();

            // Get the item from registry
            ResourceLocation itemId = ResourceLocation.parse(itemKey);
            var item = BuiltInRegistries.ITEM.get(itemId);
            if (item == null) {
                continue;
            }

            // Calculate average drop per sample (mob)
            // This represents the expected average drop amount
            int averageCount = Math.max(1, totalDropped / totalSamples);

            ItemStack stack = new ItemStack(item, averageCount);
            result.add(stack);
        }

        return result;
    }

    /**
     * Get drop chance percentage for a specific item
     * Used by GUI for tooltip display
     */
    public float getDropChance(ItemStack itemStack) {
        if (totalSamples == 0 || itemStack.isEmpty()) {
            return 0.0f;
        }

        String itemKey = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
        int totalDropped = dropStatistics.getOrDefault(itemKey, 0);

        if (totalDropped == 0) {
            return 0.0f;
        }

        // Calculate how many times this item appeared (regardless of stack size)
        // For simplicity, we'll approximate: if we got 60 gunpowder total from 100 creepers,
        // we'll say it dropped ~60% of the time (this is an approximation)
        float dropChance = Math.min(100.0f, (totalDropped / (float) totalSamples) * 100.0f);
        return Math.max(1.0f, dropChance); // Minimum 1%
    }

    /**
     * Get the entity type from the programmed mob
     */
    @Nullable
    private EntityType<?> getEntityType() {
        if (farmSetup == null || !farmSetup.isProgrammed()) {
            return null;
        }

        String entityKey = farmSetup.getProgrammedMob().entityKey();
        ResourceLocation entityId = ResourceLocation.parse(entityKey);
        return BuiltInRegistries.ENTITY_TYPE.get(entityId);
    }

    /**
     * Reset progress for next cycle
     */
    private void resetProgress() {
        consumedPower = 0;
        setChanged();
    }

    /**
     * Stop processing
     */
    private void stopProcessing() {
        if (isRunning) {
            isRunning = false;
            setChanged();
        }
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

                // Update power recipe based on tier (matches original Woot)
                int tierLevel = farmSetup.getTier().getLevel(); // 1-4
                this.powerRecipe = PowerRecipe.forTier(tierLevel, 320);
                Woot.LOGGER.info("Farm setup updated: {} - Power recipe: {}", farmSetup, powerRecipe);
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

        // Save learning system statistics
        tag.putInt("TotalSamples", totalSamples);

        CompoundTag statsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : dropStatistics.entrySet()) {
            statsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("DropStatistics", statsTag);
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

        // Load learning system statistics
        if (tag.contains("TotalSamples")) {
            totalSamples = tag.getInt("TotalSamples");
        }

        if (tag.contains("DropStatistics")) {
            dropStatistics.clear();
            CompoundTag statsTag = tag.getCompound("DropStatistics");
            for (String key : statsTag.getAllKeys()) {
                dropStatistics.put(key, statsTag.getInt(key));
            }
            // Update GUI display with loaded statistics
            lastDrops = calculateDropsWithPercentages();
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
        return isRunning;
    }

    /**
     * Get spawn recipe for the programmed mob
     */
    @Nullable
    private ipsis.woot.recipes.SpawnRecipe getSpawnRecipe() {
        if (farmSetup == null || !farmSetup.isProgrammed()) {
            return null;
        }

        String mobKey = farmSetup.getProgrammedMob().entityKey();
        return Woot.SPAWN_RECIPE_REPOSITORY.get(mobKey);
    }

    /**
     * Check if missing required ingredients
     */
    public boolean hasMissingIngredients() {
        if (farmSetup == null || !farmSetup.isProgrammed()) {
            return false;
        }

        if (level == null || level.isClientSide()) {
            return false;
        }

        ipsis.woot.recipes.SpawnRecipe recipe = getSpawnRecipe();
        if (recipe == null || recipe.isEmpty()) {
            return false; // No ingredients required
        }

        // Check if ingredients are available
        return !ipsis.woot.recipes.SpawnRecipeConsumer.hasIngredients(
            level,
            farmSetup.getImporterPositions(),
            recipe,
            getMobCount()
        );
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

            // Add drop information (show cumulative learned drops)
            for (ItemStack drop : lastDrops) {
                info.addDrop(drop.copy());
            }

            // Add total samples for drop chance calculation
            info.setTotalSamples(totalSamples);
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
