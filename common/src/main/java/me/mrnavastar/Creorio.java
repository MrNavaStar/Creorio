package me.mrnavastar;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.platform.Platform;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Creorio {
    private static final ArrayList<String> whitelist = new ArrayList<>();

    private static boolean isWhitelisted(String check) {
        for (String item : whitelist) {
            if (check.startsWith(item)) return true;
        }
        return false;
    }

    private static void setForced(ServerWorld world, ChunkPos pos, boolean forced) {
        world.setChunkForced(pos.x, pos.z, forced);
    }

    // TODO: Should this be async?
    private static void scan(Chunk chunk, ServerWorld world, NbtCompound nbt) {
        if (world == null || !world.getForcedChunks().contains(chunk.getPos().toLong())) return;

        // Scan chunk data for any whitelisted blocks, return early if one is found
        for (NbtElement section : nbt.getList("sections", NbtElement.COMPOUND_TYPE)) {
            NbtList palette = ((NbtCompound) section).getCompound("block_states").getList("palette", NbtElement.COMPOUND_TYPE);

            for (NbtElement block : palette) {
                if (isWhitelisted(((NbtCompound) block).getString("Name"))) {
                    setForced(world, chunk.getPos(), true);
                    return;
                }
            }
        }
        setForced(world, chunk.getPos(), false);
    }

    public static void init() {
        // TODO: Should this be async?
        // Force load all chunks with whitelisted blocks placed in them
        BlockEvent.PLACE.register((world, blockPos, blockState, entity) -> {
            if (!isWhitelisted(blockState.getBlock().getTranslationKey())) return EventResult.pass();
            setForced((ServerWorld) world, new ChunkPos(blockPos), true);
            return EventResult.pass();
        });

        // Purge any force loaded chunks that no longer have whitelist blocks inside them
        ChunkEvent.SAVE_DATA.register(Creorio::scan);
        //ChunkEvent.LOAD_DATA.register(Creorio::scan);

        // Load all configs
        try (Stream<Path> path = Files.walk(Path.of(Platform.getConfigFolder() + "/creorio"))) {
            path.filter(Files::isRegularFile)
                    .filter(f -> f.getFileName()
                    .endsWith(".creorio"))
                    .forEach(file -> {
                        try (BufferedReader r = new BufferedReader(new FileReader(String.valueOf(file)))) {
                            whitelist.addAll(r.lines().toList());
                        } catch (IOException ignore) {}
                    });
        } catch (IOException ignore) {}
    }
}
