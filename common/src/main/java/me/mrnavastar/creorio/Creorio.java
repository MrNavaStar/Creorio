package me.mrnavastar.creorio;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.TickEvent;
import me.mrnavastar.creorio.access.IChunkTicketManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Creorio {

    private record ChunkChange(RegistryKey<World> world, ChunkPos pos, Boolean state) {

        public void apply(MinecraftServer server) {
            Optional.ofNullable(server.getWorld(world)).ifPresent(w -> {
                ServerChunkManager manager = w.getChunkManager();
                if (state) manager.addTicket(TICKET, pos, 1, pos);
                else if (((IChunkTicketManager) manager.threadedAnvilChunkStorage.getTicketManager()).creorio$isLoadedByCreorio(pos.toLong()))
                    manager.removeTicket(TICKET, pos, 1, pos);
            });
        }
    }

    public static final ChunkTicketType<ChunkPos> TICKET = ChunkTicketType.create("creorio", (a, b) -> 0);
    private static final ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
    private static final ConcurrentLinkedQueue<ChunkChange> chunkChanges = new ConcurrentLinkedQueue<>();

    private static void setForced(World world, ChunkPos pos, boolean state) {
        chunkChanges.add(new ChunkChange(world.getRegistryKey(), pos, state));
    }

    private static void scan(Chunk chunk, ServerWorld world, NbtCompound nbt) {
        if (world == null) return;
        exec.submit(() -> {
            // Scan chunk data for any whitelisted blocks, return early if one is found
            for (NbtElement section : nbt.getList("sections", NbtElement.COMPOUND_TYPE)) {
                NbtList palette = ((NbtCompound) section).getCompound("block_states").getList("palette", NbtElement.COMPOUND_TYPE);
                for (NbtElement block : palette) {
                    if (Config.isWhitelisted(((NbtCompound) block).getString("Name"))) {
                        setForced(world, chunk.getPos(), true);
                        return;
                    }
                }
            }
            setForced(world, chunk.getPos(), false);
        });
    }

    public static void init() {
        Config.load();

        // TODO: Check if this event fires twice on a server like it does on a client
        // Load all chunks with whitelisted blocks placed in them
        BlockEvent.PLACE.register((world, blockPos, blockState, entity) -> {
            exec.submit(() -> {
                String state = blockState.getRegistryEntry().getKey().get().getValue().toString();
                if (Config.isWhitelisted(state)) setForced(world, new ChunkPos(blockPos), true);
            });
            return EventResult.pass();
        });

        // Purge any force loaded chunks that no longer have whitelist blocks inside them
        ChunkEvent.SAVE_DATA.register(Creorio::scan);
        // Load all the chunks that have whitelisted blocks in them
        ChunkEvent.LOAD_DATA.register(Creorio::scan);

        // Apply forced loaded chunk modifications on main thread to keep things safe
        TickEvent.SERVER_PRE.register(server -> {
            while (!chunkChanges.isEmpty()) chunkChanges.remove().apply(server);
        });
    }
}