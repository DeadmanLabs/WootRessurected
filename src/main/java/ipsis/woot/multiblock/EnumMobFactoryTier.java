package ipsis.woot.multiblock;

import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

/**
 * Represents the different tiers of mob factories
 * Each tier has increasing size, power capacity, and capability
 */
public enum EnumMobFactoryTier implements StringRepresentable {
    TIER_I("tier_i", 1, 100000, 80),
    TIER_II("tier_ii", 2, 1000000, 160),
    TIER_III("tier_iii", 3, 10000000, 240),
    TIER_IV("tier_iv", 4, Integer.MAX_VALUE, 320);

    private final String name;
    private final int level;
    private final int maxPowerCapacity;
    private final int basePowerPerTick;

    EnumMobFactoryTier(String name, int level, int maxPowerCapacity, int basePowerPerTick) {
        this.name = name;
        this.level = level;
        this.maxPowerCapacity = maxPowerCapacity;
        this.basePowerPerTick = basePowerPerTick;
    }

    @Override
    @Nonnull
    public String getSerializedName() {
        return name;
    }

    /**
     * Get the tier level (1-4)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the maximum power capacity for this tier
     */
    public int getMaxPowerCapacity() {
        return maxPowerCapacity;
    }

    /**
     * Get the base power consumption per tick for this tier
     */
    public int getBasePowerPerTick() {
        return basePowerPerTick;
    }

    /**
     * Get translation key for this tier
     */
    public String getTranslationKey() {
        return "info.woot.tier." + name;
    }

    /**
     * Get tier by level (1-4)
     */
    @Nonnull
    public static EnumMobFactoryTier byLevel(int level) {
        for (EnumMobFactoryTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        return TIER_I; // Default to tier 1
    }

    /**
     * Check if this tier is at least the specified tier
     */
    public boolean isAtLeast(@Nonnull EnumMobFactoryTier other) {
        return this.level >= other.level;
    }

    /**
     * Get the next tier, or null if already at max
     */
    @Nonnull
    public EnumMobFactoryTier getNext() {
        if (this == TIER_IV) {
            return TIER_IV;
        }
        return byLevel(this.level + 1);
    }

    @Override
    public String toString() {
        return name;
    }
}
