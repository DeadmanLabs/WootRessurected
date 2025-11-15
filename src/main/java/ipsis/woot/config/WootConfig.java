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

        SPEC = BUILDER.build();
    }
}
