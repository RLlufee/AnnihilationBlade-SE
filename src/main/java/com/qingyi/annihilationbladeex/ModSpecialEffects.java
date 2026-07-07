package com.qingyi.annihilationbladeex;

import com.qingyi.annihilationbladeex.specialeffect.AbyssalDecree;
import com.qingyi.annihilationbladeex.specialeffect.CausalityCollapse;
import com.qingyi.annihilationbladeex.specialeffect.Dankong;
import com.qingyi.annihilationbladeex.specialeffect.PhantomJudgement;
import com.qingyi.annihilationbladeex.specialeffect.StarlessJudgement;
import com.qingyi.annihilationbladeex.specialeffect.TerminusEcho;
import com.qingyi.annihilationbladeex.specialeffect.VoidDominion;
import com.qingyi.annihilationbladeex.specialeffect.WorldRift;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSpecialEffects {
    public static final DeferredRegister<SpecialEffect> SPECIAL_EFFECTS =
            DeferredRegister.create(SpecialEffect.REGISTRY_KEY, AnnihilationBladeEX.MODID);

    public static final DeferredHolder<SpecialEffect, SpecialEffect> DANKONG =
            SPECIAL_EFFECTS.register("dankong", Dankong::new);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> WORLD_RIFT =
            SPECIAL_EFFECTS.register("world_rift", WorldRift::new);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> TERMINUS_ECHO =
            SPECIAL_EFFECTS.register("terminus_echo", TerminusEcho::new);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> VOID_DOMINION =
            SPECIAL_EFFECTS.register("void_dominion", VoidDominion::new);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> CAUSALITY_COLLAPSE =
            SPECIAL_EFFECTS.register("causality_collapse", CausalityCollapse::new);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> STARLESS_JUDGEMENT =
            SPECIAL_EFFECTS.register("starless_judgement", StarlessJudgement::new);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> PHANTOM_JUDGEMENT =
            SPECIAL_EFFECTS.register("phantom_judgement", PhantomJudgement::new);
    public static final DeferredHolder<SpecialEffect, SpecialEffect> ABYSSAL_DECREE =
            SPECIAL_EFFECTS.register("abyssal_decree", AbyssalDecree::new);

    public static void register(IEventBus eventBus) {
        SPECIAL_EFFECTS.register(eventBus);
    }
}
