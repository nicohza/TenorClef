package adris.altoclef.mixins;

//#if MC < 260000
import net.minecraft.item.AxeItem;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

//#if MC >= 260000
//$$ @Mixin(net.minecraft.world.item.Item.class)
//#else
@Mixin(AxeItem.class)
//#endif
public interface AxeItemAccessor {

    //#if MC <= 11605
    //$$ @Accessor("field_23139")
    //$$ Set<net.minecraft.block.Material> getEffectiveMaterials();
    //#endif

}
