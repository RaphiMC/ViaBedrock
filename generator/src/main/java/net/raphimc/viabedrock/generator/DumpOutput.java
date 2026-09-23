package net.raphimc.viabedrock.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DumpOutput {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final Path DIRECTORY = Path.of(System.getProperty("viabedrock.generator.output", "dumps"));

    private DumpOutput() {
    }

    public static void prepare() throws IOException {
        Files.createDirectories(DIRECTORY);
        Files.deleteIfExists(DIRECTORY.resolve("registries.nbt"));
        Files.deleteIfExists(DIRECTORY.resolve("tags.nbt"));
    }

    public static void writeJson(final String name, final Object value) throws IOException {
        Files.writeString(DIRECTORY.resolve(name), GSON.toJson(value) + System.lineSeparator());
    }

    public static void writeNbt(final String name, final CompoundTag value) throws IOException {
        NbtIo.writeCompressed(value, DIRECTORY.resolve(name));
    }
}
