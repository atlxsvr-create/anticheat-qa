package dev.anticheatqa.module.player;

import dev.anticheatqa.module.Category;
import dev.anticheatqa.module.Module;
import net.minecraft.class_310;

/**
 * Flight + No Clip.
 *
 * On servers WITHOUT anti-cheat / movement checks (singleplayer, LAN,
 * creative, or permissive cracked hosts), this works smoothly.
 * Vanilla survival multiplayer still validates position server-side and
 * may rubberband even with no AC plugin — that is Minecraft, not a bug
 * in this module.
 */
public class Flight extends Module {
   public final Module.NumberSetting speed = new Module.NumberSetting("Speed", 1.5, 0.1, 10.0, 0.1);
   public final Module.NumberSetting vertical = new Module.NumberSetting("Vertical", 1.2, 0.1, 10.0, 0.1);
   public final Module.BooleanSetting noClip = new Module.BooleanSetting("No Clip", true);
   public final Module.BooleanSetting smooth = new Module.BooleanSetting("Smooth", true);
   public final Module.BooleanSetting resetOnDisable = new Module.BooleanSetting("Reset On Disable", true);

   private boolean prevFlying;
   private boolean prevAllowFlying;
   private float prevFlySpeed;

   public Flight() {
      super("Flight", "Fly + no-clip. Best on no-AC / singleplayer / creative.", Category.PLAYER);
      settings.add(speed);
      settings.add(vertical);
      settings.add(noClip);
      settings.add(smooth);
      settings.add(resetOnDisable);
   }

   @Override
   protected void onEnable() {
      try {
         class_310 c = class_310.method_1551();
         if (c.field_1724 == null) return;
         var ab = c.field_1724.method_31549();
         prevFlying = ab.field_7479;
         prevAllowFlying = ab.field_7478;
         prevFlySpeed = ab.method_7252();
         ab.field_7478 = true;
         ab.field_7479 = true;
         ab.method_7248(0.05F * speed.get().floatValue());
         if (Boolean.TRUE.equals(noClip.get())) {
            c.field_1724.field_5960 = true;
         }
      } catch (Throwable ignored) {
      }
   }

   @Override
   protected void onDisable() {
      try {
         class_310 c = class_310.method_1551();
         if (c.field_1724 == null) return;
         c.field_1724.field_5960 = false;
         if (Boolean.TRUE.equals(resetOnDisable.get())) {
            var ab = c.field_1724.method_31549();
            ab.field_7479 = prevFlying;
            ab.field_7478 = prevAllowFlying;
            ab.method_7248(prevFlySpeed);
            c.field_1724.method_18800(0.0, 0.0, 0.0);
         }
      } catch (Throwable ignored) {
      }
   }

   @Override
   public void onTick(class_310 c) {
      try {
         if (c.field_1724 == null || c.field_1687 == null) return;

         var ab = c.field_1724.method_31549();
         ab.field_7478 = true;
         ab.field_7479 = true;
         ab.method_7248(0.05F * Math.max(0.1F, speed.get().floatValue()));

         // No-clip must be re-applied every tick (vanilla clears it)
         c.field_1724.field_5960 = Boolean.TRUE.equals(noClip.get());

         // Don't fight GUIs
         if (c.field_1755 != null) {
            c.field_1724.method_18800(0.0, 0.0, 0.0);
            return;
         }

         float spd = speed.get().floatValue();
         float vspd = vertical.get().floatValue();

         float forward = 0.0F;
         float strafe = 0.0F;
         if (c.field_1690.field_1894.method_1434()) forward += 1.0F;
         if (c.field_1690.field_1881.method_1434()) forward -= 1.0F;
         if (c.field_1690.field_1913.method_1434()) strafe += 1.0F;
         if (c.field_1690.field_1849.method_1434()) strafe -= 1.0F;

         double mx = 0.0;
         double mz = 0.0;
         if (forward != 0.0F || strafe != 0.0F) {
            float len = (float) Math.sqrt(forward * forward + strafe * strafe);
            forward /= len;
            strafe /= len;
            double yaw = Math.toRadians(c.field_1724.method_36454());
            mx = (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * spd;
            mz = (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * spd;
         }

         double my = 0.0;
         if (c.field_1690.field_1903.method_1434()) my += vspd;
         if (c.field_1690.field_1832.method_1434()) my -= vspd;

         if (Boolean.TRUE.equals(smooth.get()) && mx == 0.0 && mz == 0.0 && my == 0.0) {
            // hover — kill residual velocity so you don't drift
            c.field_1724.method_18800(0.0, 0.0, 0.0);
         } else {
            c.field_1724.method_18800(mx, my, mz);
         }

         c.field_1724.field_6017 = 0.0F; // fall distance
         c.field_1724.method_24830(false); // onGround false while flying
      } catch (Throwable ignored) {
      }
   }

   @Override
   public String getDisplay() {
      if (!isEnabled()) return "";
      return Boolean.TRUE.equals(noClip.get()) ? "Fly:NoClip" : "Fly";
   }
}
