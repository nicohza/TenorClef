package adris.altoclef.multiversion;

//#if MC < 260000
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
//#else
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.ChunkPos;
//#endif

/** 26.x turned ChunkPos into a record with x()/z() accessors and no BlockPos constructor. */
public class ChunkPosVer {

    public static int x(ChunkPos p) {
        //#if MC >= 260000
        //$$ return p.x();
        //#else
        return p.x;
        //#endif
    }

    public static int z(ChunkPos p) {
        //#if MC >= 260000
        //$$ return p.z();
        //#else
        return p.z;
        //#endif
    }

    public static ChunkPos of(BlockPos pos) {
        //#if MC >= 260000
        //$$ return ChunkPos.containing(pos);
        //#else
        return new ChunkPos(pos);
        //#endif
    }
}
