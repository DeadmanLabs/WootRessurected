package ipsis.woot.blocks;

import ipsis.woot.blockentities.AnvilBlockEntity;
import ipsis.woot.blockentities.WootBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Stygian Iron Anvil Block
 * Used for special crafting recipes activated with the YahHammer
 */
public class AnvilBlock extends Block implements EntityBlock {

    public AnvilBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnvilBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AnvilBlockEntity anvilBE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Check if player is holding the YahHammer
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(ipsis.woot.Woot.YAH_HAMMER.get())) {
            // TODO: Implement anvil crafting logic
            // For now, just acknowledge the interaction
            return ItemInteractionResult.SUCCESS;
        }

        // If holding an item and anvil is empty, place the item as base
        if (!heldItem.isEmpty() && !anvilBE.hasBaseItem()) {
            ItemStack baseItem = heldItem.copy();
            baseItem.setCount(1);
            anvilBE.setBaseItem(baseItem);

            if (!player.isCreative()) {
                heldItem.shrink(1);
            }

            return ItemInteractionResult.SUCCESS;
        }

        // If anvil has a base item and player is empty-handed, retrieve the base item
        if (heldItem.isEmpty() && anvilBE.hasBaseItem()) {
            ItemStack baseItem = anvilBE.getBaseItem();
            player.setItemInHand(hand, baseItem.copy());
            anvilBE.clearBaseItem();
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AnvilBlockEntity anvilBE) {
                // Drop the base item when the anvil is broken
                if (anvilBE.hasBaseItem()) {
                    Block.popResource(level, pos, anvilBE.getBaseItem());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
