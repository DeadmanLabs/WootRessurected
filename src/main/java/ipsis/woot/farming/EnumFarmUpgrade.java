package ipsis.woot.farming;

import ipsis.woot.Woot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the different types of factory upgrades
 * Each upgrade type has 3 tiers (I, II, III) with increasing effects and power costs
 * Based on original Woot 1.12.2 upgrade system
 */
public enum EnumFarmUpgrade {
    /**
     * LOOTING - Applies looting enchantment to mob drops
     * Tier I: Looting I (+80 RF/t)
     * Tier II: Looting II (+160 RF/t)
     * Tier III: Looting III (+240 RF/t)
     */
    LOOTING("looting", 3),

    /**
     * RATE - Reduces time between spawn cycles
     * Tier I: 160 ticks (+80 RF/t)
     * Tier II: 80 ticks (+160 RF/t)
     * Tier III: 40 ticks (+240 RF/t)
     */
    RATE("rate", 3),

    /**
     * MASS - Spawns multiple mobs per cycle
     * Tier I: 4 mobs (+80 RF/t)
     * Tier II: 6 mobs (+160 RF/t)
     * Tier III: 8 mobs (+240 RF/t)
     */
    MASS("mass", 3),

    /**
     * EFFICIENCY - Reduces power consumption
     * Tier I: -15% power (+80 RF/t)
     * Tier II: -25% power (+160 RF/t)
     * Tier III: -30% power (+240 RF/t)
     */
    EFFICIENCY("efficiency", 3),

    /**
     * XP - Increases experience generation
     * Tier I: +20% XP (+80 RF/t)
     * Tier II: +40% XP (+160 RF/t)
     * Tier III: +80% XP (+240 RF/t)
     */
    XP("xp", 3),

    /**
     * DECAPITATE - Increases mob head drop chance
     * Tier I: 20% chance (+80 RF/t)
     * Tier II: 40% chance (+160 RF/t)
     * Tier III: 80% chance (+240 RF/t)
     */
    DECAPITATE("decapitate", 3);

    private final String name;
    private final int maxTier;

    EnumFarmUpgrade(String name, int maxTier) {
        this.name = name;
        this.maxTier = maxTier;
    }

    public String getName() {
        return name;
    }

    public int getMaxTier() {
        return maxTier;
    }

    /**
     * Get the power cost per tick for a specific tier of this upgrade
     * @param tier Upgrade tier (1-3)
     * @return Power cost in RF/tick
     */
    public int getPowerCostPerTick(int tier) {
        if (tier < 1 || tier > maxTier) {
            return 0;
        }
        return tier * 80; // Tier I: 80 RF/t, Tier II: 160 RF/t, Tier III: 240 RF/t
    }

    /**
     * Get the looting level for this upgrade tier
     * Only applicable to LOOTING upgrade
     */
    public int getLootingLevel(int tier) {
        if (this != LOOTING) {
            return 0;
        }
        return Math.min(tier, 3); // Direct mapping: Tier I = Looting I, etc.
    }

    /**
     * Get the spawn rate in ticks for this upgrade tier
     * Only applicable to RATE upgrade
     */
    public int getSpawnRateTicks(int tier, int baseTicks) {
        if (this != RATE) {
            return baseTicks;
        }
        return switch (tier) {
            case 1 -> 160; // 2× faster than base 320
            case 2 -> 80;  // 4× faster
            case 3 -> 40;  // 8× faster
            default -> baseTicks;
        };
    }

    /**
     * Get the mass spawn count for this upgrade tier
     * Only applicable to MASS upgrade
     */
    public int getMassCount(int tier) {
        if (this != MASS) {
            return 1;
        }
        return switch (tier) {
            case 1 -> 4;
            case 2 -> 6;
            case 3 -> 8;
            default -> 1;
        };
    }

    /**
     * Get the efficiency multiplier for this upgrade tier
     * Only applicable to EFFICIENCY upgrade
     * @return Multiplier (1.0 = no reduction, 0.7 = 30% reduction)
     */
    public float getEfficiencyMultiplier(int tier) {
        if (this != EFFICIENCY) {
            return 1.0f;
        }
        return switch (tier) {
            case 1 -> 0.85f; // 15% reduction
            case 2 -> 0.75f; // 25% reduction
            case 3 -> 0.70f; // 30% reduction
            default -> 1.0f;
        };
    }

    /**
     * Get the XP multiplier for this upgrade tier
     * Only applicable to XP upgrade
     */
    public float getXPMultiplier(int tier) {
        if (this != XP) {
            return 1.0f;
        }
        return switch (tier) {
            case 1 -> 1.2f;  // +20%
            case 2 -> 1.4f;  // +40%
            case 3 -> 1.8f;  // +80%
            default -> 1.0f;
        };
    }

    /**
     * Get the decapitate (head drop) chance for this upgrade tier
     * Only applicable to DECAPITATE upgrade
     * @return Chance as a value from 0.0 to 1.0
     */
    public float getDecapitateChance(int tier) {
        if (this != DECAPITATE) {
            return 0.0f;
        }
        return switch (tier) {
            case 1 -> 0.20f; // 20%
            case 2 -> 0.40f; // 40%
            case 3 -> 0.80f; // 80%
            default -> 0.0f;
        };
    }

    /**
     * Get upgrade type from block
     * Maps upgrade blocks to their corresponding upgrade type
     */
    @Nullable
    /**
     * Get upgrade type from a block instance
     * Handles all tiered variants (e.g., UPGRADE_LOOTING_I, UPGRADE_LOOTING_II, UPGRADE_LOOTING_III)
     * @param block The upgrade block
     * @return The upgrade type, or null if not an upgrade block
     */
    public static EnumFarmUpgrade fromBlock(@Nonnull Block block) {
        // Use block name to determine type (strip tier suffix)
        ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null) {
            return null;
        }

        String blockName = blockId.getPath();

        // Strip tier suffix (_iii, _ii, _i) - longest to shortest to ensure proper matching
        String baseName = blockName.replaceAll("_(iii|ii|i)$", "");

        if (baseName.equals("upgrade_looting")) return LOOTING;
        if (baseName.equals("upgrade_rate")) return RATE;
        if (baseName.equals("upgrade_mass")) return MASS;
        if (baseName.equals("upgrade_efficiency")) return EFFICIENCY;
        if (baseName.equals("upgrade_xp")) return XP;
        if (baseName.equals("upgrade_decapitate")) return DECAPITATE;

        return null;
    }

    /**
     * Get the tier level directly from a block instance
     * @param block The upgrade block
     * @return Tier level (1-3), or 0 if not an upgrade block
     */
    public static int getTierFromBlock(@Nonnull Block block) {
        ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null) {
            return 0;
        }

        return getTierFromBlockName(blockId.getPath());
    }

    /**
     * Extract tier from block name
     * upgrade_looting_i → 1 (Tier I)
     * upgrade_looting_ii → 2 (Tier II)
     * upgrade_looting_iii → 3 (Tier III)
     */
    public static int getTierFromBlockName(@Nonnull String blockName) {
        if (blockName.endsWith("_iii")) {
            return 3;
        } else if (blockName.endsWith("_ii")) {
            return 2;
        } else if (blockName.endsWith("_i")) {
            return 1;
        } else {
            return 0; // Not a tiered upgrade block
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
