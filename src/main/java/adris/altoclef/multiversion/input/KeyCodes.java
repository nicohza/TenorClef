package adris.altoclef.multiversion.input;

/** Key codes: GLFW's before 26.x, which dropped GLFW for SDL and renumbered every key. */
public class KeyCodes {

    //#if MC >= 260000
    //$$ public static final int LEFT_CONTROL = com.mojang.blaze3d.platform.InputConstants.KEY_LCONTROL;
    //$$ public static final int RIGHT_CONTROL = com.mojang.blaze3d.platform.InputConstants.KEY_RCONTROL;
    //$$ public static final int LEFT_SHIFT = com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT;
    //$$ public static final int RIGHT_SHIFT = com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT;
    //$$ public static final int GRAVE = com.mojang.blaze3d.platform.InputConstants.KEY_GRAVE;
    //$$ public static final int K = com.mojang.blaze3d.platform.InputConstants.KEY_K;
    //$$ public static final int M = com.mojang.blaze3d.platform.InputConstants.KEY_M;
    //$$ public static final int O = com.mojang.blaze3d.platform.InputConstants.KEY_O;
    //$$ public static final int F8 = com.mojang.blaze3d.platform.InputConstants.KEY_F8;
    //$$ public static final int F9 = com.mojang.blaze3d.platform.InputConstants.KEY_F9;
    //#else
    public static final int LEFT_CONTROL = org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
    public static final int RIGHT_CONTROL = org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
    public static final int LEFT_SHIFT = org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
    public static final int RIGHT_SHIFT = org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
    public static final int GRAVE = org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT;
    public static final int K = org.lwjgl.glfw.GLFW.GLFW_KEY_K;
    public static final int M = org.lwjgl.glfw.GLFW.GLFW_KEY_M;
    public static final int O = org.lwjgl.glfw.GLFW.GLFW_KEY_O;
    public static final int F8 = org.lwjgl.glfw.GLFW.GLFW_KEY_F8;
    public static final int F9 = org.lwjgl.glfw.GLFW.GLFW_KEY_F9;
    //#endif
}
