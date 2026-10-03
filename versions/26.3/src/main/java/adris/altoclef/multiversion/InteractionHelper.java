package adris.altoclef.multiversion;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/** 26.x requires the hit result when interacting with an entity. */
public class InteractionHelper {

    public static ActionResult interactEntity(ClientPlayerInteractionManager interactionManager, PlayerEntity player, Entity entity, Hand hand) {
        //#if MC >= 260000
        //$$ return interactionManager.interact(player, entity, new net.minecraft.world.phys.EntityHitResult(entity), hand);
        //#else
        return interactionManager.interactEntity(player, entity, hand);
        //#endif
    }
}
