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
 * Client → Server request for farm UI information
 * Sent when player opens the Factory Heart GUI
 */
public record RequestFarmInfoPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<RequestFarmInfoPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(MODID, "request_farm_info")
    );

    public static final StreamCodec<ByteBuf, RequestFarmInfoPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        RequestFarmInfoPayload::pos,
        RequestFarmInfoPayload::new
    );

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
