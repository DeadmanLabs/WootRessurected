package ipsis.woot.items;

import ipsis.woot.Woot;
import ipsis.woot.items.data.BuilderTierData;
import ipsis.woot.items.data.EnderShardData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all custom data components for Woot items
 */
public class WootDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Woot.MODID);

    /**
     * Data component for Ender Shard programming data
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnderShardData>> ENDER_SHARD =
        DATA_COMPONENTS.register("ender_shard",
            () -> DataComponentType.<EnderShardData>builder()
                .persistent(EnderShardData.CODEC)
                .networkSynchronized(EnderShardData.STREAM_CODEC)
                .build()
        );

    /**
     * Data component for Factory Builder tier selection
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BuilderTierData>> BUILDER_TIER =
        DATA_COMPONENTS.register("builder_tier",
            () -> DataComponentType.<BuilderTierData>builder()
                .persistent(BuilderTierData.CODEC)
                .networkSynchronized(BuilderTierData.STREAM_CODEC)
                .build()
        );
}
