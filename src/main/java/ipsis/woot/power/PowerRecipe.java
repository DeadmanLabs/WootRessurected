package ipsis.woot.power;

import javax.annotation.Nonnull;

/**
 * Represents the power requirements for a mob spawning cycle
 * Includes both power per tick and total time/power required
 */
public class PowerRecipe {

    private final long totalPower;      // Total RF required for one spawn cycle
    private final int ticks;            // Total ticks required for one spawn cycle
    private final int powerPerTick;     // RF consumed per tick

    public PowerRecipe(long totalPower, int ticks, int powerPerTick) {
        this.totalPower = totalPower;
        this.ticks = ticks;
        this.powerPerTick = powerPerTick;
    }

    /**
     * Get the total power required for one complete spawn cycle
     */
    public long getTotalPower() {
        return totalPower;
    }

    /**
     * Get the number of ticks required for one complete spawn cycle
     */
    public int getTicks() {
        return ticks;
    }

    /**
     * Get the power consumed per tick during the spawn cycle
     */
    public int getPowerPerTick() {
        return powerPerTick;
    }

    /**
     * Calculate progress percentage based on consumed power
     */
    public int getProgress(long consumedPower) {
        if (totalPower == 0) {
            return 0;
        }
        return (int) ((consumedPower * 100) / totalPower);
    }

    /**
     * Check if the recipe is complete based on consumed power
     */
    public boolean isComplete(long consumedPower) {
        return consumedPower >= totalPower;
    }

    /**
     * Get remaining power needed
     */
    public long getRemainingPower(long consumedPower) {
        return Math.max(0, totalPower - consumedPower);
    }

    /**
     * Get remaining ticks needed (estimated)
     */
    public int getRemainingTicks(long consumedPower) {
        if (powerPerTick == 0) {
            return 0;
        }
        long remaining = getRemainingPower(consumedPower);
        return (int) (remaining / powerPerTick);
    }

    /**
     * Create a default recipe for testing (Tier I)
     * Matches original Woot: 320 ticks = 16 seconds at 20 ticks/second
     */
    @Nonnull
    public static PowerRecipe createDefault() {
        return forTier(1, 320); // Tier I: 25,600 RF over 320 ticks at 80 RF/tick
    }

    /**
     * Create a recipe based on tier
     */
    @Nonnull
    public static PowerRecipe forTier(int tierLevel, int spawnTicks) {
        int powerPerTick = tierLevel * 80; // 80, 160, 240, 320 RF/tick
        long totalPower = (long) powerPerTick * spawnTicks;
        return new PowerRecipe(totalPower, spawnTicks, powerPerTick);
    }

    @Override
    @Nonnull
    public String toString() {
        return String.format("PowerRecipe{total=%d RF, ticks=%d, perTick=%d RF/t}",
            totalPower, ticks, powerPerTick);
    }
}
