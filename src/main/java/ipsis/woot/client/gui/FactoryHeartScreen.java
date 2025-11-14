package ipsis.woot.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import ipsis.woot.Woot;
import ipsis.woot.gui.FactoryHeartMenu;
import ipsis.woot.gui.data.FarmUIInfo;
import ipsis.woot.network.RequestFarmInfoPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

/**
 * Factory Heart GUI Screen
 * Matches the exact original 1.12.2 layout and styling
 */
public class FactoryHeartScreen extends AbstractContainerScreen<FactoryHeartMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Woot.MODID, "textures/gui/factory_heart.png");

    // Exact dimensions from original
    private static final int WIDTH = 256;
    private static final int HEIGHT = 224;
    private static final int GUI_X_MARGIN = 4;
    private static final int GUI_Y_MARGIN = 4;
    private static final int PANEL_MARGIN = 4;
    private static final int PANEL_X_MARGIN = 2;
    private static final int PANEL_Y_MARGIN = 2;
    private static final int TEXT_HEIGHT = 10;
    private static final int TEXT_X_MARGIN = 1;
    private static final int TEXT_Y_MARGIN = 1;
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_X_MARGIN = 1;
    private static final int BAR_Y_MARGIN = 1;

    // Panel heights
    private static final int RECIPE_HEIGHT = 55;
    private static final int PROGRESS_HEIGHT = 18;
    private static final int INGREDIENT_HEIGHT = 40;

    // Colors (Java AWT colors)
    private static final int COLOR_YELLOW = 0xFFFF00;      // Yellow header
    private static final int COLOR_WHITE = 0xFFFFFF;       // White text
    private static final int COLOR_GREEN = 0x55FF55;       // Green text
    private static final int COLOR_DARK_GRAY = 0x404040;   // Panel backgrounds
    private static final int COLOR_BLACK = 0x000000;       // Bar backgrounds
    private static final int COLOR_RED = 0xFF0000;         // Power bar
    private static final int COLOR_ORANGE = 0xFFA500;      // Spawning bar

    private static final DecimalFormat dfCommas = new DecimalFormat("###,###");

    // Update tracking
    private int tickCounter = 0;
    private static final int REQUEST_INTERVAL = 20;

    // Farm UI information (updated via network)
    private FarmUIInfo farmUIInfo = new FarmUIInfo();

    public FactoryHeartScreen(FactoryHeartMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        requestFarmInfo();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        tickCounter++;
        if (tickCounter % REQUEST_INTERVAL == 0) {
            requestFarmInfo();
        }
    }

    private void requestFarmInfo() {
        PacketDistributor.sendToServer(new RequestFarmInfoPayload(menu.getHeartPos()));
    }

    /**
     * Update farm UI information from network packet
     * Called by client-side network handler
     */
    public void updateFarmInfo(FarmUIInfo info) {
        this.farmUIInfo = info;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render main GUI background from texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Render panel backgrounds (converted to screen coordinates)
        renderPanelBackgrounds(guiGraphics, x, y);
    }

    private void renderPanelBackgrounds(GuiGraphics guiGraphics, int screenX, int screenY) {
        int panelWidth = WIDTH - (GUI_X_MARGIN * 2);

        // Configuration panel background
        int y = GUI_Y_MARGIN;
        drawSizedRect(guiGraphics, screenX + GUI_X_MARGIN, screenY + y, panelWidth, RECIPE_HEIGHT, COLOR_DARK_GRAY);
        y += RECIPE_HEIGHT + PANEL_MARGIN;

        // Power progress panel background
        drawSizedRect(guiGraphics, screenX + GUI_X_MARGIN, screenY + y, panelWidth, PROGRESS_HEIGHT, COLOR_DARK_GRAY);
        y += PROGRESS_HEIGHT + PANEL_MARGIN;

        // Spawning progress panel background
        drawSizedRect(guiGraphics, screenX + GUI_X_MARGIN, screenY + y, panelWidth, PROGRESS_HEIGHT, COLOR_DARK_GRAY);
        y += PROGRESS_HEIGHT + PANEL_MARGIN;

        // Ingredients panel background
        drawSizedRect(guiGraphics, screenX + GUI_X_MARGIN, screenY + y, panelWidth, INGREDIENT_HEIGHT, COLOR_DARK_GRAY);
        y += INGREDIENT_HEIGHT + PANEL_MARGIN;

        // Drops panel background (remaining height)
        int dropsHeight = HEIGHT - (GUI_Y_MARGIN * 2) - RECIPE_HEIGHT - (PROGRESS_HEIGHT * 2) - INGREDIENT_HEIGHT - (PANEL_MARGIN * 4);
        drawSizedRect(guiGraphics, screenX + GUI_X_MARGIN, screenY + y, panelWidth, dropsHeight, COLOR_DARK_GRAY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // All rendering done here (relative to GUI top-left, no need to add guiLeft/guiTop)

        int panelWidth = WIDTH - (GUI_X_MARGIN * 2);
        int yOffset = GUI_Y_MARGIN;

        // Configuration Panel
        renderConfigurationPanel(guiGraphics, GUI_X_MARGIN, yOffset, panelWidth, RECIPE_HEIGHT);
        yOffset += RECIPE_HEIGHT + PANEL_MARGIN;

        // Power Progress Bar
        renderPowerBar(guiGraphics, GUI_X_MARGIN, yOffset, panelWidth, PROGRESS_HEIGHT);
        yOffset += PROGRESS_HEIGHT + PANEL_MARGIN;

        // Spawning Progress Bar
        renderSpawningBar(guiGraphics, GUI_X_MARGIN, yOffset, panelWidth, PROGRESS_HEIGHT);
        yOffset += PROGRESS_HEIGHT + PANEL_MARGIN;

        // Ingredients Panel
        renderIngredientsPanel(guiGraphics, GUI_X_MARGIN, yOffset, panelWidth, INGREDIENT_HEIGHT);
        yOffset += INGREDIENT_HEIGHT + PANEL_MARGIN;

        // Drops Panel
        int dropsHeight = HEIGHT - (GUI_Y_MARGIN * 2) - RECIPE_HEIGHT - (PROGRESS_HEIGHT * 2) - INGREDIENT_HEIGHT - (PANEL_MARGIN * 4);
        renderDropsPanel(guiGraphics, GUI_X_MARGIN, yOffset, panelWidth, dropsHeight);

        // Render tooltips for items (must be last)
        renderDropTooltips(guiGraphics, mouseX, mouseY, GUI_X_MARGIN, yOffset, panelWidth, dropsHeight);
    }

    /**
     * Render tooltips for drop items when hovering
     */
    private void renderDropTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int width, int height) {
        int contentX = x + PANEL_X_MARGIN + 2;
        int contentY = y + PANEL_Y_MARGIN + getTextHeight() + 2;

        List<net.minecraft.world.item.ItemStack> drops = farmUIInfo.getDrops();
        if (drops.isEmpty()) {
            return;
        }

        int itemSize = 18;
        int itemsPerRow = (width - PANEL_X_MARGIN * 2 - 4) / itemSize;
        int mobCount = menu.getMobCount();
        int row = 0;
        int col = 0;

        for (int i = 0; i < drops.size(); i++) {
            net.minecraft.world.item.ItemStack stack = drops.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            int itemX = contentX + (col * itemSize);
            int itemY = contentY + (row * itemSize);

            // Check if mouse is hovering over this item
            if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
                // Calculate approximate drop chance
                float dropChance = (stack.getCount() / (float) mobCount) * 100.0f;
                String chanceText = String.format("%.1f%%", dropChance);

                // Create tooltip components
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(stack.getHoverName());
                tooltip.add(Component.literal("Drop Chance: " + chanceText).withStyle(net.minecraft.ChatFormatting.GRAY));
                tooltip.add(Component.literal("Average: " + stack.getCount() + " per " + mobCount + " mobs").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));

                // Render tooltip
                guiGraphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
                return; // Only show one tooltip at a time
            }

            col++;
            if (col >= itemsPerRow) {
                col = 0;
                row++;
            }
        }
    }

    private void renderConfigurationPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Yellow header
        drawText(guiGraphics, "Configuration", x, y, COLOR_YELLOW);

        // Content area
        int contentX = x + PANEL_X_MARGIN;
        int contentY = y + PANEL_Y_MARGIN + getTextHeight();

        boolean isValid = menu.isValid();
        if (isValid) {
            int mobCount = menu.getMobCount();
            long totalPower = menu.getRecipeTotalPower();
            int powerPerTick = menu.getRecipePowerPerTick();
            int totalTime = menu.getRecipeTotalTime();

            // Line 1: "Tier <TIER_NUMBER> <MOB> × <COUNT>" (white)
            String tierRoman = getTierRoman(farmUIInfo.getTier().getLevel());
            String mobName = farmUIInfo.getMobName().getString();
            String tierLine = "Tier " + tierRoman + " " + mobName + " \u00D7 " + mobCount;
            drawText(guiGraphics, tierLine, contentX, contentY, COLOR_WHITE);
            contentY += getTextHeight();

            // Line 2: "Power: 16,000RF @ 80RF/tick" (green)
            String powerLine = "Power: " + dfCommas.format(totalPower) + "RF @ " + dfCommas.format(powerPerTick) + "RF/tick";
            drawText(guiGraphics, powerLine, contentX, contentY, COLOR_GREEN);
            contentY += getTextHeight();

            // Line 3: "Time: 200 ticks" (green)
            String timeLine = "Time: " + totalTime + " ticks";
            drawText(guiGraphics, timeLine, contentX, contentY, COLOR_GREEN);
        } else {
            drawText(guiGraphics, "Structure Invalid", contentX, contentY, COLOR_RED);
        }
    }

    private void renderPowerBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int contentX = x + PANEL_X_MARGIN;
        int contentY = y + PANEL_Y_MARGIN;

        String tag = "Power:";
        int tagWidth = this.font.width(tag);

        // Draw tag
        drawText(guiGraphics, tag, contentX, contentY, COLOR_WHITE);

        // Calculate bar position and width
        int barX = contentX + tagWidth + (2 * TEXT_X_MARGIN) + 1;
        int barY = contentY;
        int barWidth = width - (PANEL_X_MARGIN * 2) - tagWidth - (2 * TEXT_X_MARGIN) - 2;

        // Get power data
        int powerStored = menu.getPowerStored();
        int powerCapacity = menu.getPowerCapacity();
        int percentage = powerCapacity > 0 ? (int)((100.0F / powerCapacity) * powerStored) : 0;

        // Draw bar (screen coordinates)
        drawBar(guiGraphics, barX, barY, barWidth, percentage, COLOR_RED);

        // Draw centered text showing curr/max
        String barText = powerStored + "/" + powerCapacity;
        int textX = barX + (barWidth / 2) - (this.font.width(barText) / 2);
        int textY = barY + 1;
        drawText(guiGraphics, barText, textX, textY, COLOR_WHITE);
    }

    private void renderSpawningBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int contentX = x + PANEL_X_MARGIN;
        int contentY = y + PANEL_Y_MARGIN;

        String tag = "Spawning";
        int tagWidth = this.font.width(tag);

        // Draw tag
        drawText(guiGraphics, tag, contentX, contentY, COLOR_WHITE);

        // Calculate bar position and width
        int barX = contentX + tagWidth + (2 * TEXT_X_MARGIN) + 1;
        int barY = contentY;
        int barWidth = width - (PANEL_X_MARGIN * 2) - tagWidth - (2 * TEXT_X_MARGIN) - 2;

        // Get progress data
        long consumedPower = menu.getConsumedPower();
        long totalPower = menu.getRecipeTotalPower();
        int percentage = totalPower > 0 ? (int)((100.0F / totalPower) * consumedPower) : 0;

        // Draw bar (screen coordinates)
        drawBar(guiGraphics, barX, barY, barWidth, percentage, COLOR_ORANGE);

        // Draw centered percentage text
        String barText = percentage + "%";
        int textX = barX + (barWidth / 2) - (this.font.width(barText) / 2);
        int textY = barY + 1;
        drawText(guiGraphics, barText, textX, textY, COLOR_WHITE);
    }

    private void renderIngredientsPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Yellow header
        drawText(guiGraphics, "Ingredients", x, y, COLOR_YELLOW);

        // TODO Phase 3: Render ingredient item stacks
    }

    private void renderDropsPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Yellow header
        drawText(guiGraphics, "Drops", x, y, COLOR_YELLOW);

        // Render drop items in a grid
        int contentX = x + PANEL_X_MARGIN + 2;
        int contentY = y + PANEL_Y_MARGIN + getTextHeight() + 2;

        List<net.minecraft.world.item.ItemStack> drops = farmUIInfo.getDrops();
        if (drops.isEmpty()) {
            return;
        }

        int itemSize = 18; // Standard item render size (16px + 2px spacing)
        int itemsPerRow = (width - PANEL_X_MARGIN * 2 - 4) / itemSize;
        int row = 0;
        int col = 0;

        for (int i = 0; i < drops.size(); i++) {
            net.minecraft.world.item.ItemStack stack = drops.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            int itemX = contentX + (col * itemSize);
            int itemY = contentY + (row * itemSize);

            // Render the item
            guiGraphics.renderItem(stack, itemX, itemY);
            guiGraphics.renderItemDecorations(this.font, stack, itemX, itemY);

            col++;
            if (col >= itemsPerRow) {
                col = 0;
                row++;
            }
        }
    }

    // Helper methods

    private String getTierRoman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> "I";
        };
    }

    private void drawSizedRect(GuiGraphics guiGraphics, int x1, int y1, int width, int height, int color) {
        guiGraphics.fill(x1, y1, x1 + width, y1 + height, 0xFF000000 | color);
    }

    private void drawText(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        guiGraphics.drawString(this.font, text, x + TEXT_X_MARGIN, y + TEXT_Y_MARGIN, color, false);
    }

    private int getTextHeight() {
        return TEXT_HEIGHT + (TEXT_Y_MARGIN * 2);
    }

    private void drawBar(GuiGraphics guiGraphics, int x, int y, int width, int percentage, int color) {
        // Clamp percentage
        percentage = Math.max(0, Math.min(100, percentage));

        // Calculate fill length
        int length = (int)(((float)width / 100.0F) * percentage);

        // Black background
        guiGraphics.fill(
            x + BAR_X_MARGIN,
            y + BAR_Y_MARGIN,
            x + BAR_X_MARGIN + width,
            y + BAR_Y_MARGIN + BAR_HEIGHT,
            0xFF000000 | COLOR_BLACK
        );

        // Colored fill (1px inset)
        if (percentage > 0) {
            guiGraphics.fill(
                x + BAR_X_MARGIN + 1,
                y + BAR_Y_MARGIN + 1,
                x + BAR_X_MARGIN + length - 1,
                y + BAR_Y_MARGIN + BAR_HEIGHT - 1,
                0xFF000000 | color
            );
        }
    }
}
