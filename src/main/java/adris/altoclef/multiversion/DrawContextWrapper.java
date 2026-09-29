package adris.altoclef.multiversion;

import adris.altoclef.mixins.DrawableHelperInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

public class DrawContextWrapper {


    //#if MC >= 12001
    public static DrawContextWrapper of(DrawContext context) {
        if (context == null) return null;
        return new DrawContextWrapper(context);
    }
    private final DrawContext context;

    private DrawContextWrapper(DrawContext context) {
        this.context = context;
    }
    //#else
    //$$ public static DrawContextWrapper of(MatrixStack matrices) {
    //$$    if (matrices == null) return null;
    //$$    return new DrawContextWrapper(matrices);
    //$$ }
    //$$
    //$$ private final MatrixStack matrices;
    //$$ private final DrawableHelper helper;
    //$$ private DrawContextWrapper(MatrixStack matrices) {
    //$$        this.matrices = matrices;
    //$$        this.helper = new DrawableHelper(){};
    //$$ }
    //#endif

    private RenderLayer renderLayer = null;

    // used only 1.20.1 and later... can pass null in earlier versions
    public void setRenderLayer(RenderLayer renderLayer) {
        this.renderLayer = renderLayer;
    }

    //#if MC >= 12001 && MC < 12111
    // Newer versions NPE on a null layer; default to the standard GUI layer.
    // (1.21.11 drew GUI quads through RenderPipelines; DrawContext no longer takes a layer.)
    private RenderLayer layer() {
        return renderLayer != null ? renderLayer : RenderLayer.getGui();
    }
    //#endif

    public void fill(int x1, int y1, int x2, int y2, int color) {
        //#if MC >= 12111
        //$$ context.fill(x1, y1, x2, y2, color);
        //#elseif MC >= 12001
        context.fill(layer(), x1, y1, x2, y2, color);
        //#else
        //$$  DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
        //#endif
    }

    public void drawHorizontalLine(int x1, int x2, int y, int color) {
        //#if MC >= 12111
        //$$ context.drawHorizontalLine(x1, x2, y, color);
        //#elseif MC >= 12001
        context.drawHorizontalLine(layer(), x1, x2, y, color);
        //#else
        //$$ ((DrawableHelperInvoker) helper).invokeDrawHorizontalLine(matrices, x1, x2, y, color);
        //#endif
    }

    public void drawVerticalLine(int x, int y1, int y2, int color) {
        //#if MC >= 12111
        //$$ context.drawVerticalLine(x, y1, y2, color);
        //#elseif MC >= 12001
        context.drawVerticalLine(layer(), x, y1, y2, color);
        //#else
        //$$ ((DrawableHelperInvoker) helper).invokeDrawVerticalLine(matrices, x, y1, y2, color);
        //#endif
    }

    public void drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow) {
        //#if MC >= 12001
        context.drawText(textRenderer,text,x,y,color,shadow);
        //#else
        //$$ if (shadow) {
        //$$    textRenderer.drawWithShadow(matrices, text,x,y,color);
        //$$ } else {
        //$$    textRenderer.draw(matrices, text,x,y,color);
        //$$ }
        //#endif
    }


    // 1.21.11's GUI matrix stack is a 2D joml Matrix3x2fStack, so callers use the helpers below instead.
    //#if MC < 12111
    public MatrixStack getMatrices() {
        //#if MC >= 12001
        return context.getMatrices();
        //#else
        //$$ return matrices;
        //#endif
    }
    //#endif

    public void pushMatrix() {
        //#if MC >= 12111
        //$$ context.getMatrices().pushMatrix();
        //#else
        getMatrices().push();
        //#endif
    }

    public void popMatrix() {
        //#if MC >= 12111
        //$$ context.getMatrices().popMatrix();
        //#else
        getMatrices().pop();
        //#endif
    }

    /** Scales the GUI matrix; z is ignored on versions with a 2D matrix stack. */
    public void scale(float x, float y, float z) {
        //#if MC >= 12111
        //$$ context.getMatrices().scale(x, y);
        //#else
        getMatrices().scale(x, y, z);
        //#endif
    }

    public int getScaledWindowWidth() {
        //#if MC >= 12001
        return context.getScaledWindowWidth();
        //#else
        //$$ return MinecraftClient.getInstance().getWindow().getScaledWidth();
        //#endif
    }

    public int getScaledWindowHeight() {
        //#if MC >= 12001
        return context.getScaledWindowHeight();
        //#else
        //$$ return MinecraftClient.getInstance().getWindow().getScaledHeight();
        //#endif
    }


}
