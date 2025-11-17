package ipsis.woot.farmblocks;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

/**
 * Interface for factory block communication
 * Allows blocks to communicate with the master (heart) block
 */
public interface IFactoryGlue {

    /**
     * Block types in the multiblock
     */
    enum FactoryBlockType {
        HEART,          // Main controller
        CONTROLLER,     // Programming point
        CELL,           // Power storage
        STRUCTURE,      // Structure blocks
        IMPORTER,       // Item/fluid import
        EXPORTER,       // Item/fluid export
        UPGRADE         // Upgrade blocks
    }

    /**
     * Get the type of this factory block
     */
    FactoryBlockType getType();

    /**
     * Set the master (heart) position
     */
    void setMaster(@Nullable BlockPos masterPos);

    /**
     * Get the master (heart) position
     */
    @Nullable
    BlockPos getMaster();

    /**
     * Clear the master reference
     */
    void clearMaster();

    /**
     * Check if this block is connected to a master
     */
    boolean hasValidMaster();
}
