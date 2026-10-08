package com.zurrtum.create.infrastructure.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPlacementModifiers {
    public static void register() {
        Registry.register(
            BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, "config_filter"),
            ConfigPlacementFilter.CODEC
        );
    }
}
