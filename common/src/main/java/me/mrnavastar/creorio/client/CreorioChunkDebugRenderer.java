package me.mrnavastar.creorio.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

public class CreorioChunkDebugRenderer implements DebugRenderer.Renderer {

    private static final float r = 0;
    private static final float g = 150;
    private static final float b = 150;
    private static final float a = 0.5F;

    private boolean enabled = false;

    public CreorioChunkDebugRenderer() {
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (CreorioClient.DEBUG_KEY.wasPressed()) enabled = !enabled;
        });
    }

    @Override
    public void clear() {
       // CreorioClient.getChunks().clear();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        if (!enabled) return;

        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;

        CreorioClient.getChunks(world.getRegistryKey()).forEach(pos -> {
            WorldChunk chunk = world.getChunk(pos.x, pos.z);
            Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE);

            for (int x = 0; x < 16; x ++) {
                for (int z = 0; z < 16; z ++) {
                    BlockPos block = pos.getStartPos().add(x, heightmap.get(x, z), z);
                    DebugRenderer.drawBox(matrices, vertexConsumers, block, block.add(1, world.getBottomY(), 1), r, g, b, a);
                }
            }
        });
    }
}