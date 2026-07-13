package QWQ.QingYi.annihilationblade.registry;

import QWQ.QingYi.annihilationblade.annihilation_blade.logic.SpatialFractureExecutor;
import QWQ.QingYi.annihilationblade.blood_prison.logic.BloodPrisonLogic;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.registry.combo.ComboState.Builder;
import mods.flammpfeil.slashblade.registry.combo.ComboState.TimeLineTickAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModComboStates {
   public static final DeferredRegister<ComboState> REGISTRY = DeferredRegister.create(ComboState.REGISTRY_KEY, "annihilationblade");
   public static final RegistryObject<ComboState> SPATIAL_FRACTURE_STATE = REGISTRY.register(
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
   public static final RegistryObject<ComboState> INFERNAL_SLAUGHTER_STATE = REGISTRY.register(
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

   public static void register(IEventBus bus) {
      REGISTRY.register(bus);
   }
}
