package ipsis.woot.farmstructure;

import ipsis.woot.Woot;
import ipsis.woot.blockentities.FactoryCellBlockEntity;
import ipsis.woot.farmblocks.IFactoryGlueProvider;
import ipsis.woot.power.FactoryEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages multiblock structure validation and formation
 * Phase 2: Basic structure validation
 */
public class FarmStructure implements IFarmStructure {

    private Level level;
    private BlockPos origin;

    private boolean structureDirty = false;
    private boolean changed = false;

    @Nullable
    private FarmScanner.ScannedFarm currentFarm = null;

    private final FarmScanner scanner = new FarmScanner();

    public FarmStructure() {
    }

    @Override
    public void setStructureDirty() {
        this.structureDirty = true;
    }

    @Override
    public IFarmStructure setWorld(@Nonnull Level level) {
        this.level = level;
        return this;
    }

    @Override
    public IFarmStructure setPosition(BlockPos origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public void tick() {
        if (level == null || origin == null) {
            return;
        }

        // Only validate when marked dirty (no periodic validation)
        if (structureDirty) {
            handleValidation();
            structureDirty = false;
        }
    }

    /**
     * Perform structure validation
     */
    private void handleValidation() {
        FarmScanner.ScannedFarm scannedFarm = scanner.scanFarm(level, origin);

        if (currentFarm == null && scannedFarm == null) {
            // No change - still invalid
        } else if (currentFarm == null && scannedFarm != null) {
            // New farm formed!
            Woot.LOGGER.info("Multiblock formed at {} - Tier: {}", origin, scannedFarm.getTier());
            connectNewFarm(scannedFarm);
            currentFarm = scannedFarm;
            changed = true;
        } else if (currentFarm != null && scannedFarm == null) {
            // Farm broken
            Woot.LOGGER.info("Multiblock broken at {}", origin);
            disconnectOldFarm(currentFarm);
            currentFarm = null;
            changed = true;
        } else if (currentFarm != null && scannedFarm != null) {
            // Check if farm configuration changed
            if (!farmsEqual(currentFarm, scannedFarm)) {
                Woot.LOGGER.info("Multiblock changed at {}", origin);
                disconnectOldFarm(currentFarm);
                connectNewFarm(scannedFarm);
                currentFarm = scannedFarm;
                changed = true;
            }
        }
    }

    /**
     * Connect blocks to the new farm
     */
    private void connectNewFarm(@Nonnull FarmScanner.ScannedFarm farm) {
        Set<BlockPos> allBlocks = new HashSet<>();
        allBlocks.addAll(farm.getControllerPositions());
        allBlocks.addAll(farm.getCellPositions());

        for (BlockPos pos : allBlocks) {
            if (level.isLoaded(pos)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof IFactoryGlueProvider provider) {
                    provider.getFactoryGlue().setMaster(origin);
                    Woot.LOGGER.debug("Connected block at {} to master at {}", pos, origin);
                }
            }
        }
    }

    /**
     * Disconnect blocks from the old farm
     */
    private void disconnectOldFarm(@Nonnull FarmScanner.ScannedFarm farm) {
        Set<BlockPos> allBlocks = new HashSet<>();
        allBlocks.addAll(farm.getControllerPositions());
        allBlocks.addAll(farm.getCellPositions());

        for (BlockPos pos : allBlocks) {
            if (level.isLoaded(pos)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof IFactoryGlueProvider provider) {
                    provider.getFactoryGlue().clearMaster();
                    Woot.LOGGER.debug("Disconnected block at {} from master", pos);
                }
            }
        }
    }

    /**
     * Check if two farms are equal
     */
    private boolean farmsEqual(@Nonnull FarmScanner.ScannedFarm farm1, @Nonnull FarmScanner.ScannedFarm farm2) {
        if (farm1.getTier() != farm2.getTier()) {
            return false;
        }

        Set<BlockPos> blocks1 = new HashSet<>();
        blocks1.addAll(farm1.getControllerPositions());
        blocks1.addAll(farm1.getCellPositions());

        Set<BlockPos> blocks2 = new HashSet<>();
        blocks2.addAll(farm2.getControllerPositions());
        blocks2.addAll(farm2.getCellPositions());

        return blocks1.equals(blocks2);
    }

    @Override
    @Nullable
    public FarmSetup createSetup() {
        if (currentFarm == null || level == null) {
            return null;
        }

        FarmSetup setup = new FarmSetup(
            level,
            currentFarm.getHeartPos(),
            currentFarm.getTier(),
            currentFarm.getProgrammedMob()
        );

        // Add controller positions
        for (BlockPos pos : currentFarm.getControllerPositions()) {
            setup.addControllerPosition(pos);
        }

        // Add cell positions and calculate aggregated energy
        int totalCapacity = 0;
        int totalStored = 0;

        for (BlockPos pos : currentFarm.getCellPositions()) {
            setup.addCellPosition(pos);

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FactoryCellBlockEntity cell) {
                totalCapacity += cell.getMaxEnergyStored();
                totalStored += cell.getEnergyStored();
            }
        }

        // Create aggregated energy storage
        if (totalCapacity > 0) {
            FactoryEnergyStorage aggregated = new FactoryEnergyStorage(totalCapacity, totalCapacity / 10, 0, totalStored);
            setup.setAggregatedEnergy(aggregated);
        }

        return setup;
    }

    @Override
    public boolean isFormed() {
        return currentFarm != null;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void clearChanged() {
        this.changed = false;
    }

    @Override
    public void fullDisconnect() {
        if (currentFarm != null) {
            disconnectOldFarm(currentFarm);
        }
    }
}
