package ipsis.woot.util;

import ipsis.woot.Woot;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import ipsis.woot.recipes.SpawnRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the required factory tier for spawning a mob
 * Based on original Woot's tier system
 *
 * Tier logic:
 * - Tier 4: Custom only (requires JSON config override via SpawnRecipe)
 * - Tier 3: 100+ HP (Wither 300, Ender Dragon 200, Warden 500, etc.)
 * - Tier 2: 30-100 HP (Iron Golem 100, Piglin Brute, etc.)
 * - Tier 1: <30 HP (Creeper 20, Zombie 20, Skeleton 20, Spider 16, etc.)
 */
public class MobTierCalculator {

    // Health thresholds for tier assignment
    private static final int TIER_2_MIN_HEALTH = 30;   // Tier 2: 30-100 HP
    private static final int TIER_3_MIN_HEALTH = 100;  // Tier 3: 100+ HP

    // Cache for calculated tiers to avoid repeated entity creation
    private static final Map<EntityType<?>, EnumMobFactoryTier> tierCache = new HashMap<>();

    /**
     * Calculate the required factory tier for a mob
     *
     * @param level Server level (needed to create entity for health check)
     * @param entityType Type of entity
     * @return Required factory tier
     */
    @Nonnull
    public static EnumMobFactoryTier calculateTier(@Nonnull ServerLevel level, @Nonnull EntityType<?> entityType) {
        // Check cache first
        if (tierCache.containsKey(entityType)) {
            return tierCache.get(entityType);
        }

        EnumMobFactoryTier tier = calculateTierInternal(level, entityType);

        // Cache the result
        tierCache.put(entityType, tier);

        Woot.LOGGER.info("Calculated tier for {}: {}", entityType.getDescriptionId(), tier);
        return tier;
    }

    /**
     * Internal tier calculation with health-based logic
     *
     * Priority order:
     * 1. Check SpawnRecipe for tier override (allows custom Tier 2, 3, or 4)
     * 2. Calculate tier based on mob max health:
     *    - Tier 3: 100+ HP
     *    - Tier 2: 30-100 HP
     *    - Tier 1: <30 HP
     */
    @Nonnull
    private static EnumMobFactoryTier calculateTierInternal(@Nonnull ServerLevel level, @Nonnull EntityType<?> entityType) {
        // Get entity ResourceLocation
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (entityId == null) {
            Woot.LOGGER.warn("Failed to get ResourceLocation for entity type {}, defaulting to TIER_I", entityType);
            return EnumMobFactoryTier.TIER_I;
        }

        try {
            // 1. Check SpawnRecipe for tier override (allows Tier 4 and custom tier assignments)
            SpawnRecipe recipe = Woot.SPAWN_RECIPE_REPOSITORY.get(entityId);
            if (recipe.hasTierOverride()) {
                EnumMobFactoryTier overrideTier = recipe.getRequiredTier();
                Woot.LOGGER.info("Mob {} has tier override from SpawnRecipe: {}", entityId, overrideTier);
                return overrideTier;
            }

            // 2. Calculate tier based on max health
            Entity entity = entityType.create(level);
            if (entity == null) {
                Woot.LOGGER.warn("Failed to create entity of type {} for health check, defaulting to TIER_I", entityId);
                return EnumMobFactoryTier.TIER_I;
            }

            if (!(entity instanceof LivingEntity livingEntity)) {
                Woot.LOGGER.warn("Entity type {} is not a LivingEntity, defaulting to TIER_I", entityId);
                entity.discard();
                return EnumMobFactoryTier.TIER_I;
            }

            float maxHealth = livingEntity.getMaxHealth();
            entity.discard();

            // Calculate tier based on health thresholds
            EnumMobFactoryTier tier;
            if (maxHealth >= TIER_3_MIN_HEALTH) {
                tier = EnumMobFactoryTier.TIER_III;  // 100+ HP: Wither, Dragon, Warden, Iron Golem
            } else if (maxHealth >= TIER_2_MIN_HEALTH) {
                tier = EnumMobFactoryTier.TIER_II;   // 30-99 HP: Piglin Brute, Enderman, etc.
            } else {
                tier = EnumMobFactoryTier.TIER_I;    // <30 HP: Zombie, Skeleton, Creeper, Spider
            }

            Woot.LOGGER.info("Mob {} has {} HP, assigned to {}", entityId, maxHealth, tier);
            return tier;

        } catch (Exception e) {
            Woot.LOGGER.error("Error calculating tier for entity type {}: {}", entityId, e.getMessage());
            return EnumMobFactoryTier.TIER_I; // Default to Tier 1 on error
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
