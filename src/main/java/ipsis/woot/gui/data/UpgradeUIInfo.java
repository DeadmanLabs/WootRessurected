package ipsis.woot.gui.data;

import ipsis.woot.farming.EnumFarmUpgrade;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for upgrade information
 * Tracks installed upgrades and their tiers for GUI display
 */
public class UpgradeUIInfo {

    // Map of upgrade type to tier level (1-3)
    private final Map<EnumFarmUpgrade, Integer> upgrades = new HashMap<>();

    public UpgradeUIInfo() {
        // Default constructor
    }

    /**
     * Check if any upgrades are installed
     */
    public boolean hasUpgrades() {
        return !upgrades.isEmpty();
    }

    /**
     * Add an upgrade to the info
     * @param type Upgrade type
     * @param tier Upgrade tier (1-3)
     */
    public void addUpgrade(EnumFarmUpgrade type, int tier) {
        upgrades.put(type, tier);
    }

    /**
     * Get the tier of a specific upgrade
     * @param type Upgrade type
     * @return Tier level (1-3), or 0 if not installed
     */
    public int getUpgradeTier(EnumFarmUpgrade type) {
        return upgrades.getOrDefault(type, 0);
    }

    /**
     * Check if a specific upgrade is installed
     */
    public boolean hasUpgrade(EnumFarmUpgrade type) {
        return upgrades.containsKey(type);
    }

    /**
     * Get all installed upgrades
     */
    public Map<EnumFarmUpgrade, Integer> getUpgrades() {
        return upgrades;
    }

    /**
     * Get the count of installed upgrades
     */
    public int getUpgradeCount() {
        return upgrades.size();
    }

    /**
     * Clear all upgrade information
     */
    public void clear() {
        upgrades.clear();
    }

    /**
     * Update from FarmSetup upgrade data
     * @param farmUpgrades Map of upgrades from FarmSetup
     */
    public void updateFromFarmSetup(Map<EnumFarmUpgrade, Integer> farmUpgrades) {
        upgrades.clear();
        upgrades.putAll(farmUpgrades);
    }

    @Override
    public String toString() {
        return String.format("UpgradeUIInfo{upgrades=%d}", upgrades.size());
    }
}
