package me.mrnavastar.creorio.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.mrnavastar.creorio.access.IChunkTicketManager;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {

    @Shadow public abstract ChunkTicketManager getTicketManager();

    @ModifyReturnValue(method = "shouldTick", at = @At("RETURN"))
    private boolean shouldTick(boolean original, @Local(argsOnly = true) ChunkPos pos) {
        if (original) return true;
        return ((IChunkTicketManager) getTicketManager()).creorio$isLoadedByCreorio(pos.toLong());
    }
}