package dev.anticheatqa.module.qa;

import dev.anticheatqa.module.Category;
import dev.anticheatqa.module.Module;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_2338.class_2339;

public class SusChunkFinder extends Module {
   public static final List<SusChunk> flagged = new ArrayList<>();

   public final Module.NumberSetting chunkRadius = new Module.NumberSetting("Radius", 5.0, 1.0, 10.0, 1.0);
   public final Module.NumberSetting minY = new Module.NumberSetting("Min Y", 0.0, -64.0, 128.0, 8.0);
   public final Module.NumberSetting maxY = new Module.NumberSetting("Max Y", 96.0, 16.0, 320.0, 8.0);
   public final Module.NumberSetting chunksPerTick = new Module.NumberSetting("Chunks / Tick", 3.0, 1.0, 8.0, 1.0);

   public final Module.BooleanSetting detectCobbled = new Module.BooleanSetting("Cobbled Deepslate", true);
   public final Module.BooleanSetting detectRotated = new Module.BooleanSetting("Rotated Deepslate", true);
   public final Module.BooleanSetting detectDeepslate = new Module.BooleanSetting("Plain Deepslate", false);
   public final Module.BooleanSetting detectEndStone = new Module.BooleanSetting("End Stone", true);
   public final Module.BooleanSetting detectKelp = new Module.BooleanSetting("Fully Grown Kelp", true);
   public final Module.BooleanSetting detectAmethyst = new Module.BooleanSetting("Full Amethyst Cluster", true);

   public final Module.NumberSetting cobbledThreshold = new Module.NumberSetting("Cobbled Min", 3.0, 1.0, 30.0, 1.0);
   public final Module.NumberSetting rotatedThreshold = new Module.NumberSetting("Rotated Min", 2.0, 1.0, 20.0, 1.0);
   public final Module.NumberSetting deepslateThreshold = new Module.NumberSetting("Deepslate Min", 15.0, 1.0, 50.0, 1.0);
   public final Module.NumberSetting endStoneThreshold = new Module.NumberSetting("End Stone Min", 1.0, 1.0, 15.0, 1.0);
   public final Module.NumberSetting kelpThreshold = new Module.NumberSetting("Kelp Min", 3.0, 1.0, 40.0, 1.0);
   public final Module.NumberSetting amethystThreshold = new Module.NumberSetting("Amethyst Min", 2.0, 1.0, 30.0, 1.0);

   public final Module.BooleanSetting highlightBoxes = new Module.BooleanSetting("Yellow Square", true);
   public final Module.BooleanSetting tracers = new Module.BooleanSetting("Tracers", true);
   public final Module.BooleanSetting cornerBeams = new Module.BooleanSetting("Corner Beams", true);
   public final Module.NumberSetting boxHeight = new Module.NumberSetting("Box Height", 8.0, 2.0, 64.0, 1.0);
   public final Module.NumberSetting lineWidth = new Module.NumberSetting("Line Width", 2.0, 1.0, 6.0, 0.5);
   public final Module.ColorSetting colorLow = new Module.ColorSetting("Color Low", 0xFFFFFF00);
   public final Module.ColorSetting colorHigh = new Module.ColorSetting("Color High", 0xFFFF5500);
   public final Module.BooleanSetting notify = new Module.BooleanSetting("Chat Notify", true);

   private final Set<Long> scanned = new HashSet<>();
   private final LinkedHashSet<Long> pendingKeys = new LinkedHashSet<>();
   private final Set<Long> notified = new HashSet<>();
   private int ticker;

