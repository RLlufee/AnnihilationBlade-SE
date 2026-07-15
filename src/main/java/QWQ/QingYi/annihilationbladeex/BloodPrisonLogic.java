package QWQ.QingYi.annihilationbladeex;

import mods.flammpfeil.slashblade.RegistryEvents;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import QWQ.QingYi.annihilationbladeex.network.ModNetwork;
import QWQ.QingYi.annihilationbladeex.visual.AnnihilationVisuals;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 血狱的被动、SE 与 SA 结算；所有目标均先通过 SlashBlade 原生配置判定。 */
@EventBusSubscriber(modid = AnnihilationBladeEX.MODID)
public final class BloodPrisonLogic {
    public static final String BLOOD_PRISON_TRANSLATION_KEY = "item.annihilationbladeex.blood_prison";
    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID = AnnihilationBladeEX.prefix("blood_prison_max_health");
    private static final double MAX_HEALTH_BONUS = 40.0D;
    private static final float MIN_HEALTH = 8.0F;
    private static final long DOMAIN_DURATION = 20L * 20L;
    private static final int SHIELD_BUFF_TICKS = 20 * 5;
    private static final Map<UUID, DrainWindow> DRAIN_WINDOWS = new HashMap<>();
    private static final Map<UUID, Float> BLOOD_SHIELDS = new HashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> MARKS = new HashMap<>();
    private static final Map<UUID, Domain> DOMAINS = new HashMap<>();
    private static final Set<UUID> PHANTOM_BURST_TARGETS = new HashSet<>();

    private BloodPrisonLogic() {
    }

    public static boolean isBloodPrison(ItemStack stack) {
        return !stack.isEmpty() && BloodPrisonDefinitions.DESCRIPTION_ID.equals(stack.getDescriptionId());
    }

    public static boolean hasBloodPrison(Player player) {
        if (isBloodPrison(player.getMainHandItem()) || isBloodPrison(player.getOffhandItem())) return true;
        for (ItemStack stack : player.getInventory().items) {
            if (isBloodPrison(stack)) return true;
        }
        return false;
    }

    private static void refreshBloodPrisonStats(Player player) {
        if (isBloodPrison(player.getMainHandItem())) BloodPrisonDefinitions.ensureStats(player.getMainHandItem(), player);
        if (isBloodPrison(player.getOffhandItem())) BloodPrisonDefinitions.ensureStats(player.getOffhandItem(), player);
        for (ItemStack stack : player.getInventory().items) {
            if (isBloodPrison(stack)) {
                BloodPrisonDefinitions.ensureStats(stack, player);
            }
        }
    }

