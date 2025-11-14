package ipsis.woot.multiblock;

import ipsis.woot.Woot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Repository for mob factory multiblock patterns
 * Stores ASCII pattern definitions for all 4 tiers
 * Based on the original Woot 1.12.2 implementation
 */
public class FactoryPatternRepository {

    private final Map<EnumMobFactoryTier, FactoryTierPattern> tiers = new HashMap<>();
    private int maxYOffset = 0;
    private int maxXZOffset = 0;

    public FactoryPatternRepository() {
        loadPattern(EnumMobFactoryTier.TIER_I, TIER_I_PATTERN);
        loadPattern(EnumMobFactoryTier.TIER_II, TIER_II_PATTERN);
        loadPattern(EnumMobFactoryTier.TIER_III, TIER_III_PATTERN);
        loadPattern(EnumMobFactoryTier.TIER_IV, TIER_IV_PATTERN);
    }

    /**
     * Load a tier pattern from ASCII definition
     */
    private void loadPattern(EnumMobFactoryTier tier, String[][] pattern) {
        if (pattern.length == 0) {
            Woot.LOGGER.error("FactoryPatternRepository: pattern length is invalid");
            return;
        }

        int height = pattern.length;
        int width = pattern[0].length;

        if (width == 0) {
            Woot.LOGGER.error("FactoryPatternRepository: pattern row count is invalid");
            return;
        }

        int depth = pattern[0][0].length();
        if (depth == 0) {
            Woot.LOGGER.error("FactoryPatternRepository: pattern column count is invalid");
            return;
        }

        FactoryTierPattern tierPattern = new FactoryTierPattern();
        tierPattern.setHeight(height);
        tierPattern.setWidth(width);

        // Find the origin (heart position) first
        boolean hasOrigin = false;
        for (int layer = 0; layer < height; layer++) {
            for (int row = 0; row < width; row++) {
                for (int col = 0; col < depth; col++) {
                    char c = pattern[layer][row].charAt(col);
                    if (isOrigin(c)) {
                        tierPattern.setOrigin(layer, row, col);
                        hasOrigin = true;
                    }
                }
            }
        }

        if (!hasOrigin) {
            Woot.LOGGER.error("FactoryPatternRepository: pattern has no origin");
            return;
        }

        // Parse all blocks in the pattern
        for (int layer = 0; layer < height; layer++) {
            for (int row = 0; row < width; row++) {
                if (pattern[layer][row].length() != depth) {
                    Woot.LOGGER.error("FactoryPatternRepository: pattern row is too short");
                    return;
                }

                for (int col = 0; col < depth; col++) {
                    char c = pattern[layer][row].charAt(col);

                    if (isSpace(c) || isOrigin(c)) {
                        continue;
                    } else if (EnumMobFactoryModule.isValidChar(c)) {
                        EnumMobFactoryModule module = EnumMobFactoryModule.byChar(c);
                        tierPattern.incBlockCount(module);
                        tierPattern.addModule(new MobFactoryModule(
                            tierPattern.calcBlockPos(layer, row, col),
                            module
                        ));
                    } else {
                        Woot.LOGGER.error("FactoryPatternRepository: invalid character ({},{},{}) '{}'",
                            layer, row, col, c);
                        return;
                    }
                }
            }
        }

        tiers.put(tier, tierPattern);

        if (maxYOffset == 0 || maxYOffset < height) {
            maxYOffset = height;
        }

        if (maxXZOffset == 0 || maxXZOffset < tierPattern.getOriginCol() / 2) {
            maxXZOffset = width;
        }
    }

    private boolean isSpace(char c) {
        return c == '-';
    }

    private boolean isOrigin(char c) {
        return c == 'x';
    }

    /**
     * Get the module type at a specific offset in a tier's pattern
     */
    @Nullable
    public EnumMobFactoryModule getModule(EnumMobFactoryTier tier, BlockPos offset) {
        MobFactoryModule module = getMobFactoryModule(tier, offset);
        return module != null ? module.moduleType : null;
    }

    @Nullable
    private MobFactoryModule getMobFactoryModule(EnumMobFactoryTier tier, BlockPos offset) {
        if (!tiers.containsKey(tier)) {
            return null;
        }

        for (MobFactoryModule m : tiers.get(tier).modules) {
            if (m.offset.equals(offset)) {
                return m;
            }
        }

        return null;
    }

    /**
     * Check if a module at an offset is valid for a tier
     */
    public boolean isValid(EnumMobFactoryTier tier, EnumMobFactoryModule module, BlockPos offset) {
        MobFactoryModule factoryModule = getMobFactoryModule(tier, offset);
        return factoryModule != null && factoryModule.moduleType == module;
    }

    /**
     * Get all modules for a tier
     */
    @Nonnull
    public List<MobFactoryModule> getAllModules(EnumMobFactoryTier tier) {
        return tiers.containsKey(tier) ? tiers.get(tier).modules : Collections.emptyList();
    }

    /**
     * Get the number of a specific block type required for a tier
     */
    public int getBlockCount(EnumMobFactoryTier tier, EnumMobFactoryModule module) {
        return tiers.containsKey(tier) ? tiers.get(tier).getBlockCount(module) : 0;
    }

    public int getMaxXZOffset() {
        return maxXZOffset;
    }

    public int getMaxYOffset() {
        return maxYOffset;
    }

    /**
     * Represents a module (block) in the pattern with its offset from origin
     */
    public static class MobFactoryModule {
        public final BlockPos offset;
        public final EnumMobFactoryModule moduleType;

        public MobFactoryModule(BlockPos offset, EnumMobFactoryModule moduleType) {
            this.offset = offset;
            this.moduleType = moduleType;
        }

        public BlockPos getOffset() {
            return offset;
        }

        public EnumMobFactoryModule getModuleType() {
            return moduleType;
        }
    }

    /**
     * Internal class to store a tier's pattern information
     */
    private static class FactoryTierPattern {
        private int originLayer;
        private int originRow;
        private int originCol;
        private int width;
        private int height;
        private final Map<EnumMobFactoryModule, Integer> blockCounts = new HashMap<>();
        private final List<MobFactoryModule> modules = new ArrayList<>();

        public void setOrigin(int layer, int row, int col) {
            this.originLayer = layer;
            this.originRow = row;
            this.originCol = col;
        }

        public BlockPos calcBlockPos(int layer, int row, int col) {
            int l = (originLayer - layer) * -1;
            int r = (originRow - row) * -1;
            int c = (originCol - col) * -1;
            return new BlockPos(c, l, r);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getOriginCol() {
            return originCol;
        }

        public int getHeight() {
            return height;
        }

        public int getBlockCount(EnumMobFactoryModule m) {
            return blockCounts.getOrDefault(m, 0);
        }

        public void incBlockCount(EnumMobFactoryModule m) {
            blockCounts.put(m, blockCounts.getOrDefault(m, 0) + 1);
        }

        public void addModule(MobFactoryModule m) {
            modules.add(m);
        }
    }

    //===========================================
    // TIER PATTERNS
    //===========================================

    /**
     * Tier I Pattern: 5x3x5 structure
     * - = air/empty
     * x = heart (origin/layout block position)
     * r = structure block 1
     * g = structure block 5 (cap)
     * p = controller
     * 1 = structure tier I cap
     */
    public static final String[][] TIER_I_PATTERN = {
        {
            "-rrr-",
            "rgggr",
            "rg-gr",
            "rgggr",
            "-rrr-"
        },
        {
            "-----",
            "-----",
            "--x--",
            "-pgp-",
            "-rgr-"
        },
        {
            "-----",
            "-----",
            "-----",
            "-----",
            "-1-1-"
        }
    };

