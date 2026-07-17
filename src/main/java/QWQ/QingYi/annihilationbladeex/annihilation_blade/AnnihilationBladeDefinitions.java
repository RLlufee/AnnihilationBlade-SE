package QWQ.QingYi.annihilationbladeex.annihilation_blade;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateData;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeDataComponents;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

@SuppressWarnings("null")
public final class AnnihilationBladeDefinitions {
   private static final String MODID = "annihilationbladeex";
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String NAME = "annihilation_blade";
   public static final ResourceLocation ID = prefix(NAME);
   public static final String DESCRIPTION_ID = "item." + MODID + ".annihilation_blade";
   private static final int LEGACY_INITIAL_PROUD_SOUL = 100000;
   private static final int LEGACY_INITIAL_KILL_COUNT = 10000;
   private static final int LEGACY_INITIAL_REFINE = 1000;
   private static final ResourceLocation COMBO_ROOT_ID = ResourceLocation.fromNamespaceAndPath("slashblade", "standby");
   private static final ResourceLocation BLADE_MODEL = prefix("model/annihilation_blade.obj");
   private static final ResourceLocation BLADE_TEXTURE = prefix("model/annihilation_blade.png");
   private static final ResourceLocation SPATIAL_FRACTURE_ID = prefix("spatial_fracture");
   private static final List<ResourceLocation> SPECIAL_EFFECTS = List.of(
      prefix("dankong"),
      prefix("world_rift"),
      prefix("terminus_echo"),
      prefix("void_dominion"),
      prefix("causality_collapse"),
      prefix("starless_judgement"),
      prefix("phantom_judgement"),
      prefix("abyssal_decree")
   );

   private AnnihilationBladeDefinitions() {
   }

   private static ResourceLocation prefix(String path) {
      return ResourceLocation.fromNamespaceAndPath(MODID, path);
   }

   private static Item bladeItem() {
      return BuiltInRegistries.ITEM.get(ID);
   }

   public static ItemStack createStack() {
      ItemStack stack = new ItemStack(bladeItem());
      applyStats(stack);
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

   public static void applyStats(ItemStack stack) {
      applyStats(stack, (HolderLookup.Provider)null);
   }

   public static void applyStats(ItemStack stack, @Nullable Level level) {
      applyStats(stack, level == null ? null : level.registryAccess());
   }

   public static void ensureStats(ItemStack stack) {
      applyStats(stack);
   }

   public static void ensureStats(ItemStack stack, @Nullable Level level) {
      applyStats(stack, level);
   }

   public static void refreshHeldStats(ItemStack stack, Entity holder) {
      applyStats(stack, holder == null ? null : holder.registryAccess());
   }

   private static ItemStack getDefinedStack(Level level) {
      try {
         SlashBladeDefinition definition = SlashBlade.getSlashBladeDefinitionRegistry(level).get(ID);
         return definition == null ? ItemStack.EMPTY : definition.getBlade(bladeItem(), level.registryAccess());
      } catch (RuntimeException exception) {
         LOGGER.warn("Failed to read SlashBlade named blade definition for {}", ID, exception);
         return ItemStack.EMPTY;
      }
   }

   private static void applyStats(ItemStack stack, @Nullable HolderLookup.Provider registries) {
      if (stack.isEmpty()) {
         return;
      }

      BladeStateData current = BladeStateAccess.getDataOrDefault(stack);
      boolean legacyInitialProgress = current.proudSoul() == LEGACY_INITIAL_PROUD_SOUL
         && current.killCount() == LEGACY_INITIAL_KILL_COUNT
         && current.refine() == LEGACY_INITIAL_REFINE;
      int proudSoul = legacyInitialProgress ? 0 : current.proudSoul();
      int killCount = legacyInitialProgress ? 0 : current.killCount();
      int refine = legacyInitialProgress ? 0 : current.refine();

      stack.set(DataComponents.MAX_DAMAGE, 2000);
      stack.setDamageValue(0);
      stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
      stack.set(DataComponents.RARITY, Rarity.EPIC);
      stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), new BladeStateData(
         DESCRIPTION_ID,
         50.0F,
         proudSoul,
         killCount,
         refine,
         false,
         false,
          SPATIAL_FRACTURE_ID,
         true,
         COMBO_ROOT_ID,
         CarryType.NINJA,
         0xFFAA00FF,
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
      stack.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.SMITE), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.BANE_OF_ARTHROPODS), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.LOOTING), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.SWEEPING_EDGE), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.POWER), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.MENDING), 10);
      stack.enchant(enchantments.getOrThrow(Enchantments.THORNS), 10);
   }

   public static ItemStack createFromLookup(HolderLookup.Provider registries) {
      if (registries != null) {
         var lookup = SlashBlade.getSlashBladeDefinitionRegistry(registries);
         var key = ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, ID);
          Optional<ItemStack> defined = lookup.get(key).map(holder -> holder.value().getBlade(bladeItem(), registries));
         if (defined.isPresent()) {
            ItemStack stack = defined.get();
            applyStats(stack, registries);
            return stack;
         }
      }
      ItemStack stack = createStack();
      applyEnchantments(stack, registries);
      return stack;
   }
}
