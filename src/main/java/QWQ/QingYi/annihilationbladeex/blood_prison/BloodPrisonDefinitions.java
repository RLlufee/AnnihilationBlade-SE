package QWQ.QingYi.annihilationbladeex.blood_prison;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateData;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeDataComponents;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BloodPrisonDefinitions {
   public static final String NAME = "blood_prison";
   public static final ResourceLocation ID = AnnihilationBladeEX.prefix(NAME);
   public static final ResourceLocation INFERNAL_SLAUGHTER_ID = AnnihilationBladeEX.prefix("infernal_slaughter");
   public static final ResourceLocation BLOOD_LEECH_ID = AnnihilationBladeEX.prefix("blood_leech");
   public static final ResourceLocation SPIRIT_SHIELD_ID = AnnihilationBladeEX.prefix("spirit_shield");
   public static final ResourceLocation PHANTOM_MARK_ID = AnnihilationBladeEX.prefix("phantom_mark");
   public static final String DESCRIPTION_ID = "item." + AnnihilationBladeEX.MODID + ".blood_prison";
   private static final ResourceLocation COMBO_ROOT_ID = ResourceLocation.fromNamespaceAndPath("slashblade", "standby");
   private static final ResourceLocation BLADE_MODEL = AnnihilationBladeEX.prefix("model/blood_prison.obj");
   private static final ResourceLocation BLADE_TEXTURE = AnnihilationBladeEX.prefix("model/blood_prison.png");
   private static final List<ResourceLocation> SPECIAL_EFFECTS = List.of(
      BLOOD_LEECH_ID,
      SPIRIT_SHIELD_ID,
      PHANTOM_MARK_ID,
      ResourceLocation.fromNamespaceAndPath("slashblade", "wither_edge")
   );

   private BloodPrisonDefinitions() {
   }

   public static ItemStack createStack() {
      ItemStack stack = createFallbackStack();
      ensureStats(stack);
      return stack;
   }

   public static ItemStack createStack(@Nullable Level level) {
      if (level != null) {
         ItemStack stack = getDefinedStack(level);
         if (!stack.isEmpty()) {
            ensureStats(stack, level);
            return stack;
         }
      }
      return createStack();
   }

   public static ItemStack createStack(HolderLookup.Provider registries) {
      if (registries != null) {
         var lookup = SlashBlade.getSlashBladeDefinitionRegistry(registries);
         var key = ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, ID);
         Optional<ItemStack> defined = lookup.get(key).map(holder -> holder.value().getBlade(registries));
         if (defined.isPresent()) {
            ItemStack stack = defined.get();
            ensureStats(stack, registries);
            return stack;
         }
      }
      ItemStack stack = createFallbackStack();
      ensureStats(stack, registries);
      return stack;
   }

   public static boolean isBloodPrison(ItemStack stack) {
      return !stack.isEmpty() && DESCRIPTION_ID.equals(stack.getDescriptionId());
   }

   public static void ensureStats(ItemStack stack) {
      ensureStats(stack, (HolderLookup.Provider)null);
   }

   public static void ensureStats(ItemStack stack, @Nullable Level level) {
      ensureStats(stack, level == null ? null : level.registryAccess());
   }

   public static void ensureStats(ItemStack stack, Entity holder) {
      ensureStats(stack, holder == null ? null : holder.registryAccess());
   }

   private static ItemStack getDefinedStack(Level level) {
      try {
         SlashBladeDefinition definition = SlashBlade.getSlashBladeDefinitionRegistry(level).get(ID);
         return definition == null ? ItemStack.EMPTY : definition.getBlade(level.registryAccess());
      } catch (RuntimeException exception) {
         AnnihilationBladeEX.LOGGER.warn("Failed to read SlashBlade named blade definition for {}", ID, exception);
         return ItemStack.EMPTY;
      }
   }

   private static ItemStack createFallbackStack() {
      return new ItemStack(mods.flammpfeil.slashblade.registry.SlashBladeItems.SLASHBLADE.get());
   }

   private static void ensureStats(ItemStack stack, @Nullable HolderLookup.Provider registries) {
      if (stack.isEmpty()) {
         return;
      }

      BladeStateData current = BladeStateAccess.getDataOrDefault(stack);
      stack.set(DataComponents.MAX_DAMAGE, 2400);
      stack.setDamageValue(0);
      stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
      stack.set(DataComponents.RARITY, Rarity.EPIC);
      stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), new BladeStateData(
         DESCRIPTION_ID,
         16.0F,
         current.proudSoul(),
         current.killCount(),
         current.refine(),
         false,
         false,
         INFERNAL_SLAUGHTER_ID,
         true,
         COMBO_ROOT_ID,
         CarryType.NINJA,
         0xFFFF2020,
         false,
         Vec3.ZERO,
         Optional.of(BLADE_TEXTURE),
         Optional.of(BLADE_MODEL),
         SPECIAL_EFFECTS
      ));
      BladeStateAccess.ensureRuntimeComponent(stack);
      applyEnchantments(stack, registries);
   }

   private static void applyEnchantments(ItemStack stack, @Nullable HolderLookup.Provider registries) {
      if (registries == null) {
         return;
      }
      var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
      stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.MENDING), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.POWER), 10);
   }
}
