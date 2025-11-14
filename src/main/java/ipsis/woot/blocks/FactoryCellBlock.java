package ipsis.woot.blocks;

import com.mojang.serialization.MapCodec;
import ipsis.woot.blockentities.FactoryCellBlockEntity;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory Cell Block - Power Storage
 * Phase 1: Basic block with block entity
 * Phase 2: Will add energy aggregation to heart
 */
public class FactoryCellBlock extends BaseEntityBlock {

    public static final MapCodec<FactoryCellBlock> CODEC = simpleCodec(FactoryCellBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private final int tier;

    public FactoryCellBlock(Properties properties) {
        this(properties, 1); // Default tier
    }

    public FactoryCellBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public int getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new FactoryCellBlockEntity(pos, state, tier);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, WootBlockEntities.FACTORY_CELL.get(),
            (lvl, pos, st, be) -> FactoryCellBlockEntity.serverTick(lvl, pos, st, be));
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
            if (blockEntity instanceof FactoryCellBlockEntity cell) {
                cell.setHeartPos(null);
            }
            // Notify adjacent Woot blocks to revalidate their structures
            WootBlockNotifier.notifyNearbyHearts(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
