package QWQ.QingYi.annihilationbladeex.infinity_stellaris.logic;

import QWQ.QingYi.annihilationbladeex.common.ServerTickScheduler;
import QWQ.QingYi.annihilationbladeex.infinity_stellaris.entity.GammaThunderboltEntity;
import QWQ.QingYi.annihilationbladeex.infinity_stellaris.item.InfinityStellarisItemSupport;
import QWQ.QingYi.annihilationbladeex.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationbladeex")
public final class GammaThunderburstLogic {
   private static final int ROUNDS = 3;
   private static final int BOLTS_PER_ROUND = 12;
   private static final int RADIUS = 128;

   private GammaThunderburstLogic() {
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onHurt(LivingIncomingDamageEvent event) {
      if (event.isCanceled() || event.getEntity().level().isClientSide || EntropyDissolutionLogic.isInternalExecution(event.getEntity())) {
         return;
      }

      Entity source = event.getSource().getEntity();
      Entity directSource = event.getSource().getDirectEntity();
      if (source instanceof Player player && !(directSource instanceof GammaThunderboltEntity) && EntropyDissolutionLogic.isInfinityDamage(player, event.getSource(), directSource)) {
         trigger(player);
      }
   }

   public static void trigger(Player player) {
      if (!(player.level() instanceof ServerLevel level) || !InfinityStellarisItemSupport.isHoldingInfinityStellaris(player)) {
         return;
      }

      for (int round = 0; round < ROUNDS; round++) {
         int delay = round;
         ServerTickScheduler.schedule(delay, () -> spawnRound(level, player));
      }
   }

   private static void spawnRound(ServerLevel level, Player player) {
      if (!player.isAlive()) {
         return;
      }

      RandomSource random = player.getRandom();
      Vec3 origin = player.position();
      level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.8F, 1.4F + random.nextFloat() * 0.4F);
      spawnRandomBoltsAround(level, origin, RADIUS, BOLTS_PER_ROUND, random);
   }

   public static void spawnRandomBoltsAround(ServerLevel level, Vec3 origin, int radius, int count, RandomSource random) {
      for (int i = 0; i < count; i++) {
         int x = (int)Math.floor(origin.x) + random.nextInt(radius * 2 + 1) - radius;
         int z = (int)Math.floor(origin.z) + random.nextInt(radius * 2 + 1) - radius;
         spawnSurfaceBolt(level, x, z, origin.y);
      }
   }

   public static void spawnRandomBoltsInSquare(ServerLevel level, Vec3 center, double halfSize, int count, RandomSource random) {
      int bound = Math.max(1, (int)Math.round(halfSize));
      for (int i = 0; i < count; i++) {
         int x = (int)Math.floor(center.x) + random.nextInt(bound * 2 + 1) - bound;
         int z = (int)Math.floor(center.z) + random.nextInt(bound * 2 + 1) - bound;
         spawnSurfaceBolt(level, x, z, center.y);
      }
   }

   public static void spawnBolt(ServerLevel level, Vec3 position) {
      GammaThunderboltEntity bolt = ModEntities.GAMMA_THUNDERBOLT.get().create(level);
      if (bolt != null) {
         bolt.moveTo(position.x, position.y, position.z);
         level.addFreshEntity(bolt);
      }
   }

   private static void spawnSurfaceBolt(ServerLevel level, int x, int z, double fallbackY) {
      int fallbackBlockY = Math.max(level.getMinBuildHeight() + 1, Math.min(level.getMaxBuildHeight() - 2, (int)Math.floor(fallbackY)));
      BlockPos loadCheck = new BlockPos(x, fallbackBlockY, z);
      if (!level.isLoaded(loadCheck)) {
         return;
      }

      int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
      if (y <= level.getMinBuildHeight()) {
         y = fallbackBlockY;
      }

      y = Math.max(level.getMinBuildHeight() + 1, Math.min(level.getMaxBuildHeight() - 2, y));
      spawnBolt(level, new Vec3(x + 0.5, y, z + 0.5));
   }
}
