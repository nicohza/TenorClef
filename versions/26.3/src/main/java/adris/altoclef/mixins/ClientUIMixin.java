package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import adris.altoclef.multiversion.DrawContextWrapper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 260000
//$$ @Mixin(net.minecraft.client.gui.Hud.class)
//#else
@Mixin(InGameHud.class)
//#endif
public final class ClientUIMixin {
    @Inject(
            //#if MC >= 260000
            //$$ method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            //#else
            method = "render",
            //#endif
            at = @At("TAIL")
    )
    //#if MC >= 260000
    //$$ private void clientRender(net.minecraft.client.gui.GuiGraphicsExtractor context, net.minecraft.client.DeltaTracker tickCounter, CallbackInfo ci) {
    //$$     EventBus.publish(new ClientRenderEvent(DrawContextWrapper.of(context), tickCounter.getGameTimeDeltaPartialTick(true)));
    //$$ }
    //#elseif MC >= 12100
    private void clientRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        EventBus.publish(new ClientRenderEvent(DrawContextWrapper.of(context), tickCounter.getTickDelta(true)));
    }
    //#else
    //#if MC >= 12001
    //$$ private void clientRender(DrawContext obj, float tickDelta, CallbackInfo ci) {
    //#else
    //$$ private void clientRender(MatrixStack obj, float tickDelta, CallbackInfo ci) {
    //#endif
    //$$    EventBus.publish(new ClientRenderEvent(DrawContextWrapper.of(obj), tickDelta));
    //$$ }
    //#endif


}
