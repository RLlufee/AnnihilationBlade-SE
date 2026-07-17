package QWQ.QingYi.annihilationbladeex.registry;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.blood_prison.logic.BloodPrisonLogic;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSlashArts {
   public static final DeferredRegister<SlashArts> ARTS = DeferredRegister.create(SlashArts.REGISTRY_KEY, AnnihilationBladeEX.MODID);
   public static final DeferredHolder<SlashArts, SlashArts> SPATIAL_FRACTURE = ARTS.register(
      "spatial_fracture", () -> new SlashArts(entity -> ModComboStates.SPATIAL_FRACTURE_STATE.getId())
   );
   public static final DeferredHolder<SlashArts, SlashArts> INFERNAL_SLAUGHTER = ARTS.register("infernal_slaughter", () -> new SlashArts(entity -> {
      if (entity instanceof Player player && !player.level().isClientSide) {
         BloodPrisonLogic.activateDomain(player);
      }

      return ModComboStates.INFERNAL_SLAUGHTER_STATE.getId();
   }));
   public static final DeferredHolder<SlashArts, SlashArts> VACUUM_DECAY_COLLAPSE = ARTS.register("vacuum_decay_collapse", () -> new SlashArts(entity -> {
      if (entity instanceof Player player && !player.level().isClientSide) {
         QWQ.QingYi.annihilationbladeex.infinity_stellaris.logic.VacuumDecayCollapseLogic.prepareCast(player);
      }

      return ModComboStates.VACUUM_DECAY_COLLAPSE_STATE.getId();
   }));

   public static void register(IEventBus eventBus) {
      ARTS.register(eventBus);
   }
}
