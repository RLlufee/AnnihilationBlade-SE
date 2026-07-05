package org.examplea.annihilationblade;

import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.examplea.annihilationblade.registry.ModComboStates;
import org.examplea.annihilationblade.registry.ModItems;
import org.examplea.annihilationblade.registry.ModSlashArts;
import org.examplea.annihilationblade.registry.ModSpecialEffects;
import org.slf4j.Logger;

import java.util.Map;

@Mod(Annihilationblade.MODID)
public class Annihilationblade {
    public static final String MODID = "annihilationblade";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final int INITIAL_KILL_COUNT = 0;
    private static final int INITIAL_PROUD_SOUL = 0;
    private static final String GROWTH_MIGRATION_KEY = "AnnihilationBladeGrowthMigrated";

    private static final String[] GOD_SPECIAL_EFFECTS = {
            "annihilationblade:dankong",
            "annihilationblade:world_rift",
            "annihilationblade:terminus_echo",
            "annihilationblade:void_dominion",
            "annihilationblade:causality_collapse",
            "annihilationblade:starless_judgement"
    };

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> SLASHBLADE_TAB = CREATIVE_MODE_TABS.register("slashblade_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item.annihilationblade.tab_title"))
                    .icon(() -> {
                        ItemStack stack = new ItemStack(SBItems.slashblade);
                        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
                            state.setModel(ResourceLocation.fromNamespaceAndPath(MODID, "model/blade.obj"));
                            state.setTexture(ResourceLocation.fromNamespaceAndPath(MODID, "model/blade.png"));
                        });
                        return stack;
                    })
                    .displayItems((parameters, output) -> {
                        ItemStack godSword = getGodBladeFromManager();
                        if (!godSword.isEmpty()) {
                            output.accept(godSword);
                        }
                        output.accept(new ItemStack(ModItems.ANNIHILATION_FRAGMENT.get()));
                        output.accept(new ItemStack(ModItems.ANNIHILATION_CORE.get()));
                    })
                    .build());

    public Annihilationblade(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        ModSlashArts.register(modEventBus);
        ModComboStates.register(modEventBus);
        ModSpecialEffects.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private static ItemStack getGodBladeFromManager() {
        if (Minecraft.getInstance().getConnection() != null) {
            var registry = BladeModelManager.getClientSlashBladeRegistry();
            for (var entry : registry.entrySet()) {
                if (entry.getKey() != null && entry.getKey().location().getNamespace().equals(MODID)) {
                    ItemStack stack = entry.getValue().getBlade().copy();
                    applyGodStats(stack);
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void applyGodStats(ItemStack stack) {
        ensureGodStats(stack);
        resetGrowthStats(stack);
    }

    public static void ensureGodStats(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("IsAnnihilationBlade", true);
        tag.putInt("RepairCost", Math.max(1, tag.getInt("RepairCost")));
        tag.putString("ModelName", "annihilationblade:model/blade");
        tag.putString("TextureName", "annihilationblade:model/blade");
        tag.putString("SlashArts", "annihilationblade:spatial_fracture");
        tag.putInt("SummonedSwordColor", 0xFFAA00FF);

        applyGodSpecialEffects(stack);
        applyGodEnchantments(stack);
        migrateGrowthStats(stack);

        tag.putInt("HideFlags", 2);
    }

    private static void applyGodEnchantments(ItemStack stack) {
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
        boolean migrated = tag.getBoolean(GROWTH_MIGRATION_KEY);
        CompoundTag bladeState = tag.getCompound("bladeState");

        int legacyKillCount = Math.max(readInt(tag, "KillCount", "killCount"),
                readInt(bladeState, "KillCount", "killCount"));
        int legacyProudSoul = Math.max(readInt(tag, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul"),
                readInt(bladeState, "ProudSoul", "ProudSoulCount", "proudSoul", "proudsoul"));
        int legacyRefine = Math.max(readInt(tag, "Refine", "RefineCount", "RepairCounter", "refine", "refineCount"),
                readInt(bladeState, "Refine", "RefineCount", "RepairCounter", "refine", "refineCount"));

        boolean hasState = stack.getCapability(ItemSlashBlade.BLADESTATE).map(state -> {
            if (!migrated) {
                state.setKillCount(Math.max(state.getKillCount(), Math.max(INITIAL_KILL_COUNT, legacyKillCount)));
                state.setProudSoulCount(Math.max(state.getProudSoulCount(), Math.max(INITIAL_PROUD_SOUL, legacyProudSoul)));
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
            tag.putBoolean(GROWTH_MIGRATION_KEY, true);
        }
    }

    private static void applyGodSpecialEffects(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag bladeState = tag.getCompound("bladeState");
        ListTag specialEffects = bladeState.getList("SpecialEffects", Tag.TAG_STRING);

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
            state.setKillCount(INITIAL_KILL_COUNT);
            state.setProudSoulCount(INITIAL_PROUD_SOUL);
            state.setRefine(0);

            CompoundTag updatedBladeState = tag.getCompound("bladeState");
            updatedBladeState.putInt("killCount", INITIAL_KILL_COUNT);
            updatedBladeState.putInt("proudSoul", INITIAL_PROUD_SOUL);
            updatedBladeState.putInt("RepairCounter", 0);
            tag.put("bladeState", updatedBladeState);
        });

        removeLegacyGrowthKeys(tag);
        tag.putBoolean(GROWTH_MIGRATION_KEY, true);
    }
}
