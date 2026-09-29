package adris.altoclef.multiversion.blockpos;

import adris.altoclef.multiversion.Pattern;
//#if MC < 260000
import net.minecraft.util.math.*;
//#else
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.util.Mth;
//$$ import net.minecraft.core.Position;
//$$ import net.minecraft.core.Vec3i;
//#endif
import adris.altoclef.multiversion.blockpos.BlockPosHelper;

public class BlockPosVer {


    public static BlockPos ofFloored(Position pos) {
        return new BlockPos(MathHelper.floor(pos.getX()), MathHelper.floor(pos.getY()), MathHelper.floor(pos.getZ()));
    }


    /** 26.x removed the BlockPos(Vec3i) constructor. */
    public static BlockPos copyOf(Vec3i v) {
        return new BlockPos(v.getX(), v.getY(), v.getZ());
    }

    /** Vec3i.isWithinDistance(Vec3i, double); 26.x splits it by argument type, so compute it directly. */
    public static boolean isWithinDistance(Vec3i a, Vec3i b, double distance) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz < distance * distance;
    }

    public static double getSquaredDistance(BlockPos pos, Position obj) {
        //#if MC >= 260000
        //$$ return pos.distToCenterSqr(obj);
        //#elseif MC >= 11802
        return pos.getSquaredDistance(obj);
        //#else
        //$$ return pos.getSquaredDistance(obj.getX(), obj.getY(), obj.getZ(), true);
        //#endif
    }

    @Pattern
    private static Vec3i north(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.north();
        //#else
        //$$ return blockPos.offset(Direction.NORTH, 1);
        //#endif
    }

    @Pattern
    private static Vec3i north(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.north(amount);
        //#else
        //$$ return blockPos.offset(Direction.NORTH, amount);
        //#endif
    }

    @Pattern
    private static Vec3i east(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.east();
        //#else
        //$$ return blockPos.offset(Direction.EAST, 1);
        //#endif
    }

    @Pattern
    private static Vec3i east(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.east(amount);
        //#else
        //$$ return blockPos.offset(Direction.EAST, amount);
        //#endif
    }

    @Pattern
    private static Vec3i west(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.west();
        //#else
        //$$ return blockPos.offset(Direction.WEST, 1);
        //#endif
    }

    @Pattern
    private static Vec3i west(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.west(amount);
        //#else
        //$$ return blockPos.offset(Direction.WEST, amount);
        //#endif
    }

    @Pattern
    private static Vec3i south(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.south();
        //#else
        //$$ return blockPos.offset(Direction.SOUTH, 1);
        //#endif
    }

    @Pattern
    private static Vec3i south(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.south(amount);
        //#else
        //$$ return blockPos.offset(Direction.SOUTH, amount);
        //#endif
    }

    @Pattern
    private static Vec3i add(Vec3i blockPos, int x, int y, int z) {
        //#if MC >= 11701
        return blockPos.add(x,y,z);
        //#else
        //$$ return adris.altoclef.multiversion.blockpos.BlockPosHelper.add(blockPos,x,y,z);
        //#endif
    }


}
