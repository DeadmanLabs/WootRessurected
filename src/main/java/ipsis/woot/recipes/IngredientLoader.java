package ipsis.woot.recipes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ipsis.woot.Woot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads spawn ingredient recipes from JSON configuration
 * Based on original Woot's FactoryIngredientsLoader pattern
 */
public class IngredientLoader {

    private static final String CONFIG_FILENAME = "factory_ingredients.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Load ingredient recipes from JSON file
     * @param repository Repository to populate with recipes
     */
    public static void loadIngredients(@Nonnull SpawnRecipeRepository repository) {
        try {
            // Try to load from config directory first
            Path configPath = Paths.get("config", "woot", CONFIG_FILENAME);
            JsonObject root;

            if (Files.exists(configPath)) {
                Woot.LOGGER.info("Loading ingredient recipes from config file: {}", configPath);
                String json = Files.readString(configPath, StandardCharsets.UTF_8);
                root = JsonParser.parseString(json).getAsJsonObject();
            } else {
                // Load from resources and copy to config
                Woot.LOGGER.info("Config file not found, loading default from resources");
                root = loadDefaultConfig();

                // Copy to config directory
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, GSON.toJson(root), StandardCharsets.UTF_8);
                Woot.LOGGER.info("Created default config file at: {}", configPath);
            }

            // Parse and load recipes
            parseIngredients(root, repository);

            Woot.LOGGER.info("Loaded {} spawn ingredient recipes", repository.getRecipeCount());

        } catch (Exception e) {
            Woot.LOGGER.error("Failed to load ingredient recipes", e);
        }
    }

    /**
     * Load default configuration from resources
     */
    @Nonnull
    private static JsonObject loadDefaultConfig() throws IOException {
        String resourcePath = "/data/woot/" + CONFIG_FILENAME;
        try (InputStream stream = IngredientLoader.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                Woot.LOGGER.warn("Default config not found in resources, creating empty config");
                return createEmptyConfig();
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    /**
     * Create an empty default configuration
     */
    @Nonnull
    private static JsonObject createEmptyConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);

        // Empty default recipe
        JsonObject defaultRecipe = new JsonObject();
        defaultRecipe.add("items", new JsonArray());
        defaultRecipe.add("fluids", new JsonArray());
        root.add("default", defaultRecipe);

        // Example ingredients array
        JsonArray ingredients = new JsonArray();

        // Example: Wither
        JsonObject wither = new JsonObject();
        wither.addProperty("mobName", "minecraft:wither");
        JsonArray witherItems = new JsonArray();

        JsonObject soulSand = new JsonObject();
        soulSand.addProperty("item", "minecraft:soul_sand");
        soulSand.addProperty("count", 4);
        witherItems.add(soulSand);

        JsonObject skull = new JsonObject();
        skull.addProperty("item", "minecraft:wither_skeleton_skull");
        skull.addProperty("count", 3);
        witherItems.add(skull);

        wither.add("items", witherItems);
        wither.add("fluids", new JsonArray());
        ingredients.add(wither);

        // Example: Ender Dragon
        JsonObject dragon = new JsonObject();
        dragon.addProperty("mobName", "minecraft:ender_dragon");
        JsonArray dragonItems = new JsonArray();

        JsonObject crystal = new JsonObject();
        crystal.addProperty("item", "minecraft:end_crystal");
        crystal.addProperty("count", 4);
        dragonItems.add(crystal);

        dragon.add("items", dragonItems);
        dragon.add("fluids", new JsonArray());
        ingredients.add(dragon);

        root.add("ingredients", ingredients);

        return root;
    }

    /**
     * Parse JSON and populate repository
     */
    private static void parseIngredients(@Nonnull JsonObject root, @Nonnull SpawnRecipeRepository repository) {
        // Clear existing recipes
        repository.clear();

        // Parse default recipe
        if (root.has("default")) {
            JsonObject defaultObj = root.getAsJsonObject("default");
            SpawnRecipe defaultRecipe = parseRecipe(defaultObj);
            repository.setDefaultRecipe(defaultRecipe);
            Woot.LOGGER.debug("Loaded default recipe: {}", defaultRecipe);
        }

        // Parse specific mob recipes
        if (root.has("ingredients")) {
            JsonArray ingredients = root.getAsJsonArray("ingredients");
            for (JsonElement element : ingredients) {
                if (!element.isJsonObject()) continue;

                JsonObject mobObj = element.getAsJsonObject();
                if (!mobObj.has("mobName")) {
                    Woot.LOGGER.warn("Ingredient entry missing mobName, skipping");
                    continue;
                }

                String mobName = mobObj.get("mobName").getAsString();
                SpawnRecipe recipe = parseRecipe(mobObj);
                repository.register(mobName, recipe);
                Woot.LOGGER.info("Loaded recipe for {}: {} items, {} fluids, {} drops",
                    mobName, recipe.getItems().size(), recipe.getFluids().size(), recipe.getDrops().size());
            }
        }
    }

    /**
     * Parse a single recipe from JSON
     */
    @Nonnull
    private static SpawnRecipe parseRecipe(@Nonnull JsonObject obj) {
        SpawnRecipe.Builder builder = new SpawnRecipe.Builder();

        // Parse efficiency flag
        if (obj.has("efficiency")) {
            builder.setEfficiency(obj.get("efficiency").getAsBoolean());
        }

        // Parse items
        if (obj.has("items")) {
            JsonArray items = obj.getAsJsonArray("items");
            for (JsonElement element : items) {
                if (!element.isJsonObject()) continue;

                JsonObject itemObj = element.getAsJsonObject();
                ItemStack stack = parseItemStack(itemObj);
                if (!stack.isEmpty()) {
                    builder.addItem(stack);
                }
            }
        }

        // Parse fluids
        if (obj.has("fluids")) {
            JsonArray fluids = obj.getAsJsonArray("fluids");
            for (JsonElement element : fluids) {
                if (!element.isJsonObject()) continue;

                JsonObject fluidObj = element.getAsJsonObject();
                FluidStack stack = parseFluidStack(fluidObj);
                if (!stack.isEmpty()) {
                    builder.addFluid(stack);
                }
            }
        }

        // Parse configured drops (if specified)
        if (obj.has("drops")) {
            JsonArray drops = obj.getAsJsonArray("drops");
            for (JsonElement element : drops) {
                if (!element.isJsonObject()) continue;

                JsonObject dropObj = element.getAsJsonObject();
                ItemStack stack = parseItemStack(dropObj);
                if (!stack.isEmpty()) {
                    builder.addDrop(stack);
                }
            }
        }

        // Parse tier override (if specified)
        if (obj.has("tier")) {
            int tierInt = obj.get("tier").getAsInt();
            ipsis.woot.multiblock.EnumMobFactoryTier tier = ipsis.woot.util.MobTierCalculator.getTierFromInt(tierInt);
            builder.setRequiredTier(tier);
            Woot.LOGGER.debug("Explicit tier override: {}", tier);
        }

        return builder.build();
    }

    /**
     * Parse ItemStack from JSON
     */
    @Nonnull
    private static ItemStack parseItemStack(@Nonnull JsonObject obj) {
        if (!obj.has("item")) {
            return ItemStack.EMPTY;
        }

        String itemId = obj.get("item").getAsString();
        ResourceLocation itemRL = ResourceLocation.tryParse(itemId);
        if (itemRL == null) {
            Woot.LOGGER.warn("Invalid item ID: {}", itemId);
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(itemRL);
        if (item == null) {
            Woot.LOGGER.warn("Unknown item: {}", itemId);
            return ItemStack.EMPTY;
        }

        int count = obj.has("count") ? obj.get("count").getAsInt() : 1;

        ItemStack stack = new ItemStack(item, count);

        // TODO: Add NBT support if needed
        // if (obj.has("nbt")) { ... }

        return stack;
    }

    /**
     * Parse FluidStack from JSON
     */
    @Nonnull
    private static FluidStack parseFluidStack(@Nonnull JsonObject obj) {
        if (!obj.has("fluid")) {
            return FluidStack.EMPTY;
        }

        String fluidId = obj.get("fluid").getAsString();
        ResourceLocation fluidRL = ResourceLocation.tryParse(fluidId);
        if (fluidRL == null) {
            Woot.LOGGER.warn("Invalid fluid ID: {}", fluidId);
            return FluidStack.EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.get(fluidRL);
        if (fluid == null) {
            Woot.LOGGER.warn("Unknown fluid: {}", fluidId);
            return FluidStack.EMPTY;
        }

        int amount = obj.has("mb") ? obj.get("mb").getAsInt() : 1000;

        return new FluidStack(fluid, amount);
    }
}
