package adris.altoclef.mixins;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileEntityAccessor {
    // 1.21.2 replaced the inGround field with an isInGround() method.
    //#if MC >= 12102
    //$$ @Invoker("isInGround")
    //#else
    @Accessor("inGround")
    //#endif
    boolean altoclef$isInGround();
}
