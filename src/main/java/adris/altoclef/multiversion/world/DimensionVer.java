package adris.altoclef.multiversion.world;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.world.World;

/** 1.21.11 moved DimensionType's ultrawarm/natural flags into environment attributes; use the dimension key.
 *
 * The replacement is pasted inline at call sites (often under a '!'), so it is a single method call with
 * fully-qualified names: no operator-precedence surprises and no import needed in the calling file. */
public class DimensionVer {

    @Pattern
    private static boolean isUltrawarm(World world) {
        //#if MC >= 12111
        //$$ return world.getRegistryKey().equals(net.minecraft.world.World.NETHER);
        //#else
        return world.getDimension().ultrawarm();
        //#endif
    }

    @Pattern
    private static boolean isNatural(World world) {
        //#if MC >= 12111
        //$$ return world.getRegistryKey().equals(net.minecraft.world.World.OVERWORLD);
        //#else
        return world.getDimension().natural();
        //#endif
    }
}
