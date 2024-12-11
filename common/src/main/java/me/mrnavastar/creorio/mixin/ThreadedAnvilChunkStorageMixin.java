package me.mrnavastar.creorio.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.mrnavastar.creorio.access.IChunkTicketManager;
import me.mrnavastar.creorio.networking.CreorioChunkUpdateS2C;
import me.mrnavastar.creorio.server.Creorio;
import net.minecraft.entity.Entity;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {

    @Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
    public static class EntityTrackerMixin {

        @Shadow @Final private Entity entity;

        @ModifyVariable(method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At(value = "STORE", ordinal = 0))
        private boolean updateCreorioPlayers(boolean b) {
            return b || Creorio.isLoadedByCreorio((ServerWorld) entity.getWorld(), entity.getChunkPos());
        }
    }

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
        Creorio.CHANNEL.sendToPlayer(player, new CreorioChunkUpdateS2C(world.getRegistryKey(), chunk.getPos(), true));
    }

    // TODO: Test if this is needed
    @ModifyReturnValue(method = "getPlayersWatchingChunk(Lnet/minecraft/util/math/ChunkPos;Z)Ljava/util/List;", at = @At("RETURN"))
    private List<ServerPlayerEntity> getCreorioPlayers(List<ServerPlayerEntity> original, @Local(argsOnly = true) ChunkPos pos) {
        if (((IChunkTicketManager) getTicketManager()).creorio$isLoadedByCreorio(pos.toLong())) return world.getPlayers();
        return original;
    }

    // TODO: Test if this is needed
    /*@ModifyReturnValue(method = "getPlayersWatchingChunk(Lnet/minecraft/util/math/ChunkPos;)Ljava/util/List;", at = @At("RETURN"))
    private List<ServerPlayerEntity> getCreorioPlayers2(List<ServerPlayerEntity> original, @Local(argsOnly = true) ChunkPos pos) {
        if (((IChunkTicketManager) getTicketManager()).creorio$isLoadedByCreorio(pos.toLong())) return world.getPlayers();
        return original;
    }*/
}