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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.resolver.MapResolver;

import java.io.*;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavaDataEnumGenerator {

    private static final String META_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String VERSION_ID = "1.21.11";
    private static final String ENUMS_PACKAGE = "net.raphimc.viabedrock.protocol.data.enums.java.generated";

    public static void main(String[] args) throws Throwable {
        final JsonObject metaObj = JsonParser.parseReader(new InputStreamReader(new URL(META_URL).openStream())).getAsJsonObject();
        final String versionUrl = metaObj.getAsJsonArray("versions").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .filter(e -> e.get("id").getAsString().equals(VERSION_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Version not found"))
                .get("url").getAsString();
        final JsonObject versionObj = JsonParser.parseReader(new InputStreamReader(new URL(versionUrl).openStream())).getAsJsonObject();
        //final String clientUrl = versionObj.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString();
        final String clientUrl = "https://piston-data.mojang.com/v1/objects/4509ee9b65f226be61142d37bf05f8d28b03417b/client.jar";
        final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new URL(clientUrl).openStream()));

        final Map<String, ClassNode> classNodes = new HashMap<>();
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            final String entryName = zipEntry.getName();
            if (!entryName.endsWith(".class")) {
                continue;
            }
            final byte[] entryData = zis.readAllBytes();
            zis.closeEntry();
            final ClassReader reader = new ClassReader(entryData);
            final ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);
            classNodes.put(classNode.name.replace('/', '.'), classNode);
        }

        final Map<String, List<EnumField>> enums = new HashMap<>();
        enums.put("BossEventOperationType", extractFromEnum(classNodes.get("net.minecraft.network.protocol.game.ClientboundBossEventPacket$OperationType")));
        enums.put("ClickType", extractFromEnum(classNodes.get("net.minecraft.world.inventory.ClickType")));
        enums.put("ClientCommandAction", extractFromEnum(classNodes.get("net.minecraft.network.protocol.game.ServerboundClientCommandPacket$Action")));
        enums.put("CustomChatCompletionsAction", extractFromEnum(classNodes.get("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket$Action")));
        enums.put("EquipmentSlot", extractFromEnum(classNodes.get("net.minecraft.world.entity.EquipmentSlot")));
        enums.put("GameMode", extractFromEnum(classNodes.get("net.minecraft.world.level.GameType")));
        enums.put("HeightmapType", extractFromEnum(classNodes.get("net.minecraft.world.level.levelgen.Heightmap$Types")));
        enums.put("InteractActionType", extractFromEnum(classNodes.get("net.minecraft.network.protocol.game.ServerboundInteractPacket$ActionType")));
        enums.put("ObjectiveCriteriaRenderType", extractFromEnum(classNodes.get("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType")));
        enums.put("PlayerActionAction", extractFromEnum(classNodes.get("net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action")));
        enums.put("PlayerCommandAction", extractFromEnum(classNodes.get("net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action")));
        enums.put("PlayerInfoUpdateAction", extractFromEnum(classNodes.get("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action")));
        enums.put("ResourcePackAction", extractFromEnum(classNodes.get("net.minecraft.network.protocol.common.ServerboundResourcePackPacket$Action")));
        enums.put("SoundSource", extractFromEnum(classNodes.get("net.minecraft.sounds.SoundSource")));
        enums.put("TeamCollisionRule", extractFromEnum(classNodes.get("net.minecraft.world.scores.Team$CollisionRule")));
        enums.put("TeamVisibility", extractFromEnum(classNodes.get("net.minecraft.world.scores.Team$Visibility")));

        final File sourceDir = new File("../src/main/java");
        final File enumsDir = new File(sourceDir, ENUMS_PACKAGE.replace(".", "/"));
        if (enumsDir.isDirectory()) {
            Files.walkFileTree(enumsDir.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        enumsDir.mkdirs();

        final MustacheEngine mustacheEngine = MustacheEngineBuilder.newBuilder()
                .addTemplateLocator(ClassPathTemplateLocator.builder().setSuffix("mustache").build())
                .setProperty(EngineConfigurationKey.SKIP_VALUE_ESCAPING, true)
                .addResolver(new MapResolver())
                .build();
        final Mustache enumTemplate = mustacheEngine.getMustache("java_enum");

        for (Map.Entry<String, List<EnumField>> entry : enums.entrySet()) {
            final String enumPackage = ENUMS_PACKAGE;
            final String enumName = entry.getKey();
            final File enumDir = new File(sourceDir, enumPackage.replace(".", "/"));
            enumDir.mkdirs();
            final File enumFile = new File(enumDir, enumName + ".java");

            final Map<String, Object> variables = new HashMap<>();
            variables.put("packageName", enumPackage);
            variables.put("enumName", enumName);
            variables.put("values", entry.getValue());
            try (final FileWriter writer = new FileWriter(enumFile)) {
                enumTemplate.render(writer, variables);
            }
        }
    }

    private static List<EnumField> extractFromEnum(final ClassNode classNode) {
        return classNode.fields.stream()
                .filter(f -> (f.access & Opcodes.ACC_ENUM) != 0)
                .map(f -> new EnumField(f.name))
                .toList();
    }

    private record EnumField(String name) {
    }

}
