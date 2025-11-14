package ipsis.woot.multiblock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the different block types that can appear in a factory multiblock structure
 * Each character in the ASCII pattern maps to one of these module types
 */
public enum EnumMobFactoryModule {
    STRUCTURE_BLOCK_1('g', "Factory Flesh Casing"),
    STRUCTURE_BLOCK_2('r', "Factory Bone Casing"),
    STRUCTURE_BLOCK_3('o', "Factory Blaze Casing"),
    STRUCTURE_BLOCK_4('h', "Factory Ender Casing"),
    STRUCTURE_BLOCK_5('w', "Factory Nether Casing"),
    STRUCTURE_UPGRADE('p', "Factory Upgrade Base"),
    STRUCTURE_TIER_I_CAP('1', "Factory Tier I Cap"),
    STRUCTURE_TIER_II_CAP('2', "Factory Tier II Cap"),
    STRUCTURE_TIER_III_CAP('3', "Factory Tier III Cap"),
    STRUCTURE_TIER_IV_CAP('4', "Factory Tier IV Cap");

    private final char patternChar;
    private final String displayName;

    EnumMobFactoryModule(char patternChar, String displayName) {
        this.patternChar = patternChar;
        this.displayName = displayName;
    }

    /**
     * Get the character used in ASCII patterns for this module
     */
    public char getPatternChar() {
        return patternChar;
    }

    /**
     * Get the display name for this module
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if a character is valid for pattern parsing
     */
    public static boolean isValidChar(char c) {
        for (EnumMobFactoryModule module : values()) {
            if (module.patternChar == c) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get module type by pattern character
     */
    @Nullable
    public static EnumMobFactoryModule byChar(char c) {
        for (EnumMobFactoryModule module : values()) {
            if (module.patternChar == c) {
                return module;
            }
        }
        return null;
    }

    /**
     * Check if this is a tier cap module
     */
    public boolean isTierCap() {
        return this == STRUCTURE_TIER_I_CAP || this == STRUCTURE_TIER_II_CAP ||
               this == STRUCTURE_TIER_III_CAP || this == STRUCTURE_TIER_IV_CAP;
    }

    /**
     * Check if this is a structure block
     */
    public boolean isStructure() {
        return this == STRUCTURE_BLOCK_1 || this == STRUCTURE_BLOCK_2 ||
               this == STRUCTURE_BLOCK_3 || this == STRUCTURE_BLOCK_4 ||
               this == STRUCTURE_BLOCK_5;
    }

    /**
     * Get the tier cap level (1-4), or 0 if not a tier cap
     */
    public int getTierCapLevel() {
        switch (this) {
            case STRUCTURE_TIER_I_CAP: return 1;
            case STRUCTURE_TIER_II_CAP: return 2;
            case STRUCTURE_TIER_III_CAP: return 3;
            case STRUCTURE_TIER_IV_CAP: return 4;
            default: return 0;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
