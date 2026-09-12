package dev.anticheatqa;

import dev.anticheatqa.config.ConfigManager;
import dev.anticheatqa.gui.ClickGuiScreen;
import dev.anticheatqa.module.ModuleManager;
import dev.anticheatqa.render.HudRenderer;
import dev.anticheatqa.render.WorldEspRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntiCheatQA implements ClientModInitializer {
    public static final String MOD_ID = "anticheat-qa";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static AntiCheatQA INSTANCE;

    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private HudRenderer hudRenderer;
    private boolean lastRightShift = false;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("AntiCheat QA Client initializing (1.21.11)");

        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        hudRenderer = new HudRenderer();
        configManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            long window = client.getWindow().getHandle();
            boolean pressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (pressed && !lastRightShift) {
                if (client.currentScreen instanceof ClickGuiScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            lastRightShift = pressed;

            moduleManager.onTick(client);
        });

        Identifier layerId = Identifier.of(MOD_ID, "overlay");
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                layerId,
                (context, tickCounter) -> {
                    if (INSTANCE != null && MinecraftClient.getInstance().player != null) {
                        hudRenderer.render(context, 0f);
                    }
                }
        );

        // 3D world-space ESP outlines + tracers
        WorldRenderEvents.AFTER_ENTITIES.register(WorldEspRenderer::render);

        LOGGER.info("AntiCheat QA ready. Press Right Shift to open ClickGUI.");
    }

    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public HudRenderer getHudRenderer() { return hudRenderer; }
}
