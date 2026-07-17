package QWQ.QingYi.annihilationblade.registry;

import QWQ.QingYi.annihilationblade.Annihilationblade;
import QWQ.QingYi.annihilationblade.infinity_stellaris.entity.GammaThunderboltEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Annihilationblade.MODID);

   public static final RegistryObject<EntityType<GammaThunderboltEntity>> GAMMA_THUNDERBOLT = ENTITY_TYPES.register(
      "gamma_thunderbolt",
      () -> EntityType.Builder.<GammaThunderboltEntity>of(GammaThunderboltEntity::new, MobCategory.MISC)
         .noSave()
         .sized(0.0F, 0.0F)
         .clientTrackingRange(16)
         .updateInterval(Integer.MAX_VALUE)
         .build("gamma_thunderbolt")
   );

   private ModEntities() {
   }
}
