package adris.altoclef.tasks.speedrun.testrun2;

import adris.altoclef.multiversion.CItems;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Set;

/**
 * Items the run must not throw, lava, or deposit.
 * Wire into your fork's throwaway classifier if it has one
 * (often `ItemHelper.canThrowAway` / `isProtected`).
 */
public final class KeepList {

    private KeepList() {}

    public static final Set<Item> KEEP = Set.of(
            Items.IRON_PICKAXE, Items.IRON_SWORD, Items.IRON_AXE,
            Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.STONE_SWORD,
            Items.SHIELD,
            Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET,
            Items.FLINT_AND_STEEL, Items.FIRE_CHARGE, Items.FLINT,
            Items.IRON_INGOT, Items.RAW_IRON,
            Items.GOLD_INGOT, Items.GOLD_BLOCK, Items.GOLD_NUGGET,
            Items.OBSIDIAN, Items.CRYING_OBSIDIAN,
            Items.ENDER_EYE, Items.ENDER_PEARL, Items.BLAZE_ROD, Items.BLAZE_POWDER,
            Items.CRAFTING_TABLE, Items.FURNACE,
            CItems.WHITE_BED, CItems.ORANGE_BED, CItems.MAGENTA_BED, CItems.LIGHT_BLUE_BED,
            CItems.YELLOW_BED, CItems.LIME_BED, CItems.PINK_BED, CItems.GRAY_BED,
            CItems.LIGHT_GRAY_BED, CItems.CYAN_BED, CItems.PURPLE_BED, CItems.BLUE_BED,
            CItems.BROWN_BED, CItems.GREEN_BED, CItems.RED_BED, CItems.BLACK_BED,
            Items.OAK_BOAT, Items.BIRCH_BOAT, Items.SPRUCE_BOAT,
            Items.COBBLESTONE, Items.NETHERRACK,
            Items.BREAD, Items.COOKED_PORKCHOP, Items.COOKED_BEEF, Items.GOLDEN_CARROT,
            Items.ENDER_CHEST, Items.DIAMOND, Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE
    );

    public static boolean keep(Item item) {
        return item != null && KEEP.contains(item);
    }
}
