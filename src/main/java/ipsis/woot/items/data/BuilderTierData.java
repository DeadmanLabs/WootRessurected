package ipsis.woot.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ipsis.woot.multiblock.EnumMobFactoryTier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;

/**
 * Data component for factory builder items
 * Stores the currently selected tier for ghost block preview
 */
public record BuilderTierData(int tierLevel) {

    /**
     * Codec for JSON/NBT serialization
     */
    public static final Codec<BuilderTierData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("tierLevel").forGetter(BuilderTierData::tierLevel)
        ).apply(instance, BuilderTierData::new)
    );

    /**
     * Stream codec for network synchronization
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, BuilderTierData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        BuilderTierData::tierLevel,
        BuilderTierData::new
    );

    /**
     * Create default builder data (Tier I)
     */
    @Nonnull
    public static BuilderTierData createDefault() {
        return new BuilderTierData(1);
    }

    /**
     * Get the tier enum
     */
    @Nonnull
    public EnumMobFactoryTier getTier() {
        return EnumMobFactoryTier.byLevel(tierLevel);
    }

    /**
     * Cycle to next tier (wraps around to Tier I after Tier IV)
     */
    @Nonnull
    public BuilderTierData cycleNext() {
        int nextLevel = tierLevel + 1;
        if (nextLevel > 4) {
            nextLevel = 1;
        }
        return new BuilderTierData(nextLevel);
    }

    /**
     * Cycle to previous tier (wraps around to Tier IV after Tier I)
     */
    @Nonnull
    public BuilderTierData cyclePrevious() {
        int prevLevel = tierLevel - 1;
        if (prevLevel < 1) {
            prevLevel = 4;
        }
        return new BuilderTierData(prevLevel);
    }

    /**
     * Check if this tier level is valid (1-4)
     */
    public boolean isValid() {
        return tierLevel >= 1 && tierLevel <= 4;
    }
}
