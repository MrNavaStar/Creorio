package me.mrnavastar.creorio.client;

import com.google.common.collect.Sets;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import lombok.Getter;
import me.mrnavastar.creorio.networking.CreorioChunkUpdateS2C;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CreorioClient {

    @Getter
    private static final CreorioChunkDebugRenderer debugRenderer = new CreorioChunkDebugRenderer();
    private static final ConcurrentHashMap<RegistryKey<World>, Set<ChunkPos>> chunks = new ConcurrentHashMap<>();

    public static final KeyBinding SCREEN_KEY = new KeyBinding("key.creorio.screen", InputUtil.Type.KEYSYM,InputUtil.GLFW_KEY_F6, "category.creorio");
    public static final KeyBinding DEBUG_KEY = new KeyBinding("key.creorio.debug", InputUtil.Type.KEYSYM,InputUtil.GLFW_KEY_F7, "category.creorio");

    public static Set<ChunkPos> getChunks(RegistryKey<World> world) {
        return chunks.computeIfAbsent(world, w -> Sets.newConcurrentHashSet());
    }

    public static void init() {
        KeyMappingRegistry.register(SCREEN_KEY);
        KeyMappingRegistry.register(DEBUG_KEY);

        CreorioScreen.setup();
    }
}
