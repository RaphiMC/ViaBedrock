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
package net.raphimc.viabedrock.protocol.types.model;

import com.google.common.collect.Sets;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandParamTypes;
import net.raphimc.viabedrock.protocol.model.CommandData;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.*;
import java.util.function.Consumer;

public class CommandDataArrayType extends Type<CommandData[]> {

    private static final int FLAG_VALID = 1 << 20;
    private static final int FLAG_ENUM = 1 << 21;
    private static final int FLAG_POSTFIX = 1 << 24;
    private static final int FLAG_DYNAMIC_ENUM = 1 << 26;
    private static final int FLAG_SUB_COMMAND = 1 << 27;

    public CommandDataArrayType() {
        super(CommandData[].class);
    }

    @Override
    public CommandData[] read(ByteBuf buffer) throws Exception {
        final String[] enumLiterals = BedrockTypes.STRING_ARRAY.read(buffer); // enum literals
        final String[] subCommandLiterals = BedrockTypes.STRING_ARRAY.read(buffer); // sub command literals
        final String[] postFixLiterals = BedrockTypes.STRING_ARRAY.read(buffer); // post fix literals

        final Type<? extends Number> indexType;
        if (enumLiterals.length <= 255) {
            indexType = Type.UNSIGNED_BYTE;
        } else if (enumLiterals.length <= 65535) {
            indexType = BedrockTypes.UNSIGNED_SHORT_LE;
        } else {
            indexType = BedrockTypes.UNSIGNED_INT_LE;
        }

        final CommandData.EnumData[] enumPalette = new CommandData.EnumData[BedrockTypes.UNSIGNED_VAR_INT.read(buffer)];
        final Map<String, CommandData.EnumData> enumPaletteMap = new HashMap<>(enumPalette.length);
        for (int i = 0; i < enumPalette.length; i++) {
            final String name = BedrockTypes.STRING.read(buffer); // name
            final int count = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // values count
            final Set<String> values = new HashSet<>(count);
            for (int j = 0; j < count; j++) {
                final int index = indexType.read(buffer).intValue(); // value
                if (index >= 0 && index < enumLiterals.length) {
                    values.add(enumLiterals[index]);
                } else {
                    values.add("default");
                }
            }

            final CommandData.EnumData enumData;
            if (enumPaletteMap.containsKey(name)) {
                enumData = enumPaletteMap.get(name);
                enumData.addValues(values);
            } else {
                enumPaletteMap.put(name, enumData = new CommandData.EnumData(name, values, false));
            }
            enumPalette[i] = enumData;
        }

        final CommandData.SubCommandData[] subCommands = new CommandData.SubCommandData[BedrockTypes.UNSIGNED_VAR_INT.read(buffer)];
        final Map<String, CommandData.SubCommandData> subCommandMap = new HashMap<>(subCommands.length);
        for (int i = 0; i < subCommands.length; i++) {
            final String name = BedrockTypes.STRING.read(buffer); // name
            final int valueCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
            final Map<String, Integer> values = new HashMap<>(valueCount);
            for (int j = 0; j < valueCount; j++) {
                final int index = buffer.readUnsignedShortLE(); // value
                final int type = buffer.readUnsignedShortLE(); // type
                if (index >= 0 && index < subCommandLiterals.length) {
                    values.put(subCommandLiterals[index], type);
                }
            }

            final CommandData.SubCommandData subCommandData;
            if (subCommandMap.containsKey(name)) {
                subCommandData = subCommandMap.get(name);
                subCommandData.addValues(values);
            } else {
                subCommandMap.put(name, subCommandData = new CommandData.SubCommandData(name, values));
            }
            subCommands[i] = subCommandData;
        }

        final List<Consumer<CommandData.EnumData[]>> dynamicEnumResolvers = new ArrayList<>();
        final CommandData[] commands = new CommandData[BedrockTypes.UNSIGNED_VAR_INT.read(buffer)];
        final Map<String, CommandData> commandMap = new HashMap<>(commands.length);
        for (int i = 0; i < commands.length; i++) {
            final String name = BedrockTypes.STRING.read(buffer); // name
            final String description = BedrockTypes.STRING.read(buffer); // description
            final int flags = buffer.readUnsignedShortLE(); // flags
            final short permission = buffer.readUnsignedByte(); // permission
            final int aliasIndex = buffer.readIntLE(); // alias
            final boolean validAliasPointer = aliasIndex >= 0 && aliasIndex < enumPalette.length;
            final CommandData.EnumData alias;
            if (validAliasPointer) {
                alias = enumPalette[aliasIndex];
            } else {
                alias = null;
            }

            final int subCommandCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
            for (int j = 0; j < subCommandCount; j++) {
                buffer.readUnsignedShortLE(); // idk
            }

            final CommandData.OverloadData[] overloads = new CommandData.OverloadData[BedrockTypes.UNSIGNED_VAR_INT.read(buffer)];
            for (int j = 0; j < overloads.length; j++) {
                final boolean chaining = buffer.readBoolean();
                final CommandData.OverloadData.ParamData[] params = new CommandData.OverloadData.ParamData[BedrockTypes.UNSIGNED_VAR_INT.read(buffer)];
                for (int k = 0; k < params.length; k++) {
                    final String paramName = BedrockTypes.STRING.read(buffer); // name
                    final int param = buffer.readIntLE(); // param
                    final boolean optional = buffer.readBoolean(); // optional
                    final short paramFlags = buffer.readUnsignedByte(); // flags
                    Integer type = null;
                    CommandData.EnumData enumData = null;
                    CommandData.SubCommandData subCommandData = null;
                    String postfix = null;

                    if ((param & FLAG_DYNAMIC_ENUM) != 0) {
                        final int index = param & ~FLAG_DYNAMIC_ENUM & ~FLAG_VALID;
                        final int finalK = k;
                        dynamicEnumResolvers.add(dynamicEnumPalette -> {
                            if (index >= 0 && index < dynamicEnumPalette.length) {
                                params[finalK] = new CommandData.OverloadData.ParamData(paramName, optional, paramFlags, null, dynamicEnumPalette[index], null, null);
                            } else {
                                params[finalK] = new CommandData.OverloadData.ParamData(paramName, optional, paramFlags, CommandParamTypes.TYPE_STRING, null, null, null);
                            }
                        });
                        continue;
                    } else if ((param & FLAG_POSTFIX) != 0) {
                        final int index = param & ~FLAG_POSTFIX;
                        postfix = postFixLiterals[index];
                    } else if ((param & FLAG_ENUM) != 0) {
                        final int index = param & ~FLAG_ENUM & ~FLAG_VALID;
                        enumData = enumPalette[index];
                    } else if ((param & FLAG_SUB_COMMAND) != 0) {
                        final int index = param & ~FLAG_SUB_COMMAND & ~FLAG_VALID;
                        subCommandData = subCommands[index];
                    } else if ((param & FLAG_VALID) != 0) {
                        type = param & ~FLAG_VALID;
                    }

                    params[k] = new CommandData.OverloadData.ParamData(paramName, optional, paramFlags, type, enumData, subCommandData, postfix);
                }
                overloads[j] = new CommandData.OverloadData(chaining, params);
            }

            CommandData commandData;
            if (commandMap.containsKey(name)) {
                final CommandData oldCommandData = commandMap.get(name);
                final CommandData.EnumData newAlias = validAliasPointer ? alias : oldCommandData.alias();
                final CommandData.OverloadData[] newOverloads = new CommandData.OverloadData[oldCommandData.overloads().length + overloads.length];
                System.arraycopy(oldCommandData.overloads(), 0, newOverloads, 0, oldCommandData.overloads().length);
                System.arraycopy(overloads, 0, newOverloads, oldCommandData.overloads().length, overloads.length);
                commandMap.put(name, commandData = new CommandData(name, oldCommandData.description(), oldCommandData.flags(), oldCommandData.permission(), newAlias, newOverloads));
            } else {
                commandMap.put(name, commandData = new CommandData(name, description, flags, permission, alias, overloads));
            }
            commands[i] = commandData;
        }

        final CommandData.EnumData[] dynamicEnumPalette = new CommandData.EnumData[BedrockTypes.UNSIGNED_VAR_INT.read(buffer)];
        final Map<String, CommandData.EnumData> dynamicEnumPaletteMap = new HashMap<>(dynamicEnumPalette.length);
        for (int i = 0; i < dynamicEnumPalette.length; i++) {
            final String name = BedrockTypes.STRING.read(buffer); // name
            final Set<String> values = Sets.newHashSet(BedrockTypes.STRING_ARRAY.read(buffer)); // values

            final CommandData.EnumData enumData;
            if (dynamicEnumPaletteMap.containsKey(name)) {
                enumData = dynamicEnumPaletteMap.get(name);
                enumData.addValues(values);
            } else {
                enumData = new CommandData.EnumData(name, values, true);
                dynamicEnumPaletteMap.put(name, enumData);
            }
            dynamicEnumPalette[i] = enumData;
        }
        dynamicEnumResolvers.forEach(c -> c.accept(dynamicEnumPalette));

        final int enumFlagsCount = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // enum flags count
        for (int i = 0; i < enumFlagsCount; i++) {
            final int valueIndex = buffer.readIntLE(); // value index
            final int enumIndex = buffer.readIntLE(); // enum index
            final byte[] flagsBytes = BedrockTypes.BYTE_ARRAY.read(buffer); // flags
            final String valueKey = enumLiterals[valueIndex];
            final CommandData.EnumData enumData = enumPalette[enumIndex];
            final Set<Short> flags = enumData.values().get(valueKey);
            if (flags == null) {
                continue;
            }

            for (byte flag : flagsBytes) {
                flags.add((short) (flag & 0xFF));
            }
        }

        return commands;
    }

    @Override
    public void write(ByteBuf buffer, CommandData[] value) throws Exception {
        throw new UnsupportedOperationException("Cannot serialize CommandDataArrayType");
    }

}
