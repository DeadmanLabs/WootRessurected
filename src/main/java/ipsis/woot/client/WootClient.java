package ipsis.woot.client;

import ipsis.woot.Woot;
import ipsis.woot.blockentities.WootBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Woot.MODID, value = Dist.CLIENT)
public class WootClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WootBlockEntities.ANVIL.get(), AnvilBlockEntityRenderer::new);
    }
}
