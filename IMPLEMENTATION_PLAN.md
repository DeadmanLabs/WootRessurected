# Woot Mod Implementation Plan - NeoForge 1.21.1

## Overview
Port of Woot mob factory system from 1.12.2 to NeoForge 1.21.1
Original: `/mnt/d/Code/WootWoot/Woot`
Target: `/mnt/d/Code/WootWoot/MDK-1.21.1-ModDevGradle`

---

## ✅ Phase 1: Core Infrastructure (COMPLETE)

### Files Created (13):
- `multiblock/EnumMobFactoryTier.java` - 4 tiers with power capacities
- `multiblock/EnumMobFactoryModule.java` - Block types for patterns
- `multiblock/FactoryPatternRepository.java` - ASCII pattern definitions
- `power/FactoryEnergyStorage.java` - RF/FE energy storage
- `power/PowerRecipe.java` - Power cost calculations
- `blockentities/FactoryHeartBlockEntity.java` - Main controller
- `blockentities/FactoryControllerBlockEntity.java` - UI point
- `blockentities/FactoryCellBlockEntity.java` - Power storage
- `blocks/FactoryHeartBlock.java` - Heart block with entity
- `blocks/FactoryControllerBlock.java` - Controller block with entity
- `blocks/FactoryCellBlock.java` - Cell blocks with entity
- `blockentities/WootBlockEntities.java` - BlockEntity registrations
- `Woot.java` - Updated block registrations

### Features:
- 4 factory tiers: 5x3x5, 7x5x7, 9x6x9, 11x7x11
- Energy storage: 100K, 500K, 2.5M, 10M RF per cell tier
- Power recipes: 80, 160, 240, 320 RF/tick per tier
- BlockEntities save/load with NBT
- Blocks place and persist in world

---

## ✅ Phase 2: Multiblock Structure Validation (COMPLETE)

### Files Created (10):
- `farmstructure/FarmSetup.java` - Stores multiblock configuration
- `farmstructure/IFarmStructure.java` - Structure management interface
- `farmstructure/FarmStructure.java` - Validation implementation
- `farmstructure/FarmScanner.java` - 3D pattern matching
- `farmblocks/IFactoryGlue.java` - Communication interface
- `farmblocks/FactoryGlue.java` - Communication implementation
- `farmblocks/IFactoryGlueProvider.java` - Provider interface

### Files Updated:
- All block entities now implement `IFactoryGlueProvider`
- Heart ticks structure validator every 20 ticks
- Blocks notify heart when broken

### Features:
- Automatic structure validation
- Pattern matching for all 4 tiers
- Block communication via FactoryGlue
- Energy aggregation from cells
- Structure formation/breaking detection
- Debug logging for multiblock status

---

## 🔄 Phase 3: Power & Progress System

### Estimated Time: 4-6 hours

### Files to Create:
1. **farming/ProgressTracker.java**
   - Tracks power consumption over time
   - Calculates progress percentage
   - Determines when spawn cycle completes
   - Methods: tick(energyConsumed), getProgress(), isComplete()

2. **power/PowerCalculator.java**
   - Calculates power requirements based on:
     - Mob type (from ender shard)
     - Factory tier
     - Upgrade modifiers (phase 5)
   - Returns PowerRecipe with totalPower, ticks, powerPerTick

### Files to Update:
1. **blockentities/FactoryHeartBlockEntity.java**
   ```java
   // Add fields:
   private ProgressTracker progressTracker;
   private boolean isPowered = false;

   // In serverTick():
   if (isFormed() && isProgrammed()) {
       // Try to consume power from aggregated energy
       int powerNeeded = powerRecipe.getPowerPerTick();
       if (energyStorage.extractEnergy(powerNeeded, true) >= powerNeeded) {
           energyStorage.extractEnergy(powerNeeded, false);
           progressTracker.tick(powerNeeded);

           if (progressTracker.isComplete()) {
               // Phase 4: Generate loot
               progressTracker.reset();
           }
       }
   }
   ```

2. **farmstructure/FarmSetup.java**
   - Add method: `boolean isProgrammed()` - checks if controller has mob
   - Add method: `PowerRecipe calculatePowerRecipe()` - uses PowerCalculator

### Features:
- Power consumption from aggregated cells
- Progress tracking toward spawning
- Visual progress feedback (for GUI in Phase 6)
- Power recipes adjust based on mob type and tier
- Pause when out of power

