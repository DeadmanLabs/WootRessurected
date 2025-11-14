package ipsis.woot.power;

import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;

/**
 * Energy storage for mob factory multiblock
 * Implements NeoForge's IEnergyStorage for RF/FE compatibility
 */
public class FactoryEnergyStorage implements IEnergyStorage {

    private int energy;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;

    public FactoryEnergyStorage(int capacity) {
        this(capacity, capacity, capacity, 0);
    }

    public FactoryEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    public FactoryEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public FactoryEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }

        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }

        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    /**
     * Set energy directly (for loading from NBT)
     */
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    /**
     * Add energy directly (for internal operations)
     */
    public void modifyEnergyStored(int energy) {
        this.energy += energy;
        if (this.energy > capacity) {
            this.energy = capacity;
        } else if (this.energy < 0) {
            this.energy = 0;
        }
    }

    /**
     * Check if we have at least the specified amount of energy
     */
    public boolean hasEnergy(int amount) {
        return energy >= amount;
    }

    /**
     * Get the fill percentage (0.0 to 1.0)
     */
    public float getFillPercentage() {
        if (capacity == 0) {
            return 0.0f;
        }
        return (float) energy / (float) capacity;
    }

    /**
     * Check if storage is full
     */
    public boolean isFull() {
        return energy >= capacity;
    }

    /**
     * Check if storage is empty
     */
    public boolean isEmpty() {
        return energy <= 0;
    }

    @Nonnull
    @Override
    public String toString() {
        return String.format("Energy: %d / %d RF", energy, capacity);
    }
}
