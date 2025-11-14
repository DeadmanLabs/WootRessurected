package ipsis.woot.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ipsis.woot.multiblock.EnumMobFactoryModule;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import ipsis.woot.multiblock.FactoryPatternRepository;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Client-side renderer for factory ghost blocks
 * Renders translucent, scaled-down preview of factory structure
 */
@OnlyIn(Dist.CLIENT)
public class GhostBlockRenderer {

    // Active preview state
    @Nullable
    private static BlockPos previewOrigin = null;
    @Nullable
    private static EnumMobFactoryTier previewTier = null;
    private static long previewExpiryTime = 0;
    private static final long PREVIEW_DURATION = 10000; // 10 seconds

    /**
     * Set the ghost block preview at the given position
     */
    public static void setPreview(@Nonnull BlockPos origin, @Nonnull EnumMobFactoryTier tier) {
        previewOrigin = origin.immutable();
        previewTier = tier;
        previewExpiryTime = System.currentTimeMillis() + PREVIEW_DURATION;
    }

    /**
     * Clear the current preview
     */
    public static void clearPreview() {
        previewOrigin = null;
        previewTier = null;
        previewExpiryTime = 0;
    }

    /**
     * Check if there's an active preview
     */
    public static boolean hasActivePreview() {
        if (previewOrigin == null || previewTier == null) {
            return false;
        }
        if (System.currentTimeMillis() > previewExpiryTime) {
            clearPreview();
            return false;
        }
        return true;
    }

    /**
     * Render ghost blocks for the current preview
     * Called from level render event
     */
    public static void renderGhostBlocks(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, float partialTick) {
        if (!hasActivePreview()) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null || previewOrigin == null || previewTier == null) {
            return;
        }

        // Get the factory pattern for this tier
        String[][] pattern = switch (previewTier) {
            case TIER_I -> FactoryPatternRepository.TIER_I_PATTERN;
            case TIER_II -> FactoryPatternRepository.TIER_II_PATTERN;
            case TIER_III -> FactoryPatternRepository.TIER_III_PATTERN;
            case TIER_IV -> FactoryPatternRepository.TIER_IV_PATTERN;
        };

