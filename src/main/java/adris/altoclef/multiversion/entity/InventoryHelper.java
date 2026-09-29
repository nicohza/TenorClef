package adris.altoclef.multiversion.entity;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.11 removed PlayerInventory's armor/offHand lists and LivingEntity's hand/armor/equipped item iterators.
 * These helpers rebuild the same read-only views from the equipment slots; older versions never call the
 * 1.21.11 branches (see InventoryVer / LivingEntityVer patterns).
 */
public class InventoryHelper {

    /** Armor slots in PlayerInventory order: 0 = boots .. 3 = helmet (inventory slots 36..39). */
    public static DefaultedList<ItemStack> armorStacks(PlayerInventory inv) {
        //#if MC >= 12111
        //$$ DefaultedList<ItemStack> list = DefaultedList.ofSize(4, ItemStack.EMPTY);
        //$$ for (int i = 0; i < 4; i++) {
        //$$     list.set(i, inv.getStack(36 + i));
        //$$ }
        //$$ return list;
        //#else
        return inv.armor;
        //#endif
    }

    public static ItemStack armorStack(PlayerInventory inv, int index) {
        //#if MC >= 12111
        //$$ return inv.getStack(36 + index);
        //#else
        return inv.getArmorStack(index);
        //#endif
    }

    public static DefaultedList<ItemStack> offHandStacks(PlayerInventory inv) {
        //#if MC >= 12111
        //$$ DefaultedList<ItemStack> list = DefaultedList.ofSize(1, ItemStack.EMPTY);
        //$$ list.set(0, inv.getStack(PlayerInventory.OFF_HAND_SLOT));
        //$$ return list;
        //#else
        return inv.offHand;
        //#endif
    }

    public static Iterable<ItemStack> handItems(LivingEntity entity) {
        //#if MC >= 12111
        //$$ return slots(entity, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND);
        //#else
        return entity.getHandItems();
        //#endif
    }

    public static Iterable<ItemStack> armorItems(LivingEntity entity) {
        //#if MC >= 12111
        //$$ return slots(entity, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
        //#else
        return entity.getArmorItems();
        //#endif
    }

    public static Iterable<ItemStack> equippedItems(LivingEntity entity) {
        //#if MC >= 12111
        //$$ return slots(entity, EquipmentSlot.values());
        //#else
        return armorItems(entity);
        //#endif
    }

    private static List<ItemStack> slots(LivingEntity entity, EquipmentSlot... slots) {
        List<ItemStack> out = new ArrayList<>();
        for (EquipmentSlot slot : slots) {
            out.add(entity.getEquippedStack(slot));
        }
        return out;
    }
}
