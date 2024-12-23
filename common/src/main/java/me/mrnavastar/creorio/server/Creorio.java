package me.mrnavastar.creorio.server;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.networking.NetworkChannel;
import me.mrnavastar.creorio.access.IChunkTicketManager;
import me.mrnavastar.creorio.networking.CreorioChunkUpdateS2C;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.chunk.Chunk;
import oshi.annotation.concurrent.NotThreadSafe;
import oshi.annotation.concurrent.ThreadSafe;

import java.util.concurrent.*;

public final class Creorio {

    private record ChunkChange(ServerWorld world, ChunkPos pos, boolean state) {

        @NotThreadSafe
        public void apply() {
            ForcedChunkState storage = getCreorioStorage(world);
            if (state) {
                world.getChunkManager().addTicket(TICKET, pos, 2, pos);
                if (storage.getChunks().add(pos.toLong())) {
                    storage.markDirty();
                    InspectionManager.updatePlayers(world, pos, true);
                }
            }
            else if (isLoadedByCreorio(world, pos)){
                world.getChunkManager().removeTicket(TICKET, pos, 2, pos);
                if (storage.getChunks().remove(pos.toLong())) {
                    storage.markDirty();
                    InspectionManager.updatePlayers(world, pos, false);
                }
            }
        }
    }

    public static final NetworkChannel CHANNEL = NetworkChannel.create(new Identifier("creorio", "chunks"));
    public static final ChunkTicketType<ChunkPos> TICKET = ChunkTicketType.create("creorio", (a, b) -> 0);
    private static final ExecutorService exec = Executors.newFixedThreadPool(5);
    private static final ConcurrentLinkedQueue<ChunkChange> chunkChanges = new ConcurrentLinkedQueue<>();

    @NotThreadSafe
    public static ForcedChunkState getCreorioStorage(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(ForcedChunkState::fromNbt, ForcedChunkState::new, "creorio");
    }

    @NotThreadSafe
    public static boolean isLoadedByCreorio(ServerWorld world, ChunkPos pos) {
        return (((IChunkTicketManager) world.getChunkManager().threadedAnvilChunkStorage.getTicketManager()).creorio$isLoadedByCreorio(pos.toLong()));
    }

    @ThreadSafe
    private static void setForced(ServerWorld world, ChunkPos pos, boolean state) {
        chunkChanges.add(new ChunkChange(world, pos, state));
    }

    @ThreadSafe
    private static void clean(Chunk chunk, ServerWorld world, NbtCompound nbt) {
        exec.submit(() -> {
            // Scan chunk data for any whitelisted blocks, return early if one is found
            for (NbtElement section : nbt.getList("sections", NbtElement.COMPOUND_TYPE))
                for (NbtElement block : ((NbtCompound) section).getCompound("block_states").getList("palette", NbtElement.COMPOUND_TYPE))
                    if (Config.isWhitelisted(((NbtCompound) block).getString("Name"))) {
                        setForced(world, chunk.getPos(), true);
                        return;
                    }
            setForced(world, chunk.getPos(), false);
        });
    }

    public static void init() {
        CreorioChunkUpdateS2C.register();
        Config.load();
        InspectionManager.init();
        CommandRegistrationEvent.EVENT.register(CreorioCommand::init);

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
        ChunkEvent.SAVE_DATA.register(Creorio::clean);
        // Allow for importing of worlds that have existing whitelisted blocks
        ChunkEvent.LOAD_DATA.register(Creorio::clean);
        // Load all the chunks that have whitelisted blocks in them
        LifecycleEvent.SERVER_LEVEL_LOAD.register(world -> getCreorioStorage(world).getChunks().forEach(pos -> setForced(world, new ChunkPos(pos), true)));

        // Apply forced loaded chunk modifications on main thread to keep things safe
        TickEvent.SERVER_PRE.register(server -> {
            while (!chunkChanges.isEmpty()) chunkChanges.remove().apply();
        });
    }
}