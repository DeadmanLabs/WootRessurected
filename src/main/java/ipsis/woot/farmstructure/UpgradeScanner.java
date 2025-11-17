package ipsis.woot.farmstructure;

import ipsis.woot.Woot;
import ipsis.woot.farming.EnumFarmUpgrade;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans for and validates upgrade "totems" - vertical stacks of 1-3 upgrade blocks
 *
 * Stacking Rules:
 * - Upgrades are placed ABOVE the upgrade base block (structure_block_upgrade)
 * - Base position (basePos.above(1)): Must be Tier I upgrade
 * - Second position (basePos.above(2)): Optional Tier II upgrade (same type)
 * - Third position (basePos.above(3)): Optional Tier III upgrade (same type)
 *
 * Sequential Validation:
 * - All blocks in stack must be same upgrade type (e.g., all LOOTING)
 * - Cannot skip tiers (must have I+II to add III)
 * - Factory tier limits maximum upgrade height
 */
public class UpgradeScanner {

    /**
     * Scan for an upgrade totem above the upgrade base position
     * Returns the highest valid tier found in the vertical stack
     *
     * @param level The world
     * @param basePos Position of the upgrade base block (structure_block_upgrade)
     * @param factoryTier The factory's tier (determines max upgrade tier allowed)
     * @return Detected upgrade type, tier, and all block positions, or null if none/invalid
     */
    @Nullable
    public static UpgradeInfo scanUpgradeTotem(@Nonnull Level level, @Nonnull BlockPos basePos,
                                                @Nonnull EnumMobFactoryTier factoryTier) {
        // ========== TIER I: Base of the totem (required) ==========
        BlockPos tier1Pos = basePos.above(1);
        BlockState tier1State = level.getBlockState(tier1Pos);
        Block tier1Block = tier1State.getBlock();

        // Check if Tier I upgrade exists
        EnumFarmUpgrade upgradeType = EnumFarmUpgrade.fromBlock(tier1Block);
        if (upgradeType == null) {
            // No upgrade installed (air or invalid block)
            return null;
        }

        int tier1Level = EnumFarmUpgrade.getTierFromBlock(tier1Block);
        if (tier1Level != 1) {
            // Base MUST be Tier I
            Woot.LOGGER.warn("Invalid upgrade totem at {}: base is Tier {} instead of Tier I",
                tier1Pos, tier1Level);
            return null;
        }

        // Start building the totem
        int maxTier = 1;
        List<BlockPos> totemPositions = new ArrayList<>();
        totemPositions.add(tier1Pos);

        // ========== TIER II: Second block (optional) ==========
        if (factoryTier.getLevel() >= 2) {
            BlockPos tier2Pos = basePos.above(2);
            BlockState tier2State = level.getBlockState(tier2Pos);
            Block tier2Block = tier2State.getBlock();

            EnumFarmUpgrade tier2Type = EnumFarmUpgrade.fromBlock(tier2Block);
            int tier2Level = EnumFarmUpgrade.getTierFromBlock(tier2Block);

            // Check if Tier II block matches
            if (tier2Type == upgradeType && tier2Level == 2) {
                maxTier = 2;
                totemPositions.add(tier2Pos);

                // ========== TIER III: Third block (optional, requires Tier II) ==========
                if (factoryTier.getLevel() >= 3) {
                    BlockPos tier3Pos = basePos.above(3);
                    BlockState tier3State = level.getBlockState(tier3Pos);
                    Block tier3Block = tier3State.getBlock();

                    EnumFarmUpgrade tier3Type = EnumFarmUpgrade.fromBlock(tier3Block);
                    int tier3Level = EnumFarmUpgrade.getTierFromBlock(tier3Block);

                    // Check if Tier III block matches
                    if (tier3Type == upgradeType && tier3Level == 3) {
                        maxTier = 3;
                        totemPositions.add(tier3Pos);
                    }
                }
            }
        }

        return new UpgradeInfo(upgradeType, maxTier, totemPositions);
    }

    /**
     * Get the maximum number of upgrade slots for a factory tier
     */
    public static int getUpgradeSlotsForTier(@Nonnull EnumMobFactoryTier tier) {
        return switch (tier) {
            case TIER_I -> 2;
            case TIER_II, TIER_III, TIER_IV -> 4;
        };
    }

    /**
     * Container for detected upgrade totem information
     * Stores the upgrade type, final tier level, and ALL block positions in the stack
     */
    public static class UpgradeInfo {
        private final EnumFarmUpgrade type;
        private final int tier;
        private final List<BlockPos> positions;

        public UpgradeInfo(@Nonnull EnumFarmUpgrade type, int tier, @Nonnull List<BlockPos> positions) {
            this.type = type;
            this.tier = tier;
            this.positions = new ArrayList<>(positions); // Defensive copy
        }

        public EnumFarmUpgrade getType() {
            return type;
        }

        public int getTier() {
            return tier;
        }

        /**
         * Get all block positions in this upgrade totem
         * For a Tier III totem, this returns 3 positions (Tier I, II, III blocks)
         */
        public List<BlockPos> getPositions() {
            return positions;
        }

        /**
         * Get the base position (first block in the totem)
         */
        public BlockPos getBasePosition() {
            return positions.isEmpty() ? null : positions.get(0);
        }

        @Override
        public String toString() {
            return String.format("%s Tier %d (%d blocks)", type.getName(), tier, positions.size());
        }
    }
}
