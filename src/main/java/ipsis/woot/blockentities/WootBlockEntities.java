package ipsis.woot.blockentities;

import ipsis.woot.Woot;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for all Woot block entities
 */
public class WootBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Woot.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnvilBlockEntity>> ANVIL =
            BLOCK_ENTITIES.register("anvil", () ->
                    BlockEntityType.Builder.of(
                            AnvilBlockEntity::new,
                            Woot.ANVIL.get()
                    ).build(null)
            );
}
