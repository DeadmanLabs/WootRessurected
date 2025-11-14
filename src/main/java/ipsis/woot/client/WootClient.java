package ipsis.woot.client;

import ipsis.woot.Woot;
import ipsis.woot.blockentities.WootBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-side event handler for MOD bus events
 */
@EventBusSubscriber(modid = Woot.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class WootClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WootBlockEntities.ANVIL.get(), AnvilBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(WootBlockEntities.LAYOUT.get(), LayoutBlockEntityRenderer::new);
    }
}
