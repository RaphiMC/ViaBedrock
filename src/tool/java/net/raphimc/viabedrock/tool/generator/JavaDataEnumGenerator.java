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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.raphimc.viabedrock.codegen.CodeGen;
import net.raphimc.viabedrock.codegen.model.member.impl.Field;
import net.raphimc.viabedrock.codegen.model.type.impl.Enum;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavaDataEnumGenerator {

    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String VERSION_ID = "1.21.11";

    public static void main(String[] args) throws Throwable {
        final JsonObject metaObj = JsonParser.parseReader(new InputStreamReader(new URL(MANIFEST_URL).openStream())).getAsJsonObject();
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

        final CodeGen codeGen = new CodeGen(new File("../src/main/java"), "net.raphimc.viabedrock.protocol.data.enums.java.generated");

        codeGen.addType(extractFromEnum("BossEventOperationType", classNodes.get("net.minecraft.network.protocol.game.ClientboundBossEventPacket$OperationType")));
        codeGen.addType(extractFromEnum("ClickType", classNodes.get("net.minecraft.world.inventory.ClickType")));
        codeGen.addType(extractFromEnum("ClientCommandAction", classNodes.get("net.minecraft.network.protocol.game.ServerboundClientCommandPacket$Action")));
        codeGen.addType(extractFromEnum("CustomChatCompletionsAction", classNodes.get("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket$Action")));
        codeGen.addType(extractFromEnum("EquipmentSlot", classNodes.get("net.minecraft.world.entity.EquipmentSlot")));
        codeGen.addType(extractFromEnum("GameMode", classNodes.get("net.minecraft.world.level.GameType")));
        codeGen.addType(extractFromEnum("HeightmapType", classNodes.get("net.minecraft.world.level.levelgen.Heightmap$Types")));
        codeGen.addType(extractFromEnum("InteractActionType", classNodes.get("net.minecraft.network.protocol.game.ServerboundInteractPacket$ActionType")));
        codeGen.addType(extractFromEnum("ObjectiveCriteriaRenderType", classNodes.get("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType")));
        codeGen.addType(extractFromEnum("PlayerActionAction", classNodes.get("net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action")));
        codeGen.addType(extractFromEnum("PlayerCommandAction", classNodes.get("net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action")));
        codeGen.addType(extractFromEnum("PlayerInfoUpdateAction", classNodes.get("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action")));
        codeGen.addType(extractFromEnum("ResourcePackAction", classNodes.get("net.minecraft.network.protocol.common.ServerboundResourcePackPacket$Action")));
        codeGen.addType(extractFromEnum("SoundSource", classNodes.get("net.minecraft.sounds.SoundSource")));
        codeGen.addType(extractFromEnum("TeamCollisionRule", classNodes.get("net.minecraft.world.scores.Team$CollisionRule")));
        codeGen.addType(extractFromEnum("TeamVisibility", classNodes.get("net.minecraft.world.scores.Team$Visibility")));

        codeGen.generate();
    }

    private static Enum extractFromEnum(final String enumName, final ClassNode classNode) {
        final Enum genEnum = new Enum(enumName);
        for (FieldNode fieldNode : classNode.fields) {
            if ((fieldNode.access & Opcodes.ACC_ENUM) != 0) {
                genEnum.enumFields().add(new Field(fieldNode.name));
            }
        }
        return genEnum;
    }

}
