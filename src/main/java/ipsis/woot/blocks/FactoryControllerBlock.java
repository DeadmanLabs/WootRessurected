package ipsis.woot.blocks;

import com.mojang.serialization.MapCodec;
import ipsis.woot.blockentities.FactoryControllerBlockEntity;
import ipsis.woot.blockentities.FactoryHeartBlockEntity;
import ipsis.woot.blockentities.WootBlockEntities;
import ipsis.woot.items.WootDataComponents;
import ipsis.woot.items.data.EnderShardData;
import ipsis.woot.util.WootBlockNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                           @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        // Read ender shard data from placed item and program the controller
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FactoryControllerBlockEntity controller) {
            EnderShardData shardData = stack.get(WootDataComponents.ENDER_SHARD.get());
            if (shardData != null) {
                controller.programFromShard(shardData);
                ipsis.woot.Woot.LOGGER.info("Programmed controller at {} with mob: {}", pos, shardData.displayName());
            }
        }
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
