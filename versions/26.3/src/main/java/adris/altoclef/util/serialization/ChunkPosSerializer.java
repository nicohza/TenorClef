package adris.altoclef.util.serialization;

import adris.altoclef.multiversion.ChunkPosVer;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Collection;

public class ChunkPosSerializer extends AbstractVectorSerializer<ChunkPos> {
    @Override
    protected Collection<String> getParts(ChunkPos value) {
        return Arrays.asList("" + ChunkPosVer.x(value), "" + ChunkPosVer.z(value));
    }
}
