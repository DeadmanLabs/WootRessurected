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
import java.util.Set;

/**
 * Calculates the required factory tier for spawning a mob
 * Based on original Woot's tier system
 *
 * Tier logic:
 * - Tier 4: Custom only (requires JSON config override via SpawnRecipe)
 * - Tier 3: Wither, Ender Dragon, Warden ONLY (hardcoded)
 * - Tier 2: Iron Golem (hardcoded) + custom mobs (JSON override via SpawnRecipe)
 * - Tier 1: Vanilla mobs <30 HP (default for most mobs)
 */
public class MobTierCalculator {

    // Health threshold for Tier 1 default assignment
    private static final int TIER_1_MAX_HEALTH = 30;  // Vanilla mobs <30 HP default to Tier I

    // Hardcoded Tier 3 mobs (bosses)
    private static final Set<ResourceLocation> TIER_3_MOBS = Set.of(
        ResourceLocation.withDefaultNamespace("wither"),
        ResourceLocation.withDefaultNamespace("ender_dragon"),
        ResourceLocation.withDefaultNamespace("warden")
    );

    // Hardcoded Tier 2 special case
    private static final ResourceLocation IRON_GOLEM = ResourceLocation.withDefaultNamespace("iron_golem");

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
     * Internal tier calculation with correct tier logic
     *
     * Priority order:
     * 1. Check SpawnRecipe for tier override (custom Tier 2 or Tier 4)
     * 2. Check hardcoded Tier 3 mobs (Wither, Dragon, Warden)
     * 3. Check hardcoded Tier 2 special case (Iron Golem)
     * 4. Default to Tier 1 for vanilla mobs <30 HP
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
            // 1. Check SpawnRecipe for tier override (custom Tier 2 or Tier 4)
            SpawnRecipe recipe = Woot.SPAWN_RECIPE_REPOSITORY.get(entityId);
            if (recipe.hasTierOverride()) {
                EnumMobFactoryTier overrideTier = recipe.getRequiredTier();
                Woot.LOGGER.info("Mob {} has tier override from SpawnRecipe: {}", entityId, overrideTier);
                return overrideTier;
            }

            // 2. Check hardcoded Tier 3 mobs (Wither, Dragon, Warden)
            if (TIER_3_MOBS.contains(entityId)) {
                Woot.LOGGER.info("Mob {} is hardcoded Tier 3 boss mob", entityId);
                return EnumMobFactoryTier.TIER_III;
            }

            // 3. Check hardcoded Tier 2 special case (Iron Golem)
            if (entityId.equals(IRON_GOLEM)) {
                Woot.LOGGER.info("Mob {} is hardcoded Tier 2 (Iron Golem)", entityId);
                return EnumMobFactoryTier.TIER_II;
            }

            // 4. Default to Tier 1 for vanilla mobs <30 HP
            // Create temporary entity to check health
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

            // Assign Tier 1 for mobs <30 HP (most vanilla mobs)
            // Unknown/modded mobs also default to Tier 1
            Woot.LOGGER.info("Mob {} has {} HP, assigned to Tier 1 (default)", entityId, maxHealth);
            return EnumMobFactoryTier.TIER_I;

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
