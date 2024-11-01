/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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

import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class BedrockPacksGenerator {

    private static final String MOJANG_LICENSE = """
            (c) Mojang AB. All rights reserved.
            
            By downloading the files in this repository, you agree to the [Minecraft End User License Agreement](https://www.minecraft.net/en-us/eula) and that these files are subject to its terms.
            """;

    public static void main(String[] args) throws Throwable {
        final File clientDataDir = new File("C:\\Users\\User\\Desktop\\data");
        final File resourcePacksDir = new File(clientDataDir, "resource_packs");
        final File outputDir = new File("bedrock_packs");
        outputDir.mkdirs();
        Arrays.stream(outputDir.listFiles()).forEach(File::delete);

        for (String vanillaPackName : ResourcePacksStorage.VANILLA_PACK_NAMES) {
            final File packDir = new File(resourcePacksDir, vanillaPackName);
            final File outputFile = new File(outputDir, vanillaPackName + ".mcpack");

            try (FileSystem fs = FileSystems.newFileSystem(new URI("jar:" + outputFile.toURI()), Map.of("create", "true"))) {
                final Path fsRoot = fs.getRootDirectories().iterator().next();

                addLicense(fsRoot);
                copyFileIfExists(packDir, fsRoot, "manifest.json");
                copyFileIfExists(packDir, fsRoot, "texts/en_US.lang");
                copyFileIfExists(packDir, fsRoot, "font/glyph_E0.png");
                copyFileIfExists(packDir, fsRoot, "font/glyph_E1.png");
                copyFolder(packDir, fsRoot, "biomes");
                copyFolder(packDir, fsRoot, "fogs");
            }
        }

        try (FileSystem fs = FileSystems.newFileSystem(new URI("jar:" + new File(outputDir, "vanilla_skin_pack.mcpack").toURI()), Map.of("create", "true"))) {
            final Path fsRoot = fs.getRootDirectories().iterator().next();
            addLicense(fsRoot);
            copyFolder(new File(clientDataDir, "skin_packs/vanilla"), fsRoot, ".");
        }
    }

    private static void addLicense(final Path targetRoot) throws IOException {
        final Path licensePath = targetRoot.resolve("LICENSE");
        Files.write(licensePath, MOJANG_LICENSE.getBytes());
    }

    private static void copyFileIfExists(final File packDir, final Path targetRoot, final String filePath) throws IOException {
        final File file = new File(packDir, filePath);
        if (file.exists()) {
            Files.createDirectories(targetRoot.resolve(filePath).getParent());
            Files.copy(file.toPath(), targetRoot.resolve(filePath));
        }
    }

    private static void copyFolder(final File packDir, final Path targetRoot, final String folderPath) throws IOException {
        final File folder = new File(packDir, folderPath);
        if (folder.exists()) {
            final Path sourcePath = folder.toPath();
            final Path targetPath = targetRoot.resolve(folderPath);
            Files.walk(sourcePath).forEach(path -> {
                try {
                    Path resolvedTargetPath = targetPath.resolve(sourcePath.relativize(path).toString());
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(resolvedTargetPath);
                    } else {
                        Files.copy(path, resolvedTargetPath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
