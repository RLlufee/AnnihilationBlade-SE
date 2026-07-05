package com.qingyi.examplemod;

import com.qingyi.examplemod.specialeffect.Dankong;
import com.qingyi.examplemod.specialeffect.TerminusEcho;
import com.qingyi.examplemod.specialeffect.WorldRift;
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

    public static void register(IEventBus eventBus) {
        SPECIAL_EFFECTS.register(eventBus);
    }
}
