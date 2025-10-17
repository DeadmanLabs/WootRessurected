package ipsis.woot.blocks;

import ipsis.woot.blockentities.AnvilBlockEntity;
import ipsis.woot.blockentities.WootBlockEntities;
import ipsis.woot.crafting.AnvilRecipe;
import ipsis.woot.util.AnvilHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
            // Try to perform anvil crafting
            tryCraft(level, pos, player, anvilBE);
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

    /**
     * Attempt to craft an item using the anvil
     */
    private void tryCraft(Level level, BlockPos pos, Player player, AnvilBlockEntity anvilBE) {
        // Check if anvil is hot (has magma block below)
        if (!AnvilHelper.isAnvilHot(level, pos)) {
            player.displayClientMessage(Component.translatable("chat.woot.anvil.nomagma"), true);
            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        // Get the base item on the anvil
        ItemStack baseItem = anvilBE.getBaseItem();
        if (baseItem.isEmpty()) {
            player.displayClientMessage(Component.translatable("chat.woot.anvil.nobase"), true);
            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        // Collect item entities around the anvil
        List<ItemEntity> entityItems = AnvilHelper.getItems(level, pos);
        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemEntity itemEntity : entityItems) {
            ingredients.add(itemEntity.getItem());
        }

        // Try to find a matching recipe
        AnvilRecipe recipe = AnvilHelper.findRecipe(level, baseItem, ingredients);
        if (recipe == null) {
            player.displayClientMessage(Component.translatable("chat.woot.anvil.invalid"), true);
            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        // Recipe found! Craft the item
        ItemStack output = recipe.assemble(null, level.registryAccess());

        // Clear base item if not preserved
        if (!recipe.shouldPreserveBase()) {
            anvilBE.clearBaseItem();
        }

        // Consume ingredients
        AnvilHelper.consumeIngredients(entityItems, recipe);

        // Play anvil hit sound
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        // Spawn the output item in the world
        ItemEntity outputEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, output);
        level.addFreshEntity(outputEntity);

        // Success message
        player.displayClientMessage(Component.translatable("chat.woot.anvil.success"), true);
    }
}
