package QWQ.QingYi.annihilationblade.common;

import QWQ.QingYi.annihilationblade.Annihilationblade;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public final class ServerTickScheduler {
   private static final List<ServerTickScheduler.DelayedTask> TASKS = new ArrayList<>();
   private static final List<ServerTickScheduler.DelayedTask> PENDING_ADDS = new ArrayList<>();
   private static boolean iterating;

   private ServerTickScheduler() {
   }

   public static void schedule(int ticks, Runnable action) {
      if (action != null) {
         ServerTickScheduler.DelayedTask task = new ServerTickScheduler.DelayedTask(Math.max(0, ticks), action);
         if (iterating) {
            PENDING_ADDS.add(task);
         } else {
            TASKS.add(task);
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         iterating = true;
         Iterator<ServerTickScheduler.DelayedTask> iterator = TASKS.iterator();

         while (iterator.hasNext()) {
            ServerTickScheduler.DelayedTask task = iterator.next();
            task.remainingTicks--;
            if (task.remainingTicks <= 0) {
               try {
                  task.action.run();
               } catch (RuntimeException exception) {
                  Annihilationblade.LOGGER.warn("Failed to run delayed AnnihilationBlade task", exception);
               }

               iterator.remove();
            }
         }

         iterating = false;
         if (!PENDING_ADDS.isEmpty()) {
            TASKS.addAll(PENDING_ADDS);
            PENDING_ADDS.clear();
         }
      }
   }

   private static final class DelayedTask {
      private int remainingTicks;
      private final Runnable action;

      private DelayedTask(int remainingTicks, Runnable action) {
         this.remainingTicks = remainingTicks;
         this.action = action;
      }
   }
}
