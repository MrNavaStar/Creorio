package me.mrnavastar.creorio.networking;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.mrnavastar.creorio.server.Creorio;
import me.mrnavastar.creorio.client.CreorioClient;
import net.fabricmc.api.EnvType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.function.Supplier;

public class CreorioChunkListS2C {

    static {
        Creorio.CHANNEL.register(CreorioChunkListS2C.class, CreorioChunkListS2C::encode, CreorioChunkListS2C::new, CreorioChunkListS2C::apply);
    }

    private final LongSet chunks;

    public CreorioChunkListS2C(PacketByteBuf buf) {
        chunks = new LongOpenHashSet();
        for (Long pos : buf.readLongArray()) chunks.add(pos.longValue());
    }

    public CreorioChunkListS2C(LongSet chunks) {
        this.chunks = chunks;
    }

    public void encode(PacketByteBuf buf) {
        buf.writeLongArray(chunks.toLongArray());
    }

    public void apply(Supplier<NetworkManager.PacketContext> ctx) {
        if (!CreorioClient.getChunks().isEmpty() || Platform.getEnv().equals(EnvType.SERVER)) {
            CreorioClient.getChunks().clear();
            return;
        }

        ArrayList<ChunkPos> list = new ArrayList<>();
        chunks.forEach(chunk -> list.add(new ChunkPos(chunk)));
        CreorioClient.getChunks().addAll(list);
    }
}