package QWQ.QingYi.annihilationblade.infinity_stellaris.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

public final class InfinityStellarisTooltipRenderer {
   private static final ResourceLocation BLACK_HOLE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
      "annihilationblade", "textures/gui/infinity_stellaris_black_hole.png"
   );
   private static final ResourceLocation STAR_MAP_OVERLAY = ResourceLocation.fromNamespaceAndPath(
      "annihilationblade", "textures/gui/infinity_stellaris_star_map.png"
   );
   private static final int BLACK_HOLE_TEXTURE_WIDTH = 1280;
   private static final int BLACK_HOLE_TEXTURE_HEIGHT = 853;
   private static final int STAR_MAP_TEXTURE_WIDTH = 1734;
   private static final int STAR_MAP_TEXTURE_HEIGHT = 907;
   private static final int WIDTH = 320;
   private static final int MIN_WIDTH = 238;
   private static final int[] COSMIC_SPECTRUM = new int[]{0x28F7FF, 0xF2FEFF, 0x8D7CFF, 0xFF4FD8, 0xFFE27A, 0x58B7FF};
   private static final long TITLE_CYCLE_MILLIS = 4200L;
   private static final String FINAL_WEAPON_KEY = "item.annihilationblade.infinity_stellaris.tooltip.final_weapon";
   private static final AuthorityChip[] AUTHORITY_CHIPS = new AuthorityChip[]{
      new AuthorityChip("item.annihilationblade.infinity_stellaris.tooltip.chip.flight", 0x58B7FF),
      new AuthorityChip("item.annihilationblade.infinity_stellaris.tooltip.chip.invulnerable", 0xF2FEFF),
      new AuthorityChip("item.annihilationblade.infinity_stellaris.tooltip.chip.kill_fallback", 0xFF4FD8),
      new AuthorityChip("item.annihilationblade.infinity_stellaris.tooltip.chip.no_recipe", 0xFFE27A),
      new AuthorityChip("item.annihilationblade.infinity_stellaris.tooltip.chip.fullbright", 0x28F7FF)
   };
   private static final EffectChip[] EFFECT_CHIPS = new EffectChip[]{
      new EffectChip("se.annihilationblade.entropy_dissolution", 0x28F7FF),
      new EffectChip("se.annihilationblade.cosmic_string_cut", 0xF2FEFF),
      new EffectChip("se.annihilationblade.curvature_rupture", 0x8D7CFF),
      new EffectChip("se.annihilationblade.gamma_thunderburst", 0xFF4FD8),
      new EffectChip("slash_art.annihilationblade.vacuum_decay_collapse", 0xFFE27A)
   };

   private InfinityStellarisTooltipRenderer() {
   }

   public static void render(GuiGraphics graphics, Font font, ItemStack stack, List<Component> vanillaLines, int mouseX, int mouseY) {
      int screenWidth = graphics.guiWidth();
      int screenHeight = graphics.guiHeight();
      int width = Math.min(WIDTH, Math.max(MIN_WIDTH, screenWidth - 18));
      int contentWidth = width - 24;
      List<EnchantmentLine> enchantments = getEnchantments(stack);
      int enchantRows = (enchantments.size() + 1) / 2;
      int enchantRowHeight = screenHeight < 300 ? 9 : 10;
      int height = 208 + Math.max(1, enchantRows) * enchantRowHeight;
      height = Math.min(height, Math.max(210, screenHeight - 12));
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
      float time = (float)(System.currentTimeMillis() % 120000L) / 1000.0F;
      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 760.0F);
      renderFrame(graphics, x, y, width, height, time);
      renderContent(graphics, font, stack, vanillaLines, enchantments, x, y, width, height, contentWidth, enchantRowHeight, time);
      graphics.pose().popPose();
   }

   private static void renderFrame(GuiGraphics graphics, int x, int y, int width, int height, float time) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Matrix4f matrix = graphics.pose().last().pose();
      int outerPadX = 28;
      int outerPadY = 26;
      int backdropMargin = 14;
      int outerX = x - outerPadX;
      int outerY = y - outerPadY;
      int outerWidth = width + outerPadX * 2;
      int outerHeight = height + outerPadY * 2;
      int backdropX = outerX - backdropMargin;
      int backdropY = outerY - backdropMargin;
      int backdropWidth = outerWidth + backdropMargin * 2;
      int backdropHeight = outerHeight + backdropMargin * 2;
      graphics.enableScissor(backdropX, backdropY, backdropX + backdropWidth, backdropY + backdropHeight);
      blitCover(
         graphics,
         BLACK_HOLE_BACKGROUND,
         backdropX,
         backdropY,
         backdropWidth,
         backdropHeight,
         BLACK_HOLE_TEXTURE_WIDTH,
         BLACK_HOLE_TEXTURE_HEIGHT,
         -10.0F,
         time * 0.28F,
         0.86F
      );
      graphics.fillGradient(backdropX, backdropY, backdropX + backdropWidth, backdropY + backdropHeight, 0x32000514, 0x70000514);
      graphics.disableScissor();
      drawOuterMagicCircle(matrix, backdropX, backdropY, backdropWidth, backdropHeight, time);
      renderWhiteOrbitBorder(matrix, outerX, outerY, outerWidth, outerHeight, time);
      drawOrbitingSigils(matrix, outerX, outerY, outerWidth, outerHeight, time);

      graphics.fillGradient(x, y, x + width, y + height, 0xB204091A, 0xC0111630);
      graphics.enableScissor(x + 2, y + 2, x + width - 2, y + height - 2);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      graphics.fillGradient(x + 2, y + 2, x + width - 2, y + height - 2, 0x9A020615, 0xD40A1024);
      blitCover(graphics, STAR_MAP_OVERLAY, x, y, width, height, STAR_MAP_TEXTURE_WIDTH, STAR_MAP_TEXTURE_HEIGHT, 10.0F, time * 0.45F, 0.16F);
      graphics.fillGradient(x + 4, y + 4, x + width - 4, y + 45, 0x8205091A, 0x2605091A);
      graphics.fillGradient(x + 5, y + 142, x + width - 5, y + height - 5, 0x4605091A, 0xB705091A);
      drawMovingAccents(matrix, x, y, width, height, time);
      graphics.disableScissor();
      renderGradientBorder(matrix, x, y, width, height, time);
      RenderSystem.disableBlend();
   }

   private static void blitCover(
      GuiGraphics graphics,
      ResourceLocation texture,
      int x,
      int y,
      int width,
      int height,
      int textureWidth,
      int textureHeight,
      float biasX,
      float time,
      float alpha
   ) {
      float imageAspect = (float)textureWidth / textureHeight;
      float targetAspect = (float)width / height;
      int imageWidth;
      int imageHeight;
      if (imageAspect > targetAspect) {
         imageHeight = height;
         imageWidth = Math.round(height * imageAspect);
      } else {
         imageWidth = width;
         imageHeight = Math.round(width / imageAspect);
      }

      int imageX = x - (imageWidth - width) / 2 + Math.round(biasX + Mth.sin(time * 0.18F) * 3.0F);
      int imageY = y - (imageHeight - height) / 2 + Math.round(Mth.cos(time * 0.16F) * 3.0F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      graphics.blit(texture, imageX, imageY, imageWidth, imageHeight, 0.0F, 0.0F, textureWidth, textureHeight, textureWidth, textureHeight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static void renderContent(
      GuiGraphics graphics,
      Font font,
      ItemStack stack,
      List<Component> vanillaLines,
      List<EnchantmentLine> enchantments,
      int x,
      int y,
      int width,
      int height,
      int contentWidth,
      int enchantRowHeight,
      float time
   ) {
      int center = x + width / 2;
      Component itemName = vanillaLines.isEmpty() ? stack.getHoverName() : vanillaLines.get(0);
      drawGlowText(graphics, font, itemName, center - font.width(itemName) / 2, y + 8, 0xF2FEFF, 0x28F7FF);
      MutableComponent title = buildSpectralTitle(time);
      drawGlowText(graphics, font, title, center - font.width(title) / 2, y + 20, 0xF2FEFF, 0xFF4FD8);
      graphics.drawCenteredString(font, Component.translatable("item.annihilationblade.infinity_stellaris.tooltip.subtitle"), center, y + 34, 0xA8D9FF);
      graphics.renderItem(stack, x + 13, y + 13);

      int cursorY = y + 52;
      drawSectionTitle(graphics, font, x + 12, cursorY, contentWidth, Component.translatable("item.annihilationblade.infinity_stellaris.tooltip.section.authority"), time);
      cursorY += 13;
      drawAuthorityChips(graphics, font, x + 12, cursorY, contentWidth, time);
      cursorY += 26;
      drawSectionTitle(graphics, font, x + 12, cursorY, contentWidth, Component.translatable("item.annihilationblade.infinity_stellaris.tooltip.section.attributes"), time + 0.4F);
      cursorY += 13;
      drawStats(graphics, font, stack, x + 12, cursorY, contentWidth, time);
      cursorY += 28;
      drawSectionTitle(graphics, font, x + 12, cursorY, contentWidth, Component.translatable("item.annihilationblade.infinity_stellaris.tooltip.section.effects"), time + 0.8F);
      cursorY += 13;
      drawEffectChips(graphics, font, x + 12, cursorY, contentWidth, time);
      cursorY += 40;
      drawSectionTitle(graphics, font, x + 12, cursorY, contentWidth, Component.translatable("item.annihilationblade.infinity_stellaris.tooltip.section.enchantments"), time + 1.2F);
      cursorY += 13;
      drawEnchantments(graphics, font, x + 12, cursorY, contentWidth, height - (cursorY - y) - 10, enchantRowHeight, enchantments, time);
      int pulse = (int)((contentWidth - 2) * (0.5F + 0.5F * Mth.sin(time * 2.1F)));
      graphics.fill(x + 13, y + height - 7, x + width - 13, y + height - 6, 0x5528F7FF);
      graphics.fill(x + 13, y + height - 7, x + 13 + pulse, y + height - 6, 0xCCF2FEFF);
   }

   private static void drawSectionTitle(GuiGraphics graphics, Font font, int x, int y, int width, Component title, float phase) {
      int accent = sampleSpectrum(phase * 1.2F);
      int railWidth = Math.max(42, Math.round(width * 0.62F));
      float pulsePhase = 0.5F + 0.5F * Mth.sin(phase * 2.3F);
      int pulseWidth = Math.max(18, Math.round(railWidth * (0.22F + pulsePhase * 0.34F)));
      graphics.fill(x, y + 10, x + railWidth, y + 11, withAlpha(accent, 0.28F));
      graphics.fill(x, y + 10, x + pulseWidth, y + 11, withAlpha(accent, 0.82F));
      graphics.drawString(font, title, x + 2, y, withAlpha(0xF2FEFF, 0.95F), false);
   }

   private static void drawAuthorityChips(GuiGraphics graphics, Font font, int x, int y, int width, float time) {
      int gap = 3;
      int chipWidth = (width - gap * (AUTHORITY_CHIPS.length - 1)) / AUTHORITY_CHIPS.length;
      for (int i = 0; i < AUTHORITY_CHIPS.length; i++) {
         AuthorityChip chip = AUTHORITY_CHIPS[i];
         drawSmallChip(graphics, font, x + i * (chipWidth + gap), y, chipWidth, 12, Component.translatable(chip.key()), chip.color(), time + i * 0.31F);
      }
   }

   private static void drawStats(GuiGraphics graphics, Font font, ItemStack stack, int x, int y, int width, float time) {
      BladeStats stats = readBladeStats(stack);
      StatCell[] cells = new StatCell[]{
         new StatCell("item.annihilationblade.infinity_stellaris.tooltip.stat.kills", formatNumber(stats.killCount()), 0x28F7FF),
         new StatCell("item.annihilationblade.infinity_stellaris.tooltip.stat.souls", formatNumber(stats.proudSoul()), 0xFFE27A),
         new StatCell("item.annihilationblade.infinity_stellaris.tooltip.stat.refine", formatNumber(stats.refine()), 0xFF4FD8),
         new StatCell("item.annihilationblade.infinity_stellaris.tooltip.stat.attack", "1,000,000", 0xF2FEFF),
         new StatCell("item.annihilationblade.infinity_stellaris.tooltip.stat.durability", "2147483647", 0x8D7CFF)
      };
      int gap = 3;
      int cellWidth = (width - gap * (cells.length - 1)) / cells.length;
      for (int i = 0; i < cells.length; i++) {
         drawStatCell(graphics, font, x + i * (cellWidth + gap), y, cellWidth, 20, cells[i], time + i * 0.27F);
      }
   }

   private static void drawEffectChips(GuiGraphics graphics, Font font, int x, int y, int width, float time) {
      int columns = 3;
      int gap = 4;
      int chipWidth = (width - gap * (columns - 1)) / columns;
      for (int i = 0; i < EFFECT_CHIPS.length; i++) {
         int col = i % columns;
         int row = i / columns;
         EffectChip chip = EFFECT_CHIPS[i];
         drawSmallChip(graphics, font, x + col * (chipWidth + gap), y + row * 15, chipWidth, 12, Component.translatable(chip.key()), chip.color(), time + i * 0.42F);
      }
   }

   private static void drawEnchantments(
      GuiGraphics graphics,
      Font font,
      int x,
      int y,
      int width,
      int height,
      int rowHeight,
      List<EnchantmentLine> enchantments,
      float time
   ) {
      if (enchantments.isEmpty() || height <= 8) {
         return;
      }

      int columns = 2;
      int gap = 5;
      int colWidth = (width - gap) / columns;
      int rows = Math.min((enchantments.size() + 1) / 2, Math.max(1, height / rowHeight));
      graphics.fill(x, y - 2, x + width, y + rows * rowHeight + 4, 0x78030A1C);
      graphics.fill(x, y - 2, x + width, y - 1, withAlpha(sampleSpectrum(time * 0.8F), 0.78F));
      for (int i = 0; i < enchantments.size() && i < rows * columns; i++) {
         int col = i % columns;
         int row = i / columns;
         EnchantmentLine line = enchantments.get(i);
         int entryX = x + col * (colWidth + gap);
         int entryY = y + row * rowHeight;
      int color = sampleSpectrum(time * 0.6F + i * 0.37F);
         int railWidth = Math.max(28, Math.round(colWidth * 0.66F));
         int pulseWidth = Math.max(16, Math.round(railWidth * (0.28F + 0.36F * (0.5F + 0.5F * Mth.sin(time * 2.0F + i * 0.31F)))));
         graphics.fill(entryX, entryY + rowHeight - 2, entryX + railWidth, entryY + rowHeight - 1, withAlpha(color, 0.18F));
         graphics.fill(entryX, entryY + rowHeight - 2, entryX + pulseWidth, entryY + rowHeight - 1, withAlpha(color, 0.42F));
         graphics.drawString(font, trimToWidth(font, line.text(), colWidth - 4), entryX + 2, entryY, 0xE8F6FF, false);
      }
   }

   private static void drawSmallChip(
      GuiGraphics graphics, Font font, int x, int y, int width, int height, Component text, int rgb, float phase
   ) {
      graphics.fill(x, y, x + width, y + height, 0x8A061024);
      graphics.fill(x, y, x + width, y + 1, withAlpha(rgb, 0.78F));
      int railWidth = Math.max(14, Math.round((width - 4) * 0.58F));
      int pulse = Math.max(8, Math.round(railWidth * (0.28F + 0.38F * (0.5F + 0.5F * Mth.sin(phase * 2.6F)))));
      graphics.fill(x + 2, y + height - 2, x + 2 + railWidth, y + height - 1, withAlpha(rgb, 0.2F));
      graphics.fill(x + 2, y + height - 2, x + 2 + pulse, y + height - 1, withAlpha(rgb, 0.72F));
      graphics.drawString(font, trimToWidth(font, text.getString(), width - 7), x + 4, y + 2, 0xF2FEFF, false);
   }

   private static void drawStatCell(GuiGraphics graphics, Font font, int x, int y, int width, int height, StatCell cell, float phase) {
      int accent = cell.color();
      graphics.fill(x, y, x + width, y + height, 0x8F050B1C);
      graphics.fill(x, y, x + width, y + 1, withAlpha(accent, 0.72F));
      int railWidth = Math.max(14, Math.round((width - 4) * 0.56F));
      int pulse = Math.max(8, Math.round(railWidth * (0.28F + 0.34F * (0.5F + 0.5F * Mth.sin(phase * 2.2F)))));
      graphics.fill(x + 2, y + height - 3, x + 2 + railWidth, y + height - 2, withAlpha(accent, 0.18F));
      graphics.fill(x + 2, y + height - 3, x + 2 + pulse, y + height - 2, withAlpha(accent, 0.68F));
      graphics.drawString(font, trimToWidth(font, Component.translatable(cell.labelKey()).getString(), width - 4), x + 3, y + 2, 0xA8D9FF, false);
      graphics.drawString(font, trimToWidth(font, cell.value(), width - 4), x + 3, y + 11, 0xF2FEFF, false);
   }

   private static MutableComponent buildSpectralTitle(float time) {
      String text = ChatFormatting.stripFormatting(I18n.get(FINAL_WEAPON_KEY));
      if (text == null || text.isEmpty()) {
         text = "Infinity Stellaris";
      }

      MutableComponent title = Component.empty();
      float drift = ((System.currentTimeMillis() % TITLE_CYCLE_MILLIS) / (float)TITLE_CYCLE_MILLIS) * COSMIC_SPECTRUM.length;
      int visualIndex = 0;
      for (int offset = 0; offset < text.length(); ) {
         int codePoint = text.codePointAt(offset);
         String glyph = new String(Character.toChars(codePoint));
         int color = sampleSpectrum(visualIndex * 0.42F - drift + Mth.sin(time * 0.8F) * 0.12F);
         title.append(Component.literal(glyph).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(true)));
         offset += Character.charCount(codePoint);
         if (!Character.isWhitespace(codePoint)) {
            visualIndex++;
         }
      }

      return title;
   }

   private static List<EnchantmentLine> getEnchantments(ItemStack stack) {
      List<EnchantmentLine> lines = new ArrayList<>();
      for (Entry<Enchantment, Integer> entry : sortedEnchantments(stack).entrySet()) {
         Enchantment enchantment = entry.getKey();
         int level = entry.getValue();
         ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
         String sortKey = id == null ? enchantment.getDescriptionId() : id.toString();
         lines.add(new EnchantmentLine(sortKey, enchantment.getFullname(level).getString()));
      }

      lines.sort(Comparator.comparing(EnchantmentLine::sortKey));
      return lines;
   }

   private static Map<Enchantment, Integer> sortedEnchantments(ItemStack stack) {
      return EnchantmentHelper.getEnchantments(stack);
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
         readInt(bladeState, "proudSoul", "ProudSoul", "ProudSoulCount", "proudsoul"), readInt(tag, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul")
      );
      int killCount = firstAvailable(readInt(bladeState, "killCount", "KillCount"), readInt(tag, "KillCount", "killCount"));
      int refine = firstAvailable(
         readInt(bladeState, "RepairCounter", "refine", "Refine", "RefineCount", "refineCount"),
         readInt(tag, "RepairCounter", "Refine", "RefineCount", "refine", "refineCount")
      );
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

   private static String trimToWidth(Font font, String text, int width) {
      if (font.width(text) <= width) {
         return text;
      }

      String ellipsis = "...";
      return font.plainSubstrByWidth(text, Math.max(0, width - font.width(ellipsis))) + ellipsis;
   }

   private static void drawGlowText(GuiGraphics graphics, Font font, Component text, int x, int y, int color, int glow) {
      graphics.drawString(font, text, x - 1, y, withAlpha(glow, 0.45F), false);
      graphics.drawString(font, text, x + 1, y, withAlpha(glow, 0.45F), false);
      graphics.drawString(font, text, x, y - 1, withAlpha(0xF2FEFF, 0.26F), false);
      graphics.drawString(font, text, x, y + 1, withAlpha(0xFF4FD8, 0.24F), false);
      graphics.drawString(font, text, x, y, color, true);
   }

   private static void drawMovingAccents(Matrix4f matrix, int x, int y, int width, int height, float time) {
      for (int i = 0; i < 8; i++) {
         float phase = (time * 0.09F + i * 0.137F) % 1.0F;
         float sx = x - 34.0F + phase * (width + 68.0F);
         int colorA = withAlpha(sampleSpectrum(i * 0.7F + time * 0.2F), 0.16F);
         int colorB = withAlpha(0xF2FEFF, 0.08F);
         drawBeam(matrix, sx, y + height + 12.0F, sx + 62.0F, y - 16.0F, 1.0F, colorA, colorB);
      }

      for (int i = 0; i < 36; i++) {
         float px = x + 8.0F + noise(i * 18.73F) * (width - 16.0F);
         float py = y + 8.0F + noise(i * 8.39F + 3.0F) * (height - 16.0F);
         float pulse = 0.42F + 0.58F * Mth.sin(time * 1.9F + i * 0.61F);
         int color = withAlpha(i % 5 == 0 ? 0xF2FEFF : 0x58B7FF, 0.12F + pulse * 0.16F);
         drawQuad(matrix, px, py, px + 1.0F + pulse, py + 1.0F + pulse, color);
      }
   }

   private static void drawOuterMagicCircle(Matrix4f matrix, int x, int y, int width, int height, float time) {
      float cx = x + width / 2.0F;
      float cy = y + height / 2.0F;
      float radius = Math.max(width, height) * 0.53F + 12.0F;
      float spin = time * 0.18F;
      float counterSpin = -time * 0.13F;
      int ghost = withAlpha(0xF2FEFF, 0.08F);
      int soft = withAlpha(0xF2FEFF, 0.18F + 0.04F * (0.5F + 0.5F * Mth.sin(time * 1.3F)));
      int bright = withAlpha(0xFFFFFF, 0.36F + 0.08F * (0.5F + 0.5F * Mth.sin(time * 1.8F)));

      drawCircle(matrix, cx, cy, radius, spin, 64, 0.65F, soft);
      drawCircle(matrix, cx, cy, radius * 0.93F, counterSpin, 64, 0.45F, ghost);
      drawCircle(matrix, cx, cy, radius * 0.78F, spin * 1.45F, 48, 0.55F, withAlpha(0xDFFBFF, 0.14F));
      drawPolygon(matrix, cx, cy, radius * 0.82F, 6, spin + Mth.PI / 6.0F, 0.62F, soft);
      drawPolygon(matrix, cx, cy, radius * 0.66F, 12, counterSpin, 0.42F, withAlpha(0xFFFFFF, 0.12F));
      drawSteppedStar(matrix, cx, cy, radius * 0.72F, 12, 5, spin * 0.9F, 0.5F, withAlpha(0xFFFFFF, 0.16F));
      drawSteppedStar(matrix, cx, cy, radius * 0.52F, 9, 4, counterSpin * 1.4F, 0.44F, withAlpha(0xF2FEFF, 0.13F));

      for (int i = 0; i < 32; i++) {
         float angle = spin + i * Mth.PI * 2.0F / 32.0F;
         float inner = radius * (i % 4 == 0 ? 0.83F : 0.9F);
         float outer = radius * (i % 4 == 0 ? 1.03F : 0.99F);
         Point2 a = radialPoint(cx, cy, inner, angle);
         Point2 b = radialPoint(cx, cy, outer, angle);
         int color = i % 4 == 0 ? bright : withAlpha(0xF2FEFF, 0.16F);
         drawBeam(matrix, a.x(), a.y(), b.x(), b.y(), i % 4 == 0 ? 0.62F : 0.38F, color, color);
      }

      for (int i = 0; i < 8; i++) {
         float angle = counterSpin * 1.2F + i * Mth.PI * 2.0F / 8.0F;
         Point2 p = radialPoint(cx, cy, radius * 0.98F, angle);
         drawRotatingDiamond(matrix, p.x(), p.y(), 5.5F, spin * 2.0F + i, withAlpha(0xFFFFFF, 0.4F));
      }

      for (int i = 0; i < 4; i++) {
         float angle = spin * 0.7F + Mth.PI / 4.0F + i * Mth.HALF_PI;
         Point2 p = radialPoint(cx, cy, radius * 0.62F, angle);
         drawRotatingHexagram(matrix, p.x(), p.y(), 8.0F, counterSpin * 2.1F + i, withAlpha(0xFFFFFF, 0.22F));
      }
   }

   private static void renderWhiteOrbitBorder(Matrix4f matrix, int x, int y, int width, int height, float time) {
      int glow = withAlpha(0xF2FEFF, 0.11F);
      int base = withAlpha(0xF2FEFF, 0.3F);
      drawBeam(matrix, x, y, x + width, y, 2.6F, glow, glow);
      drawBeam(matrix, x, y + height, x + width, y + height, 2.6F, glow, glow);
      drawBeam(matrix, x, y, x, y + height, 2.6F, glow, glow);
      drawBeam(matrix, x + width, y, x + width, y + height, 2.6F, glow, glow);
      drawBeam(matrix, x, y, x + width, y, 0.7F, base, base);
      drawBeam(matrix, x, y + height, x + width, y + height, 0.7F, base, base);
      drawBeam(matrix, x, y, x, y + height, 0.7F, base, base);
      drawBeam(matrix, x + width, y, x + width, y + height, 0.7F, base, base);

      for (int i = 0; i < 6; i++) {
         float start = (time * 0.055F + i / 6.0F) % 1.0F;
         float length = 0.045F + 0.015F * (0.5F + 0.5F * Mth.sin(time * 1.7F + i));
         int head = withAlpha(0xFFFFFF, 0.72F);
         int tail = withAlpha(i % 2 == 0 ? 0xDFFBFF : 0xF7F1FF, 0.12F);
         drawBorderTrail(matrix, x, y, width, height, start, length, 1.1F, head, tail);
      }
   }

   private static void drawBorderTrail(
      Matrix4f matrix, int x, int y, int width, int height, float start, float length, float thickness, int headColor, int tailColor
   ) {
      int segments = 18;
      for (int i = 0; i < segments; i++) {
         float a = start + length * i / segments;
         float b = start + length * (i + 1) / segments;
         Point2 p1 = pointOnBorder(x, y, width, height, a);
         Point2 p2 = pointOnBorder(x, y, width, height, b);
         float blend = i / (float)Math.max(1, segments - 1);
         int colorA = lerpArgb(headColor, tailColor, blend);
         int colorB = lerpArgb(headColor, tailColor, Math.min(1.0F, blend + 1.0F / segments));
         drawBeam(matrix, p1.x(), p1.y(), p2.x(), p2.y(), thickness, colorA, colorB);
      }
   }

   private static void drawOrbitingSigils(Matrix4f matrix, int x, int y, int width, int height, float time) {
      for (int i = 0; i < 4; i++) {
         float progress = (time * 0.048F + i * 0.25F) % 1.0F;
         Point2 point = pointOnBorder(x, y, width, height, progress);
         float radius = 8.0F + (i % 2) * 1.5F;
         float rotation = time * (1.25F + i * 0.13F) + i * 0.9F;
         int glow = withAlpha(0xF2FEFF, 0.16F);
         int white = withAlpha(0xFFFFFF, 0.86F);
         drawRotatingHexagram(matrix, point.x(), point.y(), radius + 2.0F, rotation, glow);
         drawRotatingHexagram(matrix, point.x(), point.y(), radius, -rotation * 0.78F, white);
         drawRotatingDiamond(matrix, point.x(), point.y(), radius * 0.82F, rotation * 1.7F, withAlpha(0xDFFBFF, 0.7F));
      }
   }

   private static Point2 pointOnBorder(int x, int y, int width, int height, float progress) {
      float p = progress - (float)Math.floor(progress);
      float perimeter = (width + height) * 2.0F;
      float distance = p * perimeter;
      if (distance < width) {
         return new Point2(x + distance, y);
      }

      distance -= width;
      if (distance < height) {
         return new Point2(x + width, y + distance);
      }

      distance -= height;
      if (distance < width) {
         return new Point2(x + width - distance, y + height);
      }

      distance -= width;
      return new Point2(x, y + height - distance);
   }

   private static void drawRotatingHexagram(Matrix4f matrix, float cx, float cy, float radius, float rotation, int color) {
      float[] xs = new float[6];
      float[] ys = new float[6];
      for (int i = 0; i < 6; i++) {
         float angle = rotation + i * Mth.PI / 3.0F;
         xs[i] = cx + Mth.cos(angle) * radius;
         ys[i] = cy + Mth.sin(angle) * radius;
      }

      drawBeam(matrix, xs[0], ys[0], xs[2], ys[2], 0.45F, color, color);
      drawBeam(matrix, xs[2], ys[2], xs[4], ys[4], 0.45F, color, color);
      drawBeam(matrix, xs[4], ys[4], xs[0], ys[0], 0.45F, color, color);
      drawBeam(matrix, xs[1], ys[1], xs[3], ys[3], 0.45F, color, color);
      drawBeam(matrix, xs[3], ys[3], xs[5], ys[5], 0.45F, color, color);
      drawBeam(matrix, xs[5], ys[5], xs[1], ys[1], 0.45F, color, color);
   }

   private static void drawRotatingDiamond(Matrix4f matrix, float cx, float cy, float radius, float rotation, int color) {
      float x1 = cx + Mth.cos(rotation) * radius;
      float y1 = cy + Mth.sin(rotation) * radius;
      float x2 = cx + Mth.cos(rotation + Mth.HALF_PI) * radius * 0.62F;
      float y2 = cy + Mth.sin(rotation + Mth.HALF_PI) * radius * 0.62F;
      float x3 = cx + Mth.cos(rotation + Mth.PI) * radius;
      float y3 = cy + Mth.sin(rotation + Mth.PI) * radius;
      float x4 = cx + Mth.cos(rotation + Mth.PI + Mth.HALF_PI) * radius * 0.62F;
      float y4 = cy + Mth.sin(rotation + Mth.PI + Mth.HALF_PI) * radius * 0.62F;
      drawBeam(matrix, x1, y1, x2, y2, 0.55F, color, color);
      drawBeam(matrix, x2, y2, x3, y3, 0.55F, color, color);
      drawBeam(matrix, x3, y3, x4, y4, 0.55F, color, color);
      drawBeam(matrix, x4, y4, x1, y1, 0.55F, color, color);
   }

   private static void drawCircle(Matrix4f matrix, float cx, float cy, float radius, float rotation, int segments, float thickness, int color) {
      Point2 previous = radialPoint(cx, cy, radius, rotation);
      for (int i = 1; i <= segments; i++) {
         float angle = rotation + i * Mth.PI * 2.0F / segments;
         Point2 current = radialPoint(cx, cy, radius, angle);
         drawBeam(matrix, previous.x(), previous.y(), current.x(), current.y(), thickness, color, color);
         previous = current;
      }
   }

   private static void drawPolygon(Matrix4f matrix, float cx, float cy, float radius, int points, float rotation, float thickness, int color) {
      Point2 previous = radialPoint(cx, cy, radius, rotation);
      for (int i = 1; i <= points; i++) {
         float angle = rotation + i * Mth.PI * 2.0F / points;
         Point2 current = radialPoint(cx, cy, radius, angle);
         drawBeam(matrix, previous.x(), previous.y(), current.x(), current.y(), thickness, color, color);
         previous = current;
      }
   }

   private static void drawSteppedStar(
      Matrix4f matrix, float cx, float cy, float radius, int points, int step, float rotation, float thickness, int color
   ) {
      for (int i = 0; i < points; i++) {
         Point2 a = radialPoint(cx, cy, radius, rotation + i * Mth.PI * 2.0F / points);
         Point2 b = radialPoint(cx, cy, radius, rotation + ((i + step) % points) * Mth.PI * 2.0F / points);
         drawBeam(matrix, a.x(), a.y(), b.x(), b.y(), thickness, color, color);
      }
   }

   private static Point2 radialPoint(float cx, float cy, float radius, float angle) {
      return new Point2(cx + Mth.cos(angle) * radius, cy + Mth.sin(angle) * radius);
   }

   private static void renderGradientBorder(Matrix4f matrix, int x, int y, int width, int height, float time) {
      for (int layer = 4; layer >= 1; layer--) {
         float spread = layer * 1.9F;
         float alpha = 0.07F + (4 - layer) * 0.07F;
         drawSegmentedBorder(matrix, x, y, width, height, spread, 0.8F + layer * 0.15F, time * 0.08F + layer * 0.13F, alpha);
      }

      drawSegmentedBorder(matrix, x, y, width, height, 0.8F, 1.3F, time * 0.11F, 0.88F);
      drawCornerMarks(matrix, x, y, width, height, time);
   }

   private static void drawSegmentedBorder(Matrix4f matrix, int x, int y, int width, int height, float spread, float thickness, float drift, float alpha) {
      int segments = 18;
      for (int i = 0; i < segments; i++) {
         float t1 = (float)i / segments;
         float t2 = (float)(i + 1) / segments;
         int c1 = withAlpha(sampleSpectrum(t1 * COSMIC_SPECTRUM.length + drift), alpha);
         int c2 = withAlpha(sampleSpectrum(t2 * COSMIC_SPECTRUM.length + drift), alpha);
         float sx = x + width * t1;
         float ex = x + width * t2;
         drawQuad(matrix, sx, y - spread, ex, y - spread, ex, y - spread + thickness, sx, y - spread + thickness, c1, c2, c2, c1);
         drawQuad(matrix, sx, y + height + spread, ex, y + height + spread, ex, y + height + spread - thickness, sx, y + height + spread - thickness, c2, c1, c1, c2);
         if (i < segments / 2) {
            float vt1 = (float)i / (segments / 2);
            float vt2 = (float)(i + 1) / (segments / 2);
            int v1 = withAlpha(sampleSpectrum(vt1 * COSMIC_SPECTRUM.length - drift), alpha * 0.9F);
            int v2 = withAlpha(sampleSpectrum(vt2 * COSMIC_SPECTRUM.length - drift), alpha * 0.9F);
            float sy = y + height * vt1;
            float ey = y + height * vt2;
            drawQuad(matrix, x - spread, sy, x - spread + thickness, sy, x - spread + thickness, ey, x - spread, ey, v1, v1, v2, v2);
            drawQuad(matrix, x + width + spread, sy, x + width + spread - thickness, sy, x + width + spread - thickness, ey, x + width + spread, ey, v2, v2, v1, v1);
         }
      }
   }

   private static void drawCornerMarks(Matrix4f matrix, int x, int y, int width, int height, float time) {
      int cyan = withAlpha(0x28F7FF, 0.64F + 0.18F * (0.5F + 0.5F * Mth.sin(time * 2.0F)));
      int white = withAlpha(0xF2FEFF, 0.55F);
      int gold = withAlpha(0xFFE27A, 0.5F);
      drawBeam(matrix, x + 7.0F, y + 18.0F, x + 26.0F, y + 7.0F, 1.4F, cyan, white);
      drawBeam(matrix, x + width - 7.0F, y + 18.0F, x + width - 26.0F, y + 7.0F, 1.4F, white, gold);
      drawBeam(matrix, x + 7.0F, y + height - 18.0F, x + 26.0F, y + height - 7.0F, 1.4F, gold, cyan);
      drawBeam(matrix, x + width - 7.0F, y + height - 18.0F, x + width - 26.0F, y + height - 7.0F, 1.4F, cyan, white);
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

   private static void drawQuad(
      Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color1, int color2, int color3, int color4
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      BufferBuilder builder = Tesselator.getInstance().getBuilder();
      builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      vertex(builder, matrix, x1, y1, color1);
      vertex(builder, matrix, x2, y2, color2);
      vertex(builder, matrix, x3, y3, color3);
      vertex(builder, matrix, x4, y4, color4);
      BufferUploader.drawWithShader(builder.end());
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   private static void vertex(BufferBuilder builder, Matrix4f matrix, float x, float y, int color) {
      builder.vertex(matrix, x, y, 0.0F).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24 & 0xFF).endVertex();
   }

   private static int sampleSpectrum(float position) {
      int length = COSMIC_SPECTRUM.length;
      float wrapped = position - (float)Math.floor(position / length) * length;
      if (wrapped >= length) {
         wrapped = 0.0F;
      }

      int index = Math.min(length - 1, Math.max(0, (int)Math.floor(wrapped)));
      int next = index == length - 1 ? 0 : index + 1;
      float blend = wrapped - index;
      blend = blend * blend * (3.0F - 2.0F * blend);
      return lerpColor(COSMIC_SPECTRUM[index], COSMIC_SPECTRUM[next], blend);
   }

   private static int lerpColor(int from, int to, float blend) {
      int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * blend);
      int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * blend);
      int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * blend);
      return r << 16 | g << 8 | b;
   }

   private static int lerpArgb(int from, int to, float blend) {
      float t = Mth.clamp(blend, 0.0F, 1.0F);
      int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
      int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
      int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
      int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int withAlpha(int rgb, float alpha) {
      int a = Mth.clamp(Math.round(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F), 0, 255);
      return (rgb & 0xFFFFFF) | a << 24;
   }

   private static float noise(float seed) {
      float value = Mth.sin(seed) * 43758.545F;
      return value - (float)Math.floor(value);
   }

   private record AuthorityChip(String key, int color) {
   }

   private record EffectChip(String key, int color) {
   }

   private record StatCell(String labelKey, String value, int color) {
   }

   private record EnchantmentLine(String sortKey, String text) {
   }

   private record BladeStats(int proudSoul, int killCount, int refine) {
   }

   private record Point2(float x, float y) {
   }
}
