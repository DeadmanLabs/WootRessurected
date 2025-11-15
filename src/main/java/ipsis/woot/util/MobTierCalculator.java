package ipsis.woot.util;

import ipsis.woot.Woot;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the required factory tier for spawning a mob
 * Based on original Woot's tier system using mob health
 */
public class MobTierCalculator {

    // Tier thresholds based on mob max health (matching original Woot)
    private static final int TIER_2_MIN_HEALTH = 20;  // Tier 2: 20-39 HP
    private static final int TIER_3_MIN_HEALTH = 40;  // Tier 3: 40-59 HP
    private static final int TIER_4_MIN_HEALTH = 60;  // Tier 4: 60+ HP

    // Cache for calculated tiers to avoid repeated entity creation
    private static final Map<EntityType<?>, EnumMobFactoryTier> tierCache = new HashMap<>();

    /**
     * Calculate the required factory tier for a mob
     *
     * @param level Server level (needed to create entity)
     * @param entityType Type of entity
     * @return Required factory tier (defaults to TIER_2 for unknown/modded mobs)
     */
    @Nonnull
    public static EnumMobFactoryTier calculateTier(@Nonnull ServerLevel level, @Nonnull EntityType<?> entityType) {
        // Check cache first
        if (tierCache.containsKey(entityType)) {
            return tierCache.get(entityType);
        }

        EnumMobFactoryTier tier = calculateTierFromHealth(level, entityType);

        // Cache the result
        tierCache.put(entityType, tier);

        Woot.LOGGER.debug("Calculated tier for {}: {}", entityType.getDescriptionId(), tier);
        return tier;
    }

    /**
     * Calculate tier based on mob's max health
     */
    @Nonnull
    private static EnumMobFactoryTier calculateTierFromHealth(@Nonnull ServerLevel level, @Nonnull EntityType<?> entityType) {
        try {
            // Create temporary entity to get max health
            Entity entity = entityType.create(level);
            if (entity == null) {
                Woot.LOGGER.warn("Failed to create entity of type {} for tier calculation, defaulting to TIER_2", entityType);
                return EnumMobFactoryTier.TIER_II; // Default for modded mobs
            }

            if (!(entity instanceof LivingEntity livingEntity)) {
                Woot.LOGGER.warn("Entity type {} is not a LivingEntity for tier calculation, defaulting to TIER_2", entityType);
                entity.discard();
                return EnumMobFactoryTier.TIER_II; // Default for modded mobs
            }

            // Get max health and calculate tier
            float maxHealth = livingEntity.getMaxHealth();
            entity.discard();

            EnumMobFactoryTier tier;
            if (maxHealth >= TIER_4_MIN_HEALTH) {
                tier = EnumMobFactoryTier.TIER_IV;  // Bosses (60+ HP)
            } else if (maxHealth >= TIER_3_MIN_HEALTH) {
                tier = EnumMobFactoryTier.TIER_III; // Powerful mobs (40-59 HP)
            } else if (maxHealth >= TIER_2_MIN_HEALTH) {
                tier = EnumMobFactoryTier.TIER_II;  // Nether/modded (20-39 HP)
            } else {
                tier = EnumMobFactoryTier.TIER_I;   // Basic mobs (< 20 HP)
            }

            Woot.LOGGER.info("Mob {} has {} HP, assigned to tier {}",
                entityType.getDescriptionId(), maxHealth, tier);

            return tier;

        } catch (Exception e) {
            Woot.LOGGER.error("Error calculating tier for entity type {}: {}",
                entityType, e.getMessage());
            return EnumMobFactoryTier.TIER_II; // Default for modded mobs on error
        }
    }

    /**
     * Clear the tier cache (useful for reloading configs)
     */
    public static void clearCache() {
        tierCache.clear();
        Woot.LOGGER.info("Cleared mob tier cache");
    }

    /**
     * Get tier from integer value (1-4)
     * Used for JSON parsing
     */
    @Nonnull
    public static EnumMobFactoryTier getTierFromInt(int tierInt) {
        return switch (tierInt) {
            case 1 -> EnumMobFactoryTier.TIER_I;
            case 2 -> EnumMobFactoryTier.TIER_II;
            case 3 -> EnumMobFactoryTier.TIER_III;
            case 4 -> EnumMobFactoryTier.TIER_IV;
            default -> {
                Woot.LOGGER.warn("Invalid tier integer {}, defaulting to TIER_II", tierInt);
                yield EnumMobFactoryTier.TIER_II;
            }
        };
    }

    /**
     * Convert tier to integer (1-4)
     * Used for JSON serialization
     */
    public static int getTierAsInt(@Nonnull EnumMobFactoryTier tier) {
        return tier.getLevel();
    }
}
