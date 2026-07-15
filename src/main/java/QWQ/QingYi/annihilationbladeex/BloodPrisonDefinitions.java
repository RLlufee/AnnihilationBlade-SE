package QWQ.QingYi.annihilationbladeex;

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
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class BloodPrisonDefinitions {
    public static final String NAME = "blood_prison";
    public static final ResourceLocation ID = AnnihilationBladeEX.prefix(NAME);
    public static final ResourceLocation INFERNAL_SLAUGHTER_ID = AnnihilationBladeEX.prefix("infernal_slaughter");
    public static final ResourceLocation BLOOD_LEECH_ID = AnnihilationBladeEX.prefix("blood_leech");
    public static final ResourceLocation SPIRIT_SHIELD_ID = AnnihilationBladeEX.prefix("spirit_shield");
    public static final ResourceLocation PHANTOM_MARK_ID = AnnihilationBladeEX.prefix("phantom_mark");
    public static final String DESCRIPTION_ID = "item." + AnnihilationBladeEX.MODID + ".blood_prison";
    private static final ResourceLocation COMBO_ROOT_ID = ResourceLocation.fromNamespaceAndPath("slashblade", "standby");
    private static final List<ResourceLocation> SPECIAL_EFFECTS = List.of(BLOOD_LEECH_ID, SPIRIT_SHIELD_ID, PHANTOM_MARK_ID);

    private BloodPrisonDefinitions() {
    }

    public static ItemStack createStack(HolderLookup.Provider registries) {
        if (registries == null) {
            return ItemStack.EMPTY;
        }
        var lookup = SlashBlade.getSlashBladeDefinitionRegistry(registries);
        var key = ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, ID);
        return lookup.get(key)
                .map(holder -> {
                    ItemStack stack = holder.value().getBlade(registries);
                    ensureStats(stack, registries);
                    return stack;
                })
                .orElse(ItemStack.EMPTY);
    }

    public static boolean isBloodPrison(ItemStack stack) {
        return !stack.isEmpty() && DESCRIPTION_ID.equals(stack.getDescriptionId());
    }

    public static void ensureStats(ItemStack stack, Entity holder) {
        ensureStats(stack, holder == null ? null : holder.registryAccess());
    }

    public static void ensureStats(ItemStack stack, HolderLookup.Provider registries) {
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
                Optional.of(AnnihilationBladeEX.prefix("model/blood_prison.png")),
                Optional.of(AnnihilationBladeEX.prefix("model/blood_prison.obj")),
                SPECIAL_EFFECTS
        ));
        BladeStateAccess.ensureRuntimeComponent(stack);
        applyEnchantments(stack, registries);
    }

    private static void applyEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        if (registries == null) {
            return;
        }
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        stack.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 10);
        stack.enchant(enchantments.getOrThrow(Enchantments.MENDING), 10);
    }
}
