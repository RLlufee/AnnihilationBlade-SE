package QWQ.QingYi.annihilationbladeex.common;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.item.ItemAnnihilationBlade;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.AnnihilationBladeDefinitions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpecialEffectSupport {
   private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
   private static final Vec3 FORWARD = new Vec3(0.0, 0.0, 1.0);
   private static final Vec3 RIGHT = new Vec3(1.0, 0.0, 0.0);

   private SpecialEffectSupport() {
   }

   public static boolean tryStartCooldown(Map<UUID, Long> cooldowns, Player player, long gameTime, int cooldownTicks) {
      long last = cooldowns.getOrDefault(player.getUUID(), -cooldownTicks * 2L);
      if (gameTime - last < cooldownTicks) {
         return false;
      }

      cooldowns.put(player.getUUID(), gameTime);
      return true;
   }

   public static boolean canTarget(Player player, LivingEntity candidate) {
      return SlashBladeTargeting.canAttack(player, candidate);
   }

   public static boolean hasAnnihilationBlade(Player player) {
      if (!isAnnihilationBlade(player.getMainHandItem()) && !isAnnihilationBlade(player.getOffhandItem())) {
         for (ItemStack stack : player.getInventory().items) {
            if (isAnnihilationBlade(stack)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean isAnnihilationBlade(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (stack.getItem() instanceof ItemAnnihilationBlade) {
         return true;
      } else {
         return "item.annihilationbladeex.annihilation_blade".equals(stack.getDescriptionId())
            ? true
            : BladeStateAccess.getData(stack).map(data -> AnnihilationBladeDefinitions.DESCRIPTION_ID.equals(data.translationKey())).orElse(false);
      }
   }

   public static LivingEntity findLivingEntity(ServerLevel level, UUID uuid) {
      return level.getEntity(uuid) instanceof LivingEntity living ? living : null;
   }

   public static Vec3 centerOf(LivingEntity entity) {
      return entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
   }

   public static Vec3 rightOf(Vec3 direction) {
      Vec3 right = safeNormalize(direction, FORWARD).cross(UP);
      return safeNormalize(right, RIGHT);
   }

   public static List<LivingEntity> radialTargets(ServerLevel level, Player player, Vec3 center, double radius) {
      return radialTargets(level, player, center, radius, entity -> true);
   }

   public static List<LivingEntity> radialTargets(ServerLevel level, Player player, Vec3 center, double radius, Predicate<LivingEntity> extra) {
      double radiusSqr = radius * radius;
      AABB area = new AABB(center, center).inflate(radius);
      List<LivingEntity> targets = level.getEntitiesOfClass(
         LivingEntity.class, area, entity -> canTarget(player, entity) && extra.test(entity) && centerOf(entity).distanceToSqr(center) <= radiusSqr
      );
      targets.sort(Comparator.comparingDouble(entity -> centerOf(entity).distanceToSqr(center)));
      return targets;
   }

   public static List<LivingEntity> limit(List<LivingEntity> targets, int maxTargets) {
      return targets.size() <= maxTargets ? targets : new ArrayList<>(targets.subList(0, maxTargets));
   }

   public static List<LivingEntity> nearestChain(ServerLevel level, Player player, LivingEntity firstTarget, double radius, int maxTargets) {
      List<LivingEntity> chain = new ArrayList<>();
      Set<UUID> used = new HashSet<>();
      LivingEntity current = firstTarget;

      while (current != null && chain.size() < maxTargets) {
         chain.add(current);
         used.add(current.getUUID());
         Vec3 currentCenter = centerOf(current);
         List<LivingEntity> candidates = radialTargets(level, player, currentCenter, radius, entity -> !used.contains(entity.getUUID()));
         current = candidates.isEmpty() ? null : candidates.get(0);
      }

      return chain;
   }

   public static List<LivingEntity> beamTargets(ServerLevel level, Player player, Vec3 start, Vec3 direction, double range, double width, int maxTargets) {
      Vec3 forward = safeNormalize(direction, FORWARD);
      Vec3 end = start.add(forward.scale(range));
      AABB area = new AABB(start, end).inflate(width, width * 0.8, width);
      List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, area, entity -> canTarget(player, entity));
      candidates.sort(Comparator.comparingDouble(entity -> centerOf(entity).subtract(start).dot(forward)));
      List<LivingEntity> result = new ArrayList<>();

      for (LivingEntity target : candidates) {
         Vec3 targetCenter = centerOf(target);
         double projection = targetCenter.subtract(start).dot(forward);
         if (!(projection < 0.0) && !(projection > range)) {
            Vec3 nearest = start.add(forward.scale(projection));
            double allowedWidth = width + projection / range * 4.0;
            if (!(targetCenter.distanceToSqr(nearest) > allowedWidth * allowedWidth)) {
               result.add(target);
               if (result.size() >= maxTargets) {
                  break;
               }
            }
         }
      }

      return result;
   }

   public static void pullToward(LivingEntity target, Vec3 point, double strength) {
      Vec3 center = centerOf(target);
      Vec3 delta = point.subtract(center);
      if (!(delta.lengthSqr() < 1.0E-4)) {
         Vec3 pull = delta.normalize().scale(strength);
         target.push(pull.x, Math.max(0.04, pull.y * 0.25 + 0.04), pull.z);
         target.hasImpulse = true;
      }
   }

   public static double distanceToBoxSqr(Vec3 point, AABB box) {
      double dx = distanceToAxis(point.x, box.minX, box.maxX);
      double dy = distanceToAxis(point.y, box.minY, box.maxY);
      double dz = distanceToAxis(point.z, box.minZ, box.maxZ);
      return dx * dx + dy * dy + dz * dz;
   }

   private static double distanceToAxis(double value, double min, double max) {
      if (value < min) {
         return min - value;
      } else {
         return value > max ? value - max : 0.0;
      }
   }

   private static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
      return vector.lengthSqr() < 1.0E-6 ? fallback : vector.normalize();
   }
}
