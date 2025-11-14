package ipsis.woot.farmblocks;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of factory block communication
 * Stores the master (heart) reference and block type
 */
public class FactoryGlue implements IFactoryGlue {

    private final FactoryBlockType type;
    private BlockPos masterPos = null;

    public FactoryGlue(@Nonnull FactoryBlockType type) {
        this.type = type;
    }

    @Override
    public FactoryBlockType getType() {
        return type;
    }

    @Override
    public void setMaster(@Nullable BlockPos masterPos) {
        this.masterPos = masterPos;
    }

    @Override
    @Nullable
    public BlockPos getMaster() {
        return masterPos;
    }

    @Override
    public void clearMaster() {
        this.masterPos = null;
    }

    @Override
    public boolean hasValidMaster() {
        return masterPos != null;
    }

    @Override
    public String toString() {
        return String.format("FactoryGlue{type=%s, master=%s}", type, masterPos);
    }
}
