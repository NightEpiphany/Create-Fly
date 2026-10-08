package com.zurrtum.create.client.flywheel.backend.engine;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 26.3: TextureManager.getTexture uploads a not yet loaded texture immediately, which is forbidden while a render
 * pass is open (Flywheel draws inside the main level pass). Textures used by materials are therefore only used
 * once they were loaded by {@link #loadPending()}, which runs at the start of the level render, before any pass.
 */
public final class MaterialTextures {
    private static final Set<Identifier> READY = ConcurrentHashMap.newKeySet();
    private static final Set<Identifier> PENDING = ConcurrentHashMap.newKeySet();

    private MaterialTextures() {
    }

    /**
     * @return true if the texture can be bound now. Otherwise it is queued for loading and the draw must be skipped.
     */
    public static boolean isReady(Identifier texture) {
        if (READY.contains(texture)) {
            return true;
        }
        PENDING.add(texture);
        return false;
    }

    /**
     * Must be called on the render thread, outside of any render pass.
     */
    public static void loadPending() {
        if (PENDING.isEmpty()) {
            return;
        }
        var textureManager = Minecraft.getInstance().getTextureManager();
        for (Identifier id : PENDING) {
            textureManager.getTexture(id);
            READY.add(id);
        }
        PENDING.clear();
    }

    public static void clear() {
        READY.clear();
        PENDING.clear();
    }
}
