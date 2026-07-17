package QWQ.QingYi.annihilationblade.event;

import QWQ.QingYi.annihilationblade.annihilation_blade.AnnihilationBladeDefinitions;
import QWQ.QingYi.annihilationblade.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationblade.annihilation_blade.logic.WorldRiftChain;
import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.AbyssalDecree;
import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.CausalityCollapse;
import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.Dankong;
import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.PhantomJudgement;
import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.StarlessJudgement;
import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.TerminusEcho;
import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.VoidDominion;
import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.blood_prison.logic.BloodPrisonLogic;
import QWQ.QingYi.annihilationblade.common.SlashBladeTargeting;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public class ModEventHandler {
   private static final int INVENTORY_SCAN_INTERVAL = 10;
   private static final int BLADE_REFRESH_INTERVAL = 20;
   private static final Set<UUID> PLAYERS_WITH_FLIGHT = new HashSet<>();
   private static final Map<UUID, Boolean> HAS_BLADE_CACHE = new HashMap<>();
   private static final Map<UUID, Integer> LAST_BLADE_SCAN_TICK = new HashMap<>();

   private static boolean isGodBlade(ItemStack stack) {
      return SpecialEffectSupport.isAnnihilationBlade(stack);
   }

   private static boolean hasBladeInInventory(Player player) {
      if (!isGodBlade(player.getMainHandItem()) && !isGodBlade(player.getOffhandItem())) {
         UUID id = player.getUUID();
         Integer lastScan = LAST_BLADE_SCAN_TICK.get(id);
         if (lastScan != null && player.tickCount - lastScan < INVENTORY_SCAN_INTERVAL) {
            return HAS_BLADE_CACHE.getOrDefault(id, false);
         }

         for (ItemStack stack : player.getInventory().items) {
            if (isGodBlade(stack)) {
               cacheBladeState(player, true);
               return true;
            }
         }

         cacheBladeState(player, false);
         return false;
      } else {
         cacheBladeState(player, true);
         return true;
      }
   }

   private static void cacheBladeState(Player player, boolean hasBlade) {
      UUID id = player.getUUID();
      HAS_BLADE_CACHE.put(id, hasBlade);
      LAST_BLADE_SCAN_TICK.put(id, player.tickCount);
   }

   private static void refreshBladeGrowthData(Player player) {
      refreshBladeGrowthData(player.getMainHandItem(), player);
      refreshBladeGrowthData(player.getOffhandItem(), player);

      for (ItemStack stack : player.getInventory().items) {
         refreshBladeGrowthData(stack, player);
      }
   }

   private static void refreshBladeGrowthData(ItemStack stack, Player player) {
      if (isGodBlade(stack)) {
         AnnihilationBladeDefinitions.ensureStats(stack, player.level());
      }
   }

   private static boolean hasGodBladeInHands(Player player) {
      return isGodBlade(player.getMainHandItem()) || isGodBlade(player.getOffhandItem());
   }

   private static boolean isDirectGodBladeAttack(DamageSource source, Player player) {
      return isGodBlade(player.getMainHandItem()) && source.getDirectEntity() == player && "player".equals(source.getMsgId());
   }

   private static boolean isGodBladeSlashEntityAttack(Entity directSource, Player player) {
      return directSource != null && directSource.getType().toString().contains("slashblade") && hasGodBladeInHands(player);
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public static void onItemTooltip(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (isGodBlade(stack)) {
         List<Component> tooltip = event.getToolTip();
         removeVanillaAttackLines(tooltip);
         tooltip.add(Component.literal(" "));
         tooltip.add(buildRainbowDamageLine());
         if (!tooltip.isEmpty()) {
            tooltip.add(1, Component.literal(" "));
            tooltip.add(
               1,
               Component.translatable("item.annihilationblade.desc.line4").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC})
            );
            tooltip.add(
               1,
               Component.translatable("item.annihilationblade.desc.line3").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC})
            );
            tooltip.add(
               1,
               Component.translatable("item.annihilationblade.desc.line2").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC})
            );
            tooltip.add(
               1,
               Component.translatable("item.annihilationblade.desc.line1").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC})
            );
            tooltip.add(1, Component.literal(" "));
            tooltip.add(1, Component.translatable("item.annihilationblade.passive"));
         }
      }
   }

   private static void removeVanillaAttackLines(List<Component> tooltip) {
      Iterator<Component> it = tooltip.iterator();

      while (it.hasNext()) {
         String text = it.next().getString();
         if (text.contains("\u653b\u51fb\u4f24\u5bb3")
            || text.contains("Attack Damage")
            || text.contains("\u5728\u4e3b\u624b\u65f6")
            || text.contains("When in main hand")
            || text.contains("\u653b\u51fb\u901f\u5ea6")
            || text.contains("Attack Speed")
            || text.contains("\u8303\u56f4")
            || text.contains("Range")) {
            it.remove();
         }
      }
   }

   private static MutableComponent buildRainbowDamageLine() {
      MutableComponent rainbowText = Component.literal(" ");
      String rawText = Component.translatable("item.annihilationblade.infinite_damage").getString();
      long time = System.currentTimeMillis();

      for (int i = 0; i < rawText.length(); i++) {
         float hue = ((float)(time % 3000L) / 3000.0F + i * 0.08F) % 1.0F;
         int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
         rainbowText.append(Component.literal(String.valueOf(rawText.charAt(i))).withStyle(style -> style.withColor(rgb)));
      }

      return rainbowText;
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onHurt(LivingHurtEvent event) {
      if (WorldRiftChain.isExecutingChain()) {
         return;
      }

      if (BloodPrisonLogic.isPhantomBurstDamage(event.getEntity())) {
         return;
      }

      Entity source = event.getSource().getEntity();
      Entity directSource = event.getSource().getDirectEntity();
      if (source instanceof Player player) {
         boolean shouldKill = isDirectGodBladeAttack(event.getSource(), player) || isGodBladeSlashEntityAttack(directSource, player);

         if (shouldKill && SlashBladeTargeting.canAttack(player, event.getEntity())) {
            if (event.getEntity().level() instanceof ServerLevel level) {
               WorldRiftChain.scheduleFromDamage(level, player, event.getEntity());
            }

            if (!TerminusLogic.isMarkedForDeath(event.getEntity())) {
               event.setAmount(10000.0F);
               TerminusLogic.markForDeath(event.getEntity());
               if (event.getEntity().level() instanceof ServerLevel level) {
                  AnnihilationVisuals.spawnExecutionBurst(level, event.getEntity(), player.getRandom());
               }

               if (player.distanceTo(event.getEntity()) < 6.0F) {
                  event.getEntity()
                     .level()
                     .playSound(
                        null,
                        event.getEntity().getX(),
                        event.getEntity().getY(),
                        event.getEntity().getZ(),
                        SoundEvents.TRIDENT_THUNDER,
                        SoundSource.PLAYERS,
                        0.5F,
                        2.0F
                     );
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onGodBladeAttack(LivingAttackEvent event) {
      if (event.getSource().getEntity() instanceof Player player
         && isGodBlade(player.getMainHandItem())
         && !SlashBladeTargeting.canAttack(player, event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingAttack(LivingAttackEvent event) {
      if (!event.getEntity().level().isClientSide) {
         if (event.getEntity() instanceof Player player && hasBladeInInventory(player)) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingDeath(LivingDeathEvent event) {
      if (!event.getEntity().level().isClientSide) {
         if (event.getEntity() instanceof Player player && hasBladeInInventory(player)) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player player = event.player;
         if (!player.level().isClientSide) {
            UUID key = player.getUUID();
            boolean hasBlade = hasBladeInInventory(player);
            if (hasBlade && player.tickCount % BLADE_REFRESH_INTERVAL == 0) {
               refreshBladeGrowthData(player);
            }

            if (hasBlade) {
               if (player.getHealth() < player.getMaxHealth()) {
                  player.setHealth(player.getMaxHealth());
               }

               player.getFoodData().setFoodLevel(20);
               player.getFoodData().setSaturation(20.0F);
               if (!player.isCreative() && !player.isSpectator() && !player.getAbilities().mayfly) {
                  player.getAbilities().mayfly = true;
                  PLAYERS_WITH_FLIGHT.add(key);
                  player.onUpdateAbilities();
               }

               if (player.getY() < -64.0) {
                  player.teleportTo(player.getX(), 320.0, player.getZ());
                  player.setDeltaMovement(0.0, 0.0, 0.0);
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
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onItemToss(ItemTossEvent event) {
      if (isGodBlade(event.getEntity().getItem()) && !event.getPlayer().isCreative()) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerLoggedOutEvent event) {
      clearPlayerState(event.getEntity());
      Dankong.clearBlinkMode(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      clearPlayerState(event.getEntity());
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
