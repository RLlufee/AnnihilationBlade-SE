package org.examplea.annihilationblade.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.examplea.annihilationblade.Annihilationblade;
import org.examplea.annihilationblade.item.ItemAnnihilationBlade;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TerminusTooltipRenderer {
    private static final int VOID_BLACK = 0xF010061A;
    private static final int ABYSS_PURPLE = 0xCC3E0B72;
    private static final int RIFT_MAGENTA = 0xD9D13CFF;
    private static final int COSMIC_CYAN = 0xD958E7FF;
    private static final int TERMINUS_GOLD = 0xE8FFD36D;
    private static final int PALE_STAR = 0xF0F8EFFF;
    private static final int WIDTH = 286;
    private static final int MIN_WIDTH = 190;

    private static final String[] DESCRIPTION_KEYS = {
            "item.annihilationblade.desc.line1",
            "item.annihilationblade.desc.line2",
            "item.annihilationblade.desc.line3",
            "item.annihilationblade.desc.line4"
    };

    private static final String[] SPECIAL_EFFECT_KEYS = {
            "se.annihilationblade.dankong",
            "se.annihilationblade.world_rift",
            "se.annihilationblade.terminus_echo",
            "se.annihilationblade.void_dominion",
            "se.annihilationblade.causality_collapse",
            "se.annihilationblade.starless_judgement"
    };

    private static final String[] SPECIAL_EFFECT_IDS = {
            "annihilationblade:dankong",
            "annihilationblade:world_rift",
            "annihilationblade:terminus_echo",
            "annihilationblade:void_dominion",
            "annihilationblade:causality_collapse",
            "annihilationblade:starless_judgement"
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

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (ResourceLocation.fromNamespaceAndPath(Annihilationblade.MODID, "annihilation_blade").equals(itemId)) {
            return true;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        if (tag.getBoolean("IsAnnihilationBlade")) {
            return true;
        }
        if (isBladeAsset(tag.getString("ModelName")) || isBladeAsset(tag.getString("TextureName"))) {
            return true;
        }
        if ("annihilationblade:spatial_fracture".equals(tag.getString("SlashArts"))) {
            return true;
        }

        CompoundTag bladeState = tag.getCompound("bladeState");
        ListTag specialEffects = bladeState.getList("SpecialEffects", Tag.TAG_STRING);
        for (int i = 0; i < specialEffects.size(); i++) {
            for (String effectId : SPECIAL_EFFECT_IDS) {
                if (effectId.equals(specialEffects.getString(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void render(GuiGraphics graphics, Font font, ItemStack stack, List<Component> vanillaLines, int mouseX, int mouseY) {
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int width = Math.min(WIDTH, Math.max(MIN_WIDTH, screenWidth - 18));
        int contentWidth = width - 24;
        List<FormattedCharSequence> descriptionLines = wrapDescription(font, contentWidth);
        List<FormattedCharSequence> enchantmentLines = wrapEnchantments(font, stack, contentWidth);
        BladeStats bladeStats = readBladeStats(stack);
        int descriptionHeight = descriptionLines.size() * 10;
        int enchantmentHeight = enchantmentLines.isEmpty() ? 0 : 14 + enchantmentLines.size() * 10;
        int height = 236 + enchantmentHeight + descriptionHeight;
        int x = mouseX + 12;
        int y = mouseY - 14;

        if (x + width > screenWidth - 6) {
            x = mouseX - width - 18;
        }
        if (x < 6) {
            x = 6;
        }
        if (y + height > screenHeight - 6) {
            y = screenHeight - height - 6;
        }
        if (y < 6) {
            y = 6;
        }

        float time = (System.currentTimeMillis() % 120000L) / 1000.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 720.0F);
        renderFrame(graphics, x, y, width, height, time);
        renderTerminusSeals(graphics, x, y, width, height, time);
        renderBlackHole(graphics, x, y, width, time);
        renderContent(graphics, font, stack, vanillaLines, descriptionLines, enchantmentLines, bladeStats, x, y, width, height, time);
        graphics.pose().popPose();
    }

    private static List<FormattedCharSequence> wrapDescription(Font font, int contentWidth) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (String key : DESCRIPTION_KEYS) {
            lines.addAll(font.split(Component.translatable(key), contentWidth));
        }
        return lines;
    }

    private static List<FormattedCharSequence> wrapEnchantments(Font font, ItemStack stack, int contentWidth) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) {
            return List.of();
        }

        List<Map.Entry<Enchantment, Integer>> entries = new ArrayList<>(enchantments.entrySet());
        entries.sort(Comparator.comparing(entry -> entry.getKey().getDescriptionId()));

        List<FormattedCharSequence> lines = new ArrayList<>();
        MutableComponent current = Component.empty();
        int currentWidth = 0;
        for (Map.Entry<Enchantment, Integer> entry : entries) {
            Component name = entry.getKey().getFullname(entry.getValue());
            int separatorWidth = currentWidth == 0 ? 0 : font.width(" / ");
            int nameWidth = font.width(name);
            if (currentWidth > 0 && currentWidth + separatorWidth + nameWidth > contentWidth) {
                lines.add(current.getVisualOrderText());
                current = Component.empty();
                currentWidth = 0;
                separatorWidth = 0;
            }
            if (currentWidth > 0) {
                current.append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY));
                currentWidth += separatorWidth;
            }
            current.append(name.copy().withStyle(ChatFormatting.LIGHT_PURPLE));
            currentWidth += nameWidth;
        }

        if (currentWidth > 0) {
            lines.add(current.getVisualOrderText());
        }
        return lines;
    }

    private static void renderFrame(GuiGraphics graphics, int x, int y, int width, int height, float time) {
        graphics.fillGradient(x, y, x + width, y + height, VOID_BLACK, 0xF4060210);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xAA2D0B55);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xAA5D165D);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, 0xAA26134D);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xAABF7CFF);

        int sweep = x + 8 + (int) (((time * 42.0F) % (width + 80)) - 40.0F);
        graphics.fill(sweep, y + 2, sweep + 2, y + height - 2, 0x55F8EFFF);
        graphics.fill(sweep + 3, y + 2, sweep + 4, y + height - 2, 0x35D13CFF);

        graphics.enableScissor(x + 2, y + 2, x + width - 2, y + height - 2);
        Matrix4f matrix = graphics.pose().last().pose();
        for (int i = 0; i < 9; i++) {
            float phase = (time * 0.11F + i * 0.137F) % 1.0F;
            float sx = x - 28.0F + phase * (width + 56.0F);
            float alpha = 0.18F + 0.12F * Mth.sin(time * 1.7F + i);
            drawBeam(matrix, sx, y + height + 14.0F, sx + 70.0F, y - 18.0F, 1.4F, withAlpha(COSMIC_CYAN, alpha), withAlpha(RIFT_MAGENTA, alpha * 0.65F));
        }
        for (int i = 0; i < 34; i++) {
            float seed = i * 12.9898F;
            float px = x + 8.0F + noise(seed) * (width - 16.0F);
            float py = y + 8.0F + noise(seed + 8.0F) * (height - 16.0F);
            float pulse = 0.45F + 0.55F * Mth.sin(time * 2.3F + i * 0.71F);
            int color = i % 5 == 0 ? TERMINUS_GOLD : (i % 2 == 0 ? COSMIC_CYAN : RIFT_MAGENTA);
            drawQuad(matrix, px, py, px + 1.0F + pulse, py + 1.0F + pulse, withAlpha(color, 0.18F + pulse * 0.22F));
        }
        graphics.disableScissor();

        drawCornerGlyphs(matrix, x, y, width, height, time);
    }

    private static void renderTerminusSeals(GuiGraphics graphics, int x, int y, int width, int height, float time) {
        Matrix4f matrix = graphics.pose().last().pose();
        float mainCx = x + width - 58.0F;
        float mainCy = y + 58.0F;
        float minorCx = x + 45.0F;
        float minorCy = y + 55.0F;

        drawRing(matrix, mainCx, mainCy, 42.0F, 1.1F, withAlpha(RIFT_MAGENTA, 0.38F), time * 0.35F, 48, 0);
        drawRing(matrix, mainCx, mainCy, 35.0F, 0.9F, withAlpha(COSMIC_CYAN, 0.35F), -time * 0.48F, 36, 1);
        drawRotatingHexagram(matrix, mainCx, mainCy, 31.0F, time * 0.82F, 1.25F, withAlpha(TERMINUS_GOLD, 0.62F), withAlpha(RIFT_MAGENTA, 0.46F));
        drawRotatingHexagram(matrix, mainCx, mainCy, 21.0F, -time * 1.18F, 0.9F, withAlpha(PALE_STAR, 0.40F), withAlpha(COSMIC_CYAN, 0.48F));
        drawOrbitingRunes(matrix, mainCx, mainCy, 39.0F, time, 12);

        drawRing(matrix, minorCx, minorCy, 22.0F, 0.9F, withAlpha(COSMIC_CYAN, 0.36F), -time * 0.65F, 30, 0);
        drawRotatingHexagram(matrix, minorCx, minorCy, 17.0F, -time * 1.05F, 0.95F, withAlpha(RIFT_MAGENTA, 0.55F), withAlpha(COSMIC_CYAN, 0.38F));
        drawOblivionCrown(matrix, x + width * 0.50F, y + 63.0F, width * 0.34F, time);
        drawCausalityThreads(matrix, x, y, width, height, time);
    }

    private static void renderBlackHole(GuiGraphics graphics, int x, int y, int width, float time) {
        Matrix4f matrix = graphics.pose().last().pose();
        float cx = x + width * 0.55F;
        float cy = y + 60.0F;
        float wobble = Mth.sin(time * 1.3F) * 1.8F;

        drawEllipse(matrix, cx, cy + wobble, 86.0F, 34.0F, withAlpha(RIFT_MAGENTA, 0.00F), withAlpha(RIFT_MAGENTA, 0.12F), 56);
        drawEllipse(matrix, cx, cy + wobble, 66.0F, 25.0F, withAlpha(COSMIC_CYAN, 0.00F), withAlpha(COSMIC_CYAN, 0.10F), 48);
        drawAccretionDisk(matrix, cx, cy + wobble, 78.0F, 18.0F, time * 1.15F, 2.2F);
        drawAccretionDisk(matrix, cx, cy + wobble, 58.0F, 13.0F, -time * 1.55F, 1.4F);
        drawVortexSpiral(matrix, cx, cy + wobble, time);
        drawDisc(matrix, cx, cy + wobble, 28.0F, 0xFC010006, 0xF40D0216, 48);
        drawDisc(matrix, cx, cy + wobble, 18.0F, 0xFF000000, 0xFD020008, 42);
        drawRing(matrix, cx, cy + wobble, 31.0F, 1.4F, withAlpha(PALE_STAR, 0.30F), time * 1.8F, 44, 2);
        drawRing(matrix, cx, cy + wobble, 38.0F, 0.9F, withAlpha(TERMINUS_GOLD, 0.34F), -time * 0.9F, 36, 1);
        drawInfallingStars(matrix, cx, cy + wobble, time);
    }

    private static void renderContent(GuiGraphics graphics, Font font, ItemStack stack, List<Component> vanillaLines,
                                      List<FormattedCharSequence> descriptionLines, List<FormattedCharSequence> enchantmentLines,
                                      BladeStats bladeStats, int x, int y, int width, int height, float time) {
        int center = x + width / 2;
        Component title = vanillaLines.isEmpty() ? stack.getHoverName() : vanillaLines.get(0);
        drawGlowText(graphics, font, title, center - font.width(title) / 2, y + 10, PALE_STAR, RIFT_MAGENTA);
        graphics.drawCenteredString(font, Component.literal("TERMINUS // SPACE  CAUSALITY  VOID").withStyle(ChatFormatting.DARK_GRAY), center, y + 23, 0x908F7DA7);

        graphics.renderItem(stack, x + 12, y + 13);

        int infoY = y + 86;
        drawTag(graphics, font, x + 12, infoY, "SA", Component.translatable("slashblade.slash_art.annihilationblade.spatial_fracture"), 0x994B136F, COSMIC_CYAN);
        drawTag(graphics, font, x + 12, infoY + 15, "AUTH", Component.translatable("item.annihilationblade.passive"), 0x995E3A12, TERMINUS_GOLD);
        drawTag(graphics, font, x + 12, infoY + 30, "DMG", Component.translatable("item.annihilationblade.infinite_damage"), 0x99440A48, RIFT_MAGENTA);

        int statsY = infoY + 45;
        drawBladeStats(graphics, font, x + 12, statsY, width - 24, bladeStats, time);

        int chipY = statsY + 30;
        int chipWidth = (width - 30) / 2;
        for (int i = 0; i < SPECIAL_EFFECT_KEYS.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int chipX = x + 12 + col * (chipWidth + 6);
            int color = i % 3 == 0 ? RIFT_MAGENTA : (i % 3 == 1 ? COSMIC_CYAN : TERMINUS_GOLD);
            drawSpecialEffectChip(graphics, font, chipX, chipY + row * 15, chipWidth, Component.translatable(SPECIAL_EFFECT_KEYS[i]), color, time + i * 0.37F);
        }

        int enchantY = chipY + 50;
        int enchantmentHeight = drawEnchantments(graphics, font, x + 12, enchantY, width - 24, enchantmentLines);

        int descY = enchantY + (enchantmentHeight == 0 ? 4 : enchantmentHeight + 7);
        graphics.fill(x + 12, descY - 5, x + width - 12, descY - 4, 0x553E0B72);
        for (FormattedCharSequence line : descriptionLines) {
            graphics.drawString(font, line, x + 13, descY, 0xA8C7B8DE, false);
            descY += 10;
        }

        int pulse = (int) ((width - 26) * (0.45F + 0.55F * (0.5F + 0.5F * Mth.sin(time * 3.4F))));
        graphics.fill(x + 13, y + height - 8, x + width - 13, y + height - 7, 0x443E0B72);
        graphics.fill(x + 13, y + height - 8, x + 13 + pulse, y + height - 7, 0xD8FFD36D);
    }

    private static void drawBladeStats(GuiGraphics graphics, Font font, int x, int y, int width, BladeStats stats, float time) {
        int gap = 5;
        int cellWidth = (width - gap * 2) / 3;
        drawStatCell(graphics, font, x, y, cellWidth, "杀敌", formatNumber(stats.killCount()), COSMIC_CYAN, time);
        drawStatCell(graphics, font, x + cellWidth + gap, y, cellWidth, "耀魂", formatNumber(stats.proudSoul()), TERMINUS_GOLD, time + 0.7F);
        drawStatCell(graphics, font, x + (cellWidth + gap) * 2, y, width - (cellWidth + gap) * 2, "精炼", formatNumber(stats.refine()), RIFT_MAGENTA, time + 1.4F);
    }

    private static void drawStatCell(GuiGraphics graphics, Font font, int x, int y, int width, String label, String value, int accent, float phase) {
        graphics.fill(x, y, x + width, y + 20, 0x7E160A22);
        graphics.fill(x, y, x + width, y + 1, withAlpha(accent, 0.70F));
        int pulseWidth = 4 + (int) ((width - 8) * (0.45F + 0.55F * (0.5F + 0.5F * Mth.sin(phase * 2.4F))));
        graphics.fill(x + 2, y + 17, x + Math.min(width - 2, pulseWidth), y + 18, withAlpha(accent, 0.72F));
        graphics.drawString(font, label, x + 4, y + 3, 0xFFB8A5C8, false);
        graphics.drawString(font, value, x + width - font.width(value) - 4, y + 10, 0xFFF4E9FF, false);
    }

    private static int drawEnchantments(GuiGraphics graphics, Font font, int x, int y, int width, List<FormattedCharSequence> enchantmentLines) {
        if (enchantmentLines.isEmpty()) {
            return 0;
        }

        int height = 14 + enchantmentLines.size() * 10;
        graphics.fill(x, y, x + width, y + height, 0x68160A22);
        graphics.fill(x, y, x + width, y + 1, 0x88FFD36D);
        graphics.fill(x, y + height - 1, x + width, y + height, 0x6658E7FF);
        graphics.drawString(font, "附魔回路", x + 5, y + 3, TERMINUS_GOLD, false);

        int lineY = y + 14;
        for (FormattedCharSequence line : enchantmentLines) {
            graphics.drawString(font, line, x + 5, lineY, 0xFFE7D8FF, false);
            lineY += 10;
        }
        return height;
    }

    private static void drawTag(GuiGraphics graphics, Font font, int x, int y, String label, Component value, int background, int accent) {
        graphics.fill(x, y, x + 30, y + 11, background);
        graphics.fill(x, y, x + 2, y + 11, accent);
        graphics.drawString(font, label, x + 5, y + 2, 0xFFECDDFF, false);
        graphics.drawString(font, value, x + 36, y + 1, 0xFFD9C9EA, true);
    }

    private static void drawSpecialEffectChip(GuiGraphics graphics, Font font, int x, int y, int width, Component text, int color, float phase) {
        graphics.fill(x, y, x + width, y + 12, 0x80200A31);
        graphics.fill(x, y, x + width, y + 1, withAlpha(color, 0.54F));
        graphics.fill(x, y + 11, x + width, y + 12, 0x5520083A);
        int fill = 12 + (int) ((width - 18) * (0.5F + 0.5F * Mth.sin(phase * 2.7F)));
        graphics.fill(x + 2, y + 9, x + Math.min(width - 2, fill), y + 10, withAlpha(color, 0.78F));
        graphics.drawString(font, text, x + 5, y + 2, 0xFFEDE2FF, false);
    }

    private static void drawGlowText(GuiGraphics graphics, Font font, Component text, int x, int y, int color, int glow) {
        graphics.drawString(font, text, x - 1, y, withAlpha(glow, 0.45F), false);
        graphics.drawString(font, text, x + 1, y, withAlpha(glow, 0.45F), false);
        graphics.drawString(font, text, x, y - 1, withAlpha(COSMIC_CYAN, 0.34F), false);
        graphics.drawString(font, text, x, y + 1, withAlpha(TERMINUS_GOLD, 0.28F), false);
        graphics.drawString(font, text, x, y, color, true);
    }

    private static void drawCornerGlyphs(Matrix4f matrix, int x, int y, int width, int height, float time) {
        float pulse = 0.35F + 0.35F * (0.5F + 0.5F * Mth.sin(time * 2.0F));
        int purple = withAlpha(RIFT_MAGENTA, pulse);
        int cyan = withAlpha(COSMIC_CYAN, pulse);
        int gold = withAlpha(TERMINUS_GOLD, pulse + 0.18F);
        drawBeam(matrix, x + 8.0F, y + 15.0F, x + 24.0F, y + 6.0F, 1.5F, purple, cyan);
        drawBeam(matrix, x + width - 8.0F, y + 15.0F, x + width - 24.0F, y + 6.0F, 1.5F, cyan, gold);
        drawBeam(matrix, x + 8.0F, y + height - 15.0F, x + 24.0F, y + height - 6.0F, 1.5F, gold, purple);
        drawBeam(matrix, x + width - 8.0F, y + height - 15.0F, x + width - 24.0F, y + height - 6.0F, 1.5F, purple, cyan);
    }

    private static void drawRotatingHexagram(Matrix4f matrix, float cx, float cy, float radius, float rotation, float width, int colorA, int colorB) {
        float[] xs = new float[6];
        float[] ys = new float[6];
        for (int i = 0; i < 6; i++) {
            float angle = rotation + (float) (Math.PI / 6.0D + i * Math.PI / 3.0D);
            xs[i] = cx + Mth.cos(angle) * radius;
            ys[i] = cy + Mth.sin(angle) * radius;
        }

        drawTriangle(matrix, xs[0], ys[0], xs[2], ys[2], xs[4], ys[4], withAlpha(colorA, 0.13F));
        drawTriangle(matrix, xs[1], ys[1], xs[3], ys[3], xs[5], ys[5], withAlpha(colorB, 0.11F));
        drawBeam(matrix, xs[0], ys[0], xs[2], ys[2], width, colorA, colorB);
        drawBeam(matrix, xs[2], ys[2], xs[4], ys[4], width, colorB, colorA);
        drawBeam(matrix, xs[4], ys[4], xs[0], ys[0], width, colorA, colorB);
        drawBeam(matrix, xs[1], ys[1], xs[3], ys[3], width, colorB, colorA);
        drawBeam(matrix, xs[3], ys[3], xs[5], ys[5], width, colorA, colorB);
        drawBeam(matrix, xs[5], ys[5], xs[1], ys[1], width, colorB, colorA);
        drawDiamond(matrix, cx, cy, Math.max(2.0F, width * 2.1F), withAlpha(PALE_STAR, 0.55F));
    }

    private static void drawRing(Matrix4f matrix, float cx, float cy, float radius, float width, int color, float rotation, int segments, int gapMode) {
        for (int i = 0; i < segments; i++) {
            if (gapMode == 1 && i % 5 == 2) {
                continue;
            }
            if (gapMode == 2 && i % 4 == 1) {
                continue;
            }
            float a1 = rotation + (float) (Math.PI * 2.0D * i / segments);
            float a2 = rotation + (float) (Math.PI * 2.0D * (i + 0.68F) / segments);
            drawBeam(matrix,
                    cx + Mth.cos(a1) * radius,
                    cy + Mth.sin(a1) * radius,
                    cx + Mth.cos(a2) * radius,
                    cy + Mth.sin(a2) * radius,
                    width,
                    color,
                    color);
        }
    }

    private static void drawOrbitingRunes(Matrix4f matrix, float cx, float cy, float radius, float time, int count) {
        for (int i = 0; i < count; i++) {
            float angle = time * 0.75F + (float) (Math.PI * 2.0D * i / count);
            float pulse = 0.55F + 0.45F * Mth.sin(time * 2.2F + i * 0.9F);
            int color = i % 3 == 0 ? TERMINUS_GOLD : (i % 3 == 1 ? COSMIC_CYAN : RIFT_MAGENTA);
            float px = cx + Mth.cos(angle) * radius;
            float py = cy + Mth.sin(angle) * radius;
            drawDiamond(matrix, px, py, 1.7F + pulse * 1.4F, withAlpha(color, 0.35F + pulse * 0.35F));
            if (i % 2 == 0) {
                float inner = radius - 8.0F;
                drawBeam(matrix, cx + Mth.cos(angle) * inner, cy + Mth.sin(angle) * inner, px, py, 0.65F, withAlpha(color, 0.25F), withAlpha(PALE_STAR, 0.36F));
            }
        }
    }

    private static void drawOblivionCrown(Matrix4f matrix, float cx, float cy, float radius, float time) {
        for (int i = 0; i < 9; i++) {
            float t = (i - 4) / 4.0F;
            float x1 = cx + t * radius;
            float height = 8.0F + 6.0F * (0.5F + 0.5F * Mth.sin(time * 2.6F + i));
            int color = i % 2 == 0 ? withAlpha(TERMINUS_GOLD, 0.42F) : withAlpha(RIFT_MAGENTA, 0.36F);
            drawBeam(matrix, x1 - 4.0F, cy + 7.0F, x1, cy - height, 1.0F, color, withAlpha(PALE_STAR, 0.30F));
            drawBeam(matrix, x1, cy - height, x1 + 4.0F, cy + 7.0F, 1.0F, withAlpha(PALE_STAR, 0.30F), color);
        }
    }

    private static void drawCausalityThreads(Matrix4f matrix, int x, int y, int width, int height, float time) {
        for (int i = 0; i < 8; i++) {
            float seed = i * 17.17F;
            float px = x + 20.0F + noise(seed) * (width - 40.0F);
            float py = y + 30.0F + noise(seed + 5.0F) * (height - 62.0F);
            float slash = 8.0F + noise(seed + 9.0F) * 13.0F;
            float sway = Mth.sin(time * 1.7F + i) * 2.8F;
            int color = i % 3 == 0 ? withAlpha(COSMIC_CYAN, 0.28F) : (i % 3 == 1 ? withAlpha(RIFT_MAGENTA, 0.24F) : withAlpha(TERMINUS_GOLD, 0.22F));
            drawBeam(matrix, px - slash, py + sway, px + slash * 0.72F, py - slash * 0.48F + sway, 0.8F, color, withAlpha(PALE_STAR, 0.20F));
        }
    }

    private static void drawAccretionDisk(Matrix4f matrix, float cx, float cy, float rx, float ry, float rotation, float width) {
        int segments = 54;
        for (int i = 0; i < segments; i++) {
            if (i % 9 == 4) {
                continue;
            }
            float a1 = rotation + (float) (Math.PI * 2.0D * i / segments);
            float a2 = rotation + (float) (Math.PI * 2.0D * (i + 0.78F) / segments);
            float heat = 0.5F + 0.5F * Mth.sin(rotation * 1.7F + i * 0.41F);
            int front = heat > 0.62F ? TERMINUS_GOLD : (i % 2 == 0 ? COSMIC_CYAN : RIFT_MAGENTA);
            int back = i % 3 == 0 ? RIFT_MAGENTA : COSMIC_CYAN;
            drawBeam(matrix,
                    cx + Mth.cos(a1) * rx,
                    cy + Mth.sin(a1) * ry,
                    cx + Mth.cos(a2) * rx,
                    cy + Mth.sin(a2) * ry,
                    width,
                    withAlpha(front, 0.42F + heat * 0.38F),
                    withAlpha(back, 0.18F + heat * 0.26F));
        }
    }

    private static void drawVortexSpiral(Matrix4f matrix, float cx, float cy, float time) {
        for (int arm = 0; arm < 3; arm++) {
            float base = time * (1.45F + arm * 0.18F) + arm * 2.094F;
            int color = arm == 0 ? TERMINUS_GOLD : (arm == 1 ? RIFT_MAGENTA : COSMIC_CYAN);
            for (int i = 0; i < 9; i++) {
                float t1 = i / 9.0F;
                float t2 = (i + 0.85F) / 9.0F;
                float a1 = base + t1 * 5.2F;
                float a2 = base + t2 * 5.2F;
                float r1 = Mth.lerp(t1, 22.0F, 58.0F);
                float r2 = Mth.lerp(t2, 22.0F, 58.0F);
                drawBeam(matrix,
                        cx + Mth.cos(a1) * r1,
                        cy + Mth.sin(a1) * r1 * 0.42F,
                        cx + Mth.cos(a2) * r2,
                        cy + Mth.sin(a2) * r2 * 0.42F,
                        0.9F,
                        withAlpha(color, 0.44F - t1 * 0.18F),
                        withAlpha(PALE_STAR, 0.28F - t1 * 0.12F));
            }
        }
    }

    private static void drawInfallingStars(Matrix4f matrix, float cx, float cy, float time) {
        for (int i = 0; i < 18; i++) {
            float seed = i * 8.271F;
            float orbit = (time * (0.28F + noise(seed) * 0.42F) + noise(seed + 2.0F)) % 1.0F;
            float radius = Mth.lerp(orbit, 92.0F, 23.0F);
            float angle = time * 1.8F + i * 1.37F + orbit * 5.8F;
            float px = cx + Mth.cos(angle) * radius;
            float py = cy + Mth.sin(angle) * radius * 0.42F;
            int color = i % 4 == 0 ? TERMINUS_GOLD : (i % 2 == 0 ? COSMIC_CYAN : RIFT_MAGENTA);
            float size = 1.1F + (1.0F - orbit) * 1.8F;
            drawDiamond(matrix, px, py, size, withAlpha(color, 0.62F));
            drawBeam(matrix,
                    px + Mth.cos(angle) * 9.0F,
                    py + Mth.sin(angle) * 3.8F,
                    px,
                    py,
                    0.6F,
                    withAlpha(color, 0.22F),
                    withAlpha(PALE_STAR, 0.36F));
        }
    }

    private static void drawDisc(Matrix4f matrix, float cx, float cy, float radius, int centerColor, int edgeColor, int segments) {
        drawEllipse(matrix, cx, cy, radius, radius, centerColor, edgeColor, segments);
    }

    private static void drawEllipse(Matrix4f matrix, float cx, float cy, float rx, float ry, int centerColor, int edgeColor, int segments) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (Math.PI * 2.0D * i / segments);
            float a2 = (float) (Math.PI * 2.0D * (i + 1) / segments);
            vertex(builder, matrix, cx, cy, centerColor);
            vertex(builder, matrix, cx + Mth.cos(a1) * rx, cy + Mth.sin(a1) * ry, edgeColor);
            vertex(builder, matrix, cx + Mth.cos(a2) * rx, cy + Mth.sin(a2) * ry, edgeColor);
        }
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void drawDiamond(Matrix4f matrix, float cx, float cy, float radius, int color) {
        drawQuad(matrix,
                cx, cy - radius,
                cx + radius, cy,
                cx, cy + radius,
                cx - radius, cy,
                color, color, color, color);
    }

    private static void drawBeam(Matrix4f matrix, float x1, float y1, float x2, float y2, float width, int colorA, int colorB) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = Mth.sqrt(dx * dx + dy * dy);
        if (length < 0.001F) {
            return;
        }
        float nx = -dy / length * width;
        float ny = dx / length * width;
        drawQuad(matrix, x1 - nx, y1 - ny, x1 + nx, y1 + ny, x2 + nx, y2 + ny, x2 - nx, y2 - ny, colorA, colorA, colorB, colorB);
    }

    private static void drawQuad(Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        drawQuad(matrix, x1, y1, x2, y1, x2, y2, x1, y2, color, color, color, color);
    }

    private static void drawQuad(Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                 int color1, int color2, int color3, int color4) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        vertex(builder, matrix, x1, y1, color1);
        vertex(builder, matrix, x2, y2, color2);
        vertex(builder, matrix, x3, y3, color3);
        vertex(builder, matrix, x4, y4, color4);
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void drawTriangle(Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        vertex(builder, matrix, x1, y1, color);
        vertex(builder, matrix, x2, y2, color);
        vertex(builder, matrix, x3, y3, color);
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void vertex(BufferBuilder builder, Matrix4f matrix, float x, float y, int color) {
        builder.vertex(matrix, x, y, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >>> 24) & 255)
                .endVertex();
    }

    private static BladeStats readBladeStats(ItemStack stack) {
        return stack.getCapability(ItemSlashBlade.BLADESTATE)
                .map(state -> new BladeStats(state.getProudSoulCount(), state.getKillCount(), state.getRefine()))
                .orElseGet(() -> readBladeStatsFromTag(stack));
    }

    private static BladeStats readBladeStatsFromTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        CompoundTag bladeState = tag == null ? null : tag.getCompound("bladeState");
        int proudSoul = firstAvailable(
                readInt(bladeState, "proudSoul", "ProudSoul", "ProudSoulCount", "proudsoul"),
                readInt(tag, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul"));
        int killCount = firstAvailable(
                readInt(bladeState, "killCount", "KillCount"),
                readInt(tag, "KillCount", "killCount"));
        int refine = firstAvailable(
                readInt(bladeState, "RepairCounter", "refine", "Refine", "RefineCount", "refineCount"),
                readInt(tag, "RepairCounter", "Refine", "RefineCount", "refine", "refineCount"));
        return new BladeStats(proudSoul, killCount, refine);
    }

    private static int firstAvailable(int preferred, int fallback) {
        return preferred != 0 ? preferred : fallback;
    }

    private static int readInt(CompoundTag tag, String... keys) {
        if (tag == null) {
            return 0;
        }
        for (String key : keys) {
            if (tag.contains(key)) {
                return tag.getInt(key);
            }
        }
        return 0;
    }

    private static String formatNumber(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private static boolean isBladeAsset(String value) {
        return value != null && value.startsWith("annihilationblade:model/blade");
    }

    private static int withAlpha(int color, float alpha) {
        int a = Mth.clamp((int) (((color >>> 24) & 255) * Mth.clamp(alpha, 0.0F, 1.0F)), 0, 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    private static float noise(float value) {
        return Mth.frac(Mth.sin(value) * 43758.5453F);
    }

    private record BladeStats(int proudSoul, int killCount, int refine) {
    }
}
