import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PackType;
import net.raphimc.viabedrock.protocol.rewriter.ResourcePackRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
public class ResourcePackConverterTest {

    public static void main(String[] args) throws Throwable {
        final File input = new File("input.mcpack");
        final File output = new File("output.zip");

        long start = System.currentTimeMillis();
        final byte[] bytes = Files.readAllBytes(input.toPath());
        System.out.println("Reading took " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        final ResourcePack resourcePack = new ResourcePack(null, null, new byte[0], "", "", false, false, false, null, 0, PackType.Resources);
        resourcePack.setCompressedDataLength(bytes.length, bytes.length);
        resourcePack.processDataChunk(0, bytes);

        final ResourcePacksStorage resourcePacksStorage = new ResourcePacksStorage(null);
        resourcePacksStorage.addPack(resourcePack);
        resourcePacksStorage.setPackStack(new UUID[]{resourcePack.packId()}, new UUID[0]);
        System.out.println("Preparation took " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        final ResourcePack.Content javaContent = ResourcePackRewriter.bedrockToJava(resourcePacksStorage);
        System.out.println("Conversion took " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        Files.write(output.toPath(), javaContent.toZip());
        System.out.println("Writing took " + (System.currentTimeMillis() - start) + "ms");
    }

}
