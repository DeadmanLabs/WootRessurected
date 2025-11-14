package ipsis.woot.client;

import com.mojang.blaze3d.vertex.PoseStack;
import ipsis.woot.Woot;
import ipsis.woot.blockentities.LayoutBlockEntity;
import ipsis.woot.multiblock.EnumMobFactoryModule;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import ipsis.woot.multiblock.FactoryPatternRepository;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;

/**
 * Renders ghost blocks for the factory layout block
 */
public class LayoutBlockEntityRenderer implements BlockEntityRenderer<LayoutBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public LayoutBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(@Nonnull LayoutBlockEntity layoutBE, float partialTick, @Nonnull PoseStack poseStack,
                       @Nonnull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        EnumMobFactoryTier tier = layoutBE.getSelectedTier();

        // Get the factory pattern for this tier
        String[][] pattern = switch (tier) {
            case TIER_I -> FactoryPatternRepository.TIER_I_PATTERN;
            case TIER_II -> FactoryPatternRepository.TIER_II_PATTERN;
            case TIER_III -> FactoryPatternRepository.TIER_III_PATTERN;
            case TIER_IV -> FactoryPatternRepository.TIER_IV_PATTERN;
        };

        int height = pattern.length;
        if (height == 0) return;

        int depth = pattern[0].length;
        if (depth == 0) return;

        int width = pattern[0][0].length();

        // Center offsets
        int xOffset = -(width / 2);
        int yOffset = 0;
        int zOffset = -(depth / 2);

        // Render each block in the pattern
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    char c = pattern[y][z].charAt(x);

                    // Skip air/empty spaces
                    if (c == '-') {
                        continue;
                    }

                    // Handle origin marker - render factory heart here
                    if (c == 'x') {
                        int worldX = x + xOffset;
                        int worldY = y + yOffset;
                        int worldZ = z + zOffset;
                        renderGhostBlock(poseStack, bufferSource, Woot.FACTORY_HEART.get(), worldX, worldY, worldZ, combinedLight, combinedOverlay);
                        continue;
                    }

                    EnumMobFactoryModule module = EnumMobFactoryModule.byChar(c);
                    if (module == null) {
                        continue;
                    }

                    // Get the block for this module
                    Block block = getBlockForModule(module);
                    if (block == null) {
                        continue;
                    }

                    // Calculate world position relative to layout block
                    int worldX = x + xOffset;
                    int worldY = y + yOffset;
                    int worldZ = z + zOffset;

                    // Render the ghost block
                    renderGhostBlock(poseStack, bufferSource, block, worldX, worldY, worldZ, combinedLight, combinedOverlay);
                }
            }
        }

        // Add controller at origin.up(2).offset(facing, -1)
        // Place it 2 blocks up and behind the heart (south direction, +Z as per pattern)
        renderGhostBlock(poseStack, bufferSource, Woot.CONTROLLER.get(), 0, 2, 1, combinedLight, combinedOverlay);
    }

    /**
     * Map a factory module to its corresponding block
     */
    private Block getBlockForModule(EnumMobFactoryModule module) {
        return switch (module) {
            case STRUCTURE_BLOCK_1 -> Woot.STRUCTURE_BLOCK_1.get();
            case STRUCTURE_BLOCK_2 -> Woot.STRUCTURE_BLOCK_2.get();
            case STRUCTURE_BLOCK_3 -> Woot.STRUCTURE_BLOCK_3.get();
            case STRUCTURE_BLOCK_4 -> Woot.STRUCTURE_BLOCK_4.get();
            case STRUCTURE_BLOCK_5 -> Woot.STRUCTURE_BLOCK_5.get();
            case STRUCTURE_UPGRADE -> Woot.STRUCTURE_BLOCK_UPGRADE.get();
            case STRUCTURE_TIER_I_CAP -> Woot.STRUCTURE_TIER_I_CAP.get();
            case STRUCTURE_TIER_II_CAP -> Woot.STRUCTURE_TIER_II_CAP.get();
            case STRUCTURE_TIER_III_CAP -> Woot.STRUCTURE_TIER_III_CAP.get();
            case STRUCTURE_TIER_IV_CAP -> Woot.STRUCTURE_TIER_IV_CAP.get();
            default -> null;
        };
    }

    /**
     * Render a ghost block using actual block model at 90% scale
     */
    private void renderGhostBlock(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                                   @Nonnull Block block, int x, int y, int z, int light, int overlay) {
        poseStack.pushPose();

        // Translate to the block position
        poseStack.translate(x, y, z);

        // Center the block (0.5, 0.5, 0.5) then scale down to 90%, then translate back
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.9f, 0.9f, 0.9f);
        poseStack.translate(-0.5, -0.5, -0.5);

        // Get the block state
        BlockState state = block.defaultBlockState();

        // Render the block model
        blockRenderer.renderSingleBlock(state, poseStack, bufferSource, light, overlay, ModelData.EMPTY, null);

        poseStack.popPose();
    }

    /**
     * Override render bounding box to encompass entire structure
     * This prevents ghost blocks from being culled when not looking directly at the layout block
     */
    @Override
    public AABB getRenderBoundingBox(LayoutBlockEntity layoutBE) {
        // Create a large bounding box that encompasses the entire factory structure
        // Using a 20x20x20 box centered on the layout block should cover all tier patterns
        BlockPos pos = layoutBE.getBlockPos();
        return new AABB(
            pos.getX() - 10, pos.getY() - 5, pos.getZ() - 10,
            pos.getX() + 10, pos.getY() + 15, pos.getZ() + 10
        );
    }

    /**
     * Always render this block entity, even when out of normal render distance
     */
    @Override
    public boolean shouldRenderOffScreen(LayoutBlockEntity layoutBE) {
        return true;
    }
}
