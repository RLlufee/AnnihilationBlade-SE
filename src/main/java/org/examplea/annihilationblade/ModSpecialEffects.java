package org.examplea.annihilationblade;

import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSpecialEffects {
    public static final DeferredRegister<SpecialEffect> SPECIAL_EFFECTS =
            DeferredRegister.create(SpecialEffect.REGISTRY_KEY, Annihilationblade.MODID);

    public static final RegistryObject<SpecialEffect> DANKONG =
            SPECIAL_EFFECTS.register("dankong", org.examplea.annihilationblade.specialeffect.Dankong::new);
    public static final RegistryObject<SpecialEffect> WORLD_RIFT =
            SPECIAL_EFFECTS.register("world_rift", org.examplea.annihilationblade.specialeffect.WorldRift::new);
    public static final RegistryObject<SpecialEffect> TERMINUS_ECHO =
            SPECIAL_EFFECTS.register("terminus_echo", org.examplea.annihilationblade.specialeffect.TerminusEcho::new);

    public static void register(IEventBus eventBus) {
        SPECIAL_EFFECTS.register(eventBus);
    }
}
