package adris.altoclef.multiversion.entity;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/** PlayerInventory went private / equipment-based in 1.21.5+; 1.21.11 is the first node that needs these. */
public class InventoryVer {

    @Pattern
    private static int getSelectedSlot(PlayerInventory inv) {
        //#if MC >= 12111
        //$$ return inv.getSelectedSlot();
        //#else
        return inv.selectedSlot;
        //#endif
    }

    @Pattern
    private static void setSelectedSlot(PlayerInventory inv, int slot) {
        //#if MC >= 12111
        //$$ inv.setSelectedSlot(slot);
        //#else
        inv.selectedSlot = slot;
        //#endif
    }

    @Pattern
    private static DefaultedList<ItemStack> getMain(PlayerInventory inv) {
        //#if MC >= 12111
        //$$ return inv.getMainStacks();
        //#else
        return inv.main;
        //#endif
    }

    @Pattern
    private static DefaultedList<ItemStack> getArmor(PlayerInventory inv) {
        //#if MC >= 12111
        //$$ return adris.altoclef.multiversion.entity.InventoryHelper.armorStacks(inv);
        //#else
        return inv.armor;
        //#endif
    }

    @Pattern
    private static DefaultedList<ItemStack> getOffHand(PlayerInventory inv) {
        //#if MC >= 12111
        //$$ return adris.altoclef.multiversion.entity.InventoryHelper.offHandStacks(inv);
        //#else
        return inv.offHand;
        //#endif
    }

    @Pattern
    private static ItemStack getArmorStack(PlayerInventory inv, int index) {
        //#if MC >= 12111
        //$$ return adris.altoclef.multiversion.entity.InventoryHelper.armorStack(inv, index);
        //#else
        return inv.getArmorStack(index);
        //#endif
    }
}
