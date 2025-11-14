package ipsis.woot.farmstructure;

import ipsis.woot.items.data.EnderShardData;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import ipsis.woot.power.FactoryEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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

    // Aggregated energy storage from all cells
    private FactoryEnergyStorage aggregatedEnergy;

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

    @Override
    public String toString() {
        return String.format("FarmSetup{tier=%s, mob=%s, cells=%d, controllers=%d, energy=%d/%d}",
            tier, getMobName(), cellPositions.size(), controllerPositions.size(),
            getTotalEnergyStored(), getTotalEnergyCapacity());
    }
}
