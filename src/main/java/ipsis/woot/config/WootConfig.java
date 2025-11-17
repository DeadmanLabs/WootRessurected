package ipsis.woot.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Main configuration for Woot mod
 * Handles tier shard drops, factory settings, and other gameplay options
 */
public class WootConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Tier Shard Configuration
    public static final ModConfigSpec.BooleanValue ALLOW_SHARD_RECIPES;
    public static final ModConfigSpec.IntValue T2_SHARD_DROP_CHANCE;
    public static final ModConfigSpec.IntValue T3_SHARD_DROP_CHANCE;
    public static final ModConfigSpec.IntValue T4_SHARD_DROP_CHANCE;

    // Upgrade Configuration
    public static final ModConfigSpec.IntValue XP_BASE_PER_MOB;
    public static final ModConfigSpec.BooleanValue ENABLE_UPGRADE_POWER_COSTS;

    static {
        BUILDER.comment("Factory General Settings").push("factory_general");

        ALLOW_SHARD_RECIPES = BUILDER
            .comment("Enable tier shard drops from factories and allow shard-based recipes for structure blocks")
            .define("allowShardRecipes", true);

        T2_SHARD_DROP_CHANCE = BUILDER
            .comment("Percentage chance (0-100) for Tier II shard to drop per factory spawn cycle")
            .defineInRange("tier2ShardDropChance", 15, 0, 100);

        T3_SHARD_DROP_CHANCE = BUILDER
            .comment("Percentage chance (0-100) for Tier III shard to drop per factory spawn cycle")
            .defineInRange("tier3ShardDropChance", 8, 0, 100);

        T4_SHARD_DROP_CHANCE = BUILDER
            .comment("Percentage chance (0-100) for Tier IV shard to drop per factory spawn cycle")
            .defineInRange("tier4ShardDropChance", 5, 0, 100);

        BUILDER.pop();

        // Upgrade Settings
        BUILDER.comment("Factory Upgrade Settings").push("factory_upgrades");

        XP_BASE_PER_MOB = BUILDER
            .comment("Base XP value per mob for XP upgrade calculations (default: 5)")
            .defineInRange("xpBasePerMob", 5, 1, 100);

        ENABLE_UPGRADE_POWER_COSTS = BUILDER
            .comment("Enable additional power costs for upgrades (80/160/240 RF/t per tier)")
            .define("enableUpgradePowerCosts", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
