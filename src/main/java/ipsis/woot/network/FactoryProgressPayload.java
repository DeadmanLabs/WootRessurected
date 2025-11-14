package ipsis.woot.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

import static ipsis.woot.Woot.MODID;

/**
 * Server → Client continuous progress update
 * Lightweight packet for real-time progress bar updates
 * Sent periodically while GUI is open (e.g., every 10 ticks)
 */
public record FactoryProgressPayload(
    BlockPos pos,
    long consumedPower,
    int powerStored,
    boolean isRunning,
    boolean missingIngredients
) implements CustomPacketPayload {

    public static final Type<FactoryProgressPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(MODID, "factory_progress")
    );

    public static final StreamCodec<ByteBuf, FactoryProgressPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        FactoryProgressPayload::pos,
        ByteBufCodecs.VAR_LONG,
        FactoryProgressPayload::consumedPower,
        ByteBufCodecs.VAR_INT,
        FactoryProgressPayload::powerStored,
        ByteBufCodecs.BOOL,
        FactoryProgressPayload::isRunning,
        ByteBufCodecs.BOOL,
        FactoryProgressPayload::missingIngredients,
        FactoryProgressPayload::new
    );

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
