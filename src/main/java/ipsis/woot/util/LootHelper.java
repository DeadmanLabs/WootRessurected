package ipsis.woot.util;

import ipsis.woot.Woot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for accessing mob loot tables and generating drops
 * Ensures compatibility with other mods that modify loot tables
 */
public class LootHelper {

    /**
     * Generate loot drops for a mob using its loot table
     * This simulates killing the mob and collecting its drops
     *
     * @param level Server level
     * @param entityType The type of entity to generate loot for
     * @param lootingLevel Looting enchantment level (0-3)
     * @param count Number of mobs to simulate killing
     * @return List of item stacks dropped
     */
    @Nonnull
    public static List<ItemStack> generateLoot(@Nonnull ServerLevel level,
                                               @Nonnull EntityType<?> entityType,
                                               int lootingLevel,
                                               int count) {
        List<ItemStack> allDrops = new ArrayList<>();

        try {
            // Create a temporary entity to get its loot table
            Entity entity = entityType.create(level);
            if (!(entity instanceof LivingEntity livingEntity)) {
                Woot.LOGGER.warn("Entity type {} is not a LivingEntity, cannot generate loot", entityType);
                return allDrops;
            }

            // Get the loot table location
            ResourceKey<LootTable> lootTableKey = livingEntity.getLootTable();
            if (lootTableKey == null) {
                Woot.LOGGER.warn("Entity {} has no loot table", entityType);
                livingEntity.discard();
                return allDrops;
            }

            // Build loot context
            LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, livingEntity)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic());

            // Add looting level if specified
            if (lootingLevel > 0) {
                builder.withLuck(lootingLevel);
            }

            LootParams lootParams = builder.create(LootContextParamSets.ENTITY);

            // Get loot table and generate drops for each mob
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableKey);
            for (int i = 0; i < count; i++) {
                List<ItemStack> drops = lootTable.getRandomItems(lootParams);
                allDrops.addAll(drops);
            }

            // Clean up temporary entity
            livingEntity.discard();

            Woot.LOGGER.debug("Generated {} item stacks from {} × {} mobs", allDrops.size(), count, entityType);

        } catch (Exception e) {
            Woot.LOGGER.error("Error generating loot for entity type {}: {}", entityType, e.getMessage());
        }

        return allDrops;
    }

    /**
     * Merge item stacks into consolidated stacks
     * Combines identical items into single stacks
     *
     * @param items List of item stacks to merge
     * @return Merged list of item stacks
     */
    @Nonnull
    public static List<ItemStack> mergeItemStacks(@Nonnull List<ItemStack> items) {
        List<ItemStack> merged = new ArrayList<>();

        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }

            boolean foundMatch = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(stack, existing)) {
                    existing.grow(stack.getCount());
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                merged.add(stack.copy());
            }
        }

        return merged;
    }

    /**
     * Calculate drop rate percentage for an item based on loot table
     * This samples the loot table multiple times to estimate probability
     *
     * @param level Server level
     * @param entityType Entity type
     * @param targetItem Item to check drop rate for
     * @param samples Number of samples to take (default: 100)
     * @return Drop rate as percentage (0.0 - 100.0)
     */
    public static float calculateDropRate(@Nonnull ServerLevel level,
                                          @Nonnull EntityType<?> entityType,
                                          @Nonnull ItemStack targetItem,
                                          int samples) {
        int totalDropped = 0;

        for (int i = 0; i < samples; i++) {
            List<ItemStack> drops = generateLoot(level, entityType, 0, 1);
            for (ItemStack drop : drops) {
                if (ItemStack.isSameItem(drop, targetItem)) {
                    totalDropped += drop.getCount();
                }
            }
        }

        return (totalDropped / (float) samples) * 100.0f;
    }
}
