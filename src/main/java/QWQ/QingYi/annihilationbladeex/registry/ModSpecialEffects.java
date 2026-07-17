package QWQ.QingYi.annihilationbladeex.registry;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.AbyssalDecree;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.CausalityCollapse;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.Dankong;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.PhantomJudgement;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.StarlessJudgement;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.TerminusEcho;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.VoidDominion;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.WorldRift;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSpecialEffects {
   public static final DeferredRegister<SpecialEffect> SPECIAL_EFFECTS = DeferredRegister.create(SpecialEffect.REGISTRY_KEY, AnnihilationBladeEX.MODID);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> DANKONG = SPECIAL_EFFECTS.register("dankong", Dankong::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> WORLD_RIFT = SPECIAL_EFFECTS.register("world_rift", WorldRift::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> TERMINUS_ECHO = SPECIAL_EFFECTS.register("terminus_echo", TerminusEcho::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> VOID_DOMINION = SPECIAL_EFFECTS.register("void_dominion", VoidDominion::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> CAUSALITY_COLLAPSE = SPECIAL_EFFECTS.register("causality_collapse", CausalityCollapse::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> STARLESS_JUDGEMENT = SPECIAL_EFFECTS.register("starless_judgement", StarlessJudgement::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> PHANTOM_JUDGEMENT = SPECIAL_EFFECTS.register("phantom_judgement", PhantomJudgement::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> ABYSSAL_DECREE = SPECIAL_EFFECTS.register("abyssal_decree", AbyssalDecree::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> BLOOD_LEECH = SPECIAL_EFFECTS.register("blood_leech", () -> new SpecialEffect(0, false, false));
   public static final DeferredHolder<SpecialEffect, SpecialEffect> SPIRIT_SHIELD = SPECIAL_EFFECTS.register("spirit_shield", () -> new SpecialEffect(0, false, false));
   public static final DeferredHolder<SpecialEffect, SpecialEffect> PHANTOM_MARK = SPECIAL_EFFECTS.register("phantom_mark", () -> new SpecialEffect(0, false, false));
   public static final DeferredHolder<SpecialEffect, SpecialEffect> ENTROPY_DISSOLUTION = SPECIAL_EFFECTS.register("entropy_dissolution", QWQ.QingYi.annihilationbladeex.infinity_stellaris.specialeffect.EntropyDissolution::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> CURVATURE_RUPTURE = SPECIAL_EFFECTS.register("curvature_rupture", QWQ.QingYi.annihilationbladeex.infinity_stellaris.specialeffect.CurvatureRupture::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> GAMMA_THUNDERBURST = SPECIAL_EFFECTS.register("gamma_thunderburst", QWQ.QingYi.annihilationbladeex.infinity_stellaris.specialeffect.GammaThunderburst::new);
   public static final DeferredHolder<SpecialEffect, SpecialEffect> COSMIC_STRING_CUT = SPECIAL_EFFECTS.register("cosmic_string_cut", QWQ.QingYi.annihilationbladeex.infinity_stellaris.specialeffect.CosmicStringCut::new);

   public static void register(IEventBus eventBus) {
      SPECIAL_EFFECTS.register(eventBus);
   }
}
