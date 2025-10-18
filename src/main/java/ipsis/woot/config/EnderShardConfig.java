package ipsis.woot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ipsis.woot.Woot;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for ender shard mob programming
 * Loads from data/woot/ender_shard/mob_config.json
 */
public class EnderShardConfig {

    private static final int DEFAULT_KILL_COUNT = 1;
    private static final Map<String, MobConfig> MOB_CONFIGS = new HashMap<>();
    private static final Set<String> ENTITY_BLACKLIST = new HashSet<>();
    private static final Set<String> MOD_BLACKLIST = new HashSet<>();
    private static int globalKillCount = DEFAULT_KILL_COUNT;

    /**
     * Get the required kill count for a specific mob
     */
    public static int getKillCount(@Nonnull String entityKey) {
        MobConfig config = MOB_CONFIGS.get(entityKey);
        if (config != null) {
            return config.killCount;
        }
        return globalKillCount;
    }

    /**
     * Check if a mob can be captured (not blacklisted)
     */
    public static boolean canCapture(@Nonnull String entityKey) {
        // Check entity blacklist
        if (ENTITY_BLACKLIST.contains(entityKey)) {
            return false;
        }

        // Check mod blacklist (e.g., "botania:*")
        String modId = getModId(entityKey);
        if (MOD_BLACKLIST.contains(modId)) {
            return false;
        }

        return true;
    }

    /**
     * Extract mod ID from entity key (e.g., "minecraft:zombie" -> "minecraft")
     */
    private static String getModId(@Nonnull String entityKey) {
        int colonIndex = entityKey.indexOf(':');
        if (colonIndex > 0) {
            return entityKey.substring(0, colonIndex);
        }
        return "minecraft"; // Default to minecraft if no colon found
    }

    /**
     * Load configuration from JSON file
     * Called during mod initialization
     */
    public static void load() {
        // Clear existing config
        MOB_CONFIGS.clear();
        ENTITY_BLACKLIST.clear();
        MOD_BLACKLIST.clear();
        globalKillCount = DEFAULT_KILL_COUNT;

        Woot.LOGGER.info("Loading ender shard configuration...");

        try {
            // Try to load from data pack
            // For now, we'll use a default embedded config
            // TODO: Implement proper data pack loading
            loadDefaultConfig();

            Woot.LOGGER.info("Loaded ender shard config: {} mob configs, {} entity blacklist, {} mod blacklist",
                MOB_CONFIGS.size(), ENTITY_BLACKLIST.size(), MOD_BLACKLIST.size());
        } catch (Exception e) {
            Woot.LOGGER.error("Failed to load ender shard config, using defaults", e);
        }
    }

    /**
     * Load default embedded configuration
     */
    private static void loadDefaultConfig() {
        // Set global default
        globalKillCount = 1;

        // Add special mob configurations
        addMobConfig("minecraft:ender_dragon", 3, true);
        addMobConfig("minecraft:wither", 3, true);
        addMobConfig("draconicevolution:chaosguardian", 5, true);
        addMobConfig("draconicevolution:guardian_dragon", 4, true);

        // Add some sensible defaults for harder mobs
        addMobConfig("minecraft:elder_guardian", 2, true);
        addMobConfig("minecraft:warden", 5, true);

        // Blacklist example - uncomment to blacklist specific mobs
        // ENTITY_BLACKLIST.add("twilightforest:naga");
        // MOD_BLACKLIST.add("botania");

        Woot.LOGGER.debug("Loaded default ender shard configuration");
    }

    /**
     * Add a mob configuration
     */
    private static void addMobConfig(String entityKey, int killCount, boolean canCapture) {
        MOB_CONFIGS.put(entityKey, new MobConfig(killCount, canCapture));
    }

    /**
     * Mob configuration data class
     */
    private static class MobConfig {
        final int killCount;
        final boolean canCapture;

        MobConfig(int killCount, boolean canCapture) {
            this.killCount = killCount;
            this.canCapture = canCapture;
        }
    }

    /**
     * Get global default kill count
     */
    public static int getGlobalKillCount() {
        return globalKillCount;
    }

    /**
     * Check if any mobs are configured
     */
    public static boolean hasConfigs() {
        return !MOB_CONFIGS.isEmpty();
    }

    /**
     * Get number of configured mobs
     */
    public static int getConfigCount() {
        return MOB_CONFIGS.size();
    }
}
