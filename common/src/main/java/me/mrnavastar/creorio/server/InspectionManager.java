package me.mrnavastar.creorio.server;

import dev.architectury.event.events.common.PlayerEvent;
import me.mrnavastar.creorio.networking.CreorioChunkUpdateS2C;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.List;

public class InspectionManager {

    //private static final Set<ServerPlayerEntity> players = Sets.newConcurrentHashSet();

    private static List<ServerPlayerEntity> getOps(MinecraftServer server) {
        PlayerManager playerManager = server.getPlayerManager();
        return playerManager.getPlayerList().stream().filter(player -> playerManager.isOperator(player.getGameProfile())).toList();
    }

    /*public static void updatePlayer(ServerPlayerEntity player) {
        Creorio.getCreorioStorage(player.getServerWorld()).getChunks()
                .forEach(pos -> Creorio.CHANNEL.sendToPlayer(player, new CreorioChunkUpdateS2C(player.getServerWorld().getRegistryKey(), new ChunkPos(pos))));
    }*/

    public static void updatePlayers(ServerWorld world, ChunkPos pos, boolean state) {
        HashSet<ServerPlayerEntity> watching = new HashSet<>();
        watching.addAll(world.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(pos));
        watching.addAll(getOps(world.getServer()));
        watching.forEach(player -> Creorio.CHANNEL.sendToPlayer(player, new CreorioChunkUpdateS2C(world.getRegistryKey(), pos, state)));
    }

    public static void init() {
        //PlayerEvent.PLAYER_JOIN.register(InspectionManager::updatePlayer);
    }
}