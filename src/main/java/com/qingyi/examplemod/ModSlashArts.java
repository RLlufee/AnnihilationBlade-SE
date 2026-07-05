package com.qingyi.examplemod;

import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSlashArts {
    public static final DeferredRegister<SlashArts> SLASH_ARTS =
            DeferredRegister.create(SlashArts.REGISTRY_KEY, AnnihilationBladeEX.MODID);

    public static final DeferredHolder<SlashArts, SlashArts> SPATIAL_FRACTURE =
            SLASH_ARTS.register("spatial_fracture", () ->
                    new SlashArts(entity -> ModComboStates.SPATIAL_FRACTURE_STATE.getId()).setProudSoulCost(0));

    public static void register(IEventBus eventBus) {
        SLASH_ARTS.register(eventBus);
    }
}
