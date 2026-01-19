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
package net.raphimc.viabedrock.tool.generator;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import com.viaversion.viaversion.platform.NoopInjector;
import com.viaversion.viaversion.platform.UserConnectionViaVersionPlatform;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.codegen.CodeGen;
import net.raphimc.viabedrock.codegen.model.member.impl.Field;
import net.raphimc.viabedrock.codegen.model.type.impl.Class;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MappingDataGenerator {

    public static void main(String[] args) throws Throwable {
        ViaManagerImpl.initAndLoad(new TestPlatform(), new NoopInjector(), new ViaCommandHandler(false), ViaPlatformLoader.NOOP);
        while (!Via.getManager().getProtocolManager().hasLoadedMappings()) {
            Thread.sleep(100);
        }
        BedrockProtocol.MAPPINGS.load();

        { // Bedrock
            final CodeGen codeGen = new CodeGen(new File("src/main/java"), "net.raphimc.viabedrock.protocol.data.generated.bedrock");
            codeGen.addType(generateFromSet("CustomBlockTags", new HashSet<>(BedrockProtocol.MAPPINGS.getBedrockCustomBlockTags().values())));
            codeGen.addType(generateFromSet("CustomItemTags", new HashSet<>(BedrockProtocol.MAPPINGS.getBedrockCustomItemTags().values())));
            codeGen.generate();
        }
        { // Java
            final CodeGen codeGen = new CodeGen(new File("src/main/java"), "net.raphimc.viabedrock.protocol.data.generated.java");
            codeGen.addType(generateFromSet("RegistryKeys", BedrockProtocol.MAPPINGS.getJavaRegistries().keySet()));
            codeGen.addType(generateFromSet("Attributes", BedrockProtocol.MAPPINGS.getJavaEntityAttributes().keySet()));
            codeGen.addType(generateFromSet("EntityDataFields", BedrockProtocol.MAPPINGS.getJavaEntityDataFields().values().stream().flatMap(Collection::stream).collect(Collectors.toSet())));
            codeGen.generate();
        }

        final ViaManagerImpl viaManager = (ViaManagerImpl) Via.getManager();
        viaManager.destroy();
    }

    private static Class generateFromSet(final String name, final Set<String> fields) {
        final Class clazz = new Class(name);
        for (String field : fields.stream().sorted().toList()) {
            final String fieldName = Key.stripMinecraftNamespace(field).replaceAll("[^A-Za-z0-9_]", "_").toUpperCase(Locale.ROOT);
            clazz.members().add(new Field("public static final", "String", fieldName, '"' + field + '"'));
        }
        return clazz;
    }

    private static class TestPlatform extends UserConnectionViaVersionPlatform {

        public TestPlatform() {
            super(null);
        }

        @Override
        public String getPlatformName() {
            return "Test";
        }

        @Override
        public String getPlatformVersion() {
            return "test";
        }

        @Override
        public Logger createLogger(final String name) {
            return Logger.getGlobal();
        }

        @Override
        protected AbstractViaConfig createConfig() {
            return new AbstractViaConfig(null, null) {
                @Override
                public void reload() {
                }
            };
        }
    }

}
