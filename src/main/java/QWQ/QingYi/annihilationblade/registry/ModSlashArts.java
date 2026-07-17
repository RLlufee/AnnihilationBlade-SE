package QWQ.QingYi.annihilationblade.registry;

import QWQ.QingYi.annihilationblade.blood_prison.logic.BloodPrisonLogic;
import QWQ.QingYi.annihilationblade.infinity_stellaris.logic.VacuumDecayCollapseLogic;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSlashArts {
   public static final DeferredRegister<SlashArts> ARTS = DeferredRegister.create(SlashArts.REGISTRY_KEY, "annihilationblade");
   public static final RegistryObject<SlashArts> SPATIAL_FRACTURE = ARTS.register(
      "spatial_fracture", () -> new SlashArts(entity -> ModComboStates.SPATIAL_FRACTURE_STATE.getId())
   );
   public static final RegistryObject<SlashArts> INFERNAL_SLAUGHTER = ARTS.register("infernal_slaughter", () -> new SlashArts(entity -> {
      if (entity instanceof Player player && !player.level().isClientSide) {
         BloodPrisonLogic.activateDomain(player);
      }

      return ModComboStates.INFERNAL_SLAUGHTER_STATE.getId();
   }));
   public static final RegistryObject<SlashArts> VACUUM_DECAY_COLLAPSE = ARTS.register("vacuum_decay_collapse", () -> new SlashArts(entity -> {
      if (entity instanceof Player player && !player.level().isClientSide) {
         VacuumDecayCollapseLogic.prepareCast(player);
      }

      return ModComboStates.VACUUM_DECAY_COLLAPSE_STATE.getId();
   }));

   public static void register(IEventBus eventBus) {
      ARTS.register(eventBus);
   }
}
