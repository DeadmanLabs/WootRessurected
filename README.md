# Woot - Automated Mob Farming

> **Automated mob farming made simple**
> Build multiblock factories to farm mobs without spawners or mob grinders!

---

## 📖 Overview

**Woot** is a tech-focused mob farming mod that lets you automate mob drops through sophisticated multiblock factories. Instead of traditional spawners or kill chambers, Woot uses **programmable factories** powered by RF energy to learn mob behaviors and produce their drops efficiently.

### Key Features

✨ **Multiblock Factories** - Build impressive tiered structures (Tier I through IV)
🔮 **Ender Shard System** - Program factories by capturing and studying mobs
⚡ **RF Powered** - Uses Forge Energy (RF) for all operations
🎯 **6 Upgrade Types** - Customize your factories with Looting, Rate, Mass, Efficiency, XP, and Decapitate upgrades
📊 **Learning System** - Factories learn mob drops over time through statistical sampling
🛠️ **Modular Design** - Add power cells, controllers, and upgrades to suit your needs

---

## 🚀 Getting Started

### Step 1: Craft an Ender Shard

Ender Shards are the core of Woot's mob programming system. Craft them and prepare to hunt!

### Step 2: Program Your Ender Shard

1. **Hit a mob** with an unprogrammed Ender Shard to lock it to that mob type
2. **Kill the mob** repeatedly (default: 1 kill, configurable per mob)
3. Once charged, the shard is **ready** and will glow

> **Boss Mobs:** Tougher mobs require more kills:
> - Ender Dragon, Wither: **3 kills**
> - Warden: **5 kills**
> - Elder Guardian: **2 kills**

### Step 3: Build Your Factory

Use the **Factory Layout** block to visualize the multiblock structure, then build it with:
- Structure blocks for the frame
- A **Factory Heart** (controller core)
- **Factory Controller** blocks (programmed with your Ender Shard)
- **Power Cells** for energy storage
- **Importers** for ingredients
- **Exporters** for drops

### Step 4: Power & Run

1. Supply RF power to your factory
2. Provide required ingredients via Importers
3. Collect mob drops from Exporters!

---

## 🏗️ Multiblock Structure

Woot factories are **tiered multiblock structures** that grow in size and capability:

### Factory Tiers

| Tier | Size | Upgrade Slots | Max Power Storage | Base Power/Tick | Special Features |
|------|------|---------------|-------------------|-----------------|------------------|
| **I**   | 3×5×3   | 2 slots | 100,000 RF  | 80 RF/t  | Starter tier, basic mobs |
| **II**  | 5×5×5   | 4 slots | 1,000,000 RF | 160 RF/t | Most vanilla mobs |
| **III** | 7×7×7   | 4 slots | 10,000,000 RF | 240 RF/t | Harder mobs |
| **IV**  | 11×7×11 | 4 slots | 2,147,483,647 RF | 320 RF/t | Boss mobs, ultimate tier |

> 💡 **Tier Matching:** Some powerful mobs require minimum factory tiers to spawn. The GUI will warn you if your factory tier is too low!

### Components

#### Core Blocks
- **Factory Heart** - The brain of the operation (1 per factory)
- **Factory Controller** - Programs the factory with mob types (craft with charged Ender Shard)
- **Structure Blocks** - Frame pieces for building the multiblock
- **Cap Blocks** - Tiered top caps that determine factory tier

#### Functional Blocks
- **Power Cells** - Energy storage (Tier I: 100k RF, Tier II: 1M RF, Tier III: 10M RF)
- **Importers** - Item input for spawn ingredients
- **Exporters** - Item output for mob drops
- **Upgrade Base** - Supports vertical upgrade totems

---

## ⚙️ Upgrades

Upgrades are placed on **Upgrade Base** blocks in vertical totems. Stack them vertically (I → II → III) for increasing effects!

