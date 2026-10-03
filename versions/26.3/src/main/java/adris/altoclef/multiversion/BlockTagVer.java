package adris.altoclef.multiversion;

import net.minecraft.block.Block;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.Registries;

public class BlockTagVer {


    public static boolean isWool(Block block) {
        //#if MC >= 11802
        //#if MC >= 260000
        //$$ return block.defaultBlockState().is(net.minecraft.tags.BlockTags.WOOL);
        //#elseif MC >= 12102
        //$$ return block.getDefaultState().isIn(BlockTags.WOOL);
        //#else
        return Registries.BLOCK.getKey(block).map(e -> Registries.BLOCK.entryOf(e).streamTags().anyMatch(t -> t == BlockTags.WOOL)).orElse(false);
        //#endif
        //#else
        //$$ return BlockTags.WOOL.contains(block);
        //#endif
    }

}
