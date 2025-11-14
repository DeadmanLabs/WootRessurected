package ipsis.woot.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ipsis.woot.Woot;
import ipsis.woot.config.EnderShardConfig;
import ipsis.woot.items.EnderShardItem;
import ipsis.woot.items.WootDataComponents;
import ipsis.woot.items.data.EnderShardData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Anvil Recipe for the Stygian Iron Anvil
 *
 * An anvil recipe consists of:
 * - A base item (placed on the anvil)
 * - 1-3 ingredient items (thrown around the anvil in a 2x2x2 area)
 * - An output item
 * - A preserveBase flag (whether to return the base item after crafting)
 */
public class AnvilRecipe implements Recipe<RecipeInput> {

    static {
        Woot.LOGGER.info("AnvilRecipe class is being loaded/initialized");
    }

    private final ItemStack baseItem;
    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private final boolean preserveBase;

    public AnvilRecipe(ItemStack baseItem, List<Ingredient> ingredients, ItemStack result, boolean preserveBase) {
        this.baseItem = baseItem;
        this.ingredients = ingredients;
        this.result = result;
        this.preserveBase = preserveBase;
    }

    public ItemStack getBaseItem() {
        return baseItem;
    }

    public List<Ingredient> getAnvilIngredients() {
        return ingredients;
    }

    public boolean shouldPreserveBase() {
        return preserveBase;
    }

    /**
     * Check if the given base item matches this recipe's base item
     * Only checks item type, not NBT/components or count
     */
    public boolean isMatchingBase(ItemStack stack) {
        if (stack.isEmpty() || baseItem.isEmpty()) {
            return false;
        }
        // Only check if it's the same item type (ignore NBT/components and count)
        boolean matches = ItemStack.isSameItem(baseItem, stack);
        Woot.LOGGER.debug("Base item comparison: recipe={}, player={}, matches={}",
            baseItem.getItem(), stack.getItem(), matches);
        return matches;
    }

    /**
     * Check if the collected items match this recipe's ingredients
     */
    public boolean matchesIngredients(List<ItemStack> collectedItems) {
        Woot.LOGGER.debug("Matching ingredients for recipe. Required: {}, Available: {}",
            ingredients.size(), collectedItems.size());

        // Create a mutable copy of collected items to track consumption
        List<ItemStack> remainingItems = collectedItems.stream()
            .map(ItemStack::copy)
            .collect(java.util.stream.Collectors.toList());

        // Try to match each ingredient
        for (int idx = 0; idx < ingredients.size(); idx++) {
            Ingredient ingredient = ingredients.get(idx);
            boolean found = false;

            Woot.LOGGER.debug("Looking for ingredient #{}: {}", idx, ingredient);

            // Find a matching item in the remaining items
            for (int i = 0; i < remainingItems.size(); i++) {
                ItemStack stack = remainingItems.get(i);
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    Woot.LOGGER.debug("  Found match: {} (removing from available list)", stack);
                    remainingItems.remove(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                Woot.LOGGER.debug("  Ingredient #{} NOT found! Missing: {}", idx, ingredient);
                return false; // Missing ingredient
            }
        }

        Woot.LOGGER.debug("All {} ingredients matched successfully!", ingredients.size());
        return true;
    }

    // Recipe interface implementation
    @Override
    public boolean matches(RecipeInput input, Level level) {
        // This is not used for anvil crafting - we use custom matching logic
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    /**
     * Assemble the recipe with data transfer from ender shard to controller
     * Returns null if the ender shard is not programmed or not full
     */
    @Nullable
    public ItemStack assembleWithDataTransfer(AnvilRecipeInput input, HolderLookup.Provider registries) {
        ItemStack output = result.copy();
        ItemStack baseItem = input.baseItem();

        // Check if base is an ender shard
        if (!EnderShardItem.isEnderShard(baseItem)) {
            // Not an ender shard, just return normal output
            return output;
        }

        // Check if ender shard is programmed
        if (!EnderShardItem.isProgrammed(baseItem)) {
            Woot.LOGGER.debug("Ender shard is not programmed, cannot craft controller");
            return null; // Not programmed
        }

        // Get the ender shard data
        EnderShardData shardData = EnderShardItem.getProgrammedMob(baseItem);
        if (shardData == null) {
            Woot.LOGGER.debug("Ender shard data is null, cannot craft controller");
            return null;
        }

        // Check if ender shard is full (has enough kills)
        int requiredKills = EnderShardConfig.getKillCount(shardData.entityKey());
        if (shardData.deathCount() < requiredKills) {
            Woot.LOGGER.debug("Ender shard not full: {}/{} kills", shardData.deathCount(), requiredKills);
            return null; // Not full
        }

        // Transfer ender shard data to output controller
        output.set(WootDataComponents.ENDER_SHARD.get(), shardData);
        Woot.LOGGER.info("Transferred ender shard data to controller: {}", shardData.displayName());

        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Woot.ANVIL_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Woot.ANVIL_RECIPE_TYPE.get();
    }

    /**
     * Serializer for AnvilRecipe
     */
    public static class Serializer implements RecipeSerializer<AnvilRecipe> {

        public Serializer() {
            Woot.LOGGER.info("AnvilRecipe.Serializer constructor called!");
        }

        public static final MapCodec<AnvilRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            Woot.LOGGER.info("AnvilRecipe CODEC is being built/used");
            return instance.group(
                ItemStack.CODEC.fieldOf("base").forGetter(r -> r.baseItem),
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.BOOL.optionalFieldOf("preserve_base", false).forGetter(r -> r.preserveBase)
            ).apply(instance, AnvilRecipe::new);
        });

        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork,
            Serializer::fromNetwork
        );

        @Override
        public MapCodec<AnvilRecipe> codec() {
            Woot.LOGGER.info("AnvilRecipe.Serializer.codec() method called!");
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AnvilRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AnvilRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Woot.LOGGER.info("AnvilRecipe.Serializer.fromNetwork() called!");
            ItemStack baseItem = ItemStack.STREAM_CODEC.decode(buffer);

            int ingredientCount = buffer.readVarInt();
            List<Ingredient> ingredients = new java.util.ArrayList<>();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }

            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            boolean preserveBase = buffer.readBoolean();

            Woot.LOGGER.info("AnvilRecipe.Serializer.fromNetwork() - Successfully decoded recipe");
            return new AnvilRecipe(baseItem, ingredients, result, preserveBase);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, AnvilRecipe recipe) {
            Woot.LOGGER.info("AnvilRecipe.Serializer.toNetwork() called!");
            ItemStack.STREAM_CODEC.encode(buffer, recipe.baseItem);

            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.preserveBase);
            Woot.LOGGER.info("AnvilRecipe.Serializer.toNetwork() - Successfully encoded recipe");
        }
    }
}