### 📚 Looting Upgrade
**Effect:** Applies Looting enchantment to mob drops
**Power Cost:** +80/160/240 RF/t per tier

| Tier | Looting Level | Total Power Cost |
|------|---------------|------------------|
| I    | Looting I     | +80 RF/t |
| II   | Looting II    | +160 RF/t |
| III  | Looting III   | +240 RF/t |

---

### ⏱️ Rate Upgrade
**Effect:** Reduces time between spawn cycles
**Power Cost:** +80/160/240 RF/t per tier

| Tier | Spawn Time | Speed Increase | Total Power Cost |
|------|------------|----------------|------------------|
| I    | 160 ticks (8s)  | 2× faster | +80 RF/t |
| II   | 80 ticks (4s)   | 4× faster | +160 RF/t |
| III  | 40 ticks (2s)   | 8× faster | +240 RF/t |

> 📌 *Base spawn time: 320 ticks (16 seconds)*

---

### 📦 Mass Upgrade
**Effect:** Spawns multiple mobs per cycle
**Power Cost:** +80/160/240 RF/t per tier

| Tier | Mobs per Cycle | Total Power Cost |
|------|----------------|------------------|
| I    | 4 mobs | +80 RF/t |
| II   | 6 mobs | +160 RF/t |
| III  | 8 mobs | +240 RF/t |

> 💡 **Important:** Ingredient costs remain constant regardless of mass spawned!

---

### ⚡ Efficiency Upgrade
**Effect:** Reduces base power consumption
**Power Cost:** +80/160/240 RF/t per tier (but reduces base cost more!)

| Tier | Power Reduction | Net Savings* | Total Power Cost |
|------|-----------------|--------------|------------------|
| I    | -15% | Saves RF on base cost | +80 RF/t |
| II   | -25% | Saves RF on base cost | +160 RF/t |
| III  | -30% | Saves RF on base cost | +240 RF/t |

*Net savings depends on factory tier and other upgrades

---

### ✨ XP Upgrade
**Effect:** Increases experience generation
**Power Cost:** +80/160/240 RF/t per tier

| Tier | XP Increase | Total Power Cost |
|------|-------------|------------------|
| I    | +20% XP | +80 RF/t |
| II   | +40% XP | +160 RF/t |
| III  | +80% XP | +240 RF/t |

> 📌 *Base XP per mob: 5 (configurable)*

---

### 💀 Decapitate Upgrade
**Effect:** Increases mob head drop chance
**Power Cost:** +80/160/240 RF/t per tier

| Tier | Head Drop Chance | Total Power Cost |
|------|------------------|------------------|
| I    | 20% | +80 RF/t |
| II   | 40% | +160 RF/t |
| III  | 80% | +240 RF/t |

Perfect for collecting Wither Skeleton Skulls or creeper heads!

---

## ⚡ Power Requirements

### Base Power Consumption

Factory power consumption is calculated based on:
- **Factory Tier** - Tier I: 80 RF/t, Tier II: 160 RF/t, Tier III: 240 RF/t, Tier IV: 320 RF/t
- **Upgrades** - Each upgrade adds 80/160/240 RF/t per tier
- **Efficiency** - Reduces base consumption by 15%/25%/30%

### Example Calculation

**Tier III Factory** with:
- Rate III (+240 RF/t)
- Mass III (+240 RF/t)
- Looting III (+240 RF/t)
- Efficiency III (-30% base)

```
Base: 240 RF/t × 0.70 = 168 RF/t
Upgrades: 240 + 240 + 240 = 720 RF/t
Total: 888 RF/t
```

---

## 📋 Spawn Recipes

Factories require **ingredients** to spawn mobs. These are learned through statistical sampling:

### How Recipe Learning Works

1. **Factory spawns a mob** using power
2. **Mob is "killed" internally** and drops are collected
3. **Over time**, the factory learns average drops through sampling
4. **Required ingredients** are calculated based on difficulty and tier

> 💡 More spawn cycles = more accurate drop data!

