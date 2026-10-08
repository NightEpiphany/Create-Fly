package com.zurrtum.create.client.foundation.utility;

import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.Direction;

/** Compat 26.3: MaterialInfo.shade() (boolean) was replaced by shadeDirectionOverride() (Direction, nullable). GUESS: null = shaded, UP = unshaded. */
public class MaterialShade {
    public static final Direction UNSHADED = Direction.UP;

    public static boolean shade(MaterialInfo info) {
        return info.shadeDirectionOverride() == null;
    }

    public static Direction direction(boolean shade) {
        return shade ? null : UNSHADED;
    }

    public static MaterialInfo copy(MaterialInfo info, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite, net.minecraft.client.renderer.chunk.ChunkSectionLayer layer, net.minecraft.client.renderer.rendertype.RenderType itemRenderType, boolean shade) {
        return new MaterialInfo(
            sprite,
            layer,
            itemRenderType,
            info.itemGlintRenderType(),
            info.itemGlintSpecialRenderType(),
            info.tintIndex(),
            direction(shade),
            info.lightEmission()
        );
    }
}
