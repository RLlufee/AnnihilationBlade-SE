package com.qingyi.annihilationbladeex;

import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateData;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeDataComponents;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

@Mod(AnnihilationBladeEX.MODID)
public class AnnihilationBladeEX {
    public static final String MODID = "annihilationbladeex";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation BLADE_ID = prefix("annihilation_blade");
    public static final ResourceLocation SPATIAL_FRACTURE_ID = prefix("spatial_fracture");
    public static final ResourceLocation DANKONG_ID = prefix("dankong");
    public static final ResourceLocation WORLD_RIFT_ID = prefix("world_rift");
    public static final ResourceLocation TERMINUS_ECHO_ID = prefix("terminus_echo");
    public static final ResourceLocation VOID_DOMINION_ID = prefix("void_dominion");
    public static final ResourceLocation CAUSALITY_COLLAPSE_ID = prefix("causality_collapse");
    public static final ResourceLocation STARLESS_JUDGEMENT_ID = prefix("starless_judgement");
    public static final ResourceLocation PHANTOM_JUDGEMENT_ID = prefix("phantom_judgement");
    public static final ResourceLocation ABYSSAL_DECREE_ID = prefix("abyssal_decree");
    private static final ResourceLocation COMBO_ROOT_ID = ResourceLocation.fromNamespaceAndPath("slashblade", "standby");
    public static final String BLADE_TRANSLATION_KEY = "item." + MODID + ".annihilation_blade";
    private static final int LEGACY_INITIAL_PROUD_SOUL = 100000;
    private static final int LEGACY_INITIAL_KILL_COUNT = 10000;
    private static final int LEGACY_INITIAL_REFINE = 1000;
    public static final List<ResourceLocation> GOD_SPECIAL_EFFECTS = List.of(
            DANKONG_ID,
            WORLD_RIFT_ID,
            TERMINUS_ECHO_ID,
            VOID_DOMINION_ID,
            CAUSALITY_COLLAPSE_ID,
            STARLESS_JUDGEMENT_ID,
            PHANTOM_JUDGEMENT_ID,
            ABYSSAL_DECREE_ID
    );

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SLASHBLADE_TAB =
            CREATIVE_MODE_TABS.register("slashblade_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("item." + MODID + ".tab_title"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> new ItemStack(ModItems.ANNIHILATION_CORE.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.ANNIHILATION_FRAGMENT.get());
                output.accept(ModItems.ANNIHILATION_CORE.get());
            }).build());

    public AnnihilationBladeEX(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        ModItems.register(modEventBus);
        ModSlashArts.register(modEventBus);
        ModComboStates.register(modEventBus);
        ModSpecialEffects.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Loaded {} for SlashBlade Resharped 1.21.1", MODID);
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ItemStack createGodBlade() {
        ItemStack stack = new ItemStack(ModItems.ANNIHILATION_BLADE.get());
        applyGodStats(stack);
        return stack;
    }

    public static ItemStack createGodBlade(Level level, HolderLookup.Provider registries) {
        if (level != null) {
            var registry = SlashBlade.getSlashBladeDefinitionRegistry(level);
            SlashBladeDefinition definition = registry.get(BLADE_ID);
            if (definition != null) {
                ItemStack stack = definition.getBlade(registries);
                applyGodStats(stack);
                ensureGodEnchantments(stack, registries);
                return stack;
            }
        }
        ItemStack stack = createGodBlade();
        ensureGodEnchantments(stack, registries);
        return stack;
    }

    public static void applyGodStats(ItemStack stack) {
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
        stack.set(DataComponents.RARITY, net.minecraft.world.item.Rarity.EPIC);

        stack.set(SlashBladeDataComponents.BLADE_STATE_DATA.get(), new BladeStateData(
                BLADE_TRANSLATION_KEY,
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
                Optional.of(prefix("model/blade.png")),
                Optional.of(prefix("model/blade.obj")),
                GOD_SPECIAL_EFFECTS
        ));
        BladeStateAccess.ensureRuntimeComponent(stack);
    }

    public static void ensureGodEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        if (stack.isEmpty() || registries == null) {
            return;
        }

        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        stack.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 5);
        stack.enchant(enchantments.getOrThrow(Enchantments.SMITE), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.BANE_OF_ARTHROPODS), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.LOOTING), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.SWEEPING_EDGE), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.POWER), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.MENDING), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.THORNS), 10);
    }

    public static void refreshHeldGodBlade(ItemStack stack, Entity holder) {
        applyGodStats(stack);
        if (holder != null) {
            ensureGodEnchantments(stack, holder.registryAccess());
        }
    }

    public static boolean isGodBlade(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ItemAnnihilationBlade) {
            return true;
        }
        if (BLADE_TRANSLATION_KEY.equals(stack.getDescriptionId())) {
            return true;
        }
        return BladeStateAccess.of(stack)
                .map(state -> BLADE_TRANSLATION_KEY.equals(state.getTranslationKey()))
                .orElse(false);
    }

    public static boolean hasGodBlade(net.minecraft.world.entity.player.Player player) {
        if (isGodBlade(player.getMainHandItem()) || isGodBlade(player.getOffhandItem())) {
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (isGodBlade(stack)) {
                return true;
            }
        }
        return false;
    }
}