    public static void activateDomain(Player player) {
        if (!(player.level() instanceof ServerLevel level) || !isBloodPrison(player.getMainHandItem())) return;
        BloodPrisonDefinitions.ensureStats(player.getMainHandItem(), player);
        DOMAINS.put(player.getUUID(), new Domain(player.position(), level.getGameTime() + DOMAIN_DURATION));
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.sendBloodPrisonDomain(serverPlayer, (int) DOMAIN_DURATION);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.4F, 0.55F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.55F, 1.45F);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 0.15D, player.getZ(), 120, 3.0D, 0.15D, 3.0D, 0.03D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 0.2D, player.getZ(), 180, 8.0D, 0.25D, 8.0D, 0.08D);
        AnnihilationVisuals.spawnBloodPrisonDomainPulse(level, player.position(), 10.0D);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        Player player = event.getEntity();
        if (hasBloodPrison(player) && player.tickCount % 20 == 0) {
            refreshBloodPrisonStats(player);
        }
        updateMaxHealth(player);
        updateShield(player);
        tickDomain(player);
    }

    @SubscribeEvent
    public static void onAttack(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !isBloodPrison(player.getMainHandItem())) return;
        if (!SlashBladeTargeting.canAttack(player, event.getEntity())) return;
        drainBloodForSwing(player);
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !isBloodPrison(player.getMainHandItem())) return;
        if (event.getSource().getDirectEntity() instanceof EntityAbstractSummonedSword) return;
        LivingEntity target = event.getEntity();
        if (!SlashBladeTargeting.canAttack(player, target)) return;

        float missing = Math.max(0.0F, player.getMaxHealth() - player.getHealth());
        float damageBonus = (float) Math.floor(missing / 2.0F) * (player.getMaxHealth() * 0.001F);
        event.setNewDamage(event.getNewDamage() + damageBonus);

        Domain domain = DOMAINS.get(player.getUUID());
        if (domain != null) domain.damageDealt += event.getNewDamage();
        if (PHANTOM_BURST_TARGETS.contains(target.getUUID())) return;

        Map<UUID, Integer> playerMarks = MARKS.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
        int marks = playerMarks.getOrDefault(target.getUUID(), 0) + 1;
        if (marks < 10) {
            playerMarks.put(target.getUUID(), marks);
            return;
        }

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

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            removeDomain(player);
            return;
        }
        if (event.getSource().getEntity() instanceof Player player && isBloodPrison(player.getMainHandItem())) {
            player.heal(event.getEntity().getMaxHealth() * 0.10F);
        }
    }

    @SubscribeEvent
    public static void onSlash(SlashBladeEvent.DoSlashEvent event) {
        if (event.getUser() instanceof Player player && DOMAINS.containsKey(player.getUUID())) {
            performDomainAttack(player);
        }
    }

    private static void updateMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        AttributeModifier modifier = attribute.getModifier(MAX_HEALTH_MODIFIER_ID);
        if (hasBloodPrison(player)) {
            if (modifier == null) attribute.addTransientModifier(new AttributeModifier(MAX_HEALTH_MODIFIER_ID, MAX_HEALTH_BONUS, AttributeModifier.Operation.ADD_VALUE));
        } else if (modifier != null) {
            attribute.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }
    }

    private static void updateShield(Player player) {
        UUID id = player.getUUID();
        float shield = player.getMaxHealth() * 0.20F;
        if (hasBloodPrison(player) && player.getHealth() <= MIN_HEALTH) {
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
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, SHIELD_BUFF_TICKS, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, SHIELD_BUFF_TICKS, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SHIELD_BUFF_TICKS, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, SHIELD_BUFF_TICKS, 1, false, true, true));
    }

    private static void drainBloodForSwing(Player player) {
        long time = player.level().getGameTime();
        DrainWindow window = DRAIN_WINDOWS.computeIfAbsent(player.getUUID(), ignored -> new DrainWindow(time));
        if (time - window.startedAt >= 10L) {
            window.startedAt = time;
            window.drained = 0.0F;
        }
        float amount = Math.min(4.0F, 10.0F - window.drained);
        float actual = Math.min(amount, Math.max(0.0F, player.getHealth() - MIN_HEALTH));
        if (actual > 0.0F) {
            player.setHealth(player.getHealth() - actual);
            window.drained += actual;
        }
    }

    private static void tickDomain(Player player) {
        Domain domain = DOMAINS.get(player.getUUID());
        if (domain == null) return;
        ServerLevel level = (ServerLevel) player.level();
        if (!player.isAlive()) {
            removeDomain(player);
            return;
        }
        if (level.getGameTime() >= domain.expiresAt) {
            player.heal(domain.damageDealt * 0.20F);
            removeDomain(player);
            return;
        }
        if (level.getGameTime() % 10L == 0L) {
            for (int i = -10; i <= 10; i += 2) {
                level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x + i, domain.center.y + 0.1D, domain.center.z - 10, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x + i, domain.center.y + 0.1D, domain.center.z + 10, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x - 10, domain.center.y + 0.1D, domain.center.z + i, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.DRAGON_BREATH, domain.center.x + 10, domain.center.y + 0.1D, domain.center.z + i, 1, 0, 0, 0, 0);
            }
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, domain.center.x, domain.center.y + 0.15D, domain.center.z, 18, 6.5D, 0.05D, 6.5D, 0.015D);
        }
        if (level.getGameTime() % 4L == 0L) {
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0D, player.getZ(), 8, 1.2D, 0.5D, 1.2D, 0.05D);
        }
        if (level.getGameTime() % 20L == 0L) {
            AnnihilationVisuals.spawnBloodPrisonDomainPulse(level, domain.center, 10.0D);
        }
    }

    private static void performDomainAttack(Player player) {
        Domain domain = DOMAINS.get(player.getUUID());
        if (domain == null || !(player.level() instanceof ServerLevel level)) return;
        List<LivingEntity> targets = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
                new AABB(domain.center, domain.center).inflate(10.0D), target -> SlashBladeTargeting.canAttack(player, target)));
        if (targets.isEmpty()) return;
        LivingEntity target = targets.get(player.getRandom().nextInt(targets.size()));
        Vec3 from = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        player.teleportTo(target.getX() + Math.cos(angle) * 1.5D, target.getY(), target.getZ() + Math.sin(angle) * 1.5D);
        target.hurt(level.damageSources().indirectMagic(player, player), 20.0F);
        domain.damageDealt += 20.0F;
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.75F);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(), 3, 0.45D, 0.45D, 0.45D, 0.0D);
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(), 24, 0.75D, 0.75D, 0.75D, 0.12D);
        Vec3 hit = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);
        AnnihilationVisuals.spawnBloodPrisonDash(level, from, hit, player.getRandom());
    }

    private static void spawnPhantomSwordBurst(ServerLevel level, Player player, LivingEntity target) {
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * 2.0D * i / 10.0D;
            Vec3 start = center.add(Math.cos(angle) * 4.0D, 3.0D + (i % 3), Math.sin(angle) * 4.0D);
            AnnihilationVisuals.spawnSlashBridge(level, start, center, 0.55D, player.getRandom());
            spawnPhantomSword(level, player, start, center, i);
        }
        AnnihilationVisuals.spawnBloodPrisonBurst(level, center, Math.max(1.2D, target.getBbWidth() * 2.2D), player.getRandom());
        level.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.75F, 1.7F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 0.55F);
    }

    private static void spawnPhantomSword(ServerLevel level, Player player, Vec3 start, Vec3 end, int index) {
        EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(RegistryEvents.SummonedSword, level);
        sword.setOwner(player);
        sword.setShooter(player);
        sword.setColor(0xFFFF2020);
        sword.setDamage(0.0D);
        sword.setNoClip(true);
        sword.setPierce((byte) 0);
        sword.setDelay(18 + index % 4);
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

    private static float yawToFace(Vec3 direction) {
        return (float) (Mth.atan2(direction.x, direction.z) * Mth.RAD_TO_DEG);
    }

    private static float pitchToFace(Vec3 direction) {
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        return (float) (-Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG);
    }

    private static final class DrainWindow {
        private long startedAt;
        private float drained;
        private DrainWindow(long startedAt) { this.startedAt = startedAt; }
    }

    private static final class Domain {
        private final Vec3 center;
        private final long expiresAt;
        private float damageDealt;
        private Domain(Vec3 center, long expiresAt) { this.center = center; this.expiresAt = expiresAt; }
    }
}
