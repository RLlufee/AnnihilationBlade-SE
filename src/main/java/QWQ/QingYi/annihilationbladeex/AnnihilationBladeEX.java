package QWQ.QingYi.annihilationbladeex;

import QWQ.QingYi.annihilationbladeex.network.ModNetwork;
import QWQ.QingYi.annihilationbladeex.config.ModConfig;
import QWQ.QingYi.annihilationbladeex.registry.ModEntities;
import QWQ.QingYi.annihilationbladeex.registry.ModComboStates;
import QWQ.QingYi.annihilationbladeex.registry.ModCreativeTabs;
import QWQ.QingYi.annihilationbladeex.registry.ModItems;
import QWQ.QingYi.annihilationbladeex.registry.ModSlashArts;
import QWQ.QingYi.annihilationbladeex.registry.ModSpecialEffects;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import java.util.List;

@Mod(AnnihilationBladeEX.MODID)
public class AnnihilationBladeEX {
   public static final String MODID = "annihilationbladeex";
   public static final Logger LOGGER = LogUtils.getLogger();
   public static final ResourceLocation BLADE_ID = prefix("annihilation_blade");
   public static final ResourceLocation SPATIAL_FRACTURE_ID = prefix("spatial_fracture");
   public static final ResourceLocation DANKONG_ID = prefix("dankong");
   public static final ResourceLocation WORLD_RIFT_ID = prefix("world_rift");
   public static final ResourceLocation TERMINUS_ECHO_ID = prefix("terminus_echo");
   public static final ResourceLocation VOID_DOMINION_ID = prefix("void_dominion");
   public static final ResourceLocation CAUSALITY_COLLAPSE_ID = prefix("causality_collapse");
   public static final ResourceLocation STARLESS_JUDGEMENT_ID = prefix("starless_judgement");
   public static final ResourceLocation PHANTOM_JUDGEMENT_ID = prefix("phantom_judgement");
   public static final ResourceLocation ABYSSAL_DECREE_ID = prefix("abyssal_decree");
   public static final String BLADE_TRANSLATION_KEY = "item." + MODID + ".annihilation_blade";
   public static final List<ResourceLocation> GOD_SPECIAL_EFFECTS = List.of(
      DANKONG_ID,
      WORLD_RIFT_ID,
      TERMINUS_ECHO_ID,
      VOID_DOMINION_ID,
      CAUSALITY_COLLAPSE_ID,
      STARLESS_JUDGEMENT_ID,
      PHANTOM_JUDGEMENT_ID,
      ABYSSAL_DECREE_ID
   );

   public AnnihilationBladeEX(IEventBus modEventBus, ModContainer modContainer) {
      ModItems.ITEMS.register(modEventBus);
      ModEntities.ENTITY_TYPES.register(modEventBus);
      ModSlashArts.register(modEventBus);
      ModComboStates.register(modEventBus);
      ModSpecialEffects.register(modEventBus);
      ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
      modEventBus.addListener(ModNetwork::registerPayloads);
      modContainer.registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);
   }

   public static ResourceLocation prefix(String path) {
      return ResourceLocation.fromNamespaceAndPath(MODID, path);
   }
}
