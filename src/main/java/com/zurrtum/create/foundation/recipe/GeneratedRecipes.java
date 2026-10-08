package com.zurrtum.create.foundation.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.zurrtum.create.mixin.RecipeMapInvoker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.Map;

public class GeneratedRecipes {
    /**
     * Returns a RecipeMap containing the original recipes plus the generated ones whose id is not already present.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static RecipeMap merge(RecipeMap original, Map<Identifier, Recipe<?>> generated) {
        Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = new HashMap<>();
        for (RecipeHolder<?> holder : original.values()) {
            byKey.put(holder.id(), holder);
        }
        int added = 0;
        for (Map.Entry<Identifier, Recipe<?>> entry : generated.entrySet()) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, entry.getKey());
            if (byKey.containsKey(key)) {
                continue;
            }
            byKey.put(key, new RecipeHolder(key, entry.getValue()));
            added++;
        }
        if (added == 0) {
            return original;
        }
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> keyed = ImmutableMap.builder();
        for (RecipeHolder<?> holder : byKey.values()) {
            byType.put(holder.value().getType(), holder);
            keyed.put(holder.id(), holder);
        }
        RecipeMap merged = RecipeMapInvoker.create$new(byType.build(), keyed.build());
        copyFabricSyncData(original, merged);
        return merged;
    }

    private static final java.lang.reflect.Field FABRIC_SYNCED_SERIALIZERS = findFabricField();

    private static java.lang.reflect.Field findFabricField() {
        try {
            java.lang.reflect.Field field = RecipeMap.class.getDeclaredField("bySyncedSerializer");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Fabric API recipe sync is not installed
            return null;
        }
    }

    /**
     * Fabric API (recipe sync) attaches a field to the RecipeMap built by RecipeMap.create. A map built through the
     * constructor does not get it, which crashes the player login, so it is carried over from the original map.
     */
    private static void copyFabricSyncData(RecipeMap from, RecipeMap to) {
        if (FABRIC_SYNCED_SERIALIZERS == null) {
            return;
        }
        try {
            FABRIC_SYNCED_SERIALIZERS.set(to, FABRIC_SYNCED_SERIALIZERS.get(from));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not copy Fabric recipe sync data", e);
        }
    }
}
