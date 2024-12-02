package me.mrnavastar.creorio.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.mrnavastar.creorio.access.IChunkTicketManager;
import me.mrnavastar.creorio.networking.CreorioChunkUpdateS2C;
import me.mrnavastar.creorio.server.Creorio;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {

    @Shadow public abstract ChunkTicketManager getTicketManager();

    @Shadow @Final private ServerWorld world;

    @ModifyReturnValue(method = "shouldTick", at = @At("RETURN"))
    private boolean shouldTick(boolean original, @Local(argsOnly = true) ChunkPos pos) {
        if (original) return true;
        return ((IChunkTicketManager) getTicketManager()).creorio$isLoadedByCreorio(pos.toLong());
    }

    @Inject(method = "sendChunkDataPackets", at = @At("TAIL"))
    private void sendCreorioUpdates(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        if (!Creorio.getCreorioStorage(world).getChunks().contains(chunk.getPos().toLong())) return;
        Creorio.CHANNEL.sendToPlayer(player, new CreorioChunkUpdateS2C(world.getRegistryKey(), chunk.getPos()));
    }
}