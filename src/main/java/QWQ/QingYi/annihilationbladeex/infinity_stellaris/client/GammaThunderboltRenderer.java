package QWQ.QingYi.annihilationbladeex.infinity_stellaris.client;

import QWQ.QingYi.annihilationbladeex.infinity_stellaris.entity.GammaThunderboltEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

public class GammaThunderboltRenderer extends EntityRenderer<GammaThunderboltEntity> {
   public GammaThunderboltRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   @Override
   public void render(GammaThunderboltEntity bolt, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      float[] xOffsets = new float[8];
      float[] zOffsets = new float[8];
      float x = 0.0F;
      float z = 0.0F;
      RandomSource offsetRandom = RandomSource.create(bolt.seed);

      for (int index = 7; index >= 0; --index) {
         xOffsets[index] = x;
         zOffsets[index] = z;
         x += (float)(offsetRandom.nextInt(11) - 5);
         z += (float)(offsetRandom.nextInt(11) - 5);
      }

      VertexConsumer buffer = bufferSource.getBuffer(RenderType.lightning());
      Matrix4f matrix = poseStack.last().pose();
      RandomSource colorRandom = RandomSource.create(bolt.seed ^ 0x6D9F_2B31_5A77L);
      for (int layer = 0; layer < 4; ++layer) {
         RandomSource branchRandom = RandomSource.create(bolt.seed);
         for (int branch = 0; branch < 3; ++branch) {
            int start = 7;
            int end = 0;
            if (branch > 0) {
               start = 7 - branch;
               end = start - 2;
            }

            float currentX = xOffsets[start] - x;
            float currentZ = zOffsets[start] - z;
            for (int segment = start; segment >= end; --segment) {
               float previousX = currentX;
               float previousZ = currentZ;
               if (branch == 0) {
                  currentX += (float)(branchRandom.nextInt(11) - 5);
                  currentZ += (float)(branchRandom.nextInt(11) - 5);
               } else {
                  currentX += (float)(branchRandom.nextInt(31) - 15);
                  currentZ += (float)(branchRandom.nextInt(31) - 15);
               }

               float inner = 0.1F + (float)layer * 0.2F;
               if (branch == 0) {
                  inner *= (float)segment * 0.1F + 1.0F;
               }

               float outer = 0.1F + (float)layer * 0.2F;
               if (branch == 0) {
                  outer *= ((float)segment - 1.0F) * 0.1F + 1.0F;
               }

               float red = colorRandom.nextFloat();
               float green = colorRandom.nextFloat();
               float blue = colorRandom.nextFloat();
               float alpha = 0.48F;
               quad(matrix, buffer, currentX, currentZ, segment, previousX, previousZ, red, green, blue, alpha, inner, outer, false, false, true, false);
               quad(matrix, buffer, currentX, currentZ, segment, previousX, previousZ, red, green, blue, alpha, inner, outer, true, false, true, true);
               quad(matrix, buffer, currentX, currentZ, segment, previousX, previousZ, red, green, blue, alpha, inner, outer, true, true, false, true);
               quad(matrix, buffer, currentX, currentZ, segment, previousX, previousZ, red, green, blue, alpha, inner, outer, false, true, false, false);
            }
         }
      }
   }

   private static void quad(
      Matrix4f matrix,
      VertexConsumer consumer,
      float x,
      float z,
      int segment,
      float previousX,
      float previousZ,
      float red,
      float green,
      float blue,
      float alpha,
      float inner,
      float outer,
      boolean xPositive,
      boolean zPositive,
      boolean previousXPositive,
      boolean previousZPositive
   ) {
      consumer.addVertex(matrix, x + (xPositive ? outer : -outer), (float)(segment * 16), z + (zPositive ? outer : -outer)).setColor((int)(red * 255), (int)(green * 255), (int)(blue * 255), (int)(alpha * 255));
      consumer.addVertex(matrix, previousX + (xPositive ? inner : -inner), (float)((segment + 1) * 16), previousZ + (zPositive ? inner : -inner)).setColor((int)(red * 255), (int)(green * 255), (int)(blue * 255), (int)(alpha * 255));
      consumer.addVertex(matrix, previousX + (previousXPositive ? inner : -inner), (float)((segment + 1) * 16), previousZ + (previousZPositive ? inner : -inner)).setColor((int)(red * 255), (int)(green * 255), (int)(blue * 255), (int)(alpha * 255));
      consumer.addVertex(matrix, x + (previousXPositive ? outer : -outer), (float)(segment * 16), z + (previousZPositive ? outer : -outer)).setColor((int)(red * 255), (int)(green * 255), (int)(blue * 255), (int)(alpha * 255));
   }

   @Override
   public ResourceLocation getTextureLocation(GammaThunderboltEntity bolt) {
      return InventoryMenu.BLOCK_ATLAS;
   }
}
