package QWQ.QingYi.annihilationblade.blood_prison;

import java.util.Map;
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

public final class BloodPrisonDefinitions {
   public static final String NAME = "blood_prison";
   public static final String DESCRIPTION_ID = "item.annihilationblade.blood_prison";

   private BloodPrisonDefinitions() {
   }

   public static ItemStack createStack() {
      ItemStack stack = new ItemStack(SBItems.slashblade);
      ensureStats(stack);
      return stack;
   }

   public static void ensureStats(ItemStack stack) {
      if (!stack.isEmpty()) {
         CompoundTag tag = stack.getOrCreateTag();
         tag.putString("SlashArts", "annihilationblade:infernal_slaughter");
         tag.putString("ModelName", "annihilationblade:model/blood_prison");
         tag.putString("TextureName", "annihilationblade:model/blood_prison");
         tag.putInt("SummonedSwordColor", -57312);
         tag.putBoolean("Unbreakable", true);
         applyEnchantments(stack);
         stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            state.setTranslationKey("item.annihilationblade.blood_prison");
            state.setSlashArtsKey(ResourceLocation.fromNamespaceAndPath("annihilationblade", "infernal_slaughter"));
            state.setBaseAttackModifier(15.0F);
            state.setMaxDamage(2400);
            state.setDefaultBewitched(true);
            state.setModel(ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/blood_prison.obj"));
            state.setTexture(ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/blood_prison.png"));
            state.setColorCode(-57312);
            state.setSpecialEffects(createSpecialEffects());
            CompoundTag bladeState = state.serializeNBT();
            tag.put("bladeState", bladeState);
         });
      }
   }

   private static ListTag createSpecialEffects() {
      ListTag effects = new ListTag();
      effects.add(StringTag.valueOf("annihilationblade:blood_leech"));
      effects.add(StringTag.valueOf("annihilationblade:spirit_shield"));
      effects.add(StringTag.valueOf("annihilationblade:phantom_mark"));
      return effects;
   }

   private static void applyEnchantments(ItemStack stack) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
      enchantments.put(Enchantments.UNBREAKING, Math.max(10, enchantments.getOrDefault(Enchantments.UNBREAKING, 0)));
      enchantments.put(Enchantments.MENDING, Math.max(10, enchantments.getOrDefault(Enchantments.MENDING, 0)));
      EnchantmentHelper.setEnchantments(enchantments, stack);
   }
}
