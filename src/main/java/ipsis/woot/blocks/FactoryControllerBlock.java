package ipsis.woot.blocks;

import com.mojang.serialization.MapCodec;
import ipsis.woot.blockentities.FactoryControllerBlockEntity;
import ipsis.woot.blockentities.FactoryHeartBlockEntity;
import ipsis.woot.blockentities.WootBlockEntities;
import ipsis.woot.util.WootBlockNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory Controller Block - UI and Programming Point
 * Phase 1: Basic block with block entity
 * Phase 6: Will add GUI for mob programming and status display
 */
public class FactoryControllerBlock extends BaseEntityBlock {

    public static final MapCodec<FactoryControllerBlock> CODEC = simpleCodec(FactoryControllerBlock::new);

    public FactoryControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new FactoryControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, WootBlockEntities.FACTORY_CONTROLLER.get(),
            (lvl, pos, st, be) -> FactoryControllerBlockEntity.serverTick(lvl, pos, st, be));
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
            if (blockEntity instanceof FactoryControllerBlockEntity controller) {
                controller.setHeartPos(null);
            }
            // Notify adjacent Woot blocks to revalidate their structures
            WootBlockNotifier.notifyNearbyHearts(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