### Testing:
- Place multiblock, program controller
- Add energy to cells
- Verify power consumption
- Check progress increases
- Verify reset when complete

---

## 🔄 Phase 4: Mob Spawning & Loot Generation

### Estimated Time: 6-8 hours

### Files to Create:
1. **loot/LootGenerator.java**
   - Fetches loot table for mob entity type
   - Generates drops without spawning entity
   - Applies looting enchantment (from upgrades, Phase 5)
   - Methods: generateLoot(mobKey, lootingLevel), spawnDrops(level, pos, items)

2. **loot/WootLootContext.java**
   - Creates LootContext for mob drops
   - Simulates entity death
   - Applies enchantments and modifiers

3. **farming/XPGenerator.java**
   - Calculates XP orbs to spawn
   - Based on mob type and XP upgrade (Phase 5)
   - Spawns ExperienceOrb entities

### Files to Update:
1. **blockentities/FactoryHeartBlockEntity.java**
   ```java
   // In serverTick() when progressTracker.isComplete():
   if (progressTracker.isComplete()) {
       FarmSetup setup = getFarmSetup();
       if (setup != null && setup.isProgrammed()) {
           // Generate loot
           String mobKey = setup.getProgrammedMob().entityKey();
           int lootingLevel = 0; // Phase 5: get from upgrades

           List<ItemStack> drops = LootGenerator.generateLoot(
               level, mobKey, lootingLevel
           );

           // Export items (Phase 7: to exporter blocks)
           // For now, spawn in world
           for (ItemStack drop : drops) {
               Block.popResource(level, worldPosition.above(), drop);
           }

           // Generate XP
           XPGenerator.spawnXP(level, worldPosition.above(), mobKey);

           progressTracker.reset();
       }
   }
   ```

### Original Reference Files:
- `/Woot/src/main/java/ipsis/woot/loot/LootGeneration.java`
- `/Woot/src/main/java/ipsis/woot/util/WootMobName.java`

