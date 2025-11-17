package ipsis.woot.blocks;

import com.mojang.serialization.MapCodec;
import ipsis.woot.blockentities.UpgradeBlockEntity;
import ipsis.woot.blockentities.WootBlockEntities;
import ipsis.woot.farming.EnumFarmUpgrade;
import ipsis.woot.util.WootBlockNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
 * Upgrade block with visual feedback when part of formed multiblock
 * Supports vertical stacking for multi-tier upgrades
 */
public class UpgradeBlock extends BaseEntityBlock {

    public static final MapCodec<UpgradeBlock> CODEC = simpleCodec(UpgradeBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private final EnumFarmUpgrade upgradeType;
    private final int tier;

    // Constructor for codec (required by simpleCodec)
    public UpgradeBlock(Properties properties) {
        this(properties, EnumFarmUpgrade.LOOTING, 1); // Defaults for codec
    }

    public UpgradeBlock(Properties properties, EnumFarmUpgrade upgradeType, int tier) {
        super(properties);
        this.upgradeType = upgradeType;
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public EnumFarmUpgrade getUpgradeType() {
        return upgradeType;
    }

    public int getTier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new UpgradeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(type, WootBlockEntities.UPGRADE.get(),
                (lvl, pos, st, blockEntity) -> UpgradeBlockEntity.serverTick((ServerLevel) lvl, pos, st, blockEntity));
    }

    @Override
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
            // Notify adjacent Woot blocks to revalidate their structures
            WootBlockNotifier.notifyNearbyHearts(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
