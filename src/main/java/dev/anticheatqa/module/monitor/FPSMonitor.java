package dev.anticheatqa.module.monitor;

import dev.anticheatqa.module.Category;
import dev.anticheatqa.module.Module;
import net.minecraft.client.MinecraftClient;

public class FPSMonitor extends Module {
    public FPSMonitor() {
        super("FPS Monitor", "Displays current FPS.", Category.MONITOR);
    }

    @Override
    public String getDisplay() {
        return "FPS: " + MinecraftClient.getInstance().getCurrentFps();
    }
}
