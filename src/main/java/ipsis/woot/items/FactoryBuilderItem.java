package ipsis.woot.items;

import ipsis.woot.blockentities.LayoutBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Factory Builder Item - Automatically builds factory structures
 *
 * Features:
 * - Right-click on Layout Block: Builds the factory structure using items from inventory
 * - TODO: Implement automatic building functionality
 */
public class FactoryBuilderItem extends Item {

    public FactoryBuilderItem(Properties properties) {
        super(properties);
    }

    /**
     * Called when player right-clicks on a block
     * Used to build factory structure at Layout block
     */
    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        // Check if clicking on a Layout block
        if (level.getBlockEntity(clickedPos) instanceof LayoutBlockEntity layoutBE) {
            if (!level.isClientSide()) {
                // TODO: Implement auto-building logic
                // 1. Get the tier from the layout block
                // 2. Get the pattern for that tier
                // 3. Check if player has required items
                // 4. Place blocks and consume items

                player.displayClientMessage(
                    Component.literal("Building functionality not yet implemented!")
                        .withStyle(ChatFormatting.RED),
                    false
                );
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * Add tooltip with usage information
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.literal("Automatically builds factory structures")
            .withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.literal("Right-Click Layout Block: ")
            .withStyle(ChatFormatting.AQUA)
            .append(Component.literal("Build Factory")
                .withStyle(ChatFormatting.WHITE)));

        tooltipComponents.add(Component.literal("(Not yet implemented)")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
