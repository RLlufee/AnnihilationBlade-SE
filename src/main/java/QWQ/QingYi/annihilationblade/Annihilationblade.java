package QWQ.QingYi.annihilationblade;

import QWQ.QingYi.annihilationblade.network.ModNetwork;
import QWQ.QingYi.annihilationblade.config.ModConfig;
import QWQ.QingYi.annihilationblade.registry.ModComboStates;
import QWQ.QingYi.annihilationblade.registry.ModCreativeTabs;
import QWQ.QingYi.annihilationblade.registry.ModEntities;
import QWQ.QingYi.annihilationblade.registry.ModItems;
import QWQ.QingYi.annihilationblade.registry.ModSlashArts;
import QWQ.QingYi.annihilationblade.registry.ModSpecialEffects;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("annihilationblade")
public class Annihilationblade {
   public static final String MODID = "annihilationblade";
   public static final Logger LOGGER = LogUtils.getLogger();

   public Annihilationblade(FMLJavaModLoadingContext context) {
      IEventBus modEventBus = context.getModEventBus();
      ModItems.ITEMS.register(modEventBus);
      ModEntities.ENTITY_TYPES.register(modEventBus);
      ModSlashArts.register(modEventBus);
      ModComboStates.register(modEventBus);
      ModSpecialEffects.register(modEventBus);
      ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
      ModNetwork.register();
      ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);
      MinecraftForge.EVENT_BUS.register(this);
   }
}
