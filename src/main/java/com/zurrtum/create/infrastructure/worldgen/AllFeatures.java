package com.zurrtum.create.infrastructure.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFeatures {
    public static void register() {
        Registry.register(
            BuiltInRegistries.FEATURE_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, "layered_ore"),
            LayeredOreFeature.CODEC
        );
    }
}
