package ipsis.woot.client;

import ipsis.woot.Woot;
import ipsis.woot.blockentities.WootBlockEntities;
import ipsis.woot.client.gui.FactoryHeartScreen;
import ipsis.woot.gui.WootMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-side event handler for MOD bus events
 */
@EventBusSubscriber(modid = Woot.MODID, value = Dist.CLIENT)
public class WootClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WootBlockEntities.ANVIL.get(), AnvilBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(WootBlockEntities.LAYOUT.get(), LayoutBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        Woot.LOGGER.info("Registering GUI screens...");
        event.register(WootMenuTypes.FACTORY_HEART.get(), FactoryHeartScreen::new);
        Woot.LOGGER.info("GUI screens registered");
    }
}
