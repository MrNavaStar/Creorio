package me.mrnavastar.creorio;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import me.mrnavastar.creorio.access.IChunkTicketManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.chunk.Chunk;
import oshi.annotation.concurrent.NotThreadSafe;
import oshi.annotation.concurrent.ThreadSafe;

import java.util.concurrent.*;

public final class Creorio {

    private record ChunkChange(ServerWorld world, ChunkPos pos, Boolean state) {

        @NotThreadSafe
        public void apply() {
            ServerChunkManager manager = world.getChunkManager();
            ForcedChunkState storage = getCreorioStorage(world);
            if (state) {
                manager.addTicket(TICKET, pos, 2, pos);
                if (storage.getChunks().add(pos.toLong())) storage.markDirty();
            }
            else if (((IChunkTicketManager) manager.threadedAnvilChunkStorage.getTicketManager()).creorio$isLoadedByCreorio(pos.toLong())){
                manager.removeTicket(TICKET, pos, 2, pos);
                if (storage.getChunks().remove(pos.toLong())) storage.markDirty();
            }
        }
    }

    public static final ChunkTicketType<ChunkPos> TICKET = ChunkTicketType.create("creorio", (a, b) -> 0);
    private static final ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
    private static final ConcurrentLinkedQueue<ChunkChange> chunkChanges = new ConcurrentLinkedQueue<>();

    @NotThreadSafe
    private static ForcedChunkState getCreorioStorage(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(ForcedChunkState::fromNbt, ForcedChunkState::new, "creorio");
    }

    @ThreadSafe
    private static void setForced(ServerWorld world, ChunkPos pos, boolean state) {
        chunkChanges.add(new ChunkChange(world, pos, state));
    }

    @ThreadSafe
    private static void purge(Chunk chunk, ServerWorld world, NbtCompound nbt) {
        exec.submit(() -> {
            // Scan chunk data for any whitelisted blocks, return early if one is found
            for (NbtElement section : nbt.getList("sections", NbtElement.COMPOUND_TYPE))
                for (NbtElement block : ((NbtCompound) section).getCompound("block_states").getList("palette", NbtElement.COMPOUND_TYPE))
                    if (Config.isWhitelisted(((NbtCompound) block).getString("Name"))) return;

            setForced(world, chunk.getPos(), false);
        });
    }

    public static void init() {
        Config.load();

        // TODO: Check if this event fires twice on a server like it does on a client
        // Load all chunks with whitelisted blocks placed in them
        BlockEvent.PLACE.register((world, blockPos, blockState, entity) -> {
            if (world instanceof ServerWorld serverWorld) exec.submit(() -> {
                String state = blockState.getRegistryEntry().getKey().get().getValue().toString();
                if (Config.isWhitelisted(state)) setForced(serverWorld, new ChunkPos(blockPos), true);
            });
            return EventResult.pass();
        });

        // Purge any force loaded chunks that no longer have whitelist blocks inside them
        ChunkEvent.SAVE_DATA.register(Creorio::purge);
        // Load all the chunks that have whitelisted blocks in them
        LifecycleEvent.SERVER_LEVEL_LOAD.register(world -> getCreorioStorage(world).getChunks().forEach(pos -> setForced(world, new ChunkPos(pos), true)));

        // Apply forced loaded chunk modifications on main thread to keep things safe
        TickEvent.SERVER_PRE.register(server -> {
            while (!chunkChanges.isEmpty()) chunkChanges.remove().apply();
        });
    }
}