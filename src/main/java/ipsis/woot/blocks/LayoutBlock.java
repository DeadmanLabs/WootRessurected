package ipsis.woot.blocks;

import com.mojang.serialization.MapCodec;
import ipsis.woot.blockentities.LayoutBlockEntity;
import ipsis.woot.blockentities.WootBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory Layout Block
 * Shows ghost block preview of factory structure based on selected tier
 * Right-click to cycle tiers
 */
public class LayoutBlock extends BaseEntityBlock {

    public static final MapCodec<LayoutBlock> CODEC = simpleCodec(LayoutBlock::new);

    public LayoutBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new LayoutBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, WootBlockEntities.LAYOUT.get(),
            (lvl, pos, st, be) -> LayoutBlockEntity.serverTick(lvl, pos, st, be));
    }

    @Override
    @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    @Nonnull
    protected InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                               @Nonnull Player player, @Nonnull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof LayoutBlockEntity layoutBE) {
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown()) {
                    // Shift+right-click: Cycle to previous tier
                    layoutBE.cyclePreviousTier();
                    player.displayClientMessage(
                        Component.literal("Layout: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("Tier " + layoutBE.getSelectedTier().getLevel())
                                .withStyle(ChatFormatting.GOLD)),
                        true
                    );
                } else {
                    // Right-click: Cycle to next tier
                    layoutBE.cycleNextTier();
                    player.displayClientMessage(
                        Component.literal("Layout: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("Tier " + layoutBE.getSelectedTier().getLevel())
                                .withStyle(ChatFormatting.GOLD)),
                        true
                    );
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
