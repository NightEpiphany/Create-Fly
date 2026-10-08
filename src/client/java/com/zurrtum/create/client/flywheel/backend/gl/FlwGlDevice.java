package com.zurrtum.create.client.flywheel.backend.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.zurrtum.create.client.mixin.FrontendGpuDeviceAccessor;

public final class FlwGlDevice {
    private FlwGlDevice() {
    }

    /**
     * The OpenGL device behind RenderSystem.getDevice(), unwrapping the frontend device introduced in 26.3.
     */
    public static GlDevice get() {
        GpuDevice device = RenderSystem.getDevice();
        if (device instanceof GlDevice glDevice) {
            return glDevice;
        }
        if (device instanceof FrontendGpuDeviceAccessor accessor) {
            return (GlDevice) (Object) accessor.create$getBackend();
        }
        throw new IllegalStateException("Not an OpenGL device: " + device.getClass().getName());
    }
}
