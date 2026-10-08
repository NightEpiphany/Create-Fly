package com.zurrtum.create.client.mixin;

import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 26.3: RenderSystem.getDevice() is a FrontendGpuDevice wrapping the real backend device (GlDevice for OpenGL).
 */
@Mixin(FrontendGpuDevice.class)
public interface FrontendGpuDeviceAccessor {
    @Accessor("backend")
    GpuDeviceBackend create$getBackend();
}
