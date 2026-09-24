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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Command line arguments for the tools in this source set.
 * <p>
 * Every value can be passed as {@code --key=value}, as the system property {@code viabedrock.tool.<key>}
 * or as the environment variable {@code VIABEDROCK_TOOL_<KEY>}, in that order of precedence.
 * Machine specific paths are best kept in the environment, so they don't have to be typed on every run.
 */
public class ToolArgs {

    private final Map<String, String> args = new LinkedHashMap<>();

    public static ToolArgs parse(final String[] args) {
        final ToolArgs toolArgs = new ToolArgs();
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument '" + arg + "'. Arguments have to be passed as --key=value");
            }
            final String withoutPrefix = arg.substring(2);
            final int separator = withoutPrefix.indexOf('=');
            if (separator == -1) {
                toolArgs.args.put(withoutPrefix.toLowerCase(Locale.ROOT), "true");
            } else {
                toolArgs.args.put(withoutPrefix.substring(0, separator).toLowerCase(Locale.ROOT), withoutPrefix.substring(separator + 1));
            }
        }
        return toolArgs;
    }

    public String get(final String key, final String fallback) {
        final String value = this.find(key);
        if (value == null) {
            System.out.println("  " + key + " = " + fallback + " (default)");
            return fallback;
        }
        return value;
    }

    public String require(final String key, final String hint) {
        final String value = this.find(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required argument --" + key + ". " + hint);
        }
        return value;
    }

    public boolean flag(final String key) {
        return Boolean.parseBoolean(this.get(key, "false"));
    }

    public List<String> list(final String key) {
        final String value = this.find(key);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        final List<String> values = new ArrayList<>();
        for (String part : value.split(",")) {
            if (!part.isBlank()) {
                values.add(part.trim());
            }
        }
        return values;
    }

    public Path path(final String key, final Path fallback) {
        final String value = this.find(key);
        return value == null ? fallback : Path.of(value);
    }

    /**
     * Resolves a directory which lives outside of the repository, like a game installation or a cloned data repository.
     *
     * @param key        The argument name
     * @param hint       Text explaining where the directory can be obtained
     * @param candidates Well known locations which are used when the argument is not set
     * @return The first directory which exists
     */
    public Path directory(final String key, final String hint, final Path... candidates) {
        final String value = this.find(key);
        if (value != null) {
            final Path path = Path.of(value);
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("--" + key + " points to '" + path + "' which is not a directory");
            }
            return path;
        }

        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                System.out.println("  " + key + " = " + candidate + " (auto detected)");
                return candidate;
            }
        }
        throw new IllegalArgumentException("Could not find the " + key + " directory. Pass --" + key + "=<path>. " + hint);
    }

    private String find(final String key) {
        final String argValue = this.args.get(key.toLowerCase(Locale.ROOT));
        if (argValue != null) {
            System.out.println("  " + key + " = " + argValue + " (argument)");
            return argValue;
        }

        final String propertyKey = "viabedrock.tool." + key;
        final String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null) {
            System.out.println("  " + key + " = " + propertyValue + " (system property " + propertyKey + ")");
            return propertyValue;
        }

        final String environmentKey = "VIABEDROCK_TOOL_" + key.toUpperCase(Locale.ROOT).replace('-', '_');
        final String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null) {
            System.out.println("  " + key + " = " + environmentValue + " (environment variable " + environmentKey + ")");
            return environmentValue;
        }

        return null;
    }

}
