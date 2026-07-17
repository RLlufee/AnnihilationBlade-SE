package QWQ.QingYi.annihilationbladeex.registry;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.logic.SpatialFractureExecutor;
import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.blood_prison.logic.BloodPrisonLogic;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.registry.combo.ComboState.Builder;
import mods.flammpfeil.slashblade.registry.combo.ComboState.TimeLineTickAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModComboStates {
   public static final DeferredRegister<ComboState> REGISTRY = DeferredRegister.create(ComboState.REGISTRY_KEY, AnnihilationBladeEX.MODID);
   public static final DeferredHolder<ComboState, ComboState> SPATIAL_FRACTURE_STATE = REGISTRY.register(
      "spatial_fracture_state",
      () -> Builder.newInstance()
         .priority(100)
         .startAndEnd(400, 460)
         .motionLoc(DefaultResources.ExMotionLocation)
         .next(entity -> ResourceLocation.fromNamespaceAndPath("slashblade", "none"))
         .nextOfTimeout(entity -> ResourceLocation.fromNamespaceAndPath("slashblade", "none"))
         .addTickAction(
            TimeLineTickAction.getBuilder()
               .put(
                  5,
                  entity -> entity.level()
                     .playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0F, 0.5F)
               )
               .put(15, entity -> {
                  if (entity instanceof Player player) {
                     if (!player.level().isClientSide) {
                        SpatialFractureExecutor.unleash(player);
                     }
                  }
               })
               .build()
         )
         .build()
   );
   public static final DeferredHolder<ComboState, ComboState> INFERNAL_SLAUGHTER_STATE = REGISTRY.register(
      "infernal_slaughter_state",
      () -> Builder.newInstance()
         .priority(90)
         .startAndEnd(500, 540)
         .motionLoc(DefaultResources.ExMotionLocation)
         .next(entity -> ResourceLocation.fromNamespaceAndPath("slashblade", "none"))
         .nextOfTimeout(entity -> ResourceLocation.fromNamespaceAndPath("slashblade", "none"))
         .addTickAction(TimeLineTickAction.getBuilder().put(8, entity -> {
            if (entity instanceof Player player && !player.level().isClientSide) {
               BloodPrisonLogic.activateDomain(player);
            }
         }).build())
         .build()
   );

   public static final DeferredHolder<ComboState, ComboState> VACUUM_DECAY_COLLAPSE_STATE = REGISTRY.register(
      "vacuum_decay_collapse_state",
      () -> Builder.newInstance()
         .priority(95)
         .startAndEnd(560, 620)
         .motionLoc(DefaultResources.ExMotionLocation)
         .next(entity -> ResourceLocation.fromNamespaceAndPath("slashblade", "none"))
         .nextOfTimeout(entity -> ResourceLocation.fromNamespaceAndPath("slashblade", "none"))
         .addTickAction(
            TimeLineTickAction.getBuilder()
               .put(
                  4,
                  entity -> entity.level()
                     .playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2F, 0.65F)
               )
               .put(16, entity -> {
                  if (entity instanceof Player player && !player.level().isClientSide) {
                     QWQ.QingYi.annihilationbladeex.infinity_stellaris.logic.VacuumDecayCollapseLogic.unleash(player);
                  }
               })
               .build()
         )
         .build()
   );

   public static void register(IEventBus bus) {
      REGISTRY.register(bus);
   }
}
