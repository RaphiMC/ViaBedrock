/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package util;

import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PackType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DirectoryResourcePack extends ResourcePack {

    public DirectoryResourcePack(File directory, UUID packId, String version, byte[] contentKey, String subPackName, String contentId, boolean hasScripts, boolean isAddonPack, boolean raytracingCapable, long compressedSize, PackType type) {
        super(packId, version, contentKey, subPackName, contentId, hasScripts, isAddonPack, raytracingCapable, null, compressedSize, type);

        try {
            for (Field field : ResourcePack.class.getDeclaredFields()) {
                if (field.getName().equals("content")) {
                    field.setAccessible(true);
                    field.set(this, new DirectoryContent(directory.toPath()));
                } else if (field.getName().equals("compressedData")) {
                    field.setAccessible(true);
                    field.set(this, null);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static class DirectoryContent extends Content {

        private final Path dir;

        public DirectoryContent(final Path dir) {
            this.dir = dir;
        }

        @Override
        public List<String> getFilesShallow(final String path, final String extension) {
            final Path resolvedPath = this.resolvePath(this.dir, Path.of(path));
            if (!Files.exists(resolvedPath)) {
                return List.of();
            }
            try {
                return Files.list(resolvedPath)
                        .filter(Files::isRegularFile)
                        .map(this.dir::relativize)
                        .map(Path::toString)
                        .map(s -> s.replace('\\', '/'))
                        .filter(file -> !file.contains("/") && file.endsWith(extension))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<String> getFilesDeep(final String path, final String extension) {
            final Path resolvedPath = this.resolvePath(this.dir, Path.of(path));
            if (!Files.exists(resolvedPath)) {
                return List.of();
            }
            try {
                return Files.walk(resolvedPath)
                        .filter(Files::isRegularFile)
                        .map(this.dir::relativize)
                        .map(Path::toString)
                        .map(s -> s.replace('\\', '/'))
                        .filter(file -> file.endsWith(extension))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean contains(final String path) {
            return Files.exists(this.resolvePath(this.dir, Path.of(path)));
        }

        @Override
        public byte[] get(final String path) {
            final Path resolvedPath = this.resolvePath(this.dir, Path.of(path));
            if (!Files.exists(resolvedPath)) {
                return null;
            }
            try {
                return Files.readAllBytes(resolvedPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean put(final String path, final byte[] data) {
            final boolean exists = this.contains(path);
            try {
                Files.write(this.resolvePath(this.dir, Path.of(path)), data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return exists;
        }

        @Override
        public byte[] toZip() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        private Path resolvePath(final Path baseDirPath, final Path userPath) {
            if (!baseDirPath.isAbsolute()) {
                throw new IllegalArgumentException("Base path must be absolute");
            }
            if (userPath.isAbsolute()) {
                throw new IllegalArgumentException("User path must be relative");
            }
            final Path resolvedPath = baseDirPath.resolve(userPath).normalize();
            if (!resolvedPath.startsWith(baseDirPath)) {
                throw new IllegalArgumentException("User path escapes the base path");
            }
            return resolvedPath;
        }

    }

}
