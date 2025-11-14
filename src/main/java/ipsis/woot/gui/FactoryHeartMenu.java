package ipsis.woot.gui;

import ipsis.woot.blockentities.FactoryHeartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Container menu for Factory Heart GUI
 * Handles data synchronization between server and client
 */
public class FactoryHeartMenu extends AbstractContainerMenu {

    // ContainerData slots
    private static final int SLOT_MOB_COUNT = 0;
    private static final int SLOT_RECIPE_TOTAL_TIME = 1;
    private static final int SLOT_RECIPE_POWER_PER_TICK = 2;
    private static final int SLOT_IS_RUNNING = 3;
    private static final int SLOT_MISSING_INGREDIENTS = 4;
    private static final int SLOT_POWER_STORED = 5;
    private static final int SLOT_POWER_CAPACITY = 6;
    private static final int SLOT_IS_VALID = 7;
    private static final int SLOT_RECIPE_TOTAL_POWER_LOW = 8;
    private static final int SLOT_RECIPE_TOTAL_POWER_HIGH = 9;
    private static final int SLOT_CONSUMED_POWER_LOW = 10;
    private static final int SLOT_CONSUMED_POWER_HIGH = 11;
    private static final int DATA_COUNT = 12;

    private final ContainerData data;
    private final BlockPos heartPos;
    private final Level level;

    /**
     * Client-side constructor
     */
    public FactoryHeartMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, pos, new SimpleContainerData(DATA_COUNT));
    }

    /**
     * Server-side constructor
     */
    public FactoryHeartMenu(int containerId, Inventory playerInventory, BlockPos pos, ContainerData data) {
        super(WootMenuTypes.FACTORY_HEART.get(), containerId);
        this.heartPos = pos;
        this.level = playerInventory.player.level();
        this.data = data;

        // Sync data to client
        addDataSlots(data);

        // No player inventory slots - this is a display-only GUI
        // Players will interact with the factory via importers/exporters
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        BlockEntity be = level.getBlockEntity(heartPos);
        if (!(be instanceof FactoryHeartBlockEntity heart)) {
            return false;
        }

        // Check if player is within 8 blocks
        return player.distanceToSqr(heartPos.getX() + 0.5, heartPos.getY() + 0.5, heartPos.getZ() + 0.5) <= 64.0;
    }

    @Override
    @Nonnull
    public net.minecraft.world.item.ItemStack quickMoveStack(@Nonnull Player player, int index) {
        // This is a display-only GUI with no inventory slots, so no quick move is possible
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    /**
     * Get the heart position
     */
    public BlockPos getHeartPos() {
        return heartPos;
    }

    /**
     * Get the heart block entity (client or server side)
     */
    public FactoryHeartBlockEntity getHeart() {
        BlockEntity be = level.getBlockEntity(heartPos);
        if (be instanceof FactoryHeartBlockEntity heart) {
            return heart;
        }
        return null;
    }

    // Getters for synced data (client-side access)

    public int getMobCount() {
        return data.get(SLOT_MOB_COUNT);
    }

    public int getRecipeTotalTime() {
        return data.get(SLOT_RECIPE_TOTAL_TIME);
    }

    public int getRecipePowerPerTick() {
        return data.get(SLOT_RECIPE_POWER_PER_TICK);
    }

    public boolean isRunning() {
        return data.get(SLOT_IS_RUNNING) != 0;
    }

    public boolean hasMissingIngredients() {
        return data.get(SLOT_MISSING_INGREDIENTS) != 0;
    }

    public int getPowerStored() {
        return data.get(SLOT_POWER_STORED);
    }

    public int getPowerCapacity() {
        return data.get(SLOT_POWER_CAPACITY);
    }

    public boolean isValid() {
        return data.get(SLOT_IS_VALID) != 0;
    }

    public long getRecipeTotalPower() {
        int low = data.get(SLOT_RECIPE_TOTAL_POWER_LOW);
        int high = data.get(SLOT_RECIPE_TOTAL_POWER_HIGH);
        return ((long) high << 32) | (low & 0xFFFFFFFFL);
    }

    public long getConsumedPower() {
        int low = data.get(SLOT_CONSUMED_POWER_LOW);
        int high = data.get(SLOT_CONSUMED_POWER_HIGH);
        return ((long) high << 32) | (low & 0xFFFFFFFFL);
    }

    /**
     * Helper class to create ContainerData from FactoryHeartBlockEntity
     */
    public static ContainerData createDataProvider(FactoryHeartBlockEntity heart) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case SLOT_MOB_COUNT -> heart.getMobCount();
                    case SLOT_RECIPE_TOTAL_TIME -> heart.getRecipeTotalTime();
                    case SLOT_RECIPE_POWER_PER_TICK -> heart.getRecipePowerPerTick();
                    case SLOT_IS_RUNNING -> heart.isRunning() ? 1 : 0;
                    case SLOT_MISSING_INGREDIENTS -> heart.hasMissingIngredients() ? 1 : 0;
                    case SLOT_POWER_STORED -> heart.getPowerStored();
                    case SLOT_POWER_CAPACITY -> heart.getPowerCapacity();
                    case SLOT_IS_VALID -> heart.isValid() ? 1 : 0;
                    case SLOT_RECIPE_TOTAL_POWER_LOW -> (int) (heart.getRecipeTotalPower() & 0xFFFFFFFFL);
                    case SLOT_RECIPE_TOTAL_POWER_HIGH -> (int) (heart.getRecipeTotalPower() >>> 32);
                    case SLOT_CONSUMED_POWER_LOW -> (int) (heart.getConsumedPower() & 0xFFFFFFFFL);
                    case SLOT_CONSUMED_POWER_HIGH -> (int) (heart.getConsumedPower() >>> 32);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Server-side only, no client → server sync needed
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }
}
