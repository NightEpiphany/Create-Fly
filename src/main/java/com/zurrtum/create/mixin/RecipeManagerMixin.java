package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeManager.IngredientExtractor;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipePropertySet;
import com.zurrtum.create.foundation.recipe.GeneratedRecipes;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow
    @Mutable
    @Final
    public RecipeMap recipes;

    /**
     * Item components are bound by the time recipes are finalized, so the vanilla brewing recipes can be tested
     * against potion stacks here to derive the mixer potion recipes.
     */
    @Inject(method = "finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V", at = @At("HEAD"))
    private void addPotionRecipes(FeatureFlagSet enabledFlags, CallbackInfo ci) {
        this.recipes = GeneratedRecipes.merge(this.recipes, PotionRecipe.generate(this.recipes.values()));
    }

    @WrapOperation(method = "finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;stream()Ljava/util/stream/Stream;"))
    public Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> registerRecipeSet(
        Set<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>> instance,
        Operation<Stream<Entry<ResourceKey<RecipePropertySet>, IngredientExtractor>>> original
    ) {
        return Stream.concat(original.call(instance), AllRecipeSets.ALL.entrySet().stream());
    }
}
