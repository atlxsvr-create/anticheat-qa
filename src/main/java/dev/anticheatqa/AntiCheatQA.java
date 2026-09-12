/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.minecraft.class_310
 *  net.minecraft.class_2960
 *  org.lwjgl.glfw.GLFW
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
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
import net.minecraft.class_310;
import net.minecraft.class_2960;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntiCheatQA
implements ClientModInitializer {
    public static final String MOD_ID = "fortyone-client";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)MOD_ID);
    public static AntiCheatQA INSTANCE;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private HudRenderer hudRenderer;
    private boolean lastRightShift = false;

    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("41 Client initializing (1.21.11)");
        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager();
        this.hudRenderer = new HudRenderer();
        this.configManager.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.field_1724 == null) {
                return;
            }
            long window = client.method_22683().method_4490();
            boolean pressed = GLFW.glfwGetKey((long)window, (int)344) == 1;
            if (pressed && !this.lastRightShift) {
                if (client.field_1755 instanceof ClickGuiScreen) {
                    client.method_1507(null);
                } else if (client.field_1755 == null) {
                    client.method_1507((net.minecraft.class_437)new ClickGuiScreen());
                }
            }
            this.lastRightShift = pressed;
            this.moduleManager.onTick(client);
        });
        class_2960 layerId = class_2960.method_60654((String)MOD_ID, (String)"overlay");
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, layerId, (context, tickCounter) -> {
            if (INSTANCE != null && class_310.method_1551().field_1724 != null) {
                this.hudRenderer.render(context, 0.0f);
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(WorldEspRenderer::render);
        LOGGER.info("41 Client ready. Press Right Shift to open ClickGUI.");
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public HudRenderer getHudRenderer() {
        return this.hudRenderer;
    }
}
