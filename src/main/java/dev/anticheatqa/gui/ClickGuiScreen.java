package dev.anticheatqa.gui;

import dev.anticheatqa.AntiCheatQA;
import dev.anticheatqa.module.Category;
import dev.anticheatqa.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * ClientCraft ClickGUI — black / purple theme with top-tab layout.
 */
public class ClickGuiScreen extends Screen {
    private static final int BG_DIM       = 0xE0080810;
    private static final int PANEL_BG     = 0xFF0C0C14;
    private static final int PANEL_EDGE   = 0xFF1A1228;
    private static final int HEADER_BG    = 0xFF100818;
    private static final int ACCENT       = 0xFFB24BFF;
    private static final int ACCENT_DIM   = 0xFF6B2FA0;
    private static final int ACCENT_SOFT  = 0x33B24BFF;
    private static final int ROW_ON       = 0xFF1A0F2A;
    private static final int ROW_HOVER    = 0x22FFFFFF;
    private static final int TEXT_BRIGHT  = 0xFFFFFFFF;
    private static final int TEXT_MUTED   = 0xFF9A8AB0;
    private static final int TEXT_DIM     = 0xFF5A4A70;
    private static final int TAB_IDLE     = 0xFF16101F;
    private static final int TAB_ACTIVE   = 0xFF2A1840;
    private static final int BTN_SAVE     = 0xFF2A1840;
    private static final int BTN_RESET    = 0xFF2A1020;

    private Category selectedCategory = Category.VISUALS;
    private Module settingsModule = null;
    private boolean waitingForKeybind = false;
    private int scrollOffset = 0;

    private static final int CARD_W = 420;
    private static final int CARD_H = 320;
    private static final int TAB_H = 22;
    private static final int ROW_H = 18;

    public ClickGuiScreen() {
        super(Text.literal("ClientCraft"));
    }

    private int cardX() { return (width - CARD_W) / 2; }
    private int cardY() { return (height - CARD_H) / 2 - 10; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, BG_DIM);

        int cx = cardX();
        int cy = cardY();

        context.fill(cx - 1, cy - 1, cx + CARD_W + 1, cy + CARD_H + 1, PANEL_EDGE);
        context.fill(cx, cy, cx + CARD_W, cy + CARD_H, PANEL_BG);

        context.fill(cx, cy, cx + CARD_W, cy + 28, HEADER_BG);
        context.fill(cx, cy + 28, cx + CARD_W, cy + 30, ACCENT);

        context.drawText(textRenderer, "ClientCraft", cx + 12, cy + 9, ACCENT, true);
        context.drawText(textRenderer, "v2.9", cx + CARD_W - 36, cy + 10, TEXT_DIM, false);

        int tabY = cy + 36;
        int tabX = cx + 10;
        Category[] cats = Category.values();
        int tabW = Math.max(48, (CARD_W - 20) / Math.max(1, cats.length) - 4);

        for (Category cat : cats) {
            boolean sel = cat == selectedCategory;
            boolean hover = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= tabY && mouseY < tabY + TAB_H;

            context.fill(tabX, tabY, tabX + tabW, tabY + TAB_H, sel ? TAB_ACTIVE : TAB_IDLE);
            if (sel) {
                context.fill(tabX, tabY + TAB_H - 2, tabX + tabW, tabY + TAB_H, ACCENT);
            } else if (hover) {
                context.fill(tabX, tabY, tabX + tabW, tabY + TAB_H, ROW_HOVER);
            }

            int tw = textRenderer.getWidth(cat.displayName);
            int tx = tabX + (tabW - tw) / 2;
            context.drawText(textRenderer, cat.displayName, tx, tabY + 7,
                    sel ? ACCENT : TEXT_MUTED, false);
            tabX += tabW + 4;
        }

        int listX = cx + 10;
        int listY = cy + 66;
        int listW = settingsModule != null ? 220 : CARD_W - 20;
        int listBottom = cy + CARD_H - 28;

        context.fill(listX, listY, listX + listW, listBottom, 0xFF0A0A12);
        context.drawText(textRenderer, "L-Click toggle  \u00b7  R-Click settings",
                listX + 4, listY - 12, TEXT_DIM, false);

        if (AntiCheatQA.INSTANCE != null) {
            List<Module> mods = AntiCheatQA.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory);
            int rowY = listY + 4 - scrollOffset;
            for (Module m : mods) {
                if (rowY + ROW_H > listY && rowY < listBottom - 2) {
                    boolean on = m.isEnabled();
                    boolean hover = mouseX >= listX && mouseX < listX + listW
                            && mouseY >= rowY && mouseY < rowY + ROW_H;

                    if (on) context.fill(listX + 2, rowY, listX + listW - 2, rowY + ROW_H - 1, ROW_ON);
                    if (hover) context.fill(listX + 2, rowY, listX + listW - 2, rowY + ROW_H - 1, ROW_HOVER);

                    int dot = on ? ACCENT : 0xFF3A2A4A;
                    context.fill(listX + 8, rowY + 6, listX + 14, rowY + 12, dot);

                    context.drawText(textRenderer, m.getName(), listX + 20, rowY + 5,
                            on ? TEXT_BRIGHT : TEXT_MUTED, false);

                    String kb = keyName(m.getKeybind());
                    if (!kb.isEmpty()) {
                        int kx = listX + listW - 8 - textRenderer.getWidth(kb);
                        context.drawText(textRenderer, kb, kx, rowY + 5, TEXT_DIM, false);
                    }
                }
                rowY += ROW_H;
            }
        }

        if (settingsModule != null) {
            int sx = listX + listW + 8;
            int sw = cx + CARD_W - 10 - sx;
            context.fill(sx, listY, sx + sw, listBottom, 0xFF0A0A12);
            context.fill(sx, listY, sx + 2, listBottom, ACCENT_DIM);

            context.drawText(textRenderer, settingsModule.getName(), sx + 8, listY + 6, ACCENT, true);
            context.drawText(textRenderer, settingsModule.getDescription(), sx + 8, listY + 20, TEXT_DIM, false);

            int sy = listY + 40;

            context.drawText(textRenderer, "Keybind", sx + 8, sy, TEXT_MUTED, false);
            String kbText = waitingForKeybind ? "..." : keyName(settingsModule.getKeybind());
            if (kbText.isEmpty()) kbText = "None";
            int kbBoxX = sx + sw - 70;
            context.fill(kbBoxX, sy - 2, sx + sw - 8, sy + 12,
                    waitingForKeybind ? ACCENT_SOFT : 0xFF16101F);
            context.drawText(textRenderer, kbText, kbBoxX + 4, sy, TEXT_BRIGHT, false);
            sy += 20;

            for (Module.Setting<?> s : settingsModule.getSettings()) {
                context.drawText(textRenderer, s.getName(), sx + 8, sy, TEXT_MUTED, false);
                if (s instanceof Module.BooleanSetting bs) {
                    String v = bs.get() ? "ON" : "OFF";
                    int vc = bs.get() ? ACCENT : TEXT_DIM;
                    context.drawText(textRenderer, v, sx + sw - 8 - textRenderer.getWidth(v), sy, vc, false);
                } else if (s instanceof Module.NumberSetting ns) {
                    String v = String.format("%.2f", ns.get());
                    context.drawText(textRenderer, v, sx + sw - 40 - textRenderer.getWidth(v), sy, TEXT_BRIGHT, false);
                    context.drawText(textRenderer, "+/-", sx + sw - 28, sy, TEXT_DIM, false);
                } else if (s instanceof Module.ColorSetting cs) {
                    String v = String.format("#%08X", cs.get());
                    context.drawText(textRenderer, v, sx + sw - 8 - textRenderer.getWidth(v), sy, TEXT_BRIGHT, false);
                }
                sy += 16;
            }

            sy += 8;
            context.fill(sx + 8, sy, sx + 70, sy + 16, BTN_SAVE);
            context.drawText(textRenderer, "Save", sx + 28, sy + 4, ACCENT, false);
            context.fill(sx + 78, sy, sx + 148, sy + 16, BTN_RESET);
            context.drawText(textRenderer, "Reset", sx + 96, sy + 4, 0xFFFF6688, false);
        }

        context.fill(cx, cy + CARD_H - 22, cx + CARD_W, cy + CARD_H, HEADER_BG);
        context.drawText(textRenderer, "Right Shift  \u00b7  close", cx + 12, cy + CARD_H - 15, TEXT_DIM, false);
        context.drawText(textRenderer, "ClientCraft", cx + CARD_W - 70, cy + CARD_H - 15, ACCENT_DIM, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();
        int button = click.button();

        int cx = cardX();
        int cy = cardY();

        int tabY = cy + 36;
        int tabX = cx + 10;
        Category[] cats = Category.values();
        int tabW = Math.max(48, (CARD_W - 20) / Math.max(1, cats.length) - 4);
        for (Category cat : cats) {
            if (mx >= tabX && mx < tabX + tabW && my >= tabY && my < tabY + TAB_H) {
                selectedCategory = cat;
                settingsModule = null;
                waitingForKeybind = false;
                scrollOffset = 0;
                return true;
            }
            tabX += tabW + 4;
        }

        if (AntiCheatQA.INSTANCE == null) return false;

        int listX = cx + 10;
        int listY = cy + 66;
        int listW = settingsModule != null ? 220 : CARD_W - 20;
        int listBottom = cy + CARD_H - 28;

        List<Module> mods = AntiCheatQA.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory);
        int rowY = listY + 4 - scrollOffset;
        for (Module m : mods) {
            if (mx >= listX && mx < listX + listW && my >= rowY && my < rowY + ROW_H
                    && rowY >= listY && rowY < listBottom) {
                if (button == 0) {
                    m.toggle();
                    AntiCheatQA.INSTANCE.getConfigManager().save();
                    return true;
                }
                if (button == 1) {
                    settingsModule = m;
                    waitingForKeybind = false;
                    return true;
                }
            }
            rowY += ROW_H;
        }

        if (settingsModule != null) {
            int sx = listX + listW + 8;
            int sw = cx + CARD_W - 10 - sx;
            int sy = listY + 40;

            int kbBoxX = sx + sw - 70;
            if (my >= sy - 2 && my < sy + 14 && mx >= kbBoxX && mx < sx + sw - 8) {
                waitingForKeybind = true;
                return true;
            }
            sy += 20;

            for (Module.Setting<?> s : settingsModule.getSettings()) {
                if (my >= sy && my < sy + 16 && mx >= sx && mx < sx + sw) {
                    if (s instanceof Module.BooleanSetting bs) {
                        bs.set(!bs.get());
                        AntiCheatQA.INSTANCE.getConfigManager().save();
                        return true;
                    }
                    if (s instanceof Module.NumberSetting ns) {
                        double step = ns.getStep();
                        if (button == 0) ns.setClamped(ns.get() + step);
                        else ns.setClamped(ns.get() - step);
                        AntiCheatQA.INSTANCE.getConfigManager().save();
                        return true;
                    }
                }
                sy += 16;
            }

            sy += 8;
            if (my >= sy && my < sy + 16) {
                if (mx >= sx + 8 && mx < sx + 70) {
                    AntiCheatQA.INSTANCE.getConfigManager().save();
                    return true;
                }
                if (mx >= sx + 78 && mx < sx + 148) {
                    for (Module.Setting<?> s : settingsModule.getSettings()) s.reset();
                    settingsModule.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
                    AntiCheatQA.INSTANCE.getConfigManager().save();
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, scrollOffset - (int) (verticalAmount * ROW_H));
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();

        if (waitingForKeybind && settingsModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                settingsModule.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
            } else if (keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT) {
                settingsModule.setKeybind(keyCode);
            }
            waitingForKeybind = false;
            if (AntiCheatQA.INSTANCE != null) {
                AntiCheatQA.INSTANCE.getConfigManager().save();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (AntiCheatQA.INSTANCE != null) {
            AntiCheatQA.INSTANCE.getConfigManager().save();
        }
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static String keyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN || key < 0) return "";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            default -> "KEY" + key;
        };
    }
}
