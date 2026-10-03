package adris.altoclef.multiversion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;

public class CraftingRecipeVer {


    private static ItemStack getOutput(CraftingRecipe craftingRecipe) {
        //#if MC >= 11904
        //#if MC >= 12102
        //$$ return ItemStack.EMPTY;
        //#else
        return craftingRecipe.getResult(null);
        //#endif
        //#else
        //$$ return craftingRecipe.getOutput();
        //#endif
    }

}
