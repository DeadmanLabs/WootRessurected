package ipsis.woot.events;

import ipsis.woot.Woot;
import ipsis.woot.config.EnderShardConfig;
import ipsis.woot.items.EnderShardItem;
import ipsis.woot.items.data.EnderShardData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nonnull;

/**
 * Event handler for ender shard programming and death tracking
 */
@EventBusSubscriber(modid = Woot.MODID)
public class EnderShardEventHandler {

    /**
     * Handle entity interaction to program the ender shard
     * Triggered when player attacks an entity with ender shard
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // Only server-side processing
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // Check if damage source is a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Check if entity being hit is a living entity
        if (!(event.getEntity() instanceof LivingEntity targetEntity)) {
            return;
        }

        // Check if player is holding an ender shard
        ItemStack heldItem = player.getMainHandItem();
        if (!EnderShardItem.isEnderShard(heldItem)) {
            return;
        }

        // Check if shard is already programmed
        if (EnderShardItem.isProgrammed(heldItem)) {
            player.displayClientMessage(Component.translatable("chat.woot.endershard.already_programmed"), true);
            // Don't cancel damage - let player continue attacking
            return;
        }

        // Check if entity is valid (has a resource location)
        ResourceLocation entityLocation = EntityType.getKey(targetEntity.getType());
        if (entityLocation == null) {
            player.displayClientMessage(Component.translatable("chat.woot.endershard.failure"), true);
            return;
        }

        // Check if entity can be captured (not blacklisted)
        String entityKey = entityLocation.toString();
        if (!EnderShardConfig.canCapture(entityKey)) {
            player.displayClientMessage(Component.translatable("chat.woot.endershard.failure"), true);
            Woot.LOGGER.debug("Entity {} is blacklisted and cannot be captured", entityKey);
            return;
        }

        // Program the shard
        EnderShardItem.programShard(heldItem, targetEntity);

        // Show success message
        String mobName = targetEntity.getDisplayName().getString();
        player.displayClientMessage(
            Component.translatable("chat.woot.endershard.success", mobName),
            true
        );

        Woot.LOGGER.debug("Programmed ender shard with entity: {} ({})",
            mobName, entityLocation);

        // Cancel the damage - we don't want to hurt the mob when programming
        event.setNewDamage(0);
    }

    /**
     * Handle mob deaths to increment ender shard kill counter
     * Only counts if player has a matching programmed shard in hotbar
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Only server-side processing
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // Check if damage source is a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Check if killed entity is a living entity
        if (!(event.getEntity() instanceof LivingEntity killedEntity)) {
            return;
        }

        // Get entity resource location
        ResourceLocation entityLocation = EntityType.getKey(killedEntity.getType());
        if (entityLocation == null) {
            return;
        }

        String entityKey = entityLocation.toString();

        // Search for matching programmed shard in hotbar (slots 0-8)
        ItemStack matchingShard = findMatchingShardInHotbar(player, entityKey);

        if (matchingShard.isEmpty()) {
            // No matching shard found
            return;
        }

        // Increment death count
        EnderShardItem.incrementDeaths(matchingShard, 1);

        // Get updated data for logging/feedback
        EnderShardData data = EnderShardItem.getProgrammedMob(matchingShard);
        if (data != null) {
            Woot.LOGGER.debug("Incremented death count for {}: {} kills",
                data.displayName(), data.deathCount());

            // Check if shard is now full
            int requiredKills = EnderShardConfig.getKillCount(data.entityKey());
            if (data.deathCount() >= requiredKills) {
                player.displayClientMessage(
                    Component.translatable("chat.woot.endershard.complete", data.displayName()),
                    true
                );
            }
        }
    }

    /**
     * Find a programmed ender shard in player's hotbar that matches the given entity
     */
    @Nonnull
    private static ItemStack findMatchingShardInHotbar(@Nonnull Player player, @Nonnull String entityKey) {
        // Check hotbar slots (0-8)
        for (int i = 0; i <= 8; i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (!EnderShardItem.isEnderShard(stack)) {
                continue;
            }

            if (!EnderShardItem.isProgrammed(stack)) {
                continue;
            }

            // Check if this shard matches the entity
            if (EnderShardItem.isMob(stack, entityKey)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
