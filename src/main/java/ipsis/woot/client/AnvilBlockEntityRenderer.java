package ipsis.woot.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ipsis.woot.blockentities.AnvilBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the base item on top of the Stygian Iron Anvil
 */
public class AnvilBlockEntityRenderer implements BlockEntityRenderer<AnvilBlockEntity> {

    private final ItemRenderer itemRenderer;

    public AnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AnvilBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        ItemStack baseItem = blockEntity.getBaseItem();
        if (baseItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // Position the item on top of the anvil
        poseStack.translate(0.5, 1.0, 0.5);

        // Rotate the item to lay flat
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        // Scale the item to a reasonable size
        poseStack.scale(0.5F, 0.5F, 0.5F);

        // Get the block entity's light level for proper rendering
        int blockLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());

        // Render the item
        BakedModel model = this.itemRenderer.getModel(baseItem, blockEntity.getLevel(), null, 0);
        this.itemRenderer.render(baseItem, ItemDisplayContext.FIXED, true, poseStack, bufferSource,
                               blockLight, combinedOverlay, model);

        poseStack.popPose();
    }
}
