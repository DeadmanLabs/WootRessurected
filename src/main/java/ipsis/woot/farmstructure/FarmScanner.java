package ipsis.woot.farmstructure;

import ipsis.woot.Woot;
import ipsis.woot.blockentities.FactoryCellBlockEntity;
import ipsis.woot.blockentities.FactoryControllerBlockEntity;
import ipsis.woot.blocks.FactoryCellBlock;
import ipsis.woot.farming.EnumFarmUpgrade;
import ipsis.woot.items.data.EnderShardData;
import ipsis.woot.multiblock.EnumMobFactoryModule;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import ipsis.woot.multiblock.FactoryPatternRepository;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scans and validates the 3D multiblock structure
 * Checks if blocks match the tier patterns from FactoryPatternRepository
 */
public class FarmScanner {

    private final FactoryPatternRepository patternRepository;

    public FarmScanner() {
        this.patternRepository = new FactoryPatternRepository();
    }

    /**
     * Scan the area around the heart and validate the multiblock structure
     * Returns a ScannedFarm if valid, null if invalid
     */
    @Nullable
    public ScannedFarm scanFarm(@Nonnull Level level, @Nonnull BlockPos heartPos) {
        Woot.LOGGER.info("=== FACTORY STRUCTURE SCAN START at {} ===", heartPos);

        // Try each tier from highest to lowest to match the largest valid structure
        // EnumMobFactoryTier.values() returns [TIER_I, TIER_II, TIER_III, TIER_IV]
        // We need to iterate in reverse to try higher tiers first
        EnumMobFactoryTier[] tiers = EnumMobFactoryTier.values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            Woot.LOGGER.info("Attempting to validate as {} structure...", tiers[i]);
            ScannedFarm farm = tryTier(level, heartPos, tiers[i]);
            if (farm != null) {
                Woot.LOGGER.info("=== SUCCESS: Structure validated as {} ===", tiers[i]);
                return farm;
            } else {
                Woot.LOGGER.info("{} validation FAILED, trying next tier", tiers[i]);
            }
        }

