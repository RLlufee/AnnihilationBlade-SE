package QWQ.QingYi.annihilationbladeex;

import QWQ.QingYi.annihilationbladeex.specialeffect.AbyssalDecree;
import QWQ.QingYi.annihilationbladeex.specialeffect.CausalityCollapse;
import QWQ.QingYi.annihilationbladeex.specialeffect.Dankong;
import QWQ.QingYi.annihilationbladeex.specialeffect.PhantomJudgement;
import QWQ.QingYi.annihilationbladeex.specialeffect.StarlessJudgement;
import QWQ.QingYi.annihilationbladeex.specialeffect.TerminusEcho;
import QWQ.QingYi.annihilationbladeex.specialeffect.VoidDominion;
import QWQ.QingYi.annihilationbladeex.specialeffect.WorldRift;
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
    public static final DeferredHolder<SpecialEffect, SpecialEffect> BLOOD_LEECH = SPECIAL_EFFECTS.register("blood_leech", () -> new SpecialEffect(0, false, false));
    public static final DeferredHolder<SpecialEffect, SpecialEffect> SPIRIT_SHIELD = SPECIAL_EFFECTS.register("spirit_shield", () -> new SpecialEffect(0, false, false));
    public static final DeferredHolder<SpecialEffect, SpecialEffect> PHANTOM_MARK = SPECIAL_EFFECTS.register("phantom_mark", () -> new SpecialEffect(0, false, false));

    public static void register(IEventBus eventBus) {
        SPECIAL_EFFECTS.register(eventBus);
    }
}
