package ipsis.woot.farmstructure;

import ipsis.woot.farming.EnumFarmUpgrade;
import ipsis.woot.items.data.EnderShardData;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import ipsis.woot.power.FactoryEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the configuration of a formed multiblock structure
 * Phase 2: Basic structure - tier, mob, energy, cell positions
 * Phase 3+: Will add upgrades, import/export, etc.
 */
public class FarmSetup {

    private final Level level;
    private final EnumMobFactoryTier tier;
    private final EnderShardData programmedMob;

    // Block positions
    private final BlockPos heartPos;
    private final List<BlockPos> controllerPositions = new ArrayList<>();
    private final List<BlockPos> cellPositions = new ArrayList<>();
    private final List<BlockPos> importerPositions = new ArrayList<>();
    private final List<BlockPos> exporterPositions = new ArrayList<>();

    // Aggregated energy storage from all cells
    private FactoryEnergyStorage aggregatedEnergy;

    // Upgrade tracking - maps upgrade type to tier level (1-3)
    private final Map<EnumFarmUpgrade, Integer> upgrades = new HashMap<>();

    public FarmSetup(@Nonnull Level level,
                     @Nonnull BlockPos heartPos,
                     @Nonnull EnumMobFactoryTier tier,
                     @Nullable EnderShardData programmedMob) {
        this.level = level;
        this.heartPos = heartPos;
        this.tier = tier;
        this.programmedMob = programmedMob;
    }

    /**
     * Get the world level
     */
    @Nonnull
    public Level getLevel() {
        return level;
    }

    /**
     * Get the heart position
     */
    @Nonnull
    public BlockPos getHeartPos() {
        return heartPos;
    }

    /**
     * Get the factory tier
     */
    @Nonnull
    public EnumMobFactoryTier getTier() {
        return tier;
    }

    /**
     * Check if the factory is programmed with a mob
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
     * Get the mob name for display
     */
    @Nonnull
    public String getMobName() {
        return programmedMob != null ? programmedMob.displayName() : "Not Programmed";
    }

    /**
     * Add a controller position
     */
    public void addControllerPosition(BlockPos pos) {
        controllerPositions.add(pos);
    }

    /**
     * Add a cell position
     */
    public void addCellPosition(BlockPos pos) {
        cellPositions.add(pos);
    }

    /**
     * Get all controller positions
     */
    @Nonnull
    public List<BlockPos> getControllerPositions() {
        return controllerPositions;
    }

    /**
     * Get all cell positions
     */
    @Nonnull
    public List<BlockPos> getCellPositions() {
        return cellPositions;
    }

    /**
     * Add an importer position
     */
    public void addImporterPosition(BlockPos pos) {
        importerPositions.add(pos);
    }

    /**
     * Add an exporter position
     */
    public void addExporterPosition(BlockPos pos) {
        exporterPositions.add(pos);
    }

    /**
     * Get all importer positions
     */
    @Nonnull
    public List<BlockPos> getImporterPositions() {
        return importerPositions;
    }

    /**
     * Get all exporter positions
     */
    @Nonnull
    public List<BlockPos> getExporterPositions() {
        return exporterPositions;
    }

    /**
     * Set the aggregated energy storage
     */
    public void setAggregatedEnergy(FactoryEnergyStorage energy) {
        this.aggregatedEnergy = energy;
    }

    /**
     * Get the aggregated energy storage
     */
    @Nullable
    public FactoryEnergyStorage getAggregatedEnergy() {
        return aggregatedEnergy;
    }

    /**
     * Get total energy stored across all cells
     */
    public int getTotalEnergyStored() {
        return aggregatedEnergy != null ? aggregatedEnergy.getEnergyStored() : 0;
    }

    /**
     * Get total energy capacity across all cells
     */
    public int getTotalEnergyCapacity() {
        return aggregatedEnergy != null ? aggregatedEnergy.getMaxEnergyStored() : 0;
    }

    // ========== UPGRADE SYSTEM ==========

