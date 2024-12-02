package me.mrnavastar.creorio.networking;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import me.mrnavastar.creorio.client.CreorioClient;
import me.mrnavastar.creorio.server.Creorio;
import net.fabricmc.api.EnvType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class CreorioChunkUpdateS2C {

    static {
        Creorio.CHANNEL.register(CreorioChunkUpdateS2C.class, CreorioChunkUpdateS2C::encode, CreorioChunkUpdateS2C::new, CreorioChunkUpdateS2C::apply);
    }

    private final RegistryKey<World> world;
    private final ChunkPos pos;

    public CreorioChunkUpdateS2C(PacketByteBuf buf) {
        world = buf.readRegistryKey(RegistryKeys.WORLD);
        pos = buf.readChunkPos();
    }

    public CreorioChunkUpdateS2C(RegistryKey<World> world, ChunkPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public void encode(PacketByteBuf buf) {
        buf.writeRegistryKey(world);
        buf.writeChunkPos(pos);
    }

    public void apply(Supplier<NetworkManager.PacketContext> ctx) {
        if (Platform.getEnv().equals(EnvType.SERVER)) return;
        CreorioClient.getChunks(world).add(pos);
    }
}