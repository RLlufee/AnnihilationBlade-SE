package com.qingyi.annihilationbladeex;

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
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.qingyi.annihilationbladeex.specialeffect.AbyssalDecree;
import com.qingyi.annihilationbladeex.specialeffect.CausalityCollapse;
import com.qingyi.annihilationbladeex.specialeffect.Dankong;
import com.qingyi.annihilationbladeex.specialeffect.PhantomJudgement;
import com.qingyi.annihilationbladeex.specialeffect.StarlessJudgement;
import com.qingyi.annihilationbladeex.specialeffect.TerminusEcho;
import com.qingyi.annihilationbladeex.specialeffect.VoidDominion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID)
public class ModEventHandler {
    private static final int INVENTORY_SCAN_INTERVAL = 10;
    private static final int BLADE_REFRESH_INTERVAL = 20;
    private static final Set<UUID> PLAYERS_WITH_FLIGHT = new HashSet<>();
    private static final Map<UUID, Boolean> HAS_BLADE_CACHE = new HashMap<>();
    private static final Map<UUID, Integer> LAST_BLADE_SCAN_TICK = new HashMap<>();

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

        if (AnnihilationBladeEX.isGodBlade(player.getMainHandItem())
                && !SlashBladeTargeting.canAttack(player, event.getEntity())) {
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
        Entity source = event.getSource().getEntity();
        if (source instanceof Player attacker
                && AnnihilationBladeEX.isGodBlade(attacker.getMainHandItem())
                && !SlashBladeTargeting.canAttack(attacker, event.getEntity())) {
            event.setCanceled(true);
            return;
        }

        if (!event.getEntity().level().isClientSide()
                && event.getEntity() instanceof Player player
                && hasGodBladeCached(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide()
                && event.getEntity() instanceof Player player
                && hasGodBladeCached(player)) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        UUID key = player.getUUID();
        boolean hasBlade = hasGodBladeCached(player);

        if (hasBlade) {
            if (player.tickCount % BLADE_REFRESH_INTERVAL == 0) {
                refreshIfGodBlade(player.getMainHandItem(), player);
                refreshIfGodBlade(player.getOffhandItem(), player);
            }
            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
            player.removeAllEffects();

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
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearPlayerState(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        clearPlayerState(event.getEntity());
    }

    private static boolean hasGodBladeCached(Player player) {
        if (AnnihilationBladeEX.isGodBlade(player.getMainHandItem())
                || AnnihilationBladeEX.isGodBlade(player.getOffhandItem())) {
            cacheBladeState(player, true);
            return true;
        }

        UUID id = player.getUUID();
        Integer lastScan = LAST_BLADE_SCAN_TICK.get(id);
        if (lastScan != null && player.tickCount - lastScan < INVENTORY_SCAN_INTERVAL) {
            return HAS_BLADE_CACHE.getOrDefault(id, false);
        }

        boolean hasBlade = AnnihilationBladeEX.hasGodBlade(player);
        cacheBladeState(player, hasBlade);
        return hasBlade;
    }

    private static void cacheBladeState(Player player, boolean hasBlade) {
        UUID id = player.getUUID();
        HAS_BLADE_CACHE.put(id, hasBlade);
        LAST_BLADE_SCAN_TICK.put(id, player.tickCount);
    }

    private static void refreshIfGodBlade(net.minecraft.world.item.ItemStack stack, Player player) {
        if (AnnihilationBladeEX.isGodBlade(stack)) {
            AnnihilationBladeEX.refreshHeldGodBlade(stack, player);
        }
    }

    private static void clearPlayerState(Player player) {
        UUID id = player.getUUID();
        PLAYERS_WITH_FLIGHT.remove(id);
        HAS_BLADE_CACHE.remove(id);
        LAST_BLADE_SCAN_TICK.remove(id);
        Dankong.clearPlayer(id);
        TerminusEcho.clearPlayer(id);
        AbyssalDecree.clearPlayer(id);
        PhantomJudgement.clearPlayer(player.level(), id);
        CausalityCollapse.clearPlayer(id);
        StarlessJudgement.clearPlayer(id);
        VoidDominion.clearPlayer(id);
    }
}
