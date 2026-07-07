package org.examplea.annihilationblade.registry;

import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.examplea.annihilationblade.Annihilationblade;

public class ModSpecialEffects {
    public static final DeferredRegister<SpecialEffect> SPECIAL_EFFECTS =
            DeferredRegister.create(SpecialEffect.REGISTRY_KEY, Annihilationblade.MODID);

    public static final RegistryObject<SpecialEffect> DANKONG =
            SPECIAL_EFFECTS.register("dankong", org.examplea.annihilationblade.specialeffect.Dankong::new);
    public static final RegistryObject<SpecialEffect> WORLD_RIFT =
            SPECIAL_EFFECTS.register("world_rift", org.examplea.annihilationblade.specialeffect.WorldRift::new);
    public static final RegistryObject<SpecialEffect> TERMINUS_ECHO =
            SPECIAL_EFFECTS.register("terminus_echo", org.examplea.annihilationblade.specialeffect.TerminusEcho::new);
    public static final RegistryObject<SpecialEffect> VOID_DOMINION =
            SPECIAL_EFFECTS.register("void_dominion", org.examplea.annihilationblade.specialeffect.VoidDominion::new);
    public static final RegistryObject<SpecialEffect> CAUSALITY_COLLAPSE =
            SPECIAL_EFFECTS.register("causality_collapse", org.examplea.annihilationblade.specialeffect.CausalityCollapse::new);
    public static final RegistryObject<SpecialEffect> STARLESS_JUDGEMENT =
            SPECIAL_EFFECTS.register("starless_judgement", org.examplea.annihilationblade.specialeffect.StarlessJudgement::new);
    public static final RegistryObject<SpecialEffect> PHANTOM_JUDGEMENT =
            SPECIAL_EFFECTS.register("phantom_judgement", org.examplea.annihilationblade.specialeffect.PhantomJudgement::new);
    public static final RegistryObject<SpecialEffect> ABYSSAL_DECREE =
            SPECIAL_EFFECTS.register("abyssal_decree", org.examplea.annihilationblade.specialeffect.AbyssalDecree::new);

    public static void register(IEventBus eventBus) {
        SPECIAL_EFFECTS.register(eventBus);
    }
}
