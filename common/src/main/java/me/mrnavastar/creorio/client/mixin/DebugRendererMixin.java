package me.mrnavastar.creorio.client.mixin;

import me.mrnavastar.creorio.client.CreorioClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(method = "reset", at = @At("TAIL"))
    private void reset(CallbackInfo ci) {
        CreorioClient.getDebugRenderer().clear();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        CreorioClient.getDebugRenderer().render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
    }
}