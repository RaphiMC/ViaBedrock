/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.tool;

import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.JsonElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Locations inside the repository which the tools read from and write to.
 * <p>
 * The project root is looked up from the working directory, so the tools behave the same when started
 * from Gradle, from an IDE run configuration or from a terminal inside a subdirectory.
 */
public class ToolPaths {

    public static final Path PROJECT_ROOT = findProjectRoot();
    public static final Path MAIN_JAVA = PROJECT_ROOT.resolve("src/main/java");
    public static final Path ASSETS = PROJECT_ROOT.resolve("src/main/resources/assets/viabedrock");
    public static final Path BEDROCK_DATA = ASSETS.resolve("data/bedrock");
    public static final Path CUSTOM_DATA = ASSETS.resolve("data/custom");
    public static final Path JAVA_DATA = ASSETS.resolve("data/java");
    public static final Path BLOCK_STATE_UPGRADE_SCHEMAS = ASSETS.resolve("block_state_upgrade_schema");
    public static final Path ITEM_UPGRADE_SCHEMAS = ASSETS.resolve("item_upgrade_schema");
    public static final Path RESOURCE_PACKS = ASSETS.resolve("resource_packs");
    public static final Path SKIN_PACKS = ASSETS.resolve("skin_packs");

    private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Gson GSON_WITH_NULLS = GSON.newBuilder().serializeNulls().create();

    /**
     * Writes a json file in the format used by the data assets: pretty printed, no html escaping and no trailing newline.
     */
    public static void writeJson(final Path file, final JsonElement json) throws IOException {
        writeJson(file, json, false);
    }

    public static void writeJson(final Path file, final JsonElement json, final boolean serializeNulls) throws IOException {
        writeString(file, (serializeNulls ? GSON_WITH_NULLS : GSON).toJson(json));
    }

    public static void writeString(final Path file, final String content) throws IOException {
        final Path absoluteFile = file.toAbsolutePath();
        Files.createDirectories(absoluteFile.getParent());
        Files.writeString(absoluteFile, content);
        System.out.println("Wrote " + describe(absoluteFile));
    }

    /**
     * Prints paths inside the repository relative to its root, everything else as it is.
     */
    public static String describe(final Path file) {
        final Path absoluteFile = file.toAbsolutePath();
        return absoluteFile.startsWith(PROJECT_ROOT) ? PROJECT_ROOT.relativize(absoluteFile).toString() : absoluteFile.toString();
    }

    /**
     * The {@code data} directory of an installed Bedrock client. The tools read extracted vanilla resource packs, sounds and particles from it.
     * <p>
     * Copy the directory to {@code run/client-data} when the game is not installed on this machine.
     */
    public static Path clientDataDir(final ToolArgs args) {
        return args.directory("client-data", "It is the 'data' directory of the Bedrock client installation with extracted resource_packs_unpacked, for example 'C:\\XboxGames\\Minecraft for Windows\\Content\\data'. It can also be copied to run/client-data.",
                PROJECT_ROOT.resolve("run/client-data"),
                Path.of("C:\\XboxGames\\Minecraft for Windows\\Content\\data"));
    }

    /**
     * The enum metadata from <a href="https://github.com/Mojang/bedrock-protocol-docs/releases">Mojang/bedrock-protocol-docs releases</a>.
     */
    public static Path protocolDocsDir(final ToolArgs args) {
        final Path directory = args.directory("protocol-docs", "Download the enum metadata from https://github.com/Mojang/bedrock-protocol-docs/releases and pass its directory, or extract it to run/protocol-docs.",
                PROJECT_ROOT.resolve("run/protocol-docs"),
                PROJECT_ROOT.getParent().resolve("protocol-docs"));
        return directory;
    }

    private static Path findProjectRoot() {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            if (Files.exists(directory.resolve("settings.gradle"))) {
                return directory;
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException("Could not find the ViaBedrock project root. Run the tools from inside the repository.");
    }

}
