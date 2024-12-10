package me.mrnavastar.creorio.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.ChunkInfo.class)
public class WorldRendererMixin {

    @ModifyReturnValue(method = "canCull", at = @At("RETURN"))
    private boolean no(boolean original) {
        return false;
    }
}