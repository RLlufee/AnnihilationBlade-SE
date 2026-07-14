package QWQ.QingYi.annihilationblade.annihilation_blade;

import QWQ.QingYi.annihilationblade.registry.ModItems;
import QWQ.QingYi.annihilationblade.common.NamedBladeStacks;
import java.util.Map;
import javax.annotation.Nullable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public final class AnnihilationBladeDefinitions {
   public static final String NAME = "annihilation_blade";
   public static final String DESCRIPTION_ID = "item.annihilationblade.annihilation_blade";
   private static final int INITIAL_KILL_COUNT = 0;
   private static final int INITIAL_PROUD_SOUL = 0;
   private static final String GROWTH_MIGRATION_KEY = "AnnihilationBladeGrowthMigrated";
   private static final ResourceLocation BLADE_MODEL = ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/annihilation_blade.obj");
   private static final ResourceLocation BLADE_TEXTURE = ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/annihilation_blade.png");
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
      return createStack(null);
   }

   public static ItemStack createStack(@Nullable Level level) {
      Item bladeItem = (Item)ModItems.ANNIHILATION_BLADE.get();
      ItemStack datapackStack = NamedBladeStacks.get(level, NAME, bladeItem);
      if (!datapackStack.isEmpty()) {
         ensureRuntimeStats(datapackStack);
         resetGrowthStats(datapackStack);
         return datapackStack;
      }

      ItemStack stack = new ItemStack((ItemLike)ModItems.ANNIHILATION_BLADE.get());
      applyFallbackStats(stack);
      resetGrowthStats(stack);
      return stack;
   }

   public static void applyStats(ItemStack stack) {
      applyStats(stack, null);
   }

   public static void applyStats(ItemStack stack, @Nullable Level level) {
      if (!applyDatapackStats(stack, level)) {
         applyFallbackStats(stack);
      }
      ensureStats(stack);
      resetGrowthStats(stack);
   }

   public static void ensureStats(ItemStack stack) {
      if (!stack.isEmpty()) {
         ensureRuntimeStats(stack);
      }
   }

   public static void ensureStats(ItemStack stack, @Nullable Level level) {
      if (!stack.isEmpty()) {
         applyDatapackStats(stack, level);
         ensureRuntimeStats(stack);
      }
   }

   private static boolean applyDatapackStats(ItemStack stack, @Nullable Level level) {
      if (level == null || stack.isEmpty()) {
         return false;
      }

      GrowthStats growthStats = GrowthStats.capture(stack);
      boolean applied = NamedBladeStacks.copyDefinitionTag(stack, level, NAME, (Item)ModItems.ANNIHILATION_BLADE.get());
      if (applied) {
         ensureIdentity(stack);
         growthStats.restore(stack);
      }

      return applied;
   }

   private static void applyFallbackStats(ItemStack stack) {
      if (!stack.isEmpty()) {
         CompoundTag tag = stack.getOrCreateTag();
         ensureIdentity(stack);
         tag.putInt("RepairCost", Math.max(1, tag.getInt("RepairCost")));
         tag.putString("SlashArts", "annihilationblade:spatial_fracture");
         tag.putInt("SummonedSwordColor", 11141375);
         applySpecialEffects(stack);
         applyEnchantments(stack);
         stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            state.setTranslationKey(DESCRIPTION_ID);
            state.setSlashArtsKey(ResourceLocation.fromNamespaceAndPath("annihilationblade", "spatial_fracture"));
            state.setBaseAttackModifier(50.0F);
            state.setMaxDamage(2000);
            state.setDefaultBewitched(true);
            state.setColorCode(11141375);
            state.setSpecialEffects(createSpecialEffects());
            tag.put("bladeState", state.serializeNBT());
         });
         forceHardcodedRender(stack);
         migrateGrowthStats(stack);
         tag.putInt("HideFlags", 2);
      }
   }

   private static void ensureIdentity(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      tag.putBoolean("IsAnnihilationBlade", true);
   }

   private static void ensureRuntimeStats(ItemStack stack) {
      ensureIdentity(stack);
      forceHardcodedRender(stack);
      migrateGrowthStats(stack);
   }

   private static void forceHardcodedRender(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      tag.putString("ModelName", BLADE_MODEL.toString());
      tag.putString("TextureName", BLADE_TEXTURE.toString());
      stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
         state.setModel(BLADE_MODEL);
         state.setTexture(BLADE_TEXTURE);
         CompoundTag bladeState = state.serializeNBT();
         tag.put("bladeState", bladeState);
      });
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
      ListTag specialEffects = createSpecialEffects(bladeState.getList("SpecialEffects", 8));

      bladeState.put("SpecialEffects", specialEffects);
      tag.put("bladeState", bladeState);
   }

   private static ListTag createSpecialEffects() {
      return createSpecialEffects(new ListTag());
   }

   private static ListTag createSpecialEffects(ListTag specialEffects) {

      for (String effect : GOD_SPECIAL_EFFECTS) {
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

   private record GrowthStats(int killCount, int proudSoul, int refine) {
      private static GrowthStats capture(ItemStack stack) {
         CompoundTag tag = stack.getOrCreateTag();
         CompoundTag bladeState = tag.getCompound("bladeState");
         int[] values = new int[]{
            Math.max(readInt(tag, "KillCount", "killCount"), readInt(bladeState, "KillCount", "killCount")),
            Math.max(
               readInt(tag, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul"),
               readInt(bladeState, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul")
            ),
            Math.max(
               readInt(tag, "Refine", "RefineCount", "RepairCounter", "refine", "refineCount"),
               readInt(bladeState, "Refine", "RefineCount", "RepairCounter", "refine", "refineCount")
            )
         };
         stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            values[0] = Math.max(values[0], state.getKillCount());
            values[1] = Math.max(values[1], state.getProudSoulCount());
            values[2] = Math.max(values[2], state.getRefine());
         });
         return new GrowthStats(values[0], values[1], values[2]);
      }

      private void restore(ItemStack stack) {
         CompoundTag tag = stack.getOrCreateTag();
         stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            state.setKillCount(Math.max(state.getKillCount(), this.killCount));
            state.setProudSoulCount(Math.max(state.getProudSoulCount(), this.proudSoul));
            state.setRefine(Math.max(state.getRefine(), this.refine));
            CompoundTag updatedBladeState = tag.getCompound("bladeState");
            updatedBladeState.putInt("killCount", state.getKillCount());
            updatedBladeState.putInt("proudSoul", state.getProudSoulCount());
            updatedBladeState.putInt("RepairCounter", state.getRefine());
            tag.put("bladeState", updatedBladeState);
         });
         removeLegacyGrowthKeys(tag);
         tag.putBoolean("AnnihilationBladeGrowthMigrated", true);
      }
   }
}
