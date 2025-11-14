package ipsis.woot.client;

import com.mojang.blaze3d.vertex.PoseStack;
import ipsis.woot.Woot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Client-side event handler for GAME bus events (rendering, etc.)
 */
@EventBusSubscriber(modid = Woot.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class WootClientEvents {

    /**
     * Render ghost blocks for factory builder preview
     * Called after translucent blocks are rendered
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (!GhostBlockRenderer.hasActivePreview()) {
            return;
        }

        System.out.println("[WootClientEvents] Rendering ghost blocks");

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        GhostBlockRenderer.renderGhostBlocks(poseStack, bufferSource, partialTick);

        // Flush the buffers to ensure ghost blocks are rendered
        bufferSource.endBatch();
    }
}