   public SusChunkFinder() {
      super("Sus Chunk Finder", "Flags chunks with deepslate / endstone / kelp / amethyst patterns.", Category.QA);
      settings.add(chunkRadius);
      settings.add(minY);
      settings.add(maxY);
      settings.add(chunksPerTick);
      settings.add(detectCobbled);
      settings.add(detectRotated);
      settings.add(detectDeepslate);
      settings.add(detectEndStone);
      settings.add(detectKelp);
      settings.add(detectAmethyst);
      settings.add(cobbledThreshold);
      settings.add(rotatedThreshold);
      settings.add(deepslateThreshold);
      settings.add(endStoneThreshold);
      settings.add(kelpThreshold);
      settings.add(amethystThreshold);
      settings.add(highlightBoxes);
      settings.add(tracers);
      settings.add(cornerBeams);
      settings.add(boxHeight);
      settings.add(lineWidth);
      settings.add(colorLow);
      settings.add(colorHigh);
      settings.add(notify);
   }

   @Override
   protected void onEnable() {
      flagged.clear();
      scanned.clear();
      pendingKeys.clear();
      notified.clear();
      ticker = 0;
   }

   @Override
   protected void onDisable() {
      flagged.clear();
      scanned.clear();
      pendingKeys.clear();
   }

   private static long key(int cx, int cz) {
      return ((long) cx << 32) ^ (cz & 0xffffffffL);
   }

   private static String blockPath(class_2680 st) {
      try {
         String id = st.method_26204().method_63499().toLowerCase();
         int dot = id.lastIndexOf('.');
         if (dot >= 0) id = id.substring(dot + 1);
         int colon = id.indexOf(':');
         if (colon >= 0) id = id.substring(colon + 1);
         return id;
      } catch (Throwable t) {
         return "";
      }
   }

   private static boolean isRotatedDeepslate(String path, class_2680 st) {
      if (!path.equals("deepslate")) return false;
      String s = st.toString().toLowerCase();
      return s.contains("axis=x") || s.contains("axis=z");
   }

   /** Fully grown kelp tip = age 25. */
   private static boolean isFullKelp(String path, class_2680 st) {
      if (!path.equals("kelp") && !path.equals("kelp_plant")) return false;
      if (path.equals("kelp_plant")) return false;
      String s = st.toString().toLowerCase();
      return s.contains("age=25");
   }

   private static boolean isFullAmethyst(String path) {
      return path.equals("amethyst_cluster");
   }

   @Override
   public void onTick(class_310 c) {
      try {
         if (c.field_1687 == null || c.field_1724 == null) {
            flagged.clear();
            return;
         }

         int r = Math.max(1, Math.min(10, chunkRadius.get().intValue()));
         int yMin = minY.get().intValue();
         int yMax = Math.max(yMin, maxY.get().intValue());
         int budget = Math.max(1, chunksPerTick.get().intValue());
         class_1923 o = c.field_1724.method_31476();
         var cm = c.field_1687.method_2935();

         for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
               int cx = o.field_9181 + dx;
               int cz = o.field_9180 + dz;
               long k = key(cx, cz);
               if (scanned.contains(k)) continue;
               if (cm.method_21730(cx, cz) == null) continue;
               pendingKeys.add(k);
            }
         }

         List<SusChunk> next = new ArrayList<>(flagged);
         int done = 0;
         class_2339 m = new class_2339();
         var it = pendingKeys.iterator();

