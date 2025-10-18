package ipsis.woot.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nonnull;

/**
 * Data component for ender shard items
 * Stores information about the programmed mob type and kill progress
 */
public record EnderShardData(
    @Nonnull String entityKey,     // Entity type ID (e.g., "minecraft:zombie")
    @Nonnull String displayName,   // Display name for tooltip
    int deathCount,                 // Number of kills recorded
    @Nonnull String tag             // Optional variant tag (e.g., "baby" for baby zombies)
) {

    /**
     * Codec for JSON/NBT serialization
     */
    public static final Codec<EnderShardData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("entityKey").forGetter(EnderShardData::entityKey),
            Codec.STRING.fieldOf("displayName").forGetter(EnderShardData::displayName),
            Codec.INT.fieldOf("deathCount").forGetter(EnderShardData::deathCount),
            Codec.STRING.optionalFieldOf("tag", "").forGetter(EnderShardData::tag)
        ).apply(instance, EnderShardData::new)
    );

    /**
     * Stream codec for network synchronization
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, EnderShardData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        EnderShardData::entityKey,
        ByteBufCodecs.STRING_UTF8,
        EnderShardData::displayName,
        ByteBufCodecs.INT,
        EnderShardData::deathCount,
        ByteBufCodecs.STRING_UTF8,
        EnderShardData::tag,
        EnderShardData::new
    );

    /**
     * Create ender shard data from an entity type
     */
    public static EnderShardData create(@Nonnull String entityKey, @Nonnull String displayName) {
        return new EnderShardData(entityKey, displayName, 0, "");
    }

    /**
     * Create ender shard data with a variant tag
     */
    public static EnderShardData create(@Nonnull String entityKey, @Nonnull String displayName, @Nonnull String tag) {
        return new EnderShardData(entityKey, displayName, 0, tag);
    }

    /**
     * Check if this data is valid (not empty/uninitialized)
     */
    public boolean isValid() {
        return !entityKey.isEmpty() && !displayName.isEmpty();
    }

    /**
     * Create a new EnderShardData with incremented death count
     */
    public EnderShardData incrementDeaths(int amount) {
        return new EnderShardData(entityKey, displayName, deathCount + amount, tag);
    }

    /**
     * Get the full name including tag (e.g., "minecraft:zombie,baby")
     */
    public String getFullName() {
        if (tag.isEmpty()) {
            return entityKey;
        }
        return entityKey + "," + tag;
    }

    /**
     * Check if this matches a given entity key
     */
    public boolean matches(@Nonnull String otherEntityKey) {
        return entityKey.equals(otherEntityKey);
    }

    /**
     * Check if this matches a given entity key and tag
     */
    public boolean matches(@Nonnull String otherEntityKey, @Nonnull String otherTag) {
        if (otherTag == null) {
            otherTag = "";
        }
        return entityKey.equals(otherEntityKey) && tag.equals(otherTag);
    }

    /**
     * Parse entity key from a ResourceLocation
     */
    public static String getEntityKey(@Nonnull ResourceLocation location) {
        return location.toString();
    }
}
