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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FactoryHeartBlockEntity>> FACTORY_HEART =
            BLOCK_ENTITIES.register("factory_heart", () ->
                    BlockEntityType.Builder.of(
                            FactoryHeartBlockEntity::new,
                            Woot.FACTORY_HEART.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FactoryControllerBlockEntity>> FACTORY_CONTROLLER =
            BLOCK_ENTITIES.register("factory_controller", () ->
                    BlockEntityType.Builder.of(
                            FactoryControllerBlockEntity::new,
                            Woot.CONTROLLER.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FactoryCellBlockEntity>> FACTORY_CELL =
            BLOCK_ENTITIES.register("factory_cell", () ->
                    BlockEntityType.Builder.of(
                            FactoryCellBlockEntity::new,
                            Woot.CELL_TIER_I.get(),
                            Woot.CELL_TIER_II.get(),
                            Woot.CELL_TIER_III.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LayoutBlockEntity>> LAYOUT =
            BLOCK_ENTITIES.register("layout", () ->
                    BlockEntityType.Builder.of(
                            LayoutBlockEntity::new,
                            Woot.LAYOUT.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImporterBlockEntity>> FACTORY_IMPORTER =
            BLOCK_ENTITIES.register("factory_importer", () ->
                    BlockEntityType.Builder.of(
                            ImporterBlockEntity::new,
                            Woot.IMPORTER.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExporterBlockEntity>> FACTORY_EXPORTER =
            BLOCK_ENTITIES.register("factory_exporter", () ->
                    BlockEntityType.Builder.of(
                            ExporterBlockEntity::new,
                            Woot.EXPORTER.get()
                    ).build(null)
            );
}
