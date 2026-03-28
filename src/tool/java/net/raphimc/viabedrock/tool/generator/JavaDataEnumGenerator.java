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
import net.lenni0451.commons.asm.ASMUtils;
import net.raphimc.viabedrock.codegen.CodeGen;
import net.raphimc.viabedrock.codegen.model.member.impl.Field;
import net.raphimc.viabedrock.codegen.model.type.impl.Enum;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavaDataEnumGenerator {

    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String VERSION_ID = "26.1";

    public static void main(String[] args) throws Throwable {
        final JsonObject metaObj = JsonParser.parseReader(new InputStreamReader(new URL(MANIFEST_URL).openStream())).getAsJsonObject();
        final String versionUrl = metaObj.getAsJsonArray("versions").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .filter(e -> e.get("id").getAsString().equals(VERSION_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Version not found"))
                .get("url").getAsString();
        final JsonObject versionObj = JsonParser.parseReader(new InputStreamReader(new URL(versionUrl).openStream())).getAsJsonObject();
        final String clientUrl = versionObj.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString();
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

        final CodeGen codeGen = new CodeGen(new File("src/main/java"), "net.raphimc.viabedrock.protocol.data.enums.java.generated");

        codeGen.addType(extractFromEnum("BossEventOperationType", classNodes.get("net.minecraft.network.protocol.game.ClientboundBossEventPacket$OperationType")));
        codeGen.addType(extractFromEnum("ClientCommandAction", classNodes.get("net.minecraft.network.protocol.game.ServerboundClientCommandPacket$Action")));
        codeGen.addType(extractFromFieldsWithId("ContainerInput", classNodes.get("net.minecraft.world.inventory.ContainerInput"), 2));
        codeGen.addType(extractFromEnum("CustomChatCompletionsAction", classNodes.get("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket$Action")));
        codeGen.addType(extractFromEnum("EquipmentSlot", classNodes.get("net.minecraft.world.entity.EquipmentSlot"))); // Has multiple different id systems
        codeGen.addType(extractFromFieldsWithId("GameMode", classNodes.get("net.minecraft.world.level.GameType"), 2));
        codeGen.addType(extractFromFieldsWithId("HeightmapType", classNodes.get("net.minecraft.world.level.levelgen.Heightmap$Types"), 2));
        codeGen.addType(extractFromFieldsWithId("InteractionHand", classNodes.get("net.minecraft.world.InteractionHand"), 2));
        codeGen.addType(extractFromEnum("ObjectiveCriteriaRenderType", classNodes.get("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType")));
        codeGen.addType(extractFromEnum("PlayerActionAction", classNodes.get("net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action")));
        codeGen.addType(extractFromEnum("PlayerCommandAction", classNodes.get("net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action")));
        codeGen.addType(extractFromEnum("PlayerInfoUpdateAction", classNodes.get("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action")));
        codeGen.addType(extractFromEnum("ResourcePackAction", classNodes.get("net.minecraft.network.protocol.common.ServerboundResourcePackPacket$Action")));
        codeGen.addType(extractFromEnum("SoundSource", classNodes.get("net.minecraft.sounds.SoundSource")));
        codeGen.addType(extractFromFieldsWithId("TeamCollisionRule", classNodes.get("net.minecraft.world.scores.Team$CollisionRule"), 3));
        codeGen.addType(extractFromFieldsWithId("TeamVisibility", classNodes.get("net.minecraft.world.scores.Team$Visibility"), 3));

        codeGen.generate();
    }

    private static Enum extractFromEnum(final String className, final ClassNode classNode) {
        if (classNode == null) {
            throw new IllegalArgumentException("Class node for " + className + " is null");
        }

        final Enum genEnum = new Enum(className);
        for (FieldNode fieldNode : classNode.fields) {
            if ((fieldNode.access & Opcodes.ACC_ENUM) != 0) {
                genEnum.enumFields().add(new Field(fieldNode.name));
            }
        }
        return genEnum;
    }

    private static Enum extractFromFieldsWithId(final String className, final ClassNode classNode, final int idIndex) {
        if (classNode == null) {
            throw new IllegalArgumentException("Class node for " + className + " is null");
        }

        final MethodNode clinit = ASMUtils.getMethod(classNode, "<clinit>", "()V");
        final Map<String, Integer> idMap = new LinkedHashMap<>();
        for (int i = 0; i < clinit.instructions.size(); i++) {
            final AbstractInsnNode insn = clinit.instructions.get(i);
            if (insn instanceof MethodInsnNode methodInsn && methodInsn.getOpcode() == Opcodes.INVOKESPECIAL && methodInsn.owner.equals(classNode.name) && methodInsn.name.equals("<init>")) {
                final int argumentCount = Type.getArgumentTypes(methodInsn.desc).length;
                final String name = ((FieldInsnNode) clinit.instructions.get(i + 1)).name;
                final Number id = ASMUtils.toNumber(clinit.instructions.get(i - argumentCount + idIndex));
                idMap.put(name, id.intValue());
            }
        }

        final Enum genEnum = new Enum(className);
        int ordinal = 0;
        for (Map.Entry<String, Integer> entry : idMap.entrySet()) {
            if (entry.getValue() != ordinal) {
                throw new IllegalStateException("Expected ordinal " + ordinal + " but got " + entry.getValue() + " for field " + entry.getKey());
            }
            genEnum.enumFields().add(new Field(entry.getKey()));
            ordinal++;
        }
        return genEnum;
    }

}
