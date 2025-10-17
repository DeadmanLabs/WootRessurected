package ipsis.woot;

import com.mojang.logging.LogUtils;
import ipsis.woot.blockentities.WootBlockEntities;
import ipsis.woot.blocks.AnvilBlock;
import ipsis.woot.crafting.AnvilRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Woot.MODID)
public class Woot {
    public static final String MODID = "woot";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    // ========== RECIPE TYPES ==========
    public static final DeferredHolder<RecipeType<?>, RecipeType<AnvilRecipe>> ANVIL_RECIPE_TYPE =
        RECIPE_TYPES.register("anvil", () -> new RecipeType<AnvilRecipe>() {
            @Override
            public String toString() {
                return "woot:anvil";
            }
        });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilRecipe>> ANVIL_RECIPE_SERIALIZER =
        RECIPE_SERIALIZERS.register("anvil", AnvilRecipe.Serializer::new);

    // ========== SIMPLE BLOCKS ==========
    public static final DeferredBlock<Block> STYGIAN_IRON_ORE = BLOCKS.registerSimpleBlock("stygianironore",
        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> STYGIAN_IRON_BLOCK = BLOCKS.registerSimpleBlock("stygianiron",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> SOUL_STONE = BLOCKS.registerSimpleBlock("soulstone",
        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> ANVIL = BLOCKS.register("anvil", () ->
        new AnvilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundType.ANVIL)));
    public static final DeferredBlock<Block> LAYOUT = BLOCKS.registerSimpleBlock("layout",
        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> FACTORY_HEART = BLOCKS.registerSimpleBlock("factory_heart",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> CONTROLLER = BLOCKS.registerSimpleBlock("controller",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> IMPORTER = BLOCKS.registerSimpleBlock("importer",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> EXPORTER = BLOCKS.registerSimpleBlock("exporter",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());

    // ========== STRUCTURE BLOCKS (10 variants) ==========
    public static final DeferredBlock<Block> STRUCTURE_BLOCK_1 = BLOCKS.registerSimpleBlock("structure_block_1",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_BLOCK_2 = BLOCKS.registerSimpleBlock("structure_block_2",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_BLOCK_3 = BLOCKS.registerSimpleBlock("structure_block_3",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_BLOCK_4 = BLOCKS.registerSimpleBlock("structure_block_4",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_BLOCK_5 = BLOCKS.registerSimpleBlock("structure_block_5",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_BLOCK_UPGRADE = BLOCKS.registerSimpleBlock("structure_block_upgrade",
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_TIER_I_CAP = BLOCKS.registerSimpleBlock("structure_tier_i_cap",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_TIER_II_CAP = BLOCKS.registerSimpleBlock("structure_tier_ii_cap",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_TIER_III_CAP = BLOCKS.registerSimpleBlock("structure_tier_iii_cap",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> STRUCTURE_TIER_IV_CAP = BLOCKS.registerSimpleBlock("structure_tier_iv_cap",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL));

    // ========== CELL BLOCKS (3 tiers) ==========
    public static final DeferredBlock<Block> CELL_TIER_I = BLOCKS.registerSimpleBlock("cell_tier_i",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> CELL_TIER_II = BLOCKS.registerSimpleBlock("cell_tier_ii",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> CELL_TIER_III = BLOCKS.registerSimpleBlock("cell_tier_iii",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());

    // ========== UPGRADE BLOCKS (10 variants) ==========
    public static final DeferredBlock<Block> UPGRADE_XP = BLOCKS.registerSimpleBlock("upgrade_xp",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_RATE = BLOCKS.registerSimpleBlock("upgrade_rate",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_LOOTING = BLOCKS.registerSimpleBlock("upgrade_looting",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_MASS = BLOCKS.registerSimpleBlock("upgrade_mass",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_DECAPITATE = BLOCKS.registerSimpleBlock("upgrade_decapitate",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_EFFICIENCY = BLOCKS.registerSimpleBlock("upgrade_efficiency",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_BM_CRYSTAL = BLOCKS.registerSimpleBlock("upgrade_bm_crystal",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_BM_LE_ALTAR = BLOCKS.registerSimpleBlock("upgrade_bm_le_altar",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_BM_LE_TANK = BLOCKS.registerSimpleBlock("upgrade_bm_le_tank",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> UPGRADE_EC_BLOOD = BLOCKS.registerSimpleBlock("upgrade_ec_blood",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());

    // ========== SIMPLE ITEMS ==========
    public static final DeferredItem<Item> STYGIAN_IRON_DUST = ITEMS.registerSimpleItem("stygianirondust");
    public static final DeferredItem<Item> STYGIAN_IRON_INGOT = ITEMS.registerSimpleItem("stygianironingot");
    public static final DeferredItem<Item> STYGIAN_IRON_PLATE = ITEMS.registerSimpleItem("stygianironplate");
    public static final DeferredItem<Item> SOUL_SAND_DUST = ITEMS.registerSimpleItem("soulsanddust");
    public static final DeferredItem<Item> FACTORY_BASE = ITEMS.registerSimpleItem("factorybase");
    public static final DeferredItem<Item> PRISM = ITEMS.registerSimpleItem("prism");
    public static final DeferredItem<Item> XP_SHARD = ITEMS.registerSimpleItem("xpshard");
    public static final DeferredItem<Item> ENDER_SHARD = ITEMS.registerSimpleItem("endershard");
    public static final DeferredItem<Item> YAH_HAMMER = ITEMS.registerSimpleItem("yahhammer");
    public static final DeferredItem<Item> BUILDER = ITEMS.registerSimpleItem("builder");

    // ========== DIE VARIANTS (4 types) ==========
    public static final DeferredItem<Item> DIE_MESH = ITEMS.registerSimpleItem("die_mesh");
    public static final DeferredItem<Item> DIE_PLATE = ITEMS.registerSimpleItem("die_plate");
    public static final DeferredItem<Item> DIE_CORE = ITEMS.registerSimpleItem("die_core");
    public static final DeferredItem<Item> DIE_SHARD = ITEMS.registerSimpleItem("die_shard");

    // ========== FACTORY CORE VARIANTS (7 types) ==========
    public static final DeferredItem<Item> FACTORY_CORE_HEART = ITEMS.registerSimpleItem("factorycore_heart");
    public static final DeferredItem<Item> FACTORY_CORE_CONTROLLER = ITEMS.registerSimpleItem("factorycore_controller");
    public static final DeferredItem<Item> FACTORY_CORE_T1_UPGRADE = ITEMS.registerSimpleItem("factorycore_t1_upgrade");
    public static final DeferredItem<Item> FACTORY_CORE_T2_UPGRADE = ITEMS.registerSimpleItem("factorycore_t2_upgrade");
    public static final DeferredItem<Item> FACTORY_CORE_T3_UPGRADE = ITEMS.registerSimpleItem("factorycore_t3_upgrade");
    public static final DeferredItem<Item> FACTORY_CORE_POWER = ITEMS.registerSimpleItem("factorycore_power");
    public static final DeferredItem<Item> FACTORY_CORE_CAP = ITEMS.registerSimpleItem("factorycore_cap");

    // ========== SHARD VARIANTS (7 types) ==========
    public static final DeferredItem<Item> SHARD_DIAMOND = ITEMS.registerSimpleItem("shard_diamond");
    public static final DeferredItem<Item> SHARD_EMERALD = ITEMS.registerSimpleItem("shard_emerald");
    public static final DeferredItem<Item> SHARD_QUARTZ = ITEMS.registerSimpleItem("shard_quartz");
    public static final DeferredItem<Item> SHARD_NETHERSTAR = ITEMS.register("shard_netherstar", () -> new ipsis.woot.items.EnchantedShardItem(new Item.Properties()));
    public static final DeferredItem<Item> SHARD_TIER_II = ITEMS.registerSimpleItem("shard_tier_ii");
    public static final DeferredItem<Item> SHARD_TIER_III = ITEMS.registerSimpleItem("shard_tier_iii");
    public static final DeferredItem<Item> SHARD_TIER_IV = ITEMS.registerSimpleItem("shard_tier_iv");

    // ========== BLOCK ITEMS ==========
    // Simple blocks
    public static final DeferredItem<BlockItem> STYGIAN_IRON_ORE_ITEM = ITEMS.registerSimpleBlockItem("stygianironore", STYGIAN_IRON_ORE);
    public static final DeferredItem<BlockItem> STYGIAN_IRON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("stygianiron", STYGIAN_IRON_BLOCK);
    public static final DeferredItem<BlockItem> SOUL_STONE_ITEM = ITEMS.registerSimpleBlockItem("soulstone", SOUL_STONE);
    public static final DeferredItem<BlockItem> ANVIL_ITEM = ITEMS.registerSimpleBlockItem("anvil", ANVIL);
    public static final DeferredItem<BlockItem> LAYOUT_ITEM = ITEMS.registerSimpleBlockItem("layout", LAYOUT);
    public static final DeferredItem<BlockItem> FACTORY_HEART_ITEM = ITEMS.registerSimpleBlockItem("factory_heart", FACTORY_HEART);
    public static final DeferredItem<BlockItem> CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem("controller", CONTROLLER);
    public static final DeferredItem<BlockItem> IMPORTER_ITEM = ITEMS.registerSimpleBlockItem("importer", IMPORTER);
    public static final DeferredItem<BlockItem> EXPORTER_ITEM = ITEMS.registerSimpleBlockItem("exporter", EXPORTER);

    // Structure blocks
    public static final DeferredItem<BlockItem> STRUCTURE_BLOCK_1_ITEM = ITEMS.registerSimpleBlockItem("structure_block_1", STRUCTURE_BLOCK_1);
    public static final DeferredItem<BlockItem> STRUCTURE_BLOCK_2_ITEM = ITEMS.registerSimpleBlockItem("structure_block_2", STRUCTURE_BLOCK_2);
    public static final DeferredItem<BlockItem> STRUCTURE_BLOCK_3_ITEM = ITEMS.registerSimpleBlockItem("structure_block_3", STRUCTURE_BLOCK_3);
    public static final DeferredItem<BlockItem> STRUCTURE_BLOCK_4_ITEM = ITEMS.registerSimpleBlockItem("structure_block_4", STRUCTURE_BLOCK_4);
    public static final DeferredItem<BlockItem> STRUCTURE_BLOCK_5_ITEM = ITEMS.registerSimpleBlockItem("structure_block_5", STRUCTURE_BLOCK_5);
    public static final DeferredItem<BlockItem> STRUCTURE_BLOCK_UPGRADE_ITEM = ITEMS.registerSimpleBlockItem("structure_block_upgrade", STRUCTURE_BLOCK_UPGRADE);
    public static final DeferredItem<BlockItem> STRUCTURE_TIER_I_CAP_ITEM = ITEMS.registerSimpleBlockItem("structure_tier_i_cap", STRUCTURE_TIER_I_CAP);
    public static final DeferredItem<BlockItem> STRUCTURE_TIER_II_CAP_ITEM = ITEMS.registerSimpleBlockItem("structure_tier_ii_cap", STRUCTURE_TIER_II_CAP);
    public static final DeferredItem<BlockItem> STRUCTURE_TIER_III_CAP_ITEM = ITEMS.registerSimpleBlockItem("structure_tier_iii_cap", STRUCTURE_TIER_III_CAP);
    public static final DeferredItem<BlockItem> STRUCTURE_TIER_IV_CAP_ITEM = ITEMS.registerSimpleBlockItem("structure_tier_iv_cap", STRUCTURE_TIER_IV_CAP);

    // Cell tiers
    public static final DeferredItem<BlockItem> CELL_TIER_I_ITEM = ITEMS.registerSimpleBlockItem("cell_tier_i", CELL_TIER_I);
    public static final DeferredItem<BlockItem> CELL_TIER_II_ITEM = ITEMS.registerSimpleBlockItem("cell_tier_ii", CELL_TIER_II);
    public static final DeferredItem<BlockItem> CELL_TIER_III_ITEM = ITEMS.registerSimpleBlockItem("cell_tier_iii", CELL_TIER_III);

    // Upgrade blocks
    public static final DeferredItem<BlockItem> UPGRADE_XP_ITEM = ITEMS.registerSimpleBlockItem("upgrade_xp", UPGRADE_XP);
    public static final DeferredItem<BlockItem> UPGRADE_RATE_ITEM = ITEMS.registerSimpleBlockItem("upgrade_rate", UPGRADE_RATE);
    public static final DeferredItem<BlockItem> UPGRADE_LOOTING_ITEM = ITEMS.registerSimpleBlockItem("upgrade_looting", UPGRADE_LOOTING);
    public static final DeferredItem<BlockItem> UPGRADE_MASS_ITEM = ITEMS.registerSimpleBlockItem("upgrade_mass", UPGRADE_MASS);
    public static final DeferredItem<BlockItem> UPGRADE_DECAPITATE_ITEM = ITEMS.registerSimpleBlockItem("upgrade_decapitate", UPGRADE_DECAPITATE);
    public static final DeferredItem<BlockItem> UPGRADE_EFFICIENCY_ITEM = ITEMS.registerSimpleBlockItem("upgrade_efficiency", UPGRADE_EFFICIENCY);
    public static final DeferredItem<BlockItem> UPGRADE_BM_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem("upgrade_bm_crystal", UPGRADE_BM_CRYSTAL);
    public static final DeferredItem<BlockItem> UPGRADE_BM_LE_ALTAR_ITEM = ITEMS.registerSimpleBlockItem("upgrade_bm_le_altar", UPGRADE_BM_LE_ALTAR);
    public static final DeferredItem<BlockItem> UPGRADE_BM_LE_TANK_ITEM = ITEMS.registerSimpleBlockItem("upgrade_bm_le_tank", UPGRADE_BM_LE_TANK);
    public static final DeferredItem<BlockItem> UPGRADE_EC_BLOOD_ITEM = ITEMS.registerSimpleBlockItem("upgrade_ec_blood", UPGRADE_EC_BLOOD);

    // ========== CREATIVE TAB ==========
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WOOT_TAB = CREATIVE_MODE_TABS.register("woot_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.woot"))
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> FACTORY_CORE_HEART.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            // Materials
            output.accept(STYGIAN_IRON_DUST.get());
            output.accept(STYGIAN_IRON_INGOT.get());
            output.accept(STYGIAN_IRON_PLATE.get());
            output.accept(SOUL_SAND_DUST.get());

            // Dies
            output.accept(DIE_MESH.get());
            output.accept(DIE_PLATE.get());
            output.accept(DIE_CORE.get());
            output.accept(DIE_SHARD.get());

            // Factory components
            output.accept(FACTORY_BASE.get());
            output.accept(FACTORY_CORE_HEART.get());
            output.accept(FACTORY_CORE_CONTROLLER.get());
            output.accept(FACTORY_CORE_T1_UPGRADE.get());
            output.accept(FACTORY_CORE_T2_UPGRADE.get());
            output.accept(FACTORY_CORE_T3_UPGRADE.get());
            output.accept(FACTORY_CORE_POWER.get());
            output.accept(FACTORY_CORE_CAP.get());

            output.accept(PRISM.get());

            // Shards
            output.accept(SHARD_DIAMOND.get());
            output.accept(SHARD_EMERALD.get());
            output.accept(SHARD_QUARTZ.get());
            output.accept(SHARD_NETHERSTAR.get());
            output.accept(SHARD_TIER_II.get());
            output.accept(SHARD_TIER_III.get());
            output.accept(SHARD_TIER_IV.get());
            output.accept(XP_SHARD.get());
            output.accept(ENDER_SHARD.get());

            // Tools
            output.accept(YAH_HAMMER.get());
            output.accept(BUILDER.get());

            // Blocks
            output.accept(STYGIAN_IRON_ORE_ITEM.get());
            output.accept(STYGIAN_IRON_BLOCK_ITEM.get());
            output.accept(SOUL_STONE_ITEM.get());
            output.accept(ANVIL_ITEM.get());
            output.accept(LAYOUT_ITEM.get());
            output.accept(FACTORY_HEART_ITEM.get());
            output.accept(CONTROLLER_ITEM.get());
            output.accept(IMPORTER_ITEM.get());
            output.accept(EXPORTER_ITEM.get());

            // Structure blocks
            output.accept(STRUCTURE_BLOCK_1_ITEM.get());
            output.accept(STRUCTURE_BLOCK_2_ITEM.get());
            output.accept(STRUCTURE_BLOCK_3_ITEM.get());
            output.accept(STRUCTURE_BLOCK_4_ITEM.get());
            output.accept(STRUCTURE_BLOCK_5_ITEM.get());
            output.accept(STRUCTURE_BLOCK_UPGRADE_ITEM.get());
            output.accept(STRUCTURE_TIER_I_CAP_ITEM.get());
            output.accept(STRUCTURE_TIER_II_CAP_ITEM.get());
            output.accept(STRUCTURE_TIER_III_CAP_ITEM.get());
            output.accept(STRUCTURE_TIER_IV_CAP_ITEM.get());

            // Cell tiers
            output.accept(CELL_TIER_I_ITEM.get());
            output.accept(CELL_TIER_II_ITEM.get());
            output.accept(CELL_TIER_III_ITEM.get());

            // Upgrade blocks
            output.accept(UPGRADE_XP_ITEM.get());
            output.accept(UPGRADE_RATE_ITEM.get());
            output.accept(UPGRADE_LOOTING_ITEM.get());
            output.accept(UPGRADE_MASS_ITEM.get());
            output.accept(UPGRADE_DECAPITATE_ITEM.get());
            output.accept(UPGRADE_EFFICIENCY_ITEM.get());
            output.accept(UPGRADE_BM_CRYSTAL_ITEM.get());
            output.accept(UPGRADE_BM_LE_ALTAR_ITEM.get());
            output.accept(UPGRADE_BM_LE_TANK_ITEM.get());
            output.accept(UPGRADE_EC_BLOOD_ITEM.get());
        }).build());

    public Woot(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        WootBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        LOGGER.info("Woot mod initialized with ALL variants!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Woot common setup complete!");
    }
}
