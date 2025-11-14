package ipsis.woot.util;

import ipsis.woot.blockentities.FactoryHeartBlockEntity;
import ipsis.woot.farmblocks.IFactoryGlueProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Utility class for efficiently notifying factory hearts when Woot blocks are placed or broken.
 * Uses a two-phase approach:
 * Phase 1: Check adjacent blocks for connected factory blocks (fast, O(6))
 * Phase 2: Search for hearts in radius if no adjacent blocks found (fallback, O(n))
 */
public class WootBlockNotifier {

    // Maximum search radius based on largest tier (Tier IV: 11x7x11)
    private static final int MAX_HORIZONTAL_RADIUS = 6;  // 11 / 2 = 5.5, rounded up
    private static final int MAX_VERTICAL_RADIUS = 4;    // 7 / 2 = 3.5, rounded up

    /**
     * Notify nearby factory hearts that a Woot block was placed or broken.
     * This triggers structure revalidation.
     *
     * @param level The world
     * @param pos   The position where the block was placed/broken
     */
    public static void notifyNearbyHearts(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }

        // Phase 1: Check adjacent blocks (fast)
        if (notifyAdjacentHearts(level, pos)) {
            return; // Found connected structure, done
        }

        // Phase 2: No adjacent blocks found, search for hearts in radius
        searchForHearts(level, pos);
    }

    /**
     * Phase 1: Check all 6 adjacent blocks for connected factory blocks.
     * If found, notify their heart.
     *
     * @return true if a connected heart was found and notified
     */
    private static boolean notifyAdjacentHearts(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockEntity be = level.getBlockEntity(adjacentPos);

            if (be instanceof IFactoryGlueProvider provider) {
                BlockPos heartPos = provider.getFactoryGlue().getMaster();
                if (heartPos != null) {
                    BlockEntity heartBE = level.getBlockEntity(heartPos);
                    if (heartBE instanceof FactoryHeartBlockEntity heart) {
                        heart.markStructureDirty();
                        return true; // Found connected structure
                    }
                }
            }
        }
        return false; // No adjacent connected blocks found
    }

    /**
     * Phase 2: Search for hearts in a radius around the changed position.
     * This is a fallback for when blocks are placed near an existing structure
     * but not yet connected.
     */
    private static void searchForHearts(Level level, BlockPos pos) {
        BlockPos.betweenClosed(
            pos.offset(-MAX_HORIZONTAL_RADIUS, -MAX_VERTICAL_RADIUS, -MAX_HORIZONTAL_RADIUS),
            pos.offset(MAX_HORIZONTAL_RADIUS, MAX_VERTICAL_RADIUS, MAX_HORIZONTAL_RADIUS)
        ).forEach(searchPos -> {
            BlockEntity be = level.getBlockEntity(searchPos);
            if (be instanceof FactoryHeartBlockEntity heart) {
                heart.markStructureDirty();
            }
        });
    }
}
