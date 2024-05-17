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
package net.raphimc.viabedrock.protocol.data;

import com.mojang.brigadier.arguments.*;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viabedrock.api.brigadier.*;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ArgumentTypeRegistry {

    private static final Map<Class<? extends ArgumentType<?>>, ArgumentTypeMapping> ARGUMENT_TYPES = new HashMap<>();

    public static void init() {
        if (!ARGUMENT_TYPES.isEmpty()) throw new IllegalStateException("Argument types already initialized");

        register(BoolArgumentType.class, "brigadier:bool", null);
        register(FloatArgumentType.class, "brigadier:float", (wrapper, argumentType) -> {
            final boolean hasMin = argumentType.getMinimum() != -Float.MAX_VALUE;
            final boolean hasMax = argumentType.getMaximum() != Float.MAX_VALUE;
            final byte flags = (byte) ((hasMin ? 1 : 0) | (hasMax ? 2 : 0));
            wrapper.write(Types.BYTE, flags); // flags
            if (hasMin) wrapper.write(Types.FLOAT, argumentType.getMinimum()); // min value
            if (hasMax) wrapper.write(Types.FLOAT, argumentType.getMaximum()); // max value
        });
        register(DoubleArgumentType.class, "brigadier:double", (wrapper, argumentType) -> {
            final boolean hasMin = argumentType.getMinimum() != -Double.MAX_VALUE;
            final boolean hasMax = argumentType.getMaximum() != Double.MAX_VALUE;
            final byte flags = (byte) ((hasMin ? 1 : 0) | (hasMax ? 2 : 0));
            wrapper.write(Types.BYTE, flags); // flags
            if (hasMin) wrapper.write(Types.DOUBLE, argumentType.getMinimum()); // min value
            if (hasMax) wrapper.write(Types.DOUBLE, argumentType.getMaximum()); // max value
        });
        register(IntegerArgumentType.class, "brigadier:integer", (wrapper, argumentType) -> {
            final boolean hasMin = argumentType.getMinimum() != Integer.MIN_VALUE;
            final boolean hasMax = argumentType.getMaximum() != Integer.MAX_VALUE;
            final byte flags = (byte) ((hasMin ? 1 : 0) | (hasMax ? 2 : 0));
            wrapper.write(Types.BYTE, flags); // flags
            if (hasMin) wrapper.write(Types.INT, argumentType.getMinimum()); // min value
            if (hasMax) wrapper.write(Types.INT, argumentType.getMaximum()); // max value
        });
        register(LongArgumentType.class, "brigadier:long", (wrapper, argumentType) -> {
            final boolean hasMin = argumentType.getMinimum() != Long.MIN_VALUE;
            final boolean hasMax = argumentType.getMaximum() != Long.MAX_VALUE;
            final byte flags = (byte) ((hasMin ? 1 : 0) | (hasMax ? 2 : 0));
            wrapper.write(Types.BYTE, flags); // flags
            if (hasMin) wrapper.write(Types.LONG, argumentType.getMinimum()); // min value
            if (hasMax) wrapper.write(Types.LONG, argumentType.getMaximum()); // max value
        });
        register(StringArgumentType.class, "brigadier:string", (wrapper, argumentType) -> {
            wrapper.write(Types.VAR_INT, argumentType.getType().ordinal()); // type
        });
        register(EnumArgumentType.class, "brigadier:string", (wrapper, argumentType) -> {
            wrapper.write(Types.VAR_INT, StringArgumentType.StringType.SINGLE_WORD.ordinal()); // type
        });
        register(ValueArgumentType.class, "minecraft:angle", null);
        register(WildcardIntegerArgumentType.class, "brigadier:integer", (wrapper, argumentType) -> {
            wrapper.write(Types.BYTE, (byte) 0); // flags
        });
        register(OperatorArgumentType.class, "minecraft:operation", null);
        register(CompareOperatorArgumentType.class, "brigadier:string", (wrapper, argumentType) -> {
            wrapper.write(Types.VAR_INT, StringArgumentType.StringType.SINGLE_WORD.ordinal()); // type
        });
        register(TargetArgumentType.class, "minecraft:entity", (wrapper, argumentType) -> {
            wrapper.write(Types.BYTE, (byte) 0); // flags
        });
        register(IntegerRangeArgumentType.class, "minecraft:int_range", null);
        register(EquipmentSlotArgumentType.class, "brigadier:string", (wrapper, argumentType) -> {
            wrapper.write(Types.VAR_INT, StringArgumentType.StringType.SINGLE_WORD.ordinal()); // type
        });
        register(BlockPositionArgumentType.class, "minecraft:block_pos", null);
        register(PositionArgumentType.class, "minecraft:vec3", null);
        register(JsonArgumentType.class, "brigadier:string", (wrapper, argumentType) -> {
            wrapper.write(Types.VAR_INT, StringArgumentType.StringType.GREEDY_PHRASE.ordinal()); // type
        });
        register(BlockStatesArgumentType.class, "brigadier:string", (wrapper, argumentType) -> {
            wrapper.write(Types.VAR_INT, StringArgumentType.StringType.GREEDY_PHRASE.ordinal()); // type
        });
    }

    public static ArgumentTypeMapping getArgumentTypeMapping(final ArgumentType<?> type) {
        final ArgumentTypeMapping value = ARGUMENT_TYPES.get(type.getClass());
        if (value == null) {
            throw new IllegalArgumentException("Unknown argument type: " + type.getClass().getName());
        }

        return value;
    }

    private static <T extends ArgumentType<?>> void register(final Class<T> clazz, final String name, final BiConsumer<PacketWrapper, T> writer) {
        if (!BedrockProtocol.MAPPINGS.getJavaCommandArgumentTypes().containsKey(name)) {
            throw new IllegalArgumentException("Unknown java argument type: " + name);
        }

        ARGUMENT_TYPES.put(clazz, new ArgumentTypeMapping(BedrockProtocol.MAPPINGS.getJavaCommandArgumentTypes().get(name), (BiConsumer<PacketWrapper, ArgumentType<?>>) writer));
    }

    public static class ArgumentTypeMapping {

        private final int id;
        private final BiConsumer<PacketWrapper, ArgumentType<?>> writer;

        public ArgumentTypeMapping(final int id, final BiConsumer<PacketWrapper, ArgumentType<?>> writer) {
            this.id = id;
            this.writer = writer;
        }

        public int id() {
            return this.id;
        }

        public BiConsumer<PacketWrapper, ArgumentType<?>> writer() {
            return this.writer;
        }

    }

}