         while (it.hasNext() && done < budget) {
            long k = it.next().longValue();
            it.remove();
            if (scanned.contains(k)) continue;
            scanned.add(k);
            done++;

            int cx = (int) (k >> 32);
            int cz = (int) k;
            class_2818 ch = cm.method_21730(cx, cz);
            if (ch == null) continue;

            int deep = 0, cobbled = 0, rotated = 0, endstone = 0, kelp = 0, amethyst = 0;
            int bx = cx << 4;
            int bz = cz << 4;

            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  for (int y = yMin; y <= yMax; y++) {
                     m.method_10103(bx + x, y, bz + z);
                     class_2680 st = ch.method_8320(m);
                     if (st.method_26215()) continue;
                     String path = blockPath(st);
                     if (path.isEmpty()) continue;

                     if (path.equals("cobbled_deepslate") || path.startsWith("cobbled_deepslate")) cobbled++;
                     else if (isRotatedDeepslate(path, st)) rotated++;
                     else if (path.equals("deepslate")) deep++;
                     else if (path.equals("end_stone")) endstone++;
                     else if (isFullKelp(path, st)) kelp++;
                     else if (isFullAmethyst(path)) amethyst++;
                  }
               }
            }

            boolean sus = false;
            int score = 0;
            StringBuilder reason = new StringBuilder();

            if (Boolean.TRUE.equals(detectCobbled.get()) && cobbled >= cobbledThreshold.get().intValue()) {
               sus = true; score += cobbled * 4; reason.append("Cobbled x").append(cobbled).append(" ");
            }
            if (Boolean.TRUE.equals(detectRotated.get()) && rotated >= rotatedThreshold.get().intValue()) {
               sus = true; score += rotated * 6; reason.append("Rotated x").append(rotated).append(" ");
            }
            if (Boolean.TRUE.equals(detectDeepslate.get()) && deep >= deepslateThreshold.get().intValue()) {
               sus = true; score += deep; reason.append("Deepslate x").append(deep).append(" ");
            }
            if (Boolean.TRUE.equals(detectEndStone.get()) && endstone >= endStoneThreshold.get().intValue()) {
               sus = true; score += endstone * 8; reason.append("EndStone x").append(endstone).append(" ");
            }
            if (Boolean.TRUE.equals(detectKelp.get()) && kelp >= kelpThreshold.get().intValue()) {
               sus = true; score += kelp * 3; reason.append("Kelp x").append(kelp).append(" ");
            }
            if (Boolean.TRUE.equals(detectAmethyst.get()) && amethyst >= amethystThreshold.get().intValue()) {
               sus = true; score += amethyst * 5; reason.append("Amethyst x").append(amethyst).append(" ");
            }

            if (sus) {
               SusChunk sc = new SusChunk(cx, cz, deep, cobbled, rotated, endstone, amethyst, Math.max(1, score));
               sc.reason = reason.toString().trim();
               next.removeIf(s -> s.chunkX == cx && s.chunkZ == cz);
               next.add(sc);
               if (Boolean.TRUE.equals(notify.get()) && notified.add(k)) {
                  try {
                     c.field_1724.method_7353(
                        net.minecraft.class_2561.method_43470(
                           "\u00a7e[Sus] " + cx + ", " + cz + " \u00a77" + sc.reason
                        ),
                        false
                     );
                  } catch (Throwable ignored) {
                  }
               }
            }
         }

         int keep = r + 3;
         next.removeIf(s -> Math.abs(s.chunkX - o.field_9181) > keep || Math.abs(s.chunkZ - o.field_9180) > keep);
         if (++ticker % 80 == 0) {
            scanned.removeIf((Long box) -> {
               long k0 = box;
               int scx = (int) (k0 >> 32);
               int scz = (int) k0;
               return Math.abs(scx - o.field_9181) > r + 2 || Math.abs(scz - o.field_9180) > r + 2;
            });
         }

         next.sort(Comparator.comparingInt((SusChunk s) -> s.score).reversed());
         flagged.clear();
         flagged.addAll(next);
      } catch (Throwable ignored) {
      }
   }

   @Override
   public String getDisplay() {
      return isEnabled() ? "Sus:" + flagged.size() : "";
   }

   public static final class SusChunk {
      public final int chunkX, chunkZ, diamonds, debris, ores, spawners, amethyst, score;
      public String reason = "";

      public SusChunk(int chunkX, int chunkZ, int diamonds, int debris, int ores, int spawners, int amethyst, int score) {
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
         this.diamonds = diamonds;
         this.debris = debris;
         this.ores = ores;
         this.spawners = spawners;
         this.amethyst = amethyst;
         this.score = score;
      }
   }
}
