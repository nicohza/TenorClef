package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/** 26.x made Minecraft.screen private; read it through the screen() accessor. */
public class ScreenVer {

    public static Screen current(MinecraftClient mc) {
        //#if MC >= 260000
        //$$ return mc.gui.screen();
        //#else
        return mc.currentScreen;
        //#endif
    }
}
