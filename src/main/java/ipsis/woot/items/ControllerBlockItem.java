package ipsis.woot.items;

import ipsis.woot.items.data.EnderShardData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Factory Controller Block Item
 * Shows programmed mob information in tooltip
 */
public class ControllerBlockItem extends BlockItem {

    public ControllerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable TooltipContext context,
                               @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // Check if controller has programmed mob data
        EnderShardData shardData = stack.get(WootDataComponents.ENDER_SHARD.get());
        if (shardData != null && shardData.isValid()) {
            tooltip.add(Component.translatable("info.woot.controller.programmed",
                shardData.displayName()).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("info.woot.controller.unprogrammed")
                .withStyle(ChatFormatting.GRAY));
        }
    }
}
