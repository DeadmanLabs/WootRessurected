package ipsis.woot.gui.data;

/**
 * Data transfer object for upgrade information
 * Stubbed for future implementation - upgrades not yet ported to 1.21.1
 */
public class UpgradeUIInfo {

    // Placeholder fields for future upgrade system
    private boolean hasUpgrades = false;

    public UpgradeUIInfo() {
        // Default constructor
    }

    public boolean hasUpgrades() {
        return hasUpgrades;
    }

    public void setHasUpgrades(boolean hasUpgrades) {
        this.hasUpgrades = hasUpgrades;
    }

    public void clear() {
        hasUpgrades = false;
    }
}
