package ipsis.woot.crafting.conditions;

import com.mojang.serialization.MapCodec;
import ipsis.woot.Woot;
import ipsis.woot.config.WootConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe condition that checks if tier shard recipes are enabled in the config
 * Used by structure_block_*_alt.json recipes to conditionally enable/disable them
 *
 * When ALLOW_SHARD_RECIPES config is true, shard-based recipes are loaded
 * When false, they are skipped during recipe loading
 */
public record ShardRecipeCondition() implements ICondition {

    public static final MapCodec<ShardRecipeCondition> CODEC = MapCodec.unit(new ShardRecipeCondition());

    @Override
    public boolean test(ICondition.IContext context) {
        boolean enabled = WootConfig.ALLOW_SHARD_RECIPES.get();
        Woot.LOGGER.debug("ShardRecipeCondition evaluated: {} (config: allowShardRecipes={})",
            enabled ? "ENABLED" : "DISABLED", enabled);
        return enabled;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "shard_recipe_enabled";
    }
}
