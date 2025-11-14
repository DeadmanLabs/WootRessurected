package ipsis.woot.blocks;

import com.mojang.serialization.MapCodec;
import ipsis.woot.blockentities.ExporterBlockEntity;
import ipsis.woot.util.WootBlockNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory Exporter Block
 * Provides output storage for the factory
 */
public class ExporterBlock extends BaseEntityBlock {

    public static final MapCodec<ExporterBlock> CODEC = simpleCodec(ExporterBlock::new);

    public ExporterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new ExporterBlockEntity(pos, state);
    }

    @Override
    @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            // Notify adjacent Woot blocks to revalidate their structures
            WootBlockNotifier.notifyNearbyHearts(level, pos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ExporterBlockEntity exporter) {
                exporter.clearContent();
            }
            // Notify adjacent Woot blocks to revalidate their structures
            WootBlockNotifier.notifyNearbyHearts(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
