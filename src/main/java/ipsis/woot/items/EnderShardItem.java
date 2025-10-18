package ipsis.woot.items;

import ipsis.woot.Woot;
import ipsis.woot.config.EnderShardConfig;
import ipsis.woot.items.data.EnderShardData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Ender Shard Item - Programmable shard that stores mob type and kill count
 */
public class EnderShardItem extends Item {

    public EnderShardItem(Properties properties) {
        super(properties);
    }

    /**
     * Check if this is an ender shard item
     */
    public static boolean isEnderShard(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof EnderShardItem;
    }

    /**
     * Check if the ender shard has been programmed with a mob
     */
    public static boolean isProgrammed(@Nonnull ItemStack stack) {
        if (!isEnderShard(stack)) {
            return false;
        }
        EnderShardData data = stack.get(Woot.ENDER_SHARD_DATA.get());
        return data != null && data.isValid();
    }

    /**
     * Check if the ender shard is full (has enough kills)
     */
    public static boolean isFull(@Nonnull ItemStack stack, int requiredKills) {
        if (!isProgrammed(stack)) {
            return false;
        }
        EnderShardData data = stack.get(Woot.ENDER_SHARD_DATA.get());
        return data != null && data.deathCount() >= requiredKills;
    }

    /**
     * Get the programmed mob data
     */
    @Nullable
    public static EnderShardData getProgrammedMob(@Nonnull ItemStack stack) {
        if (!isEnderShard(stack)) {
            return null;
        }
        return stack.get(Woot.ENDER_SHARD_DATA.get());
    }

    /**
     * Program the ender shard with a mob type
     */
    public static void programShard(@Nonnull ItemStack stack, @Nonnull LivingEntity entity) {
        if (!isEnderShard(stack)) {
            return;
        }

        // Get entity type resource location
        ResourceLocation entityLocation = EntityType.getKey(entity.getType());
        String entityKey = entityLocation.toString();
        String displayName = entity.getDisplayName().getString();

        // Create new ender shard data
        EnderShardData data = EnderShardData.create(entityKey, displayName);
        stack.set(Woot.ENDER_SHARD_DATA.get(), data);
    }

    /**
     * Increment the death count on the ender shard
     */
    public static void incrementDeaths(@Nonnull ItemStack stack, int amount) {
        if (!isProgrammed(stack)) {
            return;
        }

        EnderShardData data = stack.get(Woot.ENDER_SHARD_DATA.get());
        if (data != null) {
            EnderShardData newData = data.incrementDeaths(amount);
            stack.set(Woot.ENDER_SHARD_DATA.get(), newData);
        }
    }

    /**
     * Check if this shard matches a specific mob type
     */
    public static boolean isMob(@Nonnull ItemStack stack, @Nonnull String entityKey) {
        EnderShardData data = getProgrammedMob(stack);
        return data != null && data.matches(entityKey);
    }

    /**
     * Check if this shard matches a specific mob type and tag
     */
    public static boolean isMob(@Nonnull ItemStack stack, @Nonnull String entityKey, @Nonnull String tag) {
        EnderShardData data = getProgrammedMob(stack);
        return data != null && data.matches(entityKey, tag);
    }

    /**
     * Show enchantment glint effect when shard is programmed
     */
    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        // Show glint for any programmed shard
        return isProgrammed(stack);
    }

    /**
     * Handle right-click to reset the shard
     * Shift-right-click to clear programming data
     */
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Only reset on shift-right-click
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        // Only reset if programmed
        if (!isProgrammed(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        // Server-side only
        if (!level.isClientSide()) {
            // Clear the programming data
            stack.remove(Woot.ENDER_SHARD_DATA.get());

            // Play a sound
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Show message
            player.displayClientMessage(
                Component.translatable("chat.woot.endershard.reset"),
                true
            );

            Woot.LOGGER.debug("Player {} reset ender shard", player.getName().getString());
        }

        return InteractionResultHolder.success(stack);
    }

    /**
     * Add tooltip information
     */
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        if (!isEnderShard(stack)) {
            return;
        }

        // Add usage instructions
        tooltip.add(Component.translatable("info.woot.endershard.0").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.woot.endershard.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.woot.endershard.2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.woot.endershard.3").withStyle(ChatFormatting.GRAY));

        // Add reset instruction if programmed
        if (isProgrammed(stack)) {
            tooltip.add(Component.translatable("info.woot.endershard.reset").withStyle(ChatFormatting.YELLOW));
        }

        EnderShardData data = stack.get(Woot.ENDER_SHARD_DATA.get());

        if (data == null || !data.isValid()) {
            // Unprogrammed shard
            tooltip.add(Component.translatable("info.woot.endershard.unprogrammed")
                .withStyle(ChatFormatting.RED));
        } else {
            // Programmed shard - show mob name and progress
            // Get required kill count from config
            int requiredKills = EnderShardConfig.getKillCount(data.entityKey());
            int currentKills = data.deathCount();

            if (currentKills >= requiredKills) {
                // Shard is full
                tooltip.add(Component.translatable("info.woot.endershard.ready", data.displayName())
                    .withStyle(ChatFormatting.BLUE));
            } else {
                // Shard in progress
                tooltip.add(Component.translatable("info.woot.endershard.programmed",
                    data.displayName(), currentKills, requiredKills)
                    .withStyle(ChatFormatting.RED));
            }

            // Show advanced info if enabled
            if (flag.hasShiftDown()) {
                tooltip.add(Component.literal("Entity: " + data.entityKey())
                    .withStyle(ChatFormatting.DARK_GRAY));
                if (!data.tag().isEmpty()) {
                    tooltip.add(Component.literal("Tag: " + data.tag())
                        .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }
}
