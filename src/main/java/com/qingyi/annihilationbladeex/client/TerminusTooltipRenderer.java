package com.qingyi.annihilationbladeex.client;

import com.qingyi.annihilationbladeex.AnnihilationBladeEX;
import com.qingyi.annihilationbladeex.ItemAnnihilationBlade;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class TerminusTooltipRenderer {
    private static final ResourceLocation BLACK_HOLE_BACKGROUND =
            AnnihilationBladeEX.prefix("textures/gui/black_hole_asset_v1_cropped.png");
    private static final int BLACK_HOLE_TEXTURE_WIDTH = 1600;
    private static final int BLACK_HOLE_TEXTURE_HEIGHT = 1000;
    private static final int PANEL_WIDTH = 286;
    private static final int PANEL_MIN_WIDTH = 190;
    private static final int CHIP_HEIGHT = 13;
    private static final int CHIP_GAP = 4;
    private static final int CHIP_MIN_WIDTH = 84;
    private static final int VOID_BLACK = 0xF010061A;
    private static final int ABYSS_PURPLE = 0xCC3E0B72;
    private static final int RIFT_MAGENTA = 0xD9D13CFF;
    private static final int COSMIC_CYAN = 0xD958E7FF;
    private static final int TERMINUS_GOLD = 0xE8FFD36D;
    private static final int PALE_STAR = 0xF0F8EFFF;
    private static final int[] EFFECT_COLORS = {
            RIFT_MAGENTA, COSMIC_CYAN, TERMINUS_GOLD, 0xF8FFB763,
            0xE86DEBFF, PALE_STAR, 0xE88078FF, 0xE8FF7DCE
    };

    private static final String[] DESCRIPTION_KEYS = {
            "item.annihilationbladeex.desc.line1",
            "item.annihilationbladeex.desc.line2",
            "item.annihilationbladeex.desc.line3",
            "item.annihilationbladeex.desc.line4"
    };

    private TerminusTooltipRenderer() {
    }

    public static boolean shouldRender(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ItemAnnihilationBlade) {
            return true;
        }
        if (AnnihilationBladeEX.BLADE_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return true;
        }
        BladeStateData data = BladeStateAccess.getDataOrDefault(stack);
        if (AnnihilationBladeEX.BLADE_TRANSLATION_KEY.equals(data.translationKey())) {
            return true;
        }
        if (AnnihilationBladeEX.SPATIAL_FRACTURE_ID.equals(data.slashArtsKey())) {
            return true;
        }
        return data.specialEffects().stream().anyMatch(AnnihilationBladeEX.GOD_SPECIAL_EFFECTS::contains);
    }

    public static void render(GuiGraphics graphics, Font font, ItemStack stack, List<Component> vanillaLines, int mouseX, int mouseY) {
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int width = Math.min(PANEL_WIDTH, Math.max(PANEL_MIN_WIDTH, screenWidth - 18));
        int contentWidth = width - 24;
        BladeStateData data = BladeStateAccess.getDataOrDefault(stack);
        BladeStats stats = readBladeStats(stack, data);
        List<ResourceLocation> effects = activeEffects(data);
        List<FormattedCharSequence> descriptions = wrapDescription(font, contentWidth);

        int columns = effects.isEmpty() ? 1 : Math.max(1, contentWidth / (CHIP_MIN_WIDTH + CHIP_GAP));
        columns = effects.isEmpty() ? 1 : Math.min(columns, effects.size());
        int rows = effects.isEmpty() ? 0 : (effects.size() + columns - 1) / columns;
        int chipsHeight = rows == 0 ? 0 : rows * CHIP_HEIGHT + (rows - 1) * CHIP_GAP;
        int vanillaExtraLines = Math.min(Math.max(0, vanillaLines.size() - 1), 7);
        int height = 162 + chipsHeight + descriptions.size() * 10 + vanillaExtraLines * 10 + 18;
        int x = mouseX + 12;
        int y = mouseY - 14;

        if (x + width > screenWidth - 6) {
            x = mouseX - width - 18;
        }
        x = Mth.clamp(x, 6, Math.max(6, screenWidth - width - 6));
        if (y + height > screenHeight - 6) {
            y = screenHeight - height - 6;
        }
        y = Mth.clamp(y, 6, Math.max(6, screenHeight - height - 6));

        float time = (System.currentTimeMillis() % 120000L) / 1000.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 720.0F);
        renderFrame(graphics, x, y, width, height, time);
        renderContent(graphics, font, stack, vanillaLines, descriptions, effects, stats, x, y, width, contentWidth, time);
        graphics.pose().popPose();
    }

    private static void renderFrame(GuiGraphics graphics, int x, int y, int width, int height, float time) {
        graphics.fillGradient(x, y, x + width, y + height, VOID_BLACK, 0xF4060210);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.72F);
        int imageWidth = Math.min(width + 118, graphics.guiWidth() - 4);
        int imageHeight = Math.max(96, Math.round(imageWidth * BLACK_HOLE_TEXTURE_HEIGHT / (float) BLACK_HOLE_TEXTURE_WIDTH));
        int imageX = Mth.clamp(x - (imageWidth - width) / 2 + (int) (Mth.sin(time * 0.2F) * 3.0F), 2, Math.max(2, graphics.guiWidth() - imageWidth - 2));
        int imageY = Mth.clamp(y - imageHeight / 5 + (int) (Mth.cos(time * 0.18F) * 3.0F), 2, Math.max(2, graphics.guiHeight() - imageHeight - 2));
        graphics.blit(BLACK_HOLE_BACKGROUND, imageX, imageY, imageWidth, imageHeight,
                0.0F, 0.0F, BLACK_HOLE_TEXTURE_WIDTH, BLACK_HOLE_TEXTURE_HEIGHT,
                BLACK_HOLE_TEXTURE_WIDTH, BLACK_HOLE_TEXTURE_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        graphics.fillGradient(x + 3, y + 3, x + width - 3, y + height - 3, 0x2205010F, 0xD0060210);
        for (int i = 0; i < 5; i++) {
            int color = EFFECT_COLORS[i % EFFECT_COLORS.length];
            int inset = 1 + i * 2;
            graphics.fill(x + inset, y - inset, x + width - inset, y - inset + 1, withAlpha(color, 0.46F - i * 0.05F));
            graphics.fill(x + inset, y + height + inset - 1, x + width - inset, y + height + inset, withAlpha(color, 0.34F - i * 0.04F));
            graphics.fill(x - inset, y + inset, x - inset + 1, y + height - inset, withAlpha(color, 0.30F - i * 0.035F));
            graphics.fill(x + width + inset - 1, y + inset, x + width + inset, y + height - inset, withAlpha(color, 0.30F - i * 0.035F));
        }

        int pulseWidth = (int) ((width - 26) * (0.5F + 0.5F * Mth.sin(time * 3.2F)));
        graphics.fill(x + 13, y + height - 8, x + width - 13, y + height - 7, 0x443E0B72);
        graphics.fill(x + 13, y + height - 8, x + 13 + pulseWidth, y + height - 7, TERMINUS_GOLD);
    }

    private static void renderContent(GuiGraphics graphics, Font font, ItemStack stack, List<Component> vanillaLines,
                                      List<FormattedCharSequence> descriptions, List<ResourceLocation> effects,
                                      BladeStats stats, int x, int y, int width, int contentWidth, float time) {
        int center = x + width / 2;
        Component title = vanillaLines.isEmpty() ? stack.getHoverName() : vanillaLines.get(0);
        drawGlowText(graphics, font, title, center - font.width(title) / 2, y + 10, PALE_STAR);
        graphics.drawCenteredString(font, Component.literal("TERMINUS // SPACE  CAUSALITY  VOID").withStyle(ChatFormatting.DARK_GRAY), center, y + 23, 0x908F7DA7);
        graphics.renderItem(stack, x + 12, y + 13);

        int infoY = y + 82;
        drawTag(graphics, font, x + 12, infoY, "SA", Component.translatable("slashblade.slash_art.annihilationbladeex.spatial_fracture"), COSMIC_CYAN);
        drawTag(graphics, font, x + 12, infoY + 15, "AUTH", Component.translatable("item.annihilationbladeex.passive"), TERMINUS_GOLD);
        drawTag(graphics, font, x + 12, infoY + 30, "DMG", Component.translatable("item.annihilationbladeex.infinite_damage"), RIFT_MAGENTA);

        int statsY = infoY + 47;
        drawBladeStats(graphics, font, x + 12, statsY, contentWidth, stats, time);

        int chipY = statsY + 30;
        drawEffectChips(graphics, font, effects, x + 12, chipY, contentWidth, time);
        int columns = effects.isEmpty() ? 1 : Math.max(1, Math.min(effects.size(), contentWidth / (CHIP_MIN_WIDTH + CHIP_GAP)));
        int rows = effects.isEmpty() ? 0 : (effects.size() + columns - 1) / columns;
        int descY = chipY + (rows == 0 ? 0 : rows * CHIP_HEIGHT + (rows - 1) * CHIP_GAP + 10);

        graphics.fill(x + 12, descY - 5, x + width - 12, descY - 4, 0x553E0B72);
        for (FormattedCharSequence line : descriptions) {
            graphics.drawString(font, line, x + 13, descY, 0xA8C7B8DE, false);
            descY += 10;
        }

        int extraLines = 0;
        for (int i = 1; i < vanillaLines.size() && extraLines < 7; i++) {
            Component line = vanillaLines.get(i);
            if (line.getString().isBlank()) {
                continue;
            }
            graphics.drawString(font, line.copy().withStyle(ChatFormatting.GRAY), x + 13, descY + 2, 0xB8D8CCE8, false);
            descY += 10;
            extraLines++;
        }
    }

    private static void drawTag(GuiGraphics graphics, Font font, int x, int y, String label, Component value, int accent) {
        graphics.fill(x, y, x + 30, y + 11, 0x80160A22);
        graphics.fill(x, y, x + 2, y + 11, withAlpha(accent, 0.88F));
        graphics.drawString(font, label, x + 5, y + 2, 0xFFECDDFF, false);
        graphics.drawString(font, value, x + 36, y + 1, 0xFFD9C9EA, true);
    }

    private static void drawBladeStats(GuiGraphics graphics, Font font, int x, int y, int width, BladeStats stats, float time) {
        int gap = 5;
        int cellWidth = (width - gap * 2) / 3;
        drawStatCell(graphics, font, x, y, cellWidth, "杀敌", Integer.toString(stats.killCount()), COSMIC_CYAN, time);
        drawStatCell(graphics, font, x + cellWidth + gap, y, cellWidth, "耀魂", Integer.toString(stats.proudSoul()), TERMINUS_GOLD, time + 0.7F);
        drawStatCell(graphics, font, x + (cellWidth + gap) * 2, y, width - (cellWidth + gap) * 2, "精炼", Integer.toString(stats.refine()), RIFT_MAGENTA, time + 1.4F);
    }

    private static void drawStatCell(GuiGraphics graphics, Font font, int x, int y, int width, String label, String value, int accent, float phase) {
        graphics.fill(x, y, x + width, y + 20, 0x7E160A22);
        graphics.fill(x, y, x + width, y + 1, withAlpha(accent, 0.70F));
        int pulseWidth = 4 + (int) ((width - 8) * (0.5F + 0.5F * Mth.sin(phase * 2.4F)));
        graphics.fill(x + 2, y + 17, x + Math.min(width - 2, pulseWidth), y + 18, withAlpha(accent, 0.72F));
        graphics.drawString(font, label, x + 4, y + 3, 0xFFB8A5C8, false);
        graphics.drawString(font, value, x + width - font.width(value) - 4, y + 10, 0xFFF4E9FF, false);
    }

    private static void drawEffectChips(GuiGraphics graphics, Font font, List<ResourceLocation> effects, int x, int y, int width, float time) {
        if (effects.isEmpty()) {
            return;
        }
        int columns = Math.max(1, Math.min(effects.size(), width / (CHIP_MIN_WIDTH + CHIP_GAP)));
        int chipWidth = (width - (columns - 1) * CHIP_GAP) / columns;
        for (int i = 0; i < effects.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int chipX = x + col * (chipWidth + CHIP_GAP);
            int chipY = y + row * (CHIP_HEIGHT + CHIP_GAP);
            int color = EFFECT_COLORS[i % EFFECT_COLORS.length];
            Component name = Component.translatable("se." + effects.get(i).getNamespace() + "." + effects.get(i).getPath());
            graphics.fill(chipX, chipY, chipX + chipWidth, chipY + CHIP_HEIGHT, 0x80200A31);
            graphics.fill(chipX, chipY, chipX + chipWidth, chipY + 1, withAlpha(color, 0.60F));
            int pulse = 2 + (int) ((chipWidth - 4) * (0.5F + 0.5F * Mth.sin(time * 2.8F + i)));
            graphics.fill(chipX + 2, chipY + 10, chipX + Math.min(chipWidth - 2, pulse), chipY + 11, withAlpha(color, 0.78F));
            graphics.drawString(font, name, chipX + 5, chipY + 2, 0xFFEDE2FF, false);
        }
    }

    private static void drawGlowText(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x - 1, y, withAlpha(RIFT_MAGENTA, 0.45F), false);
        graphics.drawString(font, text, x + 1, y, withAlpha(RIFT_MAGENTA, 0.45F), false);
        graphics.drawString(font, text, x, y - 1, withAlpha(COSMIC_CYAN, 0.34F), false);
        graphics.drawString(font, text, x, y + 1, withAlpha(TERMINUS_GOLD, 0.28F), false);
        graphics.drawString(font, text, x, y, color, true);
    }

    private static List<FormattedCharSequence> wrapDescription(Font font, int contentWidth) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (String key : DESCRIPTION_KEYS) {
            lines.addAll(font.split(Component.translatable(key), contentWidth));
        }
        return lines;
    }

    private static List<ResourceLocation> activeEffects(BladeStateData data) {
        List<ResourceLocation> effects = new ArrayList<>();
        for (ResourceLocation id : data.specialEffects()) {
            if (AnnihilationBladeEX.GOD_SPECIAL_EFFECTS.contains(id)) {
                effects.add(id);
            }
        }
        return effects.isEmpty() ? AnnihilationBladeEX.GOD_SPECIAL_EFFECTS : effects;
    }

    private static BladeStats readBladeStats(ItemStack stack, BladeStateData data) {
        return BladeStateAccess.of(stack)
                .map(state -> new BladeStats(state.getProudSoulCount(), state.getKillCount(), state.getRefine()))
                .orElseGet(() -> new BladeStats(data.proudSoul(), data.killCount(), data.refine()));
    }

    private static int withAlpha(int color, float alpha) {
        int a = Mth.clamp((int) (((color >>> 24) & 255) * Mth.clamp(alpha, 0.0F, 1.0F)), 0, 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    private record BladeStats(int proudSoul, int killCount, int refine) {
    }
}
