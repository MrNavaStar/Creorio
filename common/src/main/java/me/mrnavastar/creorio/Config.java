package me.mrnavastar.creorio;

import dev.architectury.platform.Platform;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Config {

    private static final HashMap<String, ArrayList<String>> whitelist = new HashMap<>();
    private static final String[] files = {
            "create.creorio",
            "create_additions.creorio",
            "create_enchantment_industry.creorio",
            "test.creorio"
    };

    public static boolean isWhitelisted(String check) {
        String[] parts = check.split(":");
        for (String item : whitelist.getOrDefault(parts[0], new ArrayList<>())) {
            if (parts[1].startsWith(item)) return true;
        }
        return false;
    }

    @SneakyThrows
    public static void load() {
        Path dir = Path.of(Platform.getConfigFolder() + "/creorio");

        // Create default config setup
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
            List.of(files).forEach(file -> {
                Optional.ofNullable(Thread.currentThread().getContextClassLoader().getResource(file)).ifPresent(resource -> {
                    try (InputStream resourceStream = resource.openStream()) {
                        byte[] buffer = new byte[resourceStream.available()];
                        resourceStream.read(buffer);
                        Files.write(Path.of(dir + "/" + file), buffer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }

        // Load ".creorio" files
        try (Stream<Path> path = Files.walk(dir)) {
            path.filter(Files::isRegularFile)
                .forEach(file -> {
                    try (BufferedReader r = new BufferedReader(new FileReader(String.valueOf(file)))) {
                        r.lines().forEach(line -> {
                            String[] parts = line.split(":");
                            ArrayList<String> list = whitelist.getOrDefault(parts[0], new ArrayList<>());
                            list.add(parts[1]);
                            whitelist.put(parts[0], list);
                        });
                    } catch (IOException ignore) {}
                });
        }
    }
}