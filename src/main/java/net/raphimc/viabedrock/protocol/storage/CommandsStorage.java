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
package net.raphimc.viabedrock.protocol.storage;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.brigadier.*;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.ArgumentTypeRegistry;
import net.raphimc.viabedrock.protocol.model.CommandData;

import java.util.*;
import java.util.logging.Level;

public class CommandsStorage extends StoredObject {

    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private static final byte FLAG_EXECUTABLE = 0x4;
    private static final byte FLAG_REDIRECT = 0x8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 0x10;
    private static final String ASK_SERVER_SUGGESTION_TYPE = "minecraft:ask_server";
    private static final Command<UserConnection> NOOP = cmd -> 0;

    private final CommandData[] commands;
    private final Map<String, CommandData.EnumData> dynamicEnumMap = new HashMap<>();
    private CommandDispatcher<UserConnection> dispatcher;

    public CommandsStorage(final UserConnection user, final CommandData[] commands) {
        super(user);

        this.commands = commands;
        for (CommandData command : this.commands) {
            for (CommandData.OverloadData overload : command.overloads()) {
                for (CommandData.OverloadData.ParamData parameter : overload.parameters()) {
                    if (parameter.enumData() != null && parameter.enumData().dynamic()) {
                        this.dynamicEnumMap.put(parameter.enumData().name(), parameter.enumData());
                    }
                }
            }
        }

        this.buildCommandTree();
    }

    public void updateCommandTree() throws Exception {
        this.buildCommandTree();

        final PacketWrapper declareCommands = PacketWrapper.create(ClientboundPackets1_19_4.DECLARE_COMMANDS, this.getUser());
        this.writeCommandTree(declareCommands);
        declareCommands.send(BedrockProtocol.class);
    }