        // Render each block in the pattern
        renderPattern(poseStack, bufferSource, previewOrigin, pattern, partialTick);
    }

    /**
     * Render the factory pattern at the given origin
     */
    private static void renderPattern(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                                      @Nonnull BlockPos origin, @Nonnull String[][] pattern, float partialTick) {

        int height = pattern.length;
        if (height == 0) return;

        int depth = pattern[0].length;
        if (depth == 0) return;

        int width = pattern[0][0].length();

        // Center offsets
        int xOffset = -(width / 2);
        int yOffset = 0;
        int zOffset = -(depth / 2);

        // Get render buffers for different module types
        VertexConsumer heartBuffer = bufferSource.getBuffer(RenderType.translucent());
        VertexConsumer structureBuffer = bufferSource.getBuffer(RenderType.translucent());
        VertexConsumer upgradeBuffer = bufferSource.getBuffer(RenderType.translucent());

        // Render each block
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    char c = pattern[y][z].charAt(x);

                    // Skip air/empty spaces and origin marker
                    if (c == '-' || c == 'x') {
                        continue;
                    }

                    EnumMobFactoryModule module = EnumMobFactoryModule.byChar(c);
                    if (module == null) {
                        continue;
                    }

                    // Calculate world position
                    BlockPos pos = origin.offset(x + xOffset, y + yOffset, z + zOffset);

                    // Choose color and buffer based on module type
                    float[] color = getModuleColor(module);
                    VertexConsumer buffer = switch (module) {
                        case STRUCTURE_UPGRADE -> upgradeBuffer;
                        default -> structureBuffer;
                    };

                    // Render ghost block at this position
                    renderGhostBlock(poseStack, buffer, pos, color, partialTick);
                }
            }
        }
    }

    /**
     * Get color for a specific module type
     * Returns RGBA array [r, g, b, a]
     */
    private static float[] getModuleColor(@Nonnull EnumMobFactoryModule module) {
        return switch (module) {
            case STRUCTURE_UPGRADE -> new float[]{0.8f, 0.0f, 1.0f, 0.4f}; // Purple
            case STRUCTURE_TIER_I_CAP -> new float[]{0.0f, 1.0f, 1.0f, 0.3f}; // Cyan
            case STRUCTURE_TIER_II_CAP -> new float[]{0.0f, 1.0f, 0.5f, 0.3f}; // Green-cyan
            case STRUCTURE_TIER_III_CAP -> new float[]{1.0f, 1.0f, 0.0f, 0.3f}; // Yellow
            case STRUCTURE_TIER_IV_CAP -> new float[]{1.0f, 0.5f, 0.0f, 0.3f}; // Orange
            case STRUCTURE_BLOCK_1, STRUCTURE_BLOCK_2, STRUCTURE_BLOCK_3, STRUCTURE_BLOCK_4, STRUCTURE_BLOCK_5 ->
                new float[]{0.6f, 0.6f, 0.6f, 0.3f}; // Gray
            default -> new float[]{1.0f, 1.0f, 1.0f, 0.3f}; // White
        };
    }

    /**
     * Render a single ghost block
     * Block is slightly smaller (0.9x scale) and translucent
     */
    private static void renderGhostBlock(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer buffer,
                                         @Nonnull BlockPos pos, @Nonnull float[] color, float partialTick) {

        // Get camera position for relative rendering
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double camX = camera.x;
        double camY = camera.y;
        double camZ = camera.z;

        poseStack.pushPose();

        // Translate to block position relative to camera
        poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);

        // Scale down slightly (0.05 margin on each side = 0.9 scale)
        poseStack.translate(0.05, 0.05, 0.05);
        poseStack.scale(0.9f, 0.9f, 0.9f);

        // Get transformation matrix
        Matrix4f matrix = poseStack.last().pose();

        // Render cube (6 faces)
        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3];

        // Bottom face (y=0)
        addVertex(buffer, matrix, 0, 0, 0, r, g, b, a);
        addVertex(buffer, matrix, 1, 0, 0, r, g, b, a);
        addVertex(buffer, matrix, 1, 0, 1, r, g, b, a);
        addVertex(buffer, matrix, 0, 0, 1, r, g, b, a);

        // Top face (y=1)
        addVertex(buffer, matrix, 0, 1, 1, r, g, b, a);
        addVertex(buffer, matrix, 1, 1, 1, r, g, b, a);
        addVertex(buffer, matrix, 1, 1, 0, r, g, b, a);
        addVertex(buffer, matrix, 0, 1, 0, r, g, b, a);

        // North face (z=0)
        addVertex(buffer, matrix, 1, 0, 0, r, g, b, a);
        addVertex(buffer, matrix, 0, 0, 0, r, g, b, a);
        addVertex(buffer, matrix, 0, 1, 0, r, g, b, a);
        addVertex(buffer, matrix, 1, 1, 0, r, g, b, a);

        // South face (z=1)
        addVertex(buffer, matrix, 0, 0, 1, r, g, b, a);
        addVertex(buffer, matrix, 1, 0, 1, r, g, b, a);
        addVertex(buffer, matrix, 1, 1, 1, r, g, b, a);
        addVertex(buffer, matrix, 0, 1, 1, r, g, b, a);

        // West face (x=0)
        addVertex(buffer, matrix, 0, 0, 0, r, g, b, a);
        addVertex(buffer, matrix, 0, 0, 1, r, g, b, a);
        addVertex(buffer, matrix, 0, 1, 1, r, g, b, a);
        addVertex(buffer, matrix, 0, 1, 0, r, g, b, a);

        // East face (x=1)
        addVertex(buffer, matrix, 1, 0, 1, r, g, b, a);
        addVertex(buffer, matrix, 1, 0, 0, r, g, b, a);
        addVertex(buffer, matrix, 1, 1, 0, r, g, b, a);
        addVertex(buffer, matrix, 1, 1, 1, r, g, b, a);

        poseStack.popPose();
    }

    /**
     * Add a vertex to the buffer
     */
    private static void addVertex(@Nonnull VertexConsumer buffer, @Nonnull Matrix4f matrix,
                                  float x, float y, float z, float r, float g, float b, float a) {
        buffer.addVertex(matrix, x, y, z)
            .setColor(r, g, b, a);
    }
}
