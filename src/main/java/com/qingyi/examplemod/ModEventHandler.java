package com.qingyi.examplemod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID)
public class ModEventHandler {
    private static final Set<String> PLAYERS_WITH_FLIGHT = new HashSet<>();

    @SubscribeEvent
    public static void onSlashBladeRegistryPost(SlashBladeRegistryEvent.Post event) {
        if (AnnihilationBladeEX.BLADE_ID.equals(event.getSlashBladeDefinition().getName())) {
            AnnihilationBladeEX.applyGodStats(event.getBlade());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();
        Entity directSource = event.getSource().getDirectEntity();
        if (!(source instanceof Player player)) {
            return;
        }

        boolean shouldKill = AnnihilationBladeEX.isGodBlade(player.getMainHandItem());
        if (!shouldKill && directSource != null && directSource.getType().toString().contains("slashblade")) {
            shouldKill = AnnihilationBladeEX.hasGodBlade(player);
        }

        if (shouldKill && !TerminusLogic.isMarkedForDeath(event.getEntity())) {
            event.setNewDamage(10000.0F);
            TerminusLogic.markForDeath(event.getEntity());
            if (player.distanceTo(event.getEntity()) < 6.0F) {
                event.getEntity().level().playSound(null, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.5F, 2.0F);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && AnnihilationBladeEX.hasGodBlade(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && AnnihilationBladeEX.hasGodBlade(player)) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        String key = player.getStringUUID();

        if (AnnihilationBladeEX.hasGodBlade(player)) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
            player.removeAllEffects();

            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 220, 4, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 220, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 220, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 220, 4, false, false));

            if (!player.isCreative() && !player.isSpectator() && !player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                PLAYERS_WITH_FLIGHT.add(key);
                player.onUpdateAbilities();
            }
            if (player.getY() < -64) {
                player.teleportTo(player.getX(), 320, player.getZ());
                player.setDeltaMovement(0, 0, 0);
                if (!player.getAbilities().flying) {
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                }
            }
        } else if (!player.isCreative() && !player.isSpectator() && player.getAbilities().mayfly && PLAYERS_WITH_FLIGHT.contains(key)) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            PLAYERS_WITH_FLIGHT.remove(key);
            player.onUpdateAbilities();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemToss(ItemTossEvent event) {
        if (AnnihilationBladeEX.isGodBlade(event.getEntity().getItem()) && !event.getPlayer().isCreative()) {
            event.setCanceled(true);
            event.getPlayer().getInventory().add(event.getEntity().getItem().copy());
        }
    }
}
