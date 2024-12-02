package me.mrnavastar.creorio.client;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.util.math.ChunkPos;

import java.util.Set;

public class CreorioClient {

    @Getter
    private static final Set<ChunkPos> chunks = Sets.newConcurrentHashSet();
    @Getter
    private static final CreorioChunkDebugRenderer debugRenderer = new CreorioChunkDebugRenderer();

    public static void init() {
    }
}