### Features:
- Loot table integration (uses Minecraft's loot system)
- No entity spawning required
- XP orb generation
- Item drops spawn at heart position
- Looting enchantment support

### Testing:
- Program with different mob types
- Verify correct loot tables used
- Check item drops match expected
- Verify XP orbs spawn
- Test with high-tier mobs (Wither, Ender Dragon)

---

## 🔄 Phase 5: Upgrade System

### Estimated Time: 5-7 hours

### Files to Create:
1. **farming/EnumFarmUpgrade.java**
   - Enum for upgrade types:
     - LOOTING (I, II, III) - Increases loot drops
     - MASS (I, II, III) - Spawns multiple mobs worth of loot
     - RATE (I, II, III) - Reduces time between spawns
     - XP (I, II, III) - Increases XP generation
     - DECAPITATE (I, II, III) - Increases head drop chance
     - EFFICIENCY (I, II, III) - Reduces power consumption
   - Methods: getMaxLevel(), getModifier(level)

2. **farmstructure/UpgradeScanner.java**
   - Scans for upgrade blocks in structure
   - Returns Map<EnumFarmUpgrade, Integer> (upgrade -> level)
   - Validates upgrade positions match pattern

3. **farming/UpgradeManager.java**
   - Applies upgrade modifiers to:
     - Looting level (for loot generation)
     - Spawn count (mass upgrade)
     - Tick rate (rate upgrade)
     - Power consumption (efficiency upgrade)
     - XP multiplier (XP upgrade)

### Files to Update:
1. **farmstructure/FarmScanner.java**
   ```java
   // Add to ScannedFarm:
   private Map<EnumFarmUpgrade, Integer> upgrades = new HashMap<>();

   // In validateModule():
   case STRUCTURE_UPGRADE -> {
       // Scan for upgrade blocks around this position
       UpgradeScanner.scanUpgrade(level, pos, farm);
       yield true;
   }
   ```

2. **farmstructure/FarmSetup.java**
   ```java
   // Add fields:
   private Map<EnumFarmUpgrade, Integer> upgrades = new HashMap<>();

   // Add methods:
   public int getUpgradeLevel(EnumFarmUpgrade upgrade)
   public boolean hasUpgrade(EnumFarmUpgrade upgrade)
   public int getMassSpawnCount() // 1 + mass level
   public int getLootingLevel() // looting upgrade level
   public float getPowerMultiplier() // efficiency reduces power
   ```

3. **blockentities/FactoryHeartBlockEntity.java**
   ```java
   // In loot generation:
   int lootingLevel = setup.getLootingLevel();
   int spawnCount = setup.getMassSpawnCount();

   for (int i = 0; i < spawnCount; i++) {
       List<ItemStack> drops = LootGenerator.generateLoot(
           level, mobKey, lootingLevel
       );
       // Export drops...
   }

   // In power consumption:
   int powerNeeded = (int)(powerRecipe.getPowerPerTick()
       * setup.getPowerMultiplier());
   ```

### Original Reference Files:
- `/Woot/src/main/java/ipsis/woot/util/EnumFarmUpgrade.java`
- `/Woot/src/main/java/ipsis/woot/farmstructure/ScannedFarmUpgrade.java`

### Features:
- Scan and validate upgrade blocks
- Apply modifiers to power, loot, spawn count
- Support for 6 upgrade types with 3 levels each
- Configurable upgrade effects
- Debug logging for upgrade detection

### Testing:
- Build structure with upgrade blocks
- Verify upgrades detected
- Test mass upgrade (multiple spawns)
- Test looting upgrade (more drops)
- Test efficiency upgrade (less power)
- Test rate upgrade (faster spawning)

---

## 🔄 Phase 6: GUI & Container System

### Estimated Time: 8-10 hours

### Files to Create:
1. **menu/FactoryControllerMenu.java**
   - Container for controller GUI
   - Syncs data: mob, tier, progress, energy, upgrades
   - Handles programming with ender shards

2. **client/screen/FactoryControllerScreen.java**
   - GUI rendering
   - Displays:
     - Programmed mob name and icon
     - Factory tier
     - Energy bar (aggregated from cells)
     - Progress bar (toward next spawn)
     - Upgrade list with levels
   - Ender shard slot for programming

3. **network/FactorySyncPacket.java**
   - Server→Client packet
   - Syncs factory data for GUI display
   - Uses modern PacketDistributor system

4. **client/WootClient.java** (already exists, update)
   - Register screen handler
   - Register client-side renderers

### Files to Update:
1. **blocks/FactoryControllerBlock.java**
   ```java
   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level,
                                               BlockPos pos, Player player,
                                               BlockHitResult hit) {
       if (level.isClientSide) {
           return InteractionResult.SUCCESS;
       }

       BlockEntity be = level.getBlockEntity(pos);
       if (be instanceof FactoryControllerBlockEntity controller) {
           // Open GUI
           player.openMenu(controller, pos);
           return InteractionResult.CONSUME;
       }

       return InteractionResult.PASS;
   }
   ```

2. **blockentities/FactoryControllerBlockEntity.java**
   ```java
   // Implement MenuProvider:
   @Override
   public Component getDisplayName() {
       return Component.translatable("container.woot.factory_controller");
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory playerInv,
                                            Player player) {
       return new FactoryControllerMenu(id, playerInv, this);
   }
   ```

### Original Reference Files:
- `/Woot/src/main/java/ipsis/woot/client/gui/GuiMobFactoryController.java`
- `/Woot/src/main/java/ipsis/woot/tileentity/TileEntityMobFactoryController.java`

### Features:
- Interactive GUI for controller
- Visual progress and energy bars
- Mob icon/name display
- Ender shard programming via GUI
- Upgrade status display
- Real-time sync from server

### Testing:
- Open controller GUI
- Verify data displays correctly
- Program with ender shard in GUI
- Watch progress bar update
- Check energy bar updates
- Verify upgrade list shows

---

## 🔄 Phase 7: Import/Export & Item Handling

### Estimated Time: 4-6 hours

### Files to Create:
1. **blockentities/FactoryImporterBlockEntity.java**
   - Connects to adjacent inventories
   - Pulls items for recipes (future: custom recipes)
   - Implements IItemHandler capability

2. **blockentities/FactoryExporterBlockEntity.java**
   - Connects to adjacent inventories
   - Pushes generated loot to chests
   - Round-robin distribution
   - Implements IItemHandler capability

3. **util/InventoryHelper.java**
   - Finds adjacent inventories
   - Inserts items to inventories
   - Extracts items from inventories

### Files to Update:
1. **farmstructure/FarmScanner.java**
   ```java
   // Add to ScannedFarm:
   private List<BlockPos> importerPositions = new ArrayList<>();
   private List<BlockPos> exporterPositions = new ArrayList<>();

   // In validateModule():
   case IMPORTER -> {
       farm.addImporterPosition(pos);
       yield block == Woot.IMPORTER.get();
   }
   case EXPORTER -> {
       farm.addExporterPosition(pos);
       yield block == Woot.EXPORTER.get();
   }
   ```

2. **farmstructure/FarmSetup.java**
   ```java
   // Add fields:
   private List<BlockPos> importerPositions = new ArrayList<>();
   private List<BlockPos> exporterPositions = new ArrayList<>();

   // Add methods:
   public List<IItemHandler> getConnectedExportInventories()
   public boolean exportItems(List<ItemStack> items)
   ```

3. **blockentities/FactoryHeartBlockEntity.java**
   ```java
   // In loot generation:
   List<ItemStack> allDrops = new ArrayList<>();
   for (int i = 0; i < spawnCount; i++) {
       allDrops.addAll(LootGenerator.generateLoot(...));
   }

   // Try to export
   if (!setup.exportItems(allDrops)) {
       // No export available, spawn in world
       for (ItemStack drop : allDrops) {
           Block.popResource(level, worldPosition.above(), drop);
       }
   }
   ```

### Original Reference Files:
- `/Woot/src/main/java/ipsis/woot/tileentity/TileEntityMobFactoryImporter.java`
- `/Woot/src/main/java/ipsis/woot/tileentity/TileEntityMobFactoryExporter.java`

### Features:
- Export loot to adjacent chests
- Import items for future recipes
- IItemHandler capability support
- Round-robin distribution
- Fallback to spawning in world

### Testing:
- Place chest next to exporter
- Verify loot goes to chest
- Test with multiple chests
- Verify round-robin works
- Test when chest is full

---

## 🔄 Phase 8: Configuration & JSON Data

### Estimated Time: 3-5 hours

### Files to Create:
1. **config/WootConfig.java**
   - Mob-specific power costs
   - Spawn time overrides
   - XP amounts per mob
   - Mass spawn counts
   - File: `config/woot-common.toml`

2. **data/MobDataLoader.java**
   - Loads mob configurations from JSON
   - Path: `data/woot/mob_configs/`
   - Format:
     ```json
     {
       "minecraft:zombie": {
         "basePowerCost": 16000,
         "spawnTicks": 200,
         "xpAmount": 5,
         "massSpawnCounts": [2, 4, 6]
       }
     }
     ```

3. **config/EnderShardConfig.java** (already exists, enhance)
   - Add kill count requirements per mob
   - Add tier unlock requirements

### Files to Update:
1. **power/PowerCalculator.java**
   ```java
   public PowerRecipe calculate(EnderShardData mob, EnumMobFactoryTier tier) {
       // Get base cost from config
       int baseCost = WootConfig.getPowerCost(mob.entityKey());
       int spawnTicks = WootConfig.getSpawnTicks(mob.entityKey());

       // Apply tier multiplier
       int tierMultiplier = tier.getLevel();
       int totalPower = baseCost * tierMultiplier;
       int powerPerTick = totalPower / spawnTicks;

       return new PowerRecipe(totalPower, spawnTicks, powerPerTick);
   }
   ```

### Original Reference Files:
- `/Woot/src/main/java/ipsis/woot/configuration/ConfigurationHandler.java`
- `/Woot/src/main/resources/assets/woot/data/`

### Features:
- Configurable power costs per mob
- JSON-based mob data
- Config file for server customization
- Default values for all vanilla mobs
- Support for modded mobs

### Testing:
- Modify config, reload world
- Verify power costs change
- Test with custom mob data
- Test modded mob support

---

## 🔄 Phase 9: Polish & Integration

### Estimated Time: 4-6 hours

### Tasks:

1. **JEI Integration** (if JEI available)
   - Show factory recipes in JEI
   - Display power costs
   - Show upgrade effects

2. **Sound Effects**
   - Power consumption sound
   - Loot generation sound
   - Structure formation sound
   - Use Minecraft sound events

3. **Particle Effects**
   - Particles during spawning
   - Energy flow visualization
   - Structure validation particles

4. **Translations**
   - Complete `en_us.json`
   - All GUI text
   - All tooltip text
   - Chat messages

5. **Documentation**
   - In-game guide book (Patchouli integration?)
   - Wiki/README with setup instructions
   - Example multiblock builds

6. **Balance & Testing**
   - Adjust power costs
   - Test all mob types
   - Performance testing with multiple factories
   - Multiplayer testing

### Files to Create:
1. **client/ParticleEffects.java**
2. **compat/JEIPlugin.java** (if JEI present)
3. **resources/assets/woot/lang/en_us.json** (complete)
4. **WIKI.md** - User documentation

### Files to Update:
- Add sounds to all relevant operations
- Add particles to loot generation
- Polish GUI rendering
- Add tooltips to all items/blocks

---

## Integration Notes

### Mod Dependencies:
- **Required**: NeoForge 1.21.1 (21.1.211+)
- **Optional**: JEI (recipe viewing)
- **Optional**: Patchouli (guide book)

### API Compatibility:
- Energy: NeoForge IEnergyStorage (RF/FE)
- Items: NeoForge IItemHandler
- Data: Modern DataComponents (not NBT)
- Recipes: Minecraft RecipeManager

### Performance Considerations:
- Structure validation: Max once per second (20 ticks)
- Loot generation: Async if possible
- Energy aggregation: Cached, update on structure change
- Particle spawning: Client-side only, rate-limited

---

## Testing Checklist

### Phase 3:
- [ ] Power consumption works
- [ ] Progress tracks correctly
- [ ] Resets after completion
- [ ] Pauses when out of power

### Phase 4:
- [ ] Loot tables load correctly
- [ ] Items spawn with correct quantities
- [ ] XP orbs spawn
- [ ] No entity spawning errors

### Phase 5:
- [ ] All upgrade types detected
- [ ] Modifiers apply correctly
- [ ] Multiple upgrades stack properly
- [ ] Invalid upgrades rejected

### Phase 6:
- [ ] GUI opens on controller
- [ ] All data syncs correctly
- [ ] Progress bar updates
- [ ] Energy bar shows aggregate
- [ ] Ender shard programming works

### Phase 7:
- [ ] Export to chests works
- [ ] Round-robin distribution
- [ ] Handles full inventories
- [ ] Multiple exporters supported

### Phase 8:
- [ ] Config loads correctly
- [ ] Custom mob data works
- [ ] JSON parsing handles errors
- [ ] Modded mobs supported

### Phase 9:
- [ ] All sounds play
- [ ] Particles render correctly
- [ ] Translations complete
- [ ] No performance issues
- [ ] Multiplayer sync works

---

## Estimated Total Time

- **Phase 1**: 5-6 hours ✅ COMPLETE
- **Phase 2**: 6-8 hours ✅ COMPLETE
- **Phase 3**: 4-6 hours
- **Phase 4**: 6-8 hours
- **Phase 5**: 5-7 hours
- **Phase 6**: 8-10 hours
- **Phase 7**: 4-6 hours
- **Phase 8**: 3-5 hours
- **Phase 9**: 4-6 hours

**Total**: 45-62 hours (Phases 3-9)
**Completed**: 11-14 hours (Phases 1-2)
**Overall**: 56-76 hours

---

## Current Status

**Completed Phases**: 1-2 (Core Infrastructure + Structure Validation)

**Working Features**:
- ✅ All blocks and block entities registered
- ✅ Multiblock structure validation
- ✅ Pattern matching for all 4 tiers
- ✅ Energy aggregation from cells
- ✅ Block communication system
- ✅ Structure formation/breaking
- ✅ Debug logging

**Next Phase**: Phase 3 (Power & Progress System)

**Last Build**: BUILD SUCCESSFUL in 13s
**Last Test**: Structure validation functional

---

## Notes

- Use original Woot 1.12.2 as reference: `/mnt/d/Code/WootWoot/Woot`
- Follow NeoForge 1.21.1 APIs (no Forge legacy)
- Modern DataComponents instead of NBT where appropriate
- Codec-based serialization for block entities
- Server-side logic, client syncing via packets
- Debug logging throughout for troubleshooting

---

*Document created: 2025-10-18*
*Last updated: Phase 2 completion*