        Woot.LOGGER.warn("=== FAILURE: No valid factory structure found at {} ===", heartPos);
        return null;
    }

    /**
     * Try to validate the structure as a specific tier
     */
    @Nullable
    private ScannedFarm tryTier(@Nonnull Level level, @Nonnull BlockPos heartPos, @Nonnull EnumMobFactoryTier tier) {
        List<FactoryPatternRepository.MobFactoryModule> modules = patternRepository.getAllModules(tier);
        Woot.LOGGER.info("  {} pattern has {} module positions to validate", tier, modules.size());

        ScannedFarm farm = new ScannedFarm(heartPos, tier);
        int validatedCount = 0;

        // Validate all modules in the pattern
        for (FactoryPatternRepository.MobFactoryModule module : modules) {
            BlockPos offset = module.getOffset();
            BlockPos worldPos = heartPos.offset(offset);

            if (!validateModule(level, worldPos, tier, module.getModuleType(), farm)) {
                Woot.LOGGER.info("  {} validation STOPPED: Module validation failed at {} (offset {})",
                    tier, worldPos, offset);
                return null;  // Invalid structure
            }
            validatedCount++;
        }
        Woot.LOGGER.info("  {} pattern modules validated: {}/{}", tier, validatedCount, modules.size());

        // Validate battery position (below heart with air gap)
        if (!validateBatteryPosition(level, heartPos, farm)) {
            Woot.LOGGER.info("  {} validation STOPPED: Battery/component validation failed", tier);
            return null;  // Invalid structure - no battery or air gap
        }
        Woot.LOGGER.info("  {} battery and components validated", tier);

        // Scan for required components (importer, exporter, controller)
        if (!scanForRequiredComponents(level, heartPos, tier, farm)) {
            Woot.LOGGER.info("  {} validation STOPPED: Missing required components", tier);
            return null;  // Invalid structure - missing required components
        }
        Woot.LOGGER.info("  {} required components found", tier);

        // Check if we have a programmed controller
        farm.setProgrammedMob(findProgrammedMob(level, farm.getControllerPositions()));

        Woot.LOGGER.info("  {} structure fully validated at {}", tier, heartPos);
        return farm;
    }

    /**
     * Validate a specific module position matches the pattern
     */
    private boolean validateModule(@Nonnull Level level, @Nonnull BlockPos pos,
                                    @Nonnull EnumMobFactoryTier tier,
                                    @Nonnull EnumMobFactoryModule expectedModule,
                                    @Nonnull ScannedFarm farm) {
        if (!level.isLoaded(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // Match block to expected module type
        boolean valid = switch (expectedModule) {
            case STRUCTURE_BLOCK_1 -> block == Woot.STRUCTURE_BLOCK_1.get();
            case STRUCTURE_BLOCK_2 -> block == Woot.STRUCTURE_BLOCK_2.get();
            case STRUCTURE_BLOCK_3 -> block == Woot.STRUCTURE_BLOCK_3.get();
            case STRUCTURE_BLOCK_4 -> block == Woot.STRUCTURE_BLOCK_4.get();
            case STRUCTURE_BLOCK_5 -> block == Woot.STRUCTURE_BLOCK_5.get();
            case STRUCTURE_UPGRADE -> {
                boolean validBase = block == Woot.STRUCTURE_BLOCK_UPGRADE.get();
                if (validBase) {
                    // Scan for upgrade totem (vertical stack of 1-3 blocks) above the upgrade base
                    UpgradeScanner.UpgradeInfo upgradeInfo = UpgradeScanner.scanUpgradeTotem(level, pos, tier);
                    if (upgradeInfo != null) {
                        farm.addUpgradeTotem(upgradeInfo.getType(), upgradeInfo.getTier(), upgradeInfo.getPositions());
                    }
                }
                yield validBase;
            }
            case STRUCTURE_TIER_I_CAP -> block == Woot.STRUCTURE_TIER_I_CAP.get();
            case STRUCTURE_TIER_II_CAP -> block == Woot.STRUCTURE_TIER_II_CAP.get();
            case STRUCTURE_TIER_III_CAP -> block == Woot.STRUCTURE_TIER_III_CAP.get();
            case STRUCTURE_TIER_IV_CAP -> block == Woot.STRUCTURE_TIER_IV_CAP.get();
            default -> false;
        };

        if (!valid) {
            Woot.LOGGER.info("    MISMATCH at {}: expected {}, found {}", pos, expectedModule, block.getName().getString());
        }

        return valid;
    }

    /**
     * Find the first programmed controller in the list
     */
    @Nullable
    private EnderShardData findProgrammedMob(@Nonnull Level level, @Nonnull List<BlockPos> controllerPositions) {
        for (BlockPos pos : controllerPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FactoryControllerBlockEntity controller) {
                if (controller.isProgrammed()) {
                    return controller.getProgrammedMob();
                }
            }
        }
        return null;
    }

    /**
     * Validate required components below heart:
     * heart.below(1) = air (gap)
     * heart.below(2) = battery
     * heart.below(3) = importer
     * heart.below(4) = exporter
     * @return true if all components are correctly positioned
     */
    private boolean validateBatteryPosition(@Nonnull Level level, @Nonnull BlockPos heartPos, @Nonnull ScannedFarm farm) {
        BlockPos airGapPos = heartPos.below(1);
        BlockPos batteryPos = heartPos.below(2);
        BlockPos importerPos = heartPos.below(3);
        BlockPos exporterPos = heartPos.below(4);

        // Check air gap at heart.below(1)
        BlockState airGapState = level.getBlockState(airGapPos);
        if (!airGapState.isAir()) {
            Woot.LOGGER.debug("Structure invalid: No air gap at {} (found: {})", airGapPos, airGapState.getBlock());
            return false;
        }

        // Check battery at heart.below(2)
        Block batteryBlock = level.getBlockState(batteryPos).getBlock();
        if (!(batteryBlock instanceof FactoryCellBlock)) {
            Woot.LOGGER.debug("Structure invalid: No battery at {} (found: {})", batteryPos, batteryBlock);
            return false;
        }

        // Check importer at heart.below(3)
        Block importerBlock = level.getBlockState(importerPos).getBlock();
        if (importerBlock != Woot.IMPORTER.get()) {
            Woot.LOGGER.debug("Structure invalid: No importer at {} (found: {})", importerPos, importerBlock);
            return false;
        }

        // Check exporter at heart.below(4)
        Block exporterBlock = level.getBlockState(exporterPos).getBlock();
        if (exporterBlock != Woot.EXPORTER.get()) {
            Woot.LOGGER.debug("Structure invalid: No exporter at {} (found: {})", exporterPos, exporterBlock);
            return false;
        }

        // Add all components to farm
        farm.addCellPosition(batteryPos);
        farm.addImporterPosition(importerPos);
        farm.addExporterPosition(exporterPos);

        Woot.LOGGER.debug("All required components found: battery at {}, importer at {}, exporter at {}",
            batteryPos, importerPos, exporterPos);
        return true;
    }

    /**
     * Scan for controller within the structure bounds
     * @return true if controller found
     */
    private boolean scanForRequiredComponents(@Nonnull Level level, @Nonnull BlockPos heartPos,
                                              @Nonnull EnumMobFactoryTier tier, @Nonnull ScannedFarm farm) {
        // Battery, importer, and exporter are already validated by validateBatteryPosition()
        // Now scan for controller within structure bounds

        // Get all pattern modules to determine search bounds
        List<FactoryPatternRepository.MobFactoryModule> modules = patternRepository.getAllModules(tier);

        // Calculate structure bounds
        int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;
        for (FactoryPatternRepository.MobFactoryModule module : modules) {
            BlockPos offset = module.getOffset();
            minX = Math.min(minX, offset.getX());
            maxX = Math.max(maxX, offset.getX());
            minY = Math.min(minY, offset.getY());
            maxY = Math.max(maxY, offset.getY());
            minZ = Math.min(minZ, offset.getZ());
            maxZ = Math.max(maxZ, offset.getZ());
        }

        // Scan all positions within structure bounds for controller
        boolean hasController = false;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos checkPos = heartPos.offset(x, y, z);
                    Block block = level.getBlockState(checkPos).getBlock();

                    if (block == Woot.CONTROLLER.get()) {
                        hasController = true;
                        farm.addControllerPosition(checkPos);
                        Woot.LOGGER.debug("Controller found at {}", checkPos);
                    }
                }
            }
        }

        if (!hasController) {
            Woot.LOGGER.debug("Structure invalid: Missing controller in bounds ({}:{}, {}:{}, {}:{})",
                minX, maxX, minY, maxY, minZ, maxZ);
            return false;
        }

        Woot.LOGGER.debug("Controller found in structure");
        return true;
    }

    /**
     * Result of scanning a farm
     */
    public static class ScannedFarm {
        private final BlockPos heartPos;
        private final EnumMobFactoryTier tier;
        private final List<BlockPos> controllerPositions = new ArrayList<>();
        private final List<BlockPos> cellPositions = new ArrayList<>();
        private final List<BlockPos> importerPositions = new ArrayList<>();
        private final List<BlockPos> exporterPositions = new ArrayList<>();
        private EnderShardData programmedMob = null;
        private final Map<EnumFarmUpgrade, Integer> upgrades = new HashMap<>();
        private final Map<EnumFarmUpgrade, List<BlockPos>> upgradeTotemPositions = new HashMap<>();

        public ScannedFarm(BlockPos heartPos, EnumMobFactoryTier tier) {
            this.heartPos = heartPos;
            this.tier = tier;
        }

        public BlockPos getHeartPos() {
            return heartPos;
        }

        public EnumMobFactoryTier getTier() {
            return tier;
        }

        public List<BlockPos> getControllerPositions() {
            return controllerPositions;
        }

        public List<BlockPos> getCellPositions() {
            return cellPositions;
        }

        public void addControllerPosition(BlockPos pos) {
            controllerPositions.add(pos);
        }

        public void addCellPosition(BlockPos pos) {
            cellPositions.add(pos);
        }

        public List<BlockPos> getImporterPositions() {
            return importerPositions;
        }

        public List<BlockPos> getExporterPositions() {
            return exporterPositions;
        }

        public void addImporterPosition(BlockPos pos) {
            importerPositions.add(pos);
        }

        public void addExporterPosition(BlockPos pos) {
            exporterPositions.add(pos);
        }

        @Nullable
        public EnderShardData getProgrammedMob() {
            return programmedMob;
        }

        public void setProgrammedMob(@Nullable EnderShardData mob) {
            this.programmedMob = mob;
        }

        public boolean isProgrammed() {
            return programmedMob != null;
        }

        public void addUpgrade(EnumFarmUpgrade type, int tier) {
            upgrades.put(type, tier);
            Woot.LOGGER.debug("Added upgrade: {} Tier {} to scanned farm", type.getName(), tier);
        }

        /**
         * Add an upgrade totem (vertical stack of upgrade blocks)
         * @param type Upgrade type
         * @param tier Final tier level (1-3)
         * @param positions All block positions in the totem stack
         */
        public void addUpgradeTotem(EnumFarmUpgrade type, int tier, List<BlockPos> positions) {
            upgrades.put(type, tier);
            upgradeTotemPositions.put(type, new ArrayList<>(positions)); // Defensive copy
            Woot.LOGGER.debug("Added upgrade totem: {} Tier {} ({} blocks) to scanned farm",
                type.getName(), tier, positions.size());
        }

        public Map<EnumFarmUpgrade, Integer> getUpgrades() {
            return upgrades;
        }

        /**
         * Get all upgrade totem positions
         * Maps upgrade type to list of block positions in that totem
         */
        public Map<EnumFarmUpgrade, List<BlockPos>> getUpgradeTotemPositions() {
            return upgradeTotemPositions;
        }
    }
}