    /**
     * Tier II Pattern: 7x5x7 structure
     * o = structure block 2
     */
    public static final String[][] TIER_II_PATTERN = {
        {
            "--ooo--",
            "-orrro-",
            "orgggro",
            "org-gro",
            "orgggro",
            "-orrro-",
            "--ooo--"
        },
        {
            "-------",
            "-------",
            "-------",
            "o--x--o",
            "oppgppo",
            "-orgro-",
            "--ooo--"
        },
        {
            "-------",
            "-------",
            "-------",
            "-------",
            "o-----o",
            "-o1-1o-",
            "--o-o--"
        },
        {
            "-------",
            "-------",
            "-------",
            "-------",
            "-------",
            "-o---o-",
            "--o-o--"
        },
        {
            "-------",
            "-------",
            "-------",
            "-------",
            "-------",
            "-2---2-",
            "--2-2--"
        }
    };

    /**
     * Tier III Pattern: 9x6x9 structure
     * h = structure block 3
     */
    public static final String[][] TIER_III_PATTERN = {
        {
            "---hhh---",
            "--hoooh--",
            "-horrroh-",
            "horgggroh",
            "horg-groh",
            "horgggroh",
            "-horrroh-",
            "--hoooh--",
            "---hhh---"
        },
        {
            "---------",
            "---------",
            "---------",
            "h-------h",
            "ho--x--oh",
            "hoppgppoh",
            "-horgroh-",
            "--hoooh--",
            "---hhh---"
        },
        {
            "---------",
            "---------",
            "---------",
            "---------",
            "h-------h",
            "ho-----oh",
            "-ho1-1oh-",
            "--ho-oh--",
            "---hhh---"
        },
        {
            "---------",
            "---------",
            "---------",
            "---------",
            "---------",
            "h-------h",
            "-ho---oh-",
            "--ho-oh--",
            "---hhh---"
        },
        {
            "---------",
            "---------",
            "---------",
            "---------",
            "---------",
            "---------",
            "-h2---2h-",
            "--h2-2h--",
            "---hhh---"
        },
        {
            "---------",
            "---------",
            "---------",
            "---------",
            "---------",
            "---------",
            "-3-----3-",
            "--3---3--",
            "----3----"
        }
    };

    /**
     * Tier IV Pattern: 11x7x11 structure
     * w = structure block 4
     */
    public static final String[][] TIER_IV_PATTERN = {
        {
            "----www----",
            "---whhhw---",
            "--whooohw--",
            "-whorrrohw-",
            "whorgggrohw",
            "whorg-grohw",
            "whorgggrohw",
            "-whorrrohw-",
            "--whooohw--",
            "---whhhw---",
            "----www----"
        },
        {
            "-----------",
            "-----------",
            "--w-----w--",
            "-w-------w-",
            "wh-------hw",
            "who--x--ohw",
            "whoppgppohw",
            "-whorgrohw-",
            "--whooohw--",
            "---whhhw---",
            "----www----"
        },
        {
            "-----------",
            "-----------",
            "-----------",
            "-w-------w-",
            "w---------w",
            "wh-------hw",
            "who-----ohw",
            "-who1-1ohw-",
            "--who-ohw--",
            "---whhhw---",
            "----www----"
        },
        {
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "w---------w",
            "w---------w",
            "wh-------hw",
            "-who---ohw-",
            "--who-ohw--",
            "---whhhw---",
            "----www----"
        },
        {
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "w---------w",
            "w---------w",
            "-wh2---2hw-",
            "--wh2-2hw--",
            "---whhhw---",
            "----www----"
        },
        {
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "w---------w",
            "-w3-----3w-",
            "---3---3---",
            "---w-3-w---",
            "-----w-----"
        },
        {
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "-----------",
            "4---------4",
            "-4-------4-",
            "-----------",
            "---4---4---",
            "-----4-----"
        }
    };
}
