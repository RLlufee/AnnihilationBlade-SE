package QWQ.QingYi.annihilationblade.infinity_stellaris;

import QWQ.QingYi.annihilationblade.common.NamedBladeStacks;
import java.util.Map;
import javax.annotation.Nullable;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public final class InfinityStellarisDefinitions {
   public static final String NAME = "infinity_stellaris";
   public static final String DESCRIPTION_ID = "item.annihilationblade.infinity_stellaris";
   public static final String IDENTITY_TAG = "IsInfinityStellaris";
   private static final ResourceLocation BLADE_MODEL = ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/infinity_stellaris.obj");
   private static final ResourceLocation BLADE_TEXTURE = ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/infinity_stellaris.png");
   private static final ResourceLocation VACUUM_DECAY_COLLAPSE = ResourceLocation.fromNamespaceAndPath("annihilationblade", "vacuum_decay_collapse");
   private static final int SUMMONED_SWORD_COLOR = 4248287;
   private static final String[] INFINITY_SPECIAL_EFFECTS = new String[]{
      "annihilationblade:entropy_dissolution",
      "annihilationblade:curvature_rupture",
      "annihilationblade:gamma_thunderburst",
      "annihilationblade:cosmic_string_cut"
   };

   private InfinityStellarisDefinitions() {
   }

   public static ItemStack createStack() {
      return createStack(null);
   }

   public static ItemStack createStack(@Nullable Level level) {
      ItemStack datapackStack = NamedBladeStacks.get(level, NAME);
      if (!datapackStack.isEmpty()) {
         ensureRuntimeStats(datapackStack);
         return datapackStack;
      }

      ItemStack stack = new ItemStack(SBItems.slashblade);
      applyFallbackStats(stack);
      return stack;
   }

   public static void ensureStats(ItemStack stack) {
      if (!stack.isEmpty()) {
         ensureRuntimeStats(stack);
      }
   }

   public static void ensureStats(ItemStack stack, @Nullable Level level) {
      if (!stack.isEmpty()) {
         NamedBladeStacks.copyDefinitionTag(stack, level, NAME, null);
         ensureRuntimeStats(stack);
      }
   }

   private static void applyFallbackStats(ItemStack stack) {
      if (!stack.isEmpty()) {
         CompoundTag tag = stack.getOrCreateTag();
         ensureIdentity(stack);
         tag.putString("SlashArts", VACUUM_DECAY_COLLAPSE.toString());
         tag.putBoolean("Unbreakable", true);
         tag.putInt("HideFlags", Math.max(2, tag.getInt("HideFlags")));
         applySpecialEffects(stack);
         applyEnchantments(stack);
         applyBladeState(stack);
      }
   }

   private static void ensureRuntimeStats(ItemStack stack) {
      ensureIdentity(stack);
      applySpecialEffects(stack);
      applyEnchantments(stack);
      applyBladeState(stack);
   }

   private static void ensureIdentity(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      tag.putBoolean(IDENTITY_TAG, true);
      tag.putString("ModelName", BLADE_MODEL.toString());
      tag.putString("TextureName", BLADE_TEXTURE.toString());
      tag.putInt("SummonedSwordColor", SUMMONED_SWORD_COLOR);
      tag.putBoolean("Unbreakable", true);
   }

   private static void applyBladeState(ItemStack stack) {
      stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
         state.setTranslationKey(DESCRIPTION_ID);
         state.setSlashArtsKey(VACUUM_DECAY_COLLAPSE);
         state.setBaseAttackModifier(1000000.0F);
         state.setMaxDamage(Integer.MAX_VALUE);
         state.setDefaultBewitched(true);
         state.setModel(BLADE_MODEL);
         state.setTexture(BLADE_TEXTURE);
         state.setColorCode(SUMMONED_SWORD_COLOR);
         state.setSpecialEffects(createSpecialEffects());
         CompoundTag tag = stack.getOrCreateTag();
         state.setBroken(false);

         tag.put("bladeState", state.serializeNBT());
      });
   }

   private static void applySpecialEffects(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      CompoundTag bladeState = tag.getCompound("bladeState");
      ListTag specialEffects = createSpecialEffects(bladeState.getList("SpecialEffects", 8));
      bladeState.put("SpecialEffects", specialEffects);
      tag.put("bladeState", bladeState);
   }

   private static ListTag createSpecialEffects() {
      return createSpecialEffects(new ListTag());
   }

   private static ListTag createSpecialEffects(ListTag specialEffects) {
      for (String effect : INFINITY_SPECIAL_EFFECTS) {
         if (!containsString(specialEffects, effect)) {
            specialEffects.add(StringTag.valueOf(effect));
         }
      }

      return specialEffects;
   }

   private static boolean containsString(ListTag list, String value) {
      for (int i = 0; i < list.size(); i++) {
         if (value.equals(list.getString(i))) {
            return true;
         }
      }

      return false;
   }

   private static void applyEnchantments(ItemStack stack) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
      putMaxEnchant(enchantments, Enchantments.SHARPNESS, 10);
      putMaxEnchant(enchantments, Enchantments.SMITE, 10);
      putMaxEnchant(enchantments, Enchantments.BANE_OF_ARTHROPODS, 10);
      putMaxEnchant(enchantments, Enchantments.KNOCKBACK, 10);
      putMaxEnchant(enchantments, Enchantments.MOB_LOOTING, 10);
      putMaxEnchant(enchantments, Enchantments.SWEEPING_EDGE, 10);
      putMaxEnchant(enchantments, Enchantments.POWER_ARROWS, 10);
      putMaxEnchant(enchantments, Enchantments.PUNCH_ARROWS, 10);
      putMaxEnchant(enchantments, Enchantments.INFINITY_ARROWS, 10);
      putMaxEnchant(enchantments, Enchantments.QUICK_CHARGE, 10);
      putMaxEnchant(enchantments, Enchantments.MULTISHOT, 10);
      putMaxEnchant(enchantments, Enchantments.PIERCING, 10);
      putMaxEnchant(enchantments, Enchantments.UNBREAKING, 10);
      putMaxEnchant(enchantments, Enchantments.MENDING, 10);
      putMaxEnchant(enchantments, Enchantments.THORNS, 10);
      EnchantmentHelper.setEnchantments(enchantments, stack);
   }

   private static void putMaxEnchant(Map<Enchantment, Integer> enchantments, Enchantment enchantment, int level) {
      enchantments.put(enchantment, Math.max(level, enchantments.getOrDefault(enchantment, 0)));
   }
}
