package org.examplea.annihilationblade.client;

import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.examplea.annihilationblade.Annihilationblade;

@OnlyIn(Dist.CLIENT)
public final class ClientBladeLookup {
    private ClientBladeLookup() {
    }

    public static ItemStack getNamedBladeFromManager(String bladeName) {
        if (Minecraft.getInstance().getConnection() == null) {
            return ItemStack.EMPTY;
        }

        var registry = BladeModelManager.getClientSlashBladeRegistry();
        for (var entry : registry.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals(Annihilationblade.MODID) && id.getPath().equals(bladeName)) {
                return entry.getValue().getBlade().copy();
            }
        }
        return ItemStack.EMPTY;
    }
}
