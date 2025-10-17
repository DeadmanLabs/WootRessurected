package ipsis.woot.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

/**
 * Recipe input for anvil crafting
 * Contains the base item and surrounding ingredients
 */
public record AnvilRecipeInput(ItemStack baseItem, List<ItemStack> ingredients) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index == 0) {
            return baseItem;
        }
        int ingredientIndex = index - 1;
        if (ingredientIndex >= 0 && ingredientIndex < ingredients.size()) {
            return ingredients.get(ingredientIndex);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1 + ingredients.size();
    }
}
