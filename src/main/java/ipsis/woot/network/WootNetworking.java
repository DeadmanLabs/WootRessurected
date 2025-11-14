package ipsis.woot.network;

import ipsis.woot.Woot;
import ipsis.woot.blockentities.FactoryHeartBlockEntity;
import ipsis.woot.gui.data.FarmUIInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static ipsis.woot.Woot.MODID;

/**
 * Network packet registration and handling for Woot
 */
@EventBusSubscriber(modid = MODID)
public class WootNetworking {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        Woot.LOGGER.info("Registering network packets...");

        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Client → Server: Request farm info
        registrar.playToServer(
            RequestFarmInfoPayload.TYPE,
            RequestFarmInfoPayload.STREAM_CODEC,
            WootNetworking::handleRequestFarmInfo
        );

        // Server → Client: Farm info response
        registrar.playToClient(
            FarmInfoPayload.TYPE,
            FarmInfoPayload.STREAM_CODEC,
            WootNetworking::handleFarmInfo
        );

        // Server → Client: Progress updates
        registrar.playToClient(
            FactoryProgressPayload.TYPE,
            FactoryProgressPayload.STREAM_CODEC,
            WootNetworking::handleFactoryProgress
        );

        Woot.LOGGER.info("Network packets registered");
    }

    /**
     * Handle client request for farm information
     * Server-side handler
     */
    private static void handleRequestFarmInfo(RequestFarmInfoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.level().getBlockEntity(payload.pos());
                if (be instanceof FactoryHeartBlockEntity heart) {
                    // Get UI info from heart
                    FarmUIInfo uiInfo = heart.getUIInfo();

                    // Send response back to client
                    FarmInfoPayload response = FarmInfoPayload.fromFarmUIInfo(payload.pos(), uiInfo);
                    context.reply(response);

                    Woot.LOGGER.debug("Sent farm info to client for position {}", payload.pos());
                } else {
                    Woot.LOGGER.warn("RequestFarmInfo received for non-heart block at {}", payload.pos());
                }
            }
        });
    }

    /**
     * Handle farm information response
     * Client-side handler
     */
    private static void handleFarmInfo(FarmInfoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This will be handled by the GUI screen when it's implemented
            // For now, just log receipt
            Woot.LOGGER.debug("Received farm info for position {}", payload.pos());

            // The GUI screen will need to implement a static method or use a client handler
            // to update its display when this packet arrives
        });
    }

    /**
     * Handle factory progress update
     * Client-side handler
     */
    private static void handleFactoryProgress(FactoryProgressPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This will be handled by the GUI screen when it's implemented
            // For now, just log receipt
            Woot.LOGGER.debug("Received factory progress for position {}: power={}, running={}",
                payload.pos(), payload.consumedPower(), payload.isRunning());

            // The GUI screen will need to implement a static method or use a client handler
            // to update progress bars when this packet arrives
        });
    }
}
