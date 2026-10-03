package adris.altoclef.multiversion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.world.World;

import java.util.List;

public class RecipeVer {



    public static ItemStack getOutput(Recipe<?> recipe, World world) {
        //#if MC >= 11904
        //#if MC >= 12102
        //$$ return ItemStack.EMPTY;
        //#else
        return recipe.getResult(world.getRegistryManager());
        //#endif
        //#else
        //$$ return recipe.getOutput();
        //#endif
    }

    public static List<Ingredient> getIngredients(Recipe<?> recipe) {
        //#if MC >= 11605
        //#if MC >= 12102
        //$$ return List.of();
        //#else
        return recipe.getIngredients();
        //#endif
        //#else
        //$$ return recipe.getPreviewInputs();
        //#endif
    }


}