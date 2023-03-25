import net.raphimc.viabedrock.protocol.model.ResourcePack;
import net.raphimc.viabedrock.protocol.rewriter.ResourcePackRewriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        final ResourcePack.Content bedrockContent = new ResourcePack.Content();
        final ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(input.toPath()));
        ZipEntry zipEntry;
        int len;
        final byte[] buf = new byte[4096];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            while ((len = zipInputStream.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            bedrockContent.put(zipEntry.getName(), baos.toByteArray());
            baos.reset();
        }
        zipInputStream.close();
        System.out.println("Reading took " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        final ResourcePack.Content javaContent = ResourcePackRewriter.bedrockToJava(bedrockContent);
        System.out.println("Conversion took " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        Files.write(output.toPath(), javaContent.toZip());
        System.out.println("Writing took " + (System.currentTimeMillis() - start) + "ms");
    }

}
