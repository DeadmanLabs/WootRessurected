package ipsis.woot.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EnchantedShardItem extends Item {
    public EnchantedShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
