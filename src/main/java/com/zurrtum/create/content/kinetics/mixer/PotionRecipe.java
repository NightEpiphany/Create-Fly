package com.zurrtum.create.content.kinetics.mixer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.zurrtum.create.Create.MOD_ID;

public record PotionRecipe(FluidStack result, FluidIngredient fluidIngredient,
                           Ingredient ingredient) implements BasinRecipe {
    public static final MapCodec<PotionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((RecordCodecBuilder.Instance<PotionRecipe> instance) -> instance.group(
        FluidStack.CODEC.fieldOf("result").forGetter(PotionRecipe::result),
        FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(PotionRecipe::fluidIngredient),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(PotionRecipe::ingredient)
    ).apply(instance, PotionRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionRecipe> STREAM_CODEC = StreamCodec.composite(
        FluidStack.PACKET_CODEC,
        PotionRecipe::result,
        FluidIngredient.PACKET_CODEC,
        PotionRecipe::fluidIngredient,
        Ingredient.CONTENTS_STREAM_CODEC,
        PotionRecipe::ingredient,
        PotionRecipe::new
    );
    public static final RecipeSerializer<PotionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    public static @Nullable ReloadData data;

    private static final List<Item> BOTTLES = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

    /**
     * 26.3: PotionBrewing is gone, brewing is data driven (BrewingRecipe). The mixer recipes are derived from the
     * loaded vanilla brewing recipes: every (bottle, potion) accepted as input by a recipe gives one mixer recipe.
     * A recipe whose output carries no potion contents (bottle conversions) keeps the potion of its input.
     */
    public static Map<Identifier, Recipe<?>> generate(Collection<? extends RecipeHolder<?>> holders) {
        List<RecipeHolder<?>> brewing = new ArrayList<>();
        for (RecipeHolder<?> holder : holders) {
            if (holder.value() instanceof BrewingRecipe) {
                brewing.add(holder);
            }
        }
        brewing.sort(Comparator.comparing(holder -> holder.id().toString()));
        List<Holder.Reference<Potion>> potions = BuiltInRegistries.POTION.listElements().toList();
        Map<Identifier, Recipe<?>> result = new LinkedHashMap<>();
        int recipeIndex = 0;
        for (RecipeHolder<?> holder : brewing) {
            BrewingRecipe recipe = (BrewingRecipe) holder.value();
            Ingredient reagent = recipe.getReagent().ingredient();
            ItemStackTemplate output = recipe.getOutput();
            Item outputItem = output.item().value();
            if (!BOTTLES.contains(outputItem)) {
                continue;
            }
            BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(outputItem);
            // The item prototype components are not bound yet while the RecipeMap is built: read the patch only.
            PotionContents outputContents = null;
            for (TypedDataComponent<?> component : output.components().split().added()) {
                if (component.type() == DataComponents.POTION_CONTENTS) {
                    outputContents = (PotionContents) component.value();
                }
            }
            for (Item bottle : BOTTLES) {
                BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(bottle);
                for (Holder.Reference<Potion> potion : potions) {
                    PotionContents fromContents = new PotionContents(potion);
                    ItemStack stack = new ItemStack(bottle);
                    stack.set(DataComponents.POTION_CONTENTS, fromContents);
                    if (!recipe.getInput().test(stack)) {
                        continue;
                    }
                    PotionContents toContents = outputContents == null || outputContents == PotionContents.EMPTY ? fromContents : outputContents;
                    if (fromBottleType == toBottleType && fromContents.equals(toContents)) {
                        continue;
                    }
                    FluidIngredient fromFluid = PotionFluidHandler.getFluidIngredientFromPotion(fromContents, fromBottleType, 81000);
                    FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(toContents, toBottleType, 81000);
                    Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "potion_mixing_vanilla_" + recipeIndex++);
                    result.put(id, new PotionRecipe(toFluid, fromFluid, reagent));
                }
            }
        }
        return result;
    }

    @Override
    public int getIngredientSize() {
        return 2;
    }

    @Override
    public List<SizedIngredient> ingredients() {
        return List.of(new SizedIngredient(ingredient, 1));
    }

    @Override
    public List<FluidIngredient> fluidIngredients() {
        return List.of(fluidIngredient);
    }

    @Override
    public HeatCondition heat() {
        return HeatCondition.HEATED;
    }

    @Override
    public boolean matches(BasinInput input, Level world) {
        if (!HeatCondition.HEATED.testBlazeBurner(input.heat())) {
            return false;
        }
        ServerFilteringBehaviour filter = input.filter();
        if (filter == null) {
            return false;
        }
        if (!filter.test(result)) {
            return false;
        }
        List<ItemStack> outputs = BasinRecipe.tryCraft(input, ingredient);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.matchFluidIngredient(input, fluidIngredient)) {
            return false;
        }
        return input.acceptOutputs(outputs, List.of(result), true);
    }

    @Override
    public boolean apply(BasinInput input) {
        if (!HeatCondition.HEATED.testBlazeBurner(input.heat())) {
            return false;
        }
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = BasinRecipe.prepareCraft(input, ingredient, changes);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.prepareFluidCraft(input, fluidIngredient, changes)) {
            return false;
        }
        List<FluidStack> fluids = List.of(result);
        if (!input.acceptOutputs(outputs, fluids, true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, fluids, false);
    }

    @Override
    public RecipeSerializer<PotionRecipe> getSerializer() {
        return AllRecipeSerializers.POTION;
    }

    @Override
    public RecipeType<PotionRecipe> getType() {
        return AllRecipeTypes.POTION;
    }

    public record ReloadData(HolderLookup.Provider registries, FeatureFlagSet enabledFeatures) {
    }
}