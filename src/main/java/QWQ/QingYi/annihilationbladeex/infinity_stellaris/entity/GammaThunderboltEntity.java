package QWQ.QingYi.annihilationbladeex.infinity_stellaris.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class GammaThunderboltEntity extends Entity {
   public long seed;
   private int life;
   private int flashes;

   public GammaThunderboltEntity(EntityType<? extends GammaThunderboltEntity> entityType, Level level) {
      super(entityType, level);
      this.noCulling = true;
      this.life = 2;
      this.seed = this.random.nextLong();
      this.flashes = this.random.nextInt(3) + 1;
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.WEATHER;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.life == 2 && this.level().isClientSide()) {
         this.level()
            .playLocalSound(
               this.getX(),
               this.getY(),
               this.getZ(),
               SoundEvents.LIGHTNING_BOLT_THUNDER,
               SoundSource.WEATHER,
               10000.0F,
               0.8F + this.random.nextFloat() * 0.2F,
               false
            );
         this.level()
            .playLocalSound(
               this.getX(),
               this.getY(),
               this.getZ(),
               SoundEvents.LIGHTNING_BOLT_IMPACT,
               SoundSource.WEATHER,
               2.0F,
               0.5F + this.random.nextFloat() * 0.2F,
               false
            );
      }

      this.life--;
      if (this.life < 0) {
         if (this.flashes == 0) {
            this.discard();
         } else if (this.life < -this.random.nextInt(10)) {
            this.flashes--;
            this.life = 1;
            this.seed = this.random.nextLong();
         }
      }

      if (this.life >= 0 && this.level().isClientSide()) {
         this.level().setSkyFlashTime(2);
      }
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      double viewDistance = 64.0D * getViewScale();
      return distance < viewDistance * viewDistance;
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity trackerEntry) {
      return super.getAddEntityPacket(trackerEntry);
   }
}
