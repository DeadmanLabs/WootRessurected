package ipsis.woot.farmstructure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for farm structure management
 * Handles multiblock validation, formation, and breaking
 */
public interface IFarmStructure {

    /**
     * Mark the structure as needing validation
     */
    void setStructureDirty();

    /**
     * Disconnect all blocks in the multiblock
     */
    void fullDisconnect();

    /**
     * Set the world level
     */
    IFarmStructure setWorld(@Nonnull Level level);

    /**
     * Set the origin position (heart position)
     */
    IFarmStructure setPosition(BlockPos origin);

    /**
     * Tick the structure validator
     * Checks if validation is needed and performs it
     */
    void tick();

    /**
     * Create a FarmSetup from the current validated structure
     * Only call this when isFormed() returns true
     */
    @Nullable
    FarmSetup createSetup();

    /**
     * Check if the multiblock structure is currently formed and valid
     */
    boolean isFormed();

    /**
     * Check if the structure has changed since last check
     */
    boolean hasChanged();

    /**
     * Clear the changed flag
     */
    void clearChanged();
}
