package me.mrnavastar.creorio.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.mrnavastar.creorio.server.Creorio;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @ModifyReturnValue(method = "shouldTickEntity", at = @At("RETURN"))
    private boolean shouldTickEntity(boolean original, @Local(argsOnly = true) BlockPos pos) {
        if (original) return true;
        return Creorio.isLoadedByCreorio((ServerWorld) (Object) this, new ChunkPos(pos));
    }

    @ModifyReturnValue(method = "shouldTick(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("RETURN"))
    private boolean shouldTick(boolean original, @Local(argsOnly = true) BlockPos pos) {
        if (original) return true;
        return Creorio.isLoadedByCreorio((ServerWorld) (Object) this, new ChunkPos(pos));
    }

    @ModifyReturnValue(method = "shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z", at = @At("RETURN"))
    private boolean shouldTick(boolean original, @Local(argsOnly = true) ChunkPos pos) {
        if (original) return true;
        return Creorio.isLoadedByCreorio((ServerWorld) (Object) this, pos);
    }
}