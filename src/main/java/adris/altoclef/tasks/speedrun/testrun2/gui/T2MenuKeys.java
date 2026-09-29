package adris.altoclef.tasks.speedrun.testrun2.gui;

import adris.altoclef.multiversion.ScreenVer;

import net.minecraft.client.MinecraftClient;
import adris.altoclef.util.helpers.InputHelper;

/**
 * Poll the configured key. Hooked from {@code AltoClef.onClientTick}.
 */
public final class T2MenuKeys {

    private static boolean wasDown;

    private T2MenuKeys() {}

    public static void tick() {
        T2MenuScreen.poll();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        if (ScreenVer.current(mc) != null) {
            wasDown = false;
            return;
        }
        int key = AgentConfig.cached().glfwKey();
        boolean down = InputHelper.isKeyPressed(key);
        if (down && !wasDown) {
            T2MenuScreen.open();
        }
        wasDown = down;
    }
}