    /**
     * Add an upgrade to the factory
     * @param type Upgrade type
     * @param tier Upgrade tier (1-3)
     */
    public void addUpgrade(@Nonnull EnumFarmUpgrade type, int tier) {
        upgrades.put(type, tier);
    }

    /**
     * Get the tier level of a specific upgrade
     * @param type Upgrade type
     * @return Tier level (1-3), or 0 if not installed
     */
    public int getUpgradeLevel(@Nonnull EnumFarmUpgrade type) {
        return upgrades.getOrDefault(type, 0);
    }

    /**
     * Check if an upgrade is installed
     */
    public boolean hasUpgrade(@Nonnull EnumFarmUpgrade type) {
        return upgrades.containsKey(type);
    }

    /**
     * Get all installed upgrades
     */
    @Nonnull
    public Map<EnumFarmUpgrade, Integer> getUpgrades() {
        return upgrades;
    }

    /**
     * Get efficiency multiplier from efficiency upgrade
     * @return Multiplier (1.0 = no reduction, 0.7 = 30% reduction)
     */
    public float getEfficiencyMultiplier() {
        int tier = getUpgradeLevel(EnumFarmUpgrade.EFFICIENCY);
        return tier > 0 ? EnumFarmUpgrade.EFFICIENCY.getEfficiencyMultiplier(tier) : 1.0f;
    }

    /**
     * Get looting level from looting upgrade
     * @return Looting level (0-3)
     */
    public int getLootingLevel() {
        int tier = getUpgradeLevel(EnumFarmUpgrade.LOOTING);
        return tier > 0 ? EnumFarmUpgrade.LOOTING.getLootingLevel(tier) : 0;
    }

    /**
     * Get mass spawn count from mass upgrade
     * @return Number of mobs to spawn per cycle (1, 4, 6, or 8)
     */
    public int getMassSpawnCount() {
        int tier = getUpgradeLevel(EnumFarmUpgrade.MASS);
        return tier > 0 ? EnumFarmUpgrade.MASS.getMassCount(tier) : 1;
    }

    /**
     * Get spawn rate ticks from rate upgrade
     * @param baseTicks Base tick duration (default 320)
     * @return Adjusted tick duration (40, 80, 160, or baseTicks)
     */
    public int getSpawnRateTicks(int baseTicks) {
        int tier = getUpgradeLevel(EnumFarmUpgrade.RATE);
        return tier > 0 ? EnumFarmUpgrade.RATE.getSpawnRateTicks(tier, baseTicks) : baseTicks;
    }

    /**
     * Get XP multiplier from XP upgrade
     * @return Multiplier (1.0, 1.2, 1.4, or 1.8)
     */
    public float getXPMultiplier() {
        int tier = getUpgradeLevel(EnumFarmUpgrade.XP);
        return tier > 0 ? EnumFarmUpgrade.XP.getXPMultiplier(tier) : 1.0f;
    }

    /**
     * Get decapitate (head drop) chance from decapitate upgrade
     * @return Chance as a value from 0.0 to 1.0
     */
    public float getDecapitateChance() {
        int tier = getUpgradeLevel(EnumFarmUpgrade.DECAPITATE);
        return tier > 0 ? EnumFarmUpgrade.DECAPITATE.getDecapitateChance(tier) : 0.0f;
    }

    /**
     * Get total additional power cost from all upgrades
     * @return Total additional RF/tick from upgrades
     */
    public int getTotalUpgradePowerCost() {
        int total = 0;
        for (Map.Entry<EnumFarmUpgrade, Integer> entry : upgrades.entrySet()) {
            total += entry.getKey().getPowerCostPerTick(entry.getValue());
        }
        return total;
    }

    @Override
    public String toString() {
        return String.format("FarmSetup{tier=%s, mob=%s, cells=%d, controllers=%d, energy=%d/%d, upgrades=%d}",
            tier, getMobName(), cellPositions.size(), controllerPositions.size(),
            getTotalEnergyStored(), getTotalEnergyCapacity(), upgrades.size());
    }
}
