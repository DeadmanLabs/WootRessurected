package ipsis.woot;

import com.mojang.logging.LogUtils;
import ipsis.woot.blockentities.WootBlockEntities;
import ipsis.woot.blocks.AnvilBlock;
import ipsis.woot.gui.WootMenuTypes;
import ipsis.woot.blocks.ExporterBlock;
import ipsis.woot.blocks.FactoryHeartBlock;
import ipsis.woot.blocks.FactoryControllerBlock;
import ipsis.woot.blocks.FactoryCellBlock;
import ipsis.woot.blocks.ImporterBlock;
import ipsis.woot.blocks.LayoutBlock;
import ipsis.woot.config.EnderShardConfig;
import ipsis.woot.config.WootConfig;
import ipsis.woot.crafting.AnvilRecipe;
import ipsis.woot.crafting.conditions.ShardRecipeCondition;
import ipsis.woot.items.ControllerBlockItem;
import ipsis.woot.items.EnderShardItem;
import ipsis.woot.items.FactoryBuilderItem;
import ipsis.woot.items.WootDataComponents;
import ipsis.woot.items.data.BuilderTierData;
import ipsis.woot.items.data.EnderShardData;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Woot.MODID)
public class Woot {
    public static final String MODID = "woot";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Spawn ingredient recipe repository
    public static final ipsis.woot.recipes.SpawnRecipeRepository SPAWN_RECIPE_REPOSITORY = new ipsis.woot.recipes.SpawnRecipeRepository();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MODID);

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

    // ========== RECIPE CONDITIONS ==========
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ShardRecipeCondition>> SHARD_RECIPE_CONDITION =
        CONDITION_CODECS.register("shard_recipe", () -> ShardRecipeCondition.CODEC);

    // ========== SIMPLE BLOCKS ==========
    public static final DeferredBlock<Block> STYGIAN_IRON_ORE = BLOCKS.registerSimpleBlock("stygianironore",
        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> STYGIAN_IRON_BLOCK = BLOCKS.registerSimpleBlock("stygianiron",
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL));
    public static final DeferredBlock<Block> SOUL_STONE = BLOCKS.registerSimpleBlock("soulstone",
        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final DeferredBlock<Block> ANVIL = BLOCKS.register("anvil", () ->
        new AnvilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundType.ANVIL)));
    public static final DeferredBlock<Block> LAYOUT = BLOCKS.register("layout", () ->
        new LayoutBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0F).sound(SoundType.STONE).noOcclusion()));
    public static final DeferredBlock<Block> FACTORY_HEART = BLOCKS.register("factory_heart", () ->
        new FactoryHeartBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<Block> CONTROLLER = BLOCKS.register("controller", () ->
        new FactoryControllerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<Block> IMPORTER = BLOCKS.register("importer", () ->
        new ImporterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<Block> EXPORTER = BLOCKS.register("exporter", () ->
        new ExporterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion()));

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
    public static final DeferredBlock<Block> CELL_TIER_I = BLOCKS.register("cell_tier_i", () ->
        new FactoryCellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), 1));
    public static final DeferredBlock<Block> CELL_TIER_II = BLOCKS.register("cell_tier_ii", () ->
        new FactoryCellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), 2));
    public static final DeferredBlock<Block> CELL_TIER_III = BLOCKS.register("cell_tier_iii", () ->
        new FactoryCellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), 3));

    // ========== UPGRADE BLOCKS (18 tiered variants: 6 types × 3 tiers) ==========
    // LOOTING Upgrades (Tiers I, II, III)
    public static final DeferredBlock<Block> UPGRADE_LOOTING_I = BLOCKS.register("upgrade_looting_i", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.LOOTING, 1));
    public static final DeferredBlock<Block> UPGRADE_LOOTING_II = BLOCKS.register("upgrade_looting_ii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.LOOTING, 2));
    public static final DeferredBlock<Block> UPGRADE_LOOTING_III = BLOCKS.register("upgrade_looting_iii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.LOOTING, 3));

    // RATE Upgrades (Tiers I, II, III)
    public static final DeferredBlock<Block> UPGRADE_RATE_I = BLOCKS.register("upgrade_rate_i", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.RATE, 1));
    public static final DeferredBlock<Block> UPGRADE_RATE_II = BLOCKS.register("upgrade_rate_ii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.RATE, 2));
    public static final DeferredBlock<Block> UPGRADE_RATE_III = BLOCKS.register("upgrade_rate_iii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.RATE, 3));

    // MASS Upgrades (Tiers I, II, III)
    public static final DeferredBlock<Block> UPGRADE_MASS_I = BLOCKS.register("upgrade_mass_i", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.MASS, 1));
    public static final DeferredBlock<Block> UPGRADE_MASS_II = BLOCKS.register("upgrade_mass_ii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.MASS, 2));
    public static final DeferredBlock<Block> UPGRADE_MASS_III = BLOCKS.register("upgrade_mass_iii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.MASS, 3));

    // EFFICIENCY Upgrades (Tiers I, II, III)
    public static final DeferredBlock<Block> UPGRADE_EFFICIENCY_I = BLOCKS.register("upgrade_efficiency_i", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.EFFICIENCY, 1));
    public static final DeferredBlock<Block> UPGRADE_EFFICIENCY_II = BLOCKS.register("upgrade_efficiency_ii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.EFFICIENCY, 2));
    public static final DeferredBlock<Block> UPGRADE_EFFICIENCY_III = BLOCKS.register("upgrade_efficiency_iii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.EFFICIENCY, 3));

    // XP Upgrades (Tiers I, II, III)
    public static final DeferredBlock<Block> UPGRADE_XP_I = BLOCKS.register("upgrade_xp_i", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.XP, 1));
    public static final DeferredBlock<Block> UPGRADE_XP_II = BLOCKS.register("upgrade_xp_ii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.XP, 2));
    public static final DeferredBlock<Block> UPGRADE_XP_III = BLOCKS.register("upgrade_xp_iii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.XP, 3));

    // DECAPITATE Upgrades (Tiers I, II, III)
    public static final DeferredBlock<Block> UPGRADE_DECAPITATE_I = BLOCKS.register("upgrade_decapitate_i", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.DECAPITATE, 1));
    public static final DeferredBlock<Block> UPGRADE_DECAPITATE_II = BLOCKS.register("upgrade_decapitate_ii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.DECAPITATE, 2));
    public static final DeferredBlock<Block> UPGRADE_DECAPITATE_III = BLOCKS.register("upgrade_decapitate_iii", () ->
        new ipsis.woot.blocks.UpgradeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 1200.0F).sound(SoundType.METAL).noOcclusion(), ipsis.woot.farming.EnumFarmUpgrade.DECAPITATE, 3));

    // ========== ADDON UPGRADE BLOCKS (Blood Magic, Extra Cells) ==========
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
    public static final DeferredItem<Item> ENDER_SHARD = ITEMS.register("endershard", () -> new EnderShardItem(new Item.Properties()));
    public static final DeferredItem<Item> YAH_HAMMER = ITEMS.registerSimpleItem("yahhammer");
    public static final DeferredItem<Item> BUILDER = ITEMS.register("builder", () ->
        new FactoryBuilderItem(new Item.Properties()));

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
    public static final DeferredItem<BlockItem> CONTROLLER_ITEM = ITEMS.register("controller",
        () -> new ControllerBlockItem(CONTROLLER.get(), new Item.Properties()));
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

    // Upgrade blocks (18 tiered variants)
    public static final DeferredItem<BlockItem> UPGRADE_LOOTING_I_ITEM = ITEMS.registerSimpleBlockItem("upgrade_looting_i", UPGRADE_LOOTING_I);
    public static final DeferredItem<BlockItem> UPGRADE_LOOTING_II_ITEM = ITEMS.registerSimpleBlockItem("upgrade_looting_ii", UPGRADE_LOOTING_II);
    public static final DeferredItem<BlockItem> UPGRADE_LOOTING_III_ITEM = ITEMS.registerSimpleBlockItem("upgrade_looting_iii", UPGRADE_LOOTING_III);
    public static final DeferredItem<BlockItem> UPGRADE_RATE_I_ITEM = ITEMS.registerSimpleBlockItem("upgrade_rate_i", UPGRADE_RATE_I);
    public static final DeferredItem<BlockItem> UPGRADE_RATE_II_ITEM = ITEMS.registerSimpleBlockItem("upgrade_rate_ii", UPGRADE_RATE_II);
    public static final DeferredItem<BlockItem> UPGRADE_RATE_III_ITEM = ITEMS.registerSimpleBlockItem("upgrade_rate_iii", UPGRADE_RATE_III);
    public static final DeferredItem<BlockItem> UPGRADE_MASS_I_ITEM = ITEMS.registerSimpleBlockItem("upgrade_mass_i", UPGRADE_MASS_I);
    public static final DeferredItem<BlockItem> UPGRADE_MASS_II_ITEM = ITEMS.registerSimpleBlockItem("upgrade_mass_ii", UPGRADE_MASS_II);
    public static final DeferredItem<BlockItem> UPGRADE_MASS_III_ITEM = ITEMS.registerSimpleBlockItem("upgrade_mass_iii", UPGRADE_MASS_III);
    public static final DeferredItem<BlockItem> UPGRADE_EFFICIENCY_I_ITEM = ITEMS.registerSimpleBlockItem("upgrade_efficiency_i", UPGRADE_EFFICIENCY_I);
    public static final DeferredItem<BlockItem> UPGRADE_EFFICIENCY_II_ITEM = ITEMS.registerSimpleBlockItem("upgrade_efficiency_ii", UPGRADE_EFFICIENCY_II);
    public static final DeferredItem<BlockItem> UPGRADE_EFFICIENCY_III_ITEM = ITEMS.registerSimpleBlockItem("upgrade_efficiency_iii", UPGRADE_EFFICIENCY_III);
    public static final DeferredItem<BlockItem> UPGRADE_XP_I_ITEM = ITEMS.registerSimpleBlockItem("upgrade_xp_i", UPGRADE_XP_I);
    public static final DeferredItem<BlockItem> UPGRADE_XP_II_ITEM = ITEMS.registerSimpleBlockItem("upgrade_xp_ii", UPGRADE_XP_II);
    public static final DeferredItem<BlockItem> UPGRADE_XP_III_ITEM = ITEMS.registerSimpleBlockItem("upgrade_xp_iii", UPGRADE_XP_III);
    public static final DeferredItem<BlockItem> UPGRADE_DECAPITATE_I_ITEM = ITEMS.registerSimpleBlockItem("upgrade_decapitate_i", UPGRADE_DECAPITATE_I);
    public static final DeferredItem<BlockItem> UPGRADE_DECAPITATE_II_ITEM = ITEMS.registerSimpleBlockItem("upgrade_decapitate_ii", UPGRADE_DECAPITATE_II);
    public static final DeferredItem<BlockItem> UPGRADE_DECAPITATE_III_ITEM = ITEMS.registerSimpleBlockItem("upgrade_decapitate_iii", UPGRADE_DECAPITATE_III);
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

            // Upgrade blocks (18 tiered variants)
            output.accept(UPGRADE_LOOTING_I_ITEM.get());
            output.accept(UPGRADE_LOOTING_II_ITEM.get());
            output.accept(UPGRADE_LOOTING_III_ITEM.get());
            output.accept(UPGRADE_RATE_I_ITEM.get());
            output.accept(UPGRADE_RATE_II_ITEM.get());
            output.accept(UPGRADE_RATE_III_ITEM.get());
            output.accept(UPGRADE_MASS_I_ITEM.get());
            output.accept(UPGRADE_MASS_II_ITEM.get());
            output.accept(UPGRADE_MASS_III_ITEM.get());
            output.accept(UPGRADE_EFFICIENCY_I_ITEM.get());
            output.accept(UPGRADE_EFFICIENCY_II_ITEM.get());
            output.accept(UPGRADE_EFFICIENCY_III_ITEM.get());
            output.accept(UPGRADE_XP_I_ITEM.get());
            output.accept(UPGRADE_XP_II_ITEM.get());
            output.accept(UPGRADE_XP_III_ITEM.get());
            output.accept(UPGRADE_DECAPITATE_I_ITEM.get());
            output.accept(UPGRADE_DECAPITATE_II_ITEM.get());
            output.accept(UPGRADE_DECAPITATE_III_ITEM.get());
            output.accept(UPGRADE_BM_CRYSTAL_ITEM.get());
            output.accept(UPGRADE_BM_LE_ALTAR_ITEM.get());
            output.accept(UPGRADE_BM_LE_TANK_ITEM.get());
            output.accept(UPGRADE_EC_BLOOD_ITEM.get());
        }).build());

    public Woot(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        WootBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        WootMenuTypes.MENU_TYPES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        CONDITION_CODECS.register(modEventBus);
        WootDataComponents.DATA_COMPONENTS.register(modEventBus);

        // Register configuration
        modContainer.registerConfig(ModConfig.Type.COMMON, WootConfig.SPEC);

        LOGGER.info("Woot mod initialized with ALL variants!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Woot common setup starting...");

        // Load ender shard configuration
        EnderShardConfig.load();

        // Load spawn ingredient recipes
        ipsis.woot.recipes.IngredientLoader.loadIngredients(SPAWN_RECIPE_REPOSITORY);

        LOGGER.info("Woot common setup complete!");
        LOGGER.info("Registered recipe types:");
        LOGGER.info("  - Anvil Recipe Type: {}", ANVIL_RECIPE_TYPE.getId());
        LOGGER.info("  - Anvil Recipe Serializer: {}", ANVIL_RECIPE_SERIALIZER.getId());
        LOGGER.info("Ender shard configuration loaded: {} mob configs", EnderShardConfig.getConfigCount());
        LOGGER.info("Spawn ingredient recipes loaded: {} recipes", SPAWN_RECIPE_REPOSITORY.getRecipeCount());
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        LOGGER.info("Registering capabilities...");

        // Register energy capability for Factory Cell
        // Always exposed so tools like Jade can see it
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            WootBlockEntities.FACTORY_CELL.get(),
            (blockEntity, direction) -> blockEntity.getEnergyStorage()
        );

        // Register energy capability for Factory Heart
        // Only exposed when multiblock is formed
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            WootBlockEntities.FACTORY_HEART.get(),
            (blockEntity, direction) -> {
                // Conditional capability: only when multiblock is formed
                if (blockEntity.isFormed()) {
                    return blockEntity.getEnergyStorage();
                }
                // Return null when not formed - no capability exposed
                return null;
            }
        );

        // Register item handler capability for Factory Exporter
        // Always exposed to allow extraction of items
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            WootBlockEntities.FACTORY_EXPORTER.get(),
            (blockEntity, direction) -> blockEntity.getItemHandler()
        );

        // Note: Factory Importer no longer has item handler capability
        // It proxies to adjacent containers instead
        // Pipes/hoppers should connect to the adjacent chests/containers, not the importer

        LOGGER.info("Capabilities registered: Energy (Cell, Heart), ItemHandler (Exporter)");
    }
}
