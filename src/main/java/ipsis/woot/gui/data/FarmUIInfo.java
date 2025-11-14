package ipsis.woot.gui.data;

import ipsis.woot.multiblock.EnumMobFactoryTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object for Factory Heart GUI
 * Contains all information needed to render the GUI display
 */
public class FarmUIInfo {

    // Configuration
    private EnumMobFactoryTier tier = EnumMobFactoryTier.TIER_I;
    private Component mobName = Component.empty();
    private int mobCount = 0;

    // Recipe requirements
    private long recipeTotalPower = 0;
    private int recipeTotalTime = 0;
    private int recipePowerPerTick = 0;

    // Current state
    private boolean isRunning = false;
    private long consumedPower = 0;
    private boolean missingIngredients = false;

    // Power storage
    private int powerStored = 0;
    private int powerCapacity = 0;

    // Ingredients and drops
    private final List<ItemStack> ingredientsItems = new ArrayList<>();
    private final List<FluidStack> ingredientsFluids = new ArrayList<>();
    private final List<ItemStack> drops = new ArrayList<>();

    // Upgrades (stubbed for future implementation)
    private final UpgradeUIInfo upgradeUIInfo = new UpgradeUIInfo();

    // Validity flag
    private boolean isValid = false;

    // Getters
    public EnumMobFactoryTier getTier() { return tier; }
    public Component getMobName() { return mobName; }
    public int getMobCount() { return mobCount; }
    public long getRecipeTotalPower() { return recipeTotalPower; }
    public int getRecipeTotalTime() { return recipeTotalTime; }
    public int getRecipePowerPerTick() { return recipePowerPerTick; }
    public boolean isRunning() { return isRunning; }
    public long getConsumedPower() { return consumedPower; }
    public boolean hasMissingIngredients() { return missingIngredients; }
    public int getPowerStored() { return powerStored; }
    public int getPowerCapacity() { return powerCapacity; }
    public List<ItemStack> getIngredientsItems() { return ingredientsItems; }
    public List<FluidStack> getIngredientsFluids() { return ingredientsFluids; }
    public List<ItemStack> getDrops() { return drops; }
    public UpgradeUIInfo getUpgradeUIInfo() { return upgradeUIInfo; }
    public boolean isValid() { return isValid; }

    // Setters
    public void setTier(EnumMobFactoryTier tier) { this.tier = tier; }
    public void setMobName(Component mobName) { this.mobName = mobName; }
    public void setMobCount(int mobCount) { this.mobCount = mobCount; }
    public void setRecipeTotalPower(long recipeTotalPower) { this.recipeTotalPower = recipeTotalPower; }
    public void setRecipeTotalTime(int recipeTotalTime) { this.recipeTotalTime = recipeTotalTime; }
    public void setRecipePowerPerTick(int recipePowerPerTick) { this.recipePowerPerTick = recipePowerPerTick; }
    public void setRunning(boolean running) { isRunning = running; }
    public void setConsumedPower(long consumedPower) { this.consumedPower = consumedPower; }
    public void setMissingIngredients(boolean missingIngredients) { this.missingIngredients = missingIngredients; }
    public void setPowerStored(int powerStored) { this.powerStored = powerStored; }
    public void setPowerCapacity(int powerCapacity) { this.powerCapacity = powerCapacity; }
    public void setValid(boolean valid) { isValid = valid; }

    // Helper methods
    public void addIngredientItem(ItemStack itemStack) {
        ingredientsItems.add(itemStack);
    }

    public void addIngredientFluid(FluidStack fluidStack) {
        ingredientsFluids.add(fluidStack);
    }

    public void addDrop(ItemStack itemStack) {
        drops.add(itemStack);
    }

    public void clear() {
        ingredientsItems.clear();
        ingredientsFluids.clear();
        drops.clear();
        isValid = false;
    }
}
