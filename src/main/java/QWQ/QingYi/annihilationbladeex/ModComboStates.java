package QWQ.QingYi.annihilationbladeex;

import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComboStates {
    public static final DeferredRegister<ComboState> COMBO_STATES =
            DeferredRegister.create(ComboState.REGISTRY_KEY, AnnihilationBladeEX.MODID);

    public static final DeferredHolder<ComboState, ComboState> SPATIAL_FRACTURE_STATE =
            COMBO_STATES.register("spatial_fracture_state", () ->
                    ComboState.Builder.newInstance()
                            .priority(100)
                            .startAndEnd(400, 460)
                            .motionLoc(DefaultResources.ExMotionLocation)
                            .next(entity -> ComboStateRegistry.NONE.getId())
                            .nextOfTimeout(entity -> ComboStateRegistry.NONE.getId())
                            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                                    .put(5, entity -> entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                            SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0F, 0.5F))
                                    .put(15, entity -> {
                                        if (entity instanceof Player player && !entity.level().isClientSide()) {
                                            SpatialFractureExecutor.unleash(player);
                                        }
                                    })
                                    .build())
                            .build());
    public static final DeferredHolder<ComboState, ComboState> INFERNAL_SLAUGHTER_STATE = COMBO_STATES.register("infernal_slaughter_state", () -> ComboState.Builder.newInstance().priority(90).startAndEnd(500,540).motionLoc(DefaultResources.ExMotionLocation).next(entity -> ComboStateRegistry.NONE.getId()).nextOfTimeout(entity -> ComboStateRegistry.NONE.getId()).addTickAction(ComboState.TimeLineTickAction.getBuilder().put(8, entity -> { if(entity instanceof Player player && !entity.level().isClientSide()) BloodPrisonLogic.activateDomain(player); }).build()).build());

    public static void register(IEventBus eventBus) {
        COMBO_STATES.register(eventBus);
    }
}
