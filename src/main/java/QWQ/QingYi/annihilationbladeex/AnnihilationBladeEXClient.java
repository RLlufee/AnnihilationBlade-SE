package QWQ.QingYi.annihilationbladeex;

import mods.flammpfeil.slashblade.client.renderer.SlashBladeTEISR;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID, value = Dist.CLIENT)
public final class AnnihilationBladeEXClient {
    private AnnihilationBladeEXClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.ANNIHILATION_BLADE.get(),
                ResourceLocation.parse("slashblade:user"),
                (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
                    BladeModel.user = entity;
                    return 0;
                }));
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        var extensions = new IClientItemExtensions() {
            final BlockEntityWithoutLevelRenderer renderer = new SlashBladeTEISR(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        };
        event.registerItem(extensions, ModItems.ANNIHILATION_BLADE.get());
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation loc = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(ModItems.ANNIHILATION_BLADE.get()));
        var bakedModel = event.getModels().get(loc);
        if (bakedModel != null) {
            event.getModels().put(loc, new BladeModel(bakedModel, event.getModelBakery()));
        }
    }
}
