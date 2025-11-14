package ipsis.woot.network;

import io.netty.buffer.ByteBuf;
import ipsis.woot.gui.data.FarmUIInfo;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static ipsis.woot.Woot.MODID;

/**
 * Server → Client response with complete farm UI information
 * Sent in response to RequestFarmInfoPayload
 */
public record FarmInfoPayload(
    BlockPos pos,
    EnumMobFactoryTier tier,
    Component mobName,
    int mobCount,
    long recipeTotalPower,
    int recipeTotalTime,
    int recipePowerPerTick,
    boolean isRunning,
    long consumedPower,
    boolean missingIngredients,
    int powerStored,
    int powerCapacity,
    List<ItemStack> ingredientsItems,
    List<FluidStack> ingredientsFluids,
    List<ItemStack> drops,
    boolean isValid
) implements CustomPacketPayload {

    public static final Type<FarmInfoPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(MODID, "farm_info")
    );

    // Custom StreamCodec for EnumMobFactoryTier
    private static final StreamCodec<ByteBuf, EnumMobFactoryTier> TIER_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buffer, EnumMobFactoryTier tier) {
            ByteBufCodecs.INT.encode(buffer, tier.ordinal());
        }

        @Override
        public EnumMobFactoryTier decode(ByteBuf buffer) {
            int ordinal = ByteBufCodecs.INT.decode(buffer);
            EnumMobFactoryTier[] values = EnumMobFactoryTier.values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : EnumMobFactoryTier.TIER_I;
        }
    };

    // Custom StreamCodec for FluidStack list
    private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidStack>> FLUID_STACK_LIST_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buffer, List<FluidStack> fluids) {
            ByteBufCodecs.VAR_INT.encode(buffer, fluids.size());
            for (FluidStack fluid : fluids) {
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
            }
        }

        @Override
        public List<FluidStack> decode(RegistryFriendlyByteBuf buffer) {
            int size = ByteBufCodecs.VAR_INT.decode(buffer);
            List<FluidStack> fluids = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                fluids.add(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            }
            return fluids;
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, FarmInfoPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buffer, FarmInfoPayload payload) {
            BlockPos.STREAM_CODEC.encode(buffer, payload.pos);
            TIER_CODEC.encode(buffer, payload.tier);
            ComponentSerialization.STREAM_CODEC.encode(buffer, payload.mobName);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.mobCount);
            ByteBufCodecs.VAR_LONG.encode(buffer, payload.recipeTotalPower);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.recipeTotalTime);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.recipePowerPerTick);
            ByteBufCodecs.BOOL.encode(buffer, payload.isRunning);
            ByteBufCodecs.VAR_LONG.encode(buffer, payload.consumedPower);
            ByteBufCodecs.BOOL.encode(buffer, payload.missingIngredients);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.powerStored);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.powerCapacity);
            ItemStack.LIST_STREAM_CODEC.encode(buffer, payload.ingredientsItems);
            FLUID_STACK_LIST_CODEC.encode(buffer, payload.ingredientsFluids);
            ItemStack.LIST_STREAM_CODEC.encode(buffer, payload.drops);
            ByteBufCodecs.BOOL.encode(buffer, payload.isValid);
        }

        @Override
        public FarmInfoPayload decode(RegistryFriendlyByteBuf buffer) {
            return new FarmInfoPayload(
                BlockPos.STREAM_CODEC.decode(buffer),
                TIER_CODEC.decode(buffer),
                ComponentSerialization.STREAM_CODEC.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_LONG.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.VAR_LONG.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ItemStack.LIST_STREAM_CODEC.decode(buffer),
                FLUID_STACK_LIST_CODEC.decode(buffer),
                ItemStack.LIST_STREAM_CODEC.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer)
            );
        }
    };

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Convert to FarmUIInfo DTO
     */
    public FarmUIInfo toFarmUIInfo() {
        FarmUIInfo info = new FarmUIInfo();
        info.setTier(tier);
        info.setMobName(mobName);
        info.setMobCount(mobCount);
        info.setRecipeTotalPower(recipeTotalPower);
        info.setRecipeTotalTime(recipeTotalTime);
        info.setRecipePowerPerTick(recipePowerPerTick);
        info.setRunning(isRunning);
        info.setConsumedPower(consumedPower);
        info.setMissingIngredients(missingIngredients);
        info.setPowerStored(powerStored);
        info.setPowerCapacity(powerCapacity);
        info.setValid(isValid);

        ingredientsItems.forEach(info::addIngredientItem);
        ingredientsFluids.forEach(info::addIngredientFluid);
        drops.forEach(info::addDrop);

        return info;
    }

    /**
     * Create from FarmUIInfo DTO
     */
    public static FarmInfoPayload fromFarmUIInfo(BlockPos pos, FarmUIInfo info) {
        return new FarmInfoPayload(
            pos,
            info.getTier(),
            info.getMobName(),
            info.getMobCount(),
            info.getRecipeTotalPower(),
            info.getRecipeTotalTime(),
            info.getRecipePowerPerTick(),
            info.isRunning(),
            info.getConsumedPower(),
            info.hasMissingIngredients(),
            info.getPowerStored(),
            info.getPowerCapacity(),
            new ArrayList<>(info.getIngredientsItems()),
            new ArrayList<>(info.getIngredientsFluids()),
            new ArrayList<>(info.getDrops()),
            info.isValid()
        );
    }
}
