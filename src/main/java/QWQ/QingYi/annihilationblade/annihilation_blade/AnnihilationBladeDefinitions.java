package QWQ.QingYi.annihilationblade.annihilation_blade;

import QWQ.QingYi.annihilationblade.registry.ModItems;
import java.util.Map;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;

public final class AnnihilationBladeDefinitions {
   public static final String NAME = "annihilation_blade";
   public static final String DESCRIPTION_ID = "item.annihilationblade.annihilation_blade";
   private static final int INITIAL_KILL_COUNT = 0;
   private static final int INITIAL_PROUD_SOUL = 0;
   private static final String GROWTH_MIGRATION_KEY = "AnnihilationBladeGrowthMigrated";
   private static final String[] GOD_SPECIAL_EFFECTS = new String[]{
      "annihilationblade:dankong",
      "annihilationblade:world_rift",
      "annihilationblade:terminus_echo",
      "annihilationblade:void_dominion",
      "annihilationblade:causality_collapse",
      "annihilationblade:starless_judgement",
      "annihilationblade:phantom_judgement",
      "annihilationblade:abyssal_decree"
   };

   private AnnihilationBladeDefinitions() {
   }

   public static ItemStack createStack() {
      ItemStack stack = new ItemStack((ItemLike)ModItems.ANNIHILATION_BLADE.get());
      applyStats(stack);
      return stack;
   }

   public static void applyStats(ItemStack stack) {
      ensureStats(stack);
      resetGrowthStats(stack);
   }

   public static void ensureStats(ItemStack stack) {
      if (!stack.isEmpty()) {
         CompoundTag tag = stack.getOrCreateTag();
         tag.putBoolean("IsAnnihilationBlade", true);
         tag.putInt("RepairCost", Math.max(1, tag.getInt("RepairCost")));
         tag.putString("ModelName", "annihilationblade:model/blade");
         tag.putString("TextureName", "annihilationblade:model/blade");
         tag.putString("SlashArts", "annihilationblade:spatial_fracture");
         tag.putInt("SummonedSwordColor", -5635841);
         applySpecialEffects(stack);
         applyEnchantments(stack);
         migrateGrowthStats(stack);
         tag.putInt("HideFlags", 2);
      }
   }

   private static void applyEnchantments(ItemStack stack) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
      putMaxEnchant(enchantments, Enchantments.SHARPNESS, 10);
      putMaxEnchant(enchantments, Enchantments.FIRE_ASPECT, 10);
      putMaxEnchant(enchantments, Enchantments.SMITE, 10);
      putMaxEnchant(enchantments, Enchantments.BANE_OF_ARTHROPODS, 10);
      putMaxEnchant(enchantments, Enchantments.MOB_LOOTING, 10);
      putMaxEnchant(enchantments, Enchantments.SWEEPING_EDGE, 10);
      putMaxEnchant(enchantments, Enchantments.POWER_ARROWS, 10);
      putMaxEnchant(enchantments, Enchantments.UNBREAKING, 10);
      putMaxEnchant(enchantments, Enchantments.MENDING, 10);
      putMaxEnchant(enchantments, Enchantments.THORNS, 10);
      EnchantmentHelper.setEnchantments(enchantments, stack);
   }

   private static void putMaxEnchant(Map<Enchantment, Integer> enchantments, Enchantment enchantment, int level) {
      enchantments.put(enchantment, Math.max(level, enchantments.getOrDefault(enchantment, 0)));
   }

   private static void migrateGrowthStats(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      boolean migrated = tag.getBoolean("AnnihilationBladeGrowthMigrated");
      CompoundTag bladeState = tag.getCompound("bladeState");
      int legacyKillCount = Math.max(readInt(tag, "KillCount", "killCount"), readInt(bladeState, "KillCount", "killCount"));
      int legacyProudSoul = Math.max(
         readInt(tag, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul"), readInt(bladeState, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul")
      );
      int legacyRefine = Math.max(
         readInt(tag, "Refine", "RefineCount", "RepairCounter", "refine", "refineCount"),
         readInt(bladeState, "Refine", "RefineCount", "RepairCounter", "refine", "refineCount")
      );
      boolean hasState = stack.getCapability(ItemSlashBlade.BLADESTATE).map(state -> {
         if (!migrated) {
            state.setKillCount(Math.max(state.getKillCount(), Math.max(0, legacyKillCount)));
            state.setProudSoulCount(Math.max(state.getProudSoulCount(), Math.max(0, legacyProudSoul)));
            state.setRefine(Math.max(state.getRefine(), legacyRefine));
         }

         CompoundTag updatedBladeState = tag.getCompound("bladeState");
         updatedBladeState.putInt("killCount", state.getKillCount());
         updatedBladeState.putInt("proudSoul", state.getProudSoulCount());
         updatedBladeState.putInt("RepairCounter", state.getRefine());
         tag.put("bladeState", updatedBladeState);
         return true;
      }).orElse(false);
      if (hasState) {
         removeLegacyGrowthKeys(tag);
         tag.putBoolean("AnnihilationBladeGrowthMigrated", true);
      }
   }

   private static void applySpecialEffects(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      CompoundTag bladeState = tag.getCompound("bladeState");
      ListTag specialEffects = bladeState.getList("SpecialEffects", 8);

      for (String effect : GOD_SPECIAL_EFFECTS) {
         if (!containsString(specialEffects, effect)) {
            specialEffects.add(StringTag.valueOf(effect));
         }
      }

      bladeState.put("SpecialEffects", specialEffects);
      tag.put("bladeState", bladeState);
   }

   private static boolean containsString(ListTag list, String value) {
      for (int i = 0; i < list.size(); i++) {
         if (value.equals(list.getString(i))) {
            return true;
         }
      }

      return false;
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

   private static void removeLegacyGrowthKeys(CompoundTag tag) {
      tag.remove("KillCount");
      tag.remove("killCount");
      tag.remove("ProudSoul");
      tag.remove("ProudSoulCount");
      tag.remove("proudSoul");
      tag.remove("proudsoul");
      tag.remove("Refine");
      tag.remove("RefineCount");
      tag.remove("refine");
      tag.remove("refineCount");
      tag.remove("RepairCounter");
   }

   private static void resetGrowthStats(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
         state.setKillCount(0);
         state.setProudSoulCount(0);
         state.setRefine(0);
         CompoundTag updatedBladeState = tag.getCompound("bladeState");
         updatedBladeState.putInt("killCount", 0);
         updatedBladeState.putInt("proudSoul", 0);
         updatedBladeState.putInt("RepairCounter", 0);
         tag.put("bladeState", updatedBladeState);
      });
      removeLegacyGrowthKeys(tag);
      tag.putBoolean("AnnihilationBladeGrowthMigrated", true);
   }
}
