package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.zurrtum.create.foundation.recipe.GeneratedRecipes;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 26.3: RecipeManager.prepare() no longer exists (recipes come from the recipe registry). The generated
 * sequenced assembly step recipes are added to the RecipeMap built by RecipeMap.create instead.
 * (Potion recipes need bound item components, they are added later, see RecipeManagerMixin.)
 */
@Mixin(RecipeMap.class)
public class RecipeMapMixin {
    @ModifyReturnValue(method = "create", at = @At("RETURN"))
    private static RecipeMap create$addGenerated(RecipeMap original) {
        if (SequencedAssemblyRecipe.GENERATE_RECIPES.isEmpty()) {
            return original;
        }
        return GeneratedRecipes.merge(original, SequencedAssemblyRecipe.GENERATE_RECIPES);
    }
}