    public void writeCommandTree(final PacketWrapper wrapper) {
        final RootCommandNode<UserConnection> root = this.dispatcher.getRoot();
        final Map<CommandNode<UserConnection>, Integer> nodeIndices = this.getNodeIndices(root);
        final List<CommandNode<UserConnection>> nodes = new ArrayList<>(nodeIndices.keySet());
        nodes.sort(Comparator.comparingInt(nodeIndices::get));

        wrapper.write(Type.VAR_INT, nodes.size()); // node count
        for (CommandNode<UserConnection> node : nodes) {
            byte flags = 0;
            if (node.getRedirect() != null) {
                flags |= FLAG_REDIRECT;
            }
            if (node.getCommand() != null) {
                flags |= FLAG_EXECUTABLE;
            }

            if (node instanceof LiteralCommandNode) {
                flags |= TYPE_LITERAL;
            } else if (node instanceof ArgumentCommandNode) {
                final ArgumentCommandNode<UserConnection, ?> argumentCommandNode = (ArgumentCommandNode<UserConnection, ?>) node;
                flags |= TYPE_ARGUMENT;
                if (argumentCommandNode.getCustomSuggestions() != null) {
                    flags |= FLAG_CUSTOM_SUGGESTIONS;
                }
            } else if (!(node instanceof RootCommandNode)) {
                throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
            }

            wrapper.write(Type.BYTE, flags); // flags
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, node.getChildren().stream().mapToInt(nodeIndices::get).toArray()); // children node indices
            if (node.getRedirect() != null) {
                wrapper.write(Type.VAR_INT, nodeIndices.get(node.getRedirect())); // redirect node index
            }

            if (node instanceof LiteralCommandNode) {
                final LiteralCommandNode<UserConnection> literalCommandNode = (LiteralCommandNode<UserConnection>) node;
                wrapper.write(Type.STRING, literalCommandNode.getLiteral()); // literal name
            } else if (node instanceof ArgumentCommandNode) {
                final ArgumentCommandNode<UserConnection, ?> argumentCommandNode = (ArgumentCommandNode<UserConnection, ?>) node;
                final ArgumentTypeRegistry.ArgumentTypeMapping mapping = ArgumentTypeRegistry.getArgumentTypeMapping(argumentCommandNode.getType());
                wrapper.write(Type.STRING, argumentCommandNode.getName()); // argument name
                wrapper.write(Type.VAR_INT, mapping.id()); // argument type
                if (mapping.writer() != null) {
                    mapping.writer().accept(wrapper, argumentCommandNode.getType()); // argument data
                }
                if (argumentCommandNode.getCustomSuggestions() != null) {
                    wrapper.write(Type.STRING, ASK_SERVER_SUGGESTION_TYPE); // custom suggestions type
                }
            }
        }
        wrapper.write(Type.VAR_INT, nodeIndices.get(root)); // root node index
    }

    public Suggestions complete(final String message) {
        final StringReader reader = new StringReader(message);
        if (reader.canRead() && reader.peek() == '/') {
            reader.skip();
        }
        final ParseResults<UserConnection> parseResults = this.dispatcher.parse(reader, this.getUser());
        return this.dispatcher.getCompletionSuggestions(parseResults).join();
    }

    public CommandData.EnumData getDynamicEnum(final String name) {
        return this.dynamicEnumMap.get(name);
    }

    private void buildCommandTree() {
        this.dispatcher = new CommandDispatcher<>();
        // For translating the description of a CommandData use the TranslatorOptions.IGNORE_STARTING_PERCENT option

        for (CommandData command : this.commands) {
            final String name = command.alias() != null ? Iterables.getFirst(command.alias().values().keySet(), null) : command.name();
            if (name == null) continue;

            final LiteralArgumentBuilder<UserConnection> cmdBuilder = literal(name)/*.requires(user -> user.get(GameSessionStorage.class).getCommandPermission() >= command.permission())*/;
            for (CommandData.OverloadData overload : command.overloads()) {
                ArgumentBuilder<UserConnection, ?> last = null;
                boolean hasRedirect = false;
                for (int i = overload.parameters().length - 1; i >= 0; i--) {
                    final CommandData.OverloadData.ParamData parameter = overload.parameters()[i];

                    final ArgumentBuilder<UserConnection, ?> argument;
                    if (parameter.enumData() != null) {
                        final ArgumentType<?> argumentType = EnumArgumentType.enumData(parameter.enumData());
                        argument = argument(parameter.name() + ": " + parameter.enumData().name(), argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.subCommandData() != null) {
                        /*argument = argument(parameter.name() + ": " + parameter.subCommandData().name(), StringArgumentType.word()).suggests((context, builder) -> {
                            return SuggestionsUtil.suggestMatching(parameter.subCommandData().values().keySet(), builder);
                        });*/
                        // TODO
                        continue;
                    } else if (parameter.postfix() != null) {
                        // TODO
                        continue;
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_INT) {
                        argument = argument(parameter.name() + ": int", IntegerArgumentType.integer());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_FLOAT1 || parameter.type() == CommandData.OverloadData.ParamData.TYPE_FLOAT2) {
                        argument = argument(parameter.name() + ": float", FloatArgumentType.floatArg());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_VALUE) {
                        final ArgumentType<?> argumentType = ValueArgumentType.value();
                        argument = argument(parameter.name() + ": value", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_WILDCARD_INT) {
                        final ArgumentType<?> argumentType = WildcardIntegerArgumentType.wildcardInteger();
                        argument = argument(parameter.name() + ": wildcard int", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_OPERATOR) {
                        argument = argument(parameter.name() + ": operator", OperatorArgumentType.operator());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_COMPARE_OPERATOR) {
                        final ArgumentType<?> argumentType = CompareOperatorArgumentType.compareOperator();
                        argument = argument(parameter.name() + ": compare operator", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_TARGET1 || parameter.type() == CommandData.OverloadData.ParamData.TYPE_TARGET2) {
                        // TODO
                        final ArgumentType<?> argumentType = TargetArgumentType.target();
                        argument = argument(parameter.name() + ": target", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_FILE_PATH) {
                        argument = argument(parameter.name() + ": filepath", StringArgumentType.string());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_INT_RANGE) {
                        final ArgumentType<?> argumentType = IntegerRangeArgumentType.integerRange();
                        argument = argument(parameter.name() + ": integer range", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_EQUIPMENT_SLOT) {
                        final ArgumentType<?> argumentType = EquipmentSlotArgumentType.equipmentSlot();
                        argument = argument(parameter.name() + ": equipment slots", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_STRING) {
                        argument = argument(parameter.name() + ": string", StringArgumentType.string());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_BLOCK_POSITION) {
                        argument = argument(parameter.name() + ": x y z", BlockPositionArgumentType.blockPosition());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_POSITION) {
                        argument = argument(parameter.name() + ": x y z", PositionArgumentType.position());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_MESSAGE) {
                        argument = argument(parameter.name() + ": message", StringArgumentType.greedyString());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_TEXT) {
                        argument = argument(parameter.name() + ": text", StringArgumentType.greedyString());
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_JSON) {
                        final ArgumentType<?> argumentType = JsonArgumentType.json();
                        argument = argument(parameter.name() + ": json", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_BLOCK_STATES) {
                        final ArgumentType<?> argumentType = BlockStatesArgumentType.blockStates();
                        argument = argument(parameter.name() + ": block states", argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.type() == CommandData.OverloadData.ParamData.TYPE_COMMAND) {
                        hasRedirect = true;
                        last = null;
                        continue;
                    } else {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown command parameter type: " + parameter.type());
                        argument = argument(parameter.name() + ": unknown", StringArgumentType.greedyString());
                    }

                    if (hasRedirect) {
                        argument.redirect(this.dispatcher.getRoot());
                        hasRedirect = false;
                    } else if (last != null) {
                        argument.then(last);
                        if (overload.parameters()[i + 1].optional()) {
                            argument.executes(NOOP);
                        }
                    } else {
                        argument.executes(NOOP);
                    }
                    last = argument;
                }
                if (hasRedirect) {
                    cmdBuilder.redirect(this.dispatcher.getRoot());
                } else if (last != null) {
                    cmdBuilder.then(last);
                } else {
                    cmdBuilder.executes(NOOP);
                }
            }

            final LiteralCommandNode<UserConnection> node = this.dispatcher.register(cmdBuilder);
            if (command.alias() != null && command.alias().values().size() > 1) {
                for (String alias : command.alias().values().keySet()) {
                    if (!alias.equals(node.getName())) {
                        this.dispatcher.register(literal(alias).redirect(node));
                    }
                }
            }
        }
    }

    private Map<CommandNode<UserConnection>, Integer> getNodeIndices(final RootCommandNode<UserConnection> root) {
        final Map<CommandNode<UserConnection>, Integer> nodes = new HashMap<>();
        final Queue<CommandNode<UserConnection>> queue = new ArrayDeque<>();
        queue.add(root);

        CommandNode<UserConnection> node;
        while ((node = queue.poll()) != null) {
            if (!nodes.containsKey(node)) {
                nodes.put(node, nodes.size());
                queue.addAll(node.getChildren());
                if (node.getRedirect() != null) {
                    queue.add(node.getRedirect());
                }
            }
        }

        return nodes;
    }

    private static LiteralArgumentBuilder<UserConnection> literal(final String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    private static <T> RequiredArgumentBuilder<UserConnection, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

}
