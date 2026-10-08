package com.zurrtum.create.foundation.utility;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replaces {@code level.fuelValues().burnDuration(stack)} (removed in 26.3).
 * Fuel is now the {@code COOKING_FUEL} component whose burn time may be a reference resolved
 * with a LootContext (server side). Resolved values are cached per item so that client-side
 * simulations work too.
 */
public final class FuelCompat {
    private static final Map<Item, Integer> CACHE = new ConcurrentHashMap<>();

    private FuelCompat() {
    }

    public static int burnDuration(@Nullable Level level, ItemStack stack) {
        CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        if (fuel == null) {
            return 0;
        }
        ResolvableInt burnTime = fuel.burnTime();
        if (burnTime instanceof ResolvableInt.Constant constant) {
            return constant.value();
        }
        if (level instanceof ServerLevel serverLevel) {
            LootParams params = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY);
            LootContext context = new LootContext.Builder(params).create(Optional.empty());
            int value = burnTime.get(context, 0);
            CACHE.put(stack.getItem(), value);
            return value;
        }
        return CACHE.getOrDefault(stack.getItem(), 0);
    }

    public static int burnDuration(ItemStack stack) {
        return burnDuration(null, stack);
    }

    public static boolean isFuel(@Nullable Level level, ItemStack stack) {
        return burnDuration(level, stack) > 0;
    }

    public static boolean isFuel(ItemStack stack) {
        return burnDuration(null, stack) > 0;
    }
}
