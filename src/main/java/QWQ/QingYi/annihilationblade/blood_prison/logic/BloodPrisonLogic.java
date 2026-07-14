package QWQ.QingYi.annihilationblade.blood_prison.logic;

import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.blood_prison.BloodPrisonDefinitions;
import QWQ.QingYi.annihilationblade.common.SlashBladeTargeting;
import QWQ.QingYi.annihilationblade.config.ModConfig;
import QWQ.QingYi.annihilationblade.network.ModNetwork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mods.flammpfeil.slashblade.SlashBlade.RegistryEvents;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.event.SlashBladeEvent.DoSlashEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public final class BloodPrisonLogic {
   public static final String BLOOD_PRISON_TRANSLATION_KEY = "item.annihilationblade.blood_prison";
   private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("4f4c5a3b-70f1-43ee-b65d-74f4e1c1c95d");
   private static final double MAX_HEALTH_BONUS = 40.0;
   private static final float MIN_HEALTH = 8.0F;
   private static final int SHIELD_BUFF_TICKS = 100;
   private static final int INVENTORY_SCAN_INTERVAL = 10;
   private static final Map<UUID, BloodPrisonLogic.DrainWindow> DRAIN_WINDOWS = new HashMap<>();
   private static final Map<UUID, Float> BLOOD_SHIELDS = new HashMap<>();
   private static final Map<UUID, Map<UUID, Integer>> MARKS = new HashMap<>();
   private static final Map<UUID, BloodPrisonLogic.Domain> DOMAINS = new HashMap<>();
   private static final Set<UUID> PHANTOM_BURST_TARGETS = new HashSet<>();
   private static final Map<UUID, Boolean> HAS_BLOOD_PRISON_CACHE = new HashMap<>();
   private static final Map<UUID, Integer> LAST_BLOOD_PRISON_SCAN_TICK = new HashMap<>();

   private BloodPrisonLogic() {
   }

   public static boolean isBloodPrison(ItemStack stack) {
      return !stack.isEmpty() && "item.annihilationblade.blood_prison".equals(stack.getDescriptionId());
   }

   public static boolean hasBloodPrison(Player player) {
      if (!isBloodPrison(player.getMainHandItem()) && !isBloodPrison(player.getOffhandItem())) {
         UUID id = player.getUUID();
         Integer lastScan = LAST_BLOOD_PRISON_SCAN_TICK.get(id);
         if (lastScan != null && player.tickCount - lastScan < INVENTORY_SCAN_INTERVAL) {
            return HAS_BLOOD_PRISON_CACHE.getOrDefault(id, false);
         }

         for (ItemStack stack : player.getInventory().items) {
            if (isBloodPrison(stack)) {
               cacheBloodPrisonState(player, true);
               return true;
            }
         }

         cacheBloodPrisonState(player, false);
         return false;
      } else {
         cacheBloodPrisonState(player, true);
         return true;
      }
   }

   private static void cacheBloodPrisonState(Player player, boolean hasBloodPrison) {
      UUID id = player.getUUID();
      HAS_BLOOD_PRISON_CACHE.put(id, hasBloodPrison);
      LAST_BLOOD_PRISON_SCAN_TICK.put(id, player.tickCount);
   }

   private static void refreshBloodPrisonStats(Player player) {
      if (isBloodPrison(player.getMainHandItem())) {
         BloodPrisonDefinitions.ensureStats(player.getMainHandItem(), player.level());
      }

      if (isBloodPrison(player.getOffhandItem())) {
         BloodPrisonDefinitions.ensureStats(player.getOffhandItem(), player.level());
      }

      for (ItemStack stack : player.getInventory().items) {
         if (isBloodPrison(stack)) {
            BloodPrisonDefinitions.ensureStats(stack, player.level());
         }
      }
   }

   public static void activateDomain(Player player) {
      if (player.level() instanceof ServerLevel level && isBloodPrison(player.getMainHandItem())) {
         ModConfig.Domain config = ModConfig.COMMON.bloodPrison.domain;
         int durationTicks = config.durationTicks.get();
         double radius = config.radius.get();
         double visualScale = config.visualScale.get();
         BloodPrisonDefinitions.ensureStats(player.getMainHandItem(), level);
         DOMAINS.put(player.getUUID(), new BloodPrisonLogic.Domain(player.position(), level.getGameTime() + durationTicks));
         if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.sendBloodPrisonDomain(serverPlayer, durationTicks);
         }

         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 0.55F);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.55F, 1.45F);
         level.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 0.15, player.getZ(), visualCount(120, visualScale), radius * 0.3, 0.15 * visualScale, radius * 0.3, 0.03);
         level.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 0.2, player.getZ(), visualCount(180, visualScale), radius * 0.8, 0.25 * visualScale, radius * 0.8, 0.08);
         AnnihilationVisuals.spawnBloodPrisonDomainPulse(level, player.position(), radius * visualScale);
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide) {
         Player player = event.player;
         boolean hasBloodPrison = hasBloodPrison(player);
         if (hasBloodPrison && player.tickCount % 20 == 0) {
            refreshBloodPrisonStats(player);
         }

         updateMaxHealth(player, hasBloodPrison);
         updateShield(player, hasBloodPrison);
         tickDomain(player);
      }
   }

   @SubscribeEvent
   public static void onAttack(LivingAttackEvent event) {
      if (event.getSource().getEntity() instanceof Player player && isBloodPrison(player.getMainHandItem())) {
         if (SlashBladeTargeting.canAttack(player, event.getEntity())) {
            drainBloodForSwing(player);
         }
      }
   }

   @SubscribeEvent
   public static void onHurt(LivingHurtEvent event) {
      if (event.getSource().getEntity() instanceof Player player && isBloodPrison(player.getMainHandItem())) {
         if (!(event.getSource().getDirectEntity() instanceof EntityAbstractSummonedSword)) {
            LivingEntity target = event.getEntity();
            if (SlashBladeTargeting.canAttack(player, target)) {
               float missing = Math.max(0.0F, player.getMaxHealth() - player.getHealth());
               float damageBonus = (float)Math.floor(missing / 2.0F) * (player.getMaxHealth() * 0.001F);
               event.setAmount(event.getAmount() + damageBonus);
               BloodPrisonLogic.Domain domain = DOMAINS.get(player.getUUID());
               if (domain != null) {
                  domain.damageDealt = domain.damageDealt + event.getAmount();
               }

               if (!PHANTOM_BURST_TARGETS.contains(target.getUUID())) {
                  Map<UUID, Integer> playerMarks = MARKS.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
                  int marks = playerMarks.getOrDefault(target.getUUID(), 0) + 1;
                  if (marks < 10) {
                     playerMarks.put(target.getUUID(), marks);
                  } else {
                     playerMarks.remove(target.getUUID());
                     float burstDamage = target.getMaxHealth() * 0.05F;
                     UUID targetId = target.getUUID();
                     PHANTOM_BURST_TARGETS.add(targetId);

                     try {
                        target.hurt(target.level().damageSources().indirectMagic(player, player), burstDamage);
                        if (target.level() instanceof ServerLevel level) {
                           spawnPhantomSwordBurst(level, player, target);
                        }
                     } finally {
                        PHANTOM_BURST_TARGETS.remove(targetId);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof Player player) {
         removeDomain(player);
      } else {
         if (event.getSource().getEntity() instanceof Player player && isBloodPrison(player.getMainHandItem())) {
            player.heal(event.getEntity().getMaxHealth() * 0.1F);
         }
      }
   }

   @SubscribeEvent
   public static void onSlash(DoSlashEvent event) {
      if (event.getUser() instanceof Player player && DOMAINS.containsKey(player.getUUID())) {
         performDomainAttack(player);
      }
   }

   private static void updateMaxHealth(Player player, boolean hasBloodPrison) {
      AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
      if (attribute != null) {
         AttributeModifier modifier = attribute.getModifier(MAX_HEALTH_MODIFIER_ID);
         if (hasBloodPrison) {
            if (modifier == null) {
               attribute.addTransientModifier(new AttributeModifier(MAX_HEALTH_MODIFIER_ID, "blood_prison_max_health", 40.0, Operation.ADDITION));
            }
         } else if (modifier != null) {
            attribute.removeModifier(MAX_HEALTH_MODIFIER_ID);
         }
      }
   }

   private static void updateShield(Player player, boolean hasBloodPrison) {
      UUID id = player.getUUID();
      float shield = player.getMaxHealth() * 0.2F;
      if (hasBloodPrison && player.getHealth() <= 8.0F) {
         boolean newlyTriggered = !BLOOD_SHIELDS.containsKey(id);
         player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), shield));
         BLOOD_SHIELDS.put(id, shield);
         if (newlyTriggered) {
            applyShieldBuffs(player);
         }
      } else if (player.getHealth() > 20.0F && BLOOD_SHIELDS.remove(id) != null && player.getAbsorptionAmount() <= shield + 0.01F) {
         player.setAbsorptionAmount(0.0F);
      }
   }

   private static void applyShieldBuffs(Player player) {
      player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, false, true, true));
      player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true, true));
      player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, true, true));
      player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 100, 1, false, true, true));
   }

   private static void drainBloodForSwing(Player player) {
      long time = player.level().getGameTime();
      BloodPrisonLogic.DrainWindow window = DRAIN_WINDOWS.computeIfAbsent(player.getUUID(), ignored -> new BloodPrisonLogic.DrainWindow(time));
      if (time - window.startedAt >= 10L) {
         window.startedAt = time;
         window.drained = 0.0F;
      }

      float amount = Math.min(4.0F, 10.0F - window.drained);
      float actual = Math.min(amount, Math.max(0.0F, player.getHealth() - 8.0F));
      if (actual > 0.0F) {
         player.setHealth(player.getHealth() - actual);
         window.drained += actual;
      }
   }

   private static void tickDomain(Player player) {
      BloodPrisonLogic.Domain domain = DOMAINS.get(player.getUUID());
      if (domain != null) {
         ServerLevel level = (ServerLevel)player.level();
         if (!player.isAlive()) {
            removeDomain(player);
         } else if (level.getGameTime() >= domain.expiresAt) {
            player.heal(domain.damageDealt * 0.2F);
            removeDomain(player);
         } else {
            ModConfig.Domain config = ModConfig.COMMON.bloodPrison.domain;
            double radius = config.radius.get();
            double visualScale = config.visualScale.get();
            if (level.getGameTime() % config.borderIntervalTicks.get() == 0L) {
               for (double offset = -radius; offset <= radius; offset += 2.0) {
                  level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x + offset, domain.center.y + 0.1, domain.center.z - radius, 1, 0.0, 0.0, 0.0, 0.0);
                  level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x + offset, domain.center.y + 0.1, domain.center.z + radius, 1, 0.0, 0.0, 0.0, 0.0);
                  level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x - radius, domain.center.y + 0.1, domain.center.z + offset, 1, 0.0, 0.0, 0.0, 0.0);
                  level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x + radius, domain.center.y + 0.1, domain.center.z + offset, 1, 0.0, 0.0, 0.0, 0.0);
               }

               level.sendParticles(ParticleTypes.CRIMSON_SPORE, domain.center.x, domain.center.y + 0.15, domain.center.z, visualCount(18, visualScale), radius * 0.65, 0.05 * visualScale, radius * 0.65, 0.015);
            }

            if (level.getGameTime() % config.playerAuraIntervalTicks.get() == 0L) {
               level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0, player.getZ(), visualCount(8, visualScale), 1.2 * visualScale, 0.5 * visualScale, 1.2 * visualScale, 0.05);
            }

            if (level.getGameTime() % config.pulseIntervalTicks.get() == 0L) {
               AnnihilationVisuals.spawnBloodPrisonDomainPulse(level, domain.center, radius * visualScale);
            }
         }
      }
   }

   private static void performDomainAttack(Player player) {
      BloodPrisonLogic.Domain domain = DOMAINS.get(player.getUUID());
      if (domain != null && player.level() instanceof ServerLevel level) {
         double radius = ModConfig.COMMON.bloodPrison.domain.radius.get();
         List<LivingEntity> targets = new ArrayList<>(
            level.getEntitiesOfClass(
               LivingEntity.class, new AABB(domain.center, domain.center).inflate(radius), targetx -> SlashBladeTargeting.canAttack(player, targetx)
            )
         );
         if (!targets.isEmpty()) {
            LivingEntity target = targets.get(player.getRandom().nextInt(targets.size()));
            Vec3 from = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
            player.teleportTo(target.getX() + Math.cos(angle) * 1.5, target.getY(), target.getZ() + Math.sin(angle) * 1.5);
            target.hurt(level.damageSources().indirectMagic(player, player), 20.0F);
            domain.damageDealt += 20.0F;
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.75F);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + target.getBbHeight() * 0.55, target.getZ(), 3, 0.45, 0.45, 0.45, 0.0);
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.55, target.getZ(), 24, 0.75, 0.75, 0.75, 0.12);
            Vec3 hit = target.position().add(0.0, target.getBbHeight() * 0.58, 0.0);
            AnnihilationVisuals.spawnBloodPrisonDash(level, from, hit, player.getRandom());
         }
      }
   }

   private static void spawnPhantomSwordBurst(ServerLevel level, Player player, LivingEntity target) {
      ModConfig.PhantomBurst config = ModConfig.COMMON.bloodPrison.phantomBurst;
      int swordCount = config.swordCount.get();
      double visualScale = config.visualScale.get();
      Vec3 center = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);

      for (int i = 0; i < swordCount; i++) {
         double angle = (Math.PI * 2) * i / swordCount;
         Vec3 start = center.add(Math.cos(angle) * 4.0 * visualScale, (3.0 + i % 3) * visualScale, Math.sin(angle) * 4.0 * visualScale);
         AnnihilationVisuals.spawnSlashBridge(level, start, center, 0.55 * visualScale, player.getRandom());
         spawnPhantomSword(level, player, start, center, i);
      }

      AnnihilationVisuals.spawnBloodPrisonBurst(level, center, Math.max(1.2, target.getBbWidth() * config.burstRadiusScale.get()) * visualScale, player.getRandom());
      level.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.75F, 1.7F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.55F);
   }

   private static void spawnPhantomSword(ServerLevel level, Player player, Vec3 start, Vec3 end, int index) {
      EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(RegistryEvents.SummonedSword, level);
      sword.setOwner(player);
      sword.setShooter(player);
      sword.setColor(-57312);
      sword.setDamage(0.0);
      sword.setNoClip(true);
      sword.setPierce((byte)0);
      sword.setDelay(ModConfig.COMMON.bloodPrison.phantomBurst.swordDelayTicks.get() + index % 4);
      sword.setRoll(index * 36.0F);
      Vec3 direction = end.subtract(start).normalize();
      sword.setPos(start.x, start.y, start.z);
      sword.moveTo(start.x, start.y, start.z, yawToFace(direction), pitchToFace(direction));
      sword.shoot(direction.x, direction.y, direction.z, 2.45F, 0.0F);
      level.addFreshEntity(sword);
   }

   private static void removeDomain(Player player) {
      if (DOMAINS.remove(player.getUUID()) != null && player instanceof ServerPlayer serverPlayer) {
         ModNetwork.sendBloodPrisonDomain(serverPlayer, 0);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerLoggedOutEvent event) {
      clearPlayerState(event.getEntity());
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      clearPlayerState(event.getEntity());
   }

   private static void clearPlayerState(Player player) {
      UUID id = player.getUUID();
      HAS_BLOOD_PRISON_CACHE.remove(id);
      LAST_BLOOD_PRISON_SCAN_TICK.remove(id);
      BLOOD_SHIELDS.remove(id);
      DRAIN_WINDOWS.remove(id);
      MARKS.remove(id);
      removeDomain(player);
   }

   private static float yawToFace(Vec3 direction) {
      return (float)(Mth.atan2(direction.x, direction.z) * 180.0F / (float)Math.PI);
   }

   private static float pitchToFace(Vec3 direction) {
      double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
      return (float)(-Mth.atan2(direction.y, horizontal) * 180.0F / (float)Math.PI);
   }

   private static int visualCount(int base, double visualScale) {
      return Math.max(1, (int)Math.round(base * visualScale));
   }

   private static final class Domain {
      private final Vec3 center;
      private final long expiresAt;
      private float damageDealt;

      private Domain(Vec3 center, long expiresAt) {
         this.center = center;
         this.expiresAt = expiresAt;
      }
   }

   private static final class DrainWindow {
      private long startedAt;
      private float drained;

      private DrainWindow(long startedAt) {
         this.startedAt = startedAt;
      }
   }
}
