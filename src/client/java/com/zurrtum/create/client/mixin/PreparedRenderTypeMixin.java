package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

/**
 * DEV diagnostic for the 26.3 port: a pipeline whose colour target count does not match the render pass throws
 * IllegalStateException and kills the whole frame. Log which pipeline it is (once) and skip that draw instead.
 */
@Mixin(PreparedRenderType.class)
public class PreparedRenderTypeMixin {
    @Unique
    private static final Logger CREATE_LOGGER = LogUtils.getLogger();
    @Unique
    private static final Set<String> CREATE_REPORTED = new HashSet<>();

    @WrapMethod(method = "draw")
    private void create$safeDraw(
        StagedVertexBuffer.ExecuteInfo info,
        RenderPass renderPass,
        RenderPipeline renderPipeline,
        Operation<Void> original
    ) {
        try {
            original.call(info, renderPass, renderPipeline);
        } catch (IllegalStateException e) {
            String key = String.valueOf(renderPipeline.getLocation());
            if (CREATE_REPORTED.add(key)) {
                CREATE_LOGGER.error(
                    "[Create 26.3 port] Draw skipped for render type '{}' pipeline '{}': {}",
                    ((PreparedRenderType) (Object) this).name(),
                    key,
                    e.getMessage()
                );
            }
            try {
                renderPass.popDebugGroup();
            } catch (RuntimeException ignored) {
            }
        }
    }
}
