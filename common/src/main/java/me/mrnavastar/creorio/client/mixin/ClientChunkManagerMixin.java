package me.mrnavastar.creorio.client.mixin;

import me.mrnavastar.creorio.client.CreorioClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin is used to clean up the client creorio chunks as they fall beyond the client render distance
 */
@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {

    @Shadow @Final private ClientWorld world;

    @Inject(method = "unload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager$ClientChunkMap;compareAndSet(ILnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/WorldChunk;)Lnet/minecraft/world/chunk/WorldChunk;"))
    private void unload(int x, int z, CallbackInfo ci) {
        CreorioClient.getChunks(world.getRegistryKey()).remove(new ChunkPos(x, z));
    }
}