package ipsis.woot.farmblocks;

import javax.annotation.Nonnull;

/**
 * Interface for block entities that provide factory glue (communication)
 * All factory block entities should implement this
 */
public interface IFactoryGlueProvider {

    /**
     * Get the factory glue instance for this block entity
     */
    @Nonnull
    IFactoryGlue getFactoryGlue();
}