### Ingredient System

- Ingredients are consumed **once per spawn cycle** (not per mob!)
- Mass upgrades **do not increase** ingredient costs
- Ingredients are pulled automatically from Importers

---

## 🎮 Configuration

Woot includes several configuration options (found in `config/woot-common.toml`):

### Factory General Settings

```toml
[factory_general]
    # Enable tier shard drops and shard-based recipes
    allowShardRecipes = true

    # Tier shard drop chances (% per spawn cycle)
    tier2ShardDropChance = 15
    tier3ShardDropChance = 8
    tier4ShardDropChance = 5
```

### Factory Upgrade Settings

```toml
[factory_upgrades]
    # Base XP value per mob
    xpBasePerMob = 5

    # Enable additional power costs for upgrades
    enableUpgradePowerCosts = true
```

### Ender Shard Configuration

Kill counts can be customized per mob:
- Default: **1 kill** for most mobs
- Bosses: **3-5 kills** (Wither, Dragon, Warden, etc.)
- Fully configurable via data packs

---

## ⚠️ Limitations & Known Behavior

### What Woot Can Do

✅ Farm any living entity (mobs, animals, villagers)
✅ Produce all mob drops including rare items
✅ Generate experience orbs
✅ Scale production with upgrades
✅ Learn mob drops through statistical sampling

### What Woot Cannot Do

❌ Spawn mobs in the world (purely virtual)
❌ Trigger mob-specific events (like Wither spawning)
❌ Bypass mob spawning rules (some mobs may be blacklisted)
❌ Work without power or ingredients

### Important Notes

- **Multiblock Validation:** Breaking any structure block invalidates the factory and stops production
- **Tier Matching:** Higher-tier mobs require higher-tier factories
- **Power Storage:** Use multiple Power Cells for sufficient energy buffer
- **Drop Learning:** Early drops may be inaccurate until sufficient samples are collected

---

## 🔧 Crafting Recipes

### Core Items

- **Stygian Iron Ingot** - Smelted from Stygian Iron Ore (found in the Nether)
- **Stygian Iron Plate** - Hammered from ingots using the Stygian Iron Anvil
- **Factory Base** - Core crafting component
- **Ender Shard** - Programmable mob capture device

### Multiblock Components

All factory blocks are crafted using Stygian Iron components and various cores. Check JEI/REI for full recipes!

---

## 🛠️ Tips & Tricks

### Optimization Strategies

1. **Start Small** - Tier I factories are perfect for basic mobs like Zombies and Cows
2. **Upgrade Wisely** - Rate + Mass = maximum production, but high power cost
3. **Efficiency First** - Add Efficiency upgrades early to reduce power needs
4. **Buffer Power** - Multiple Tier III cells prevent brownouts during peak usage
5. **Learn Drops** - Let factories run for a while to get accurate drop statistics

### Common Setups

**🔥 Speed Farmer** (Maximum Output)
- Tier IV Factory
- Rate III + Mass III + Looting III
- Huge power requirements, insane production

**⚡ Efficient Farmer** (Low Power)
- Tier II Factory
- Efficiency III + Rate I
- Reasonable power, steady output

**💀 Head Hunter** (Skull Collection)
- Any Tier Factory
- Decapitate III + Looting III
- Perfect for Wither Skeleton farms

---

## 📦 Integration

Woot works seamlessly with:
- **Forge Energy (RF)** - All major RF mods supported
- **JEI/REI** - Full recipe support
- **Modded Mobs** - Farm mobs from other mods (unless blacklisted)
- **Storage Mods** - Use any item transport for Importers/Exporters

---

## 📜 Credits

**Original Mod:** Woot by [Ipsis](https://github.com/Ipsis) (1.12.2)
**Current Port:** Woot for NeoForge 1.21.1

Special thanks to the original Woot community and all contributors!

---

**Enjoy automated mob farming with Woot! 🎉**
