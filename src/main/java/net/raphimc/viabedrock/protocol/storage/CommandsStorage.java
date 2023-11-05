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
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.util.Pair;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.lenni0451.mcstructs_bedrock.text.utils.TranslatorOptions;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.brigadier.*;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.ArgumentTypeRegistry;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandPermissions;
import net.raphimc.viabedrock.protocol.model.CommandData;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class CommandsStorage extends StoredObject {

    private static final byte TYPE_LITERAL = 1 << 0;
    private static final byte TYPE_ARGUMENT = 1 << 1;
    private static final byte FLAG_EXECUTABLE = 1 << 2;
    private static final byte FLAG_REDIRECT = 1 << 3;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 1 << 4;
    private static final String ASK_SERVER_SUGGESTION_TYPE = "minecraft:ask_server";

    public static final int RESULT_ALLOW_SEND = -1;
    public static final int RESULT_NO_OP = 0;
    public static final int RESULT_CANCEL = 1;

    private static final Command<UserConnection> ALLOW_SEND = cmd -> RESULT_ALLOW_SEND;
    private static final Command<UserConnection> NOOP = cmd -> RESULT_NO_OP;

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

        final PacketWrapper declareCommands = PacketWrapper.create(ClientboundPackets1_20_2.DECLARE_COMMANDS, this.getUser());
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

    public int execute(final String message) {
        final StringReader reader = new StringReader(message);
        if (reader.canRead() && reader.peek() == '/') {
            reader.skip();
        }
        final ParseResults<UserConnection> parseResults = this.dispatcher.parse(reader, this.getUser());
        try {
            return this.dispatcher.execute(parseResults);
        } catch (Throwable ignored) {
            if (!parseResults.getContext().getNodes().isEmpty()) {
                return RESULT_ALLOW_SEND;
            }
        }

        return RESULT_NO_OP;
    }

    public CommandData.EnumData getDynamicEnum(final String name) {
        return this.dynamicEnumMap.get(name);
    }

    private void buildCommandTree() {
        this.dispatcher = new CommandDispatcher<>();
        final GameSessionStorage gameSession = this.getUser().get(GameSessionStorage.class);
        final Command<UserConnection> action = gameSession.areCommandsEnabled() ? NOOP : ALLOW_SEND;

        for (CommandData command : this.commands) {
            final String name = command.alias() != null ? Iterables.getFirst(command.alias().values().keySet(), null) : command.name();
            if (name == null) continue;

            if (gameSession.getCommandPermission() < command.permission()) {
                continue;
            }
            if ((command.flags() & CommandData.FLAG_HIDDEN_FROM_COMMAND_BLOCK) != 0 && (command.flags() & CommandData.FLAG_HIDDEN_FROM_PLAYER) != 0 && (command.flags() & CommandData.FLAG_HIDDEN_FROM_AUTOMATION) != 0) {
                continue;
            }
            if (!gameSession.areCommandsEnabled() && (command.flags() & CommandData.FLAG_NOT_CHEAT) == 0) {
                continue;
            }

            final LiteralArgumentBuilder<UserConnection> cmdBuilder = literal(name);
            for (CommandData.OverloadData overload : command.overloads()) {
                ArgumentBuilder<UserConnection, ?> last = null;
                boolean hasRedirect = false;
                for (int i = overload.parameters().length - 1; i >= 0; i--) {
                    final CommandData.OverloadData.ParamData parameter = overload.parameters()[i];

                    final ArgumentBuilder<UserConnection, ?> argument;
                    if (parameter.enumData() != null) {
                        if ((parameter.flags() & CommandData.OverloadData.ParamData.FLAG_ENUM_AS_CHAINED_COMMAND) != 0) {
                            throw new UnsupportedOperationException("Enum as chained command is not supported yet");
                        }

                        final ArgumentType<?> argumentType;
                        if ((parameter.flags() & CommandData.OverloadData.ParamData.FLAG_HAS_ENUM_CONSTRAINT) != 0) {
                            final Map<String, Set<Short>> enumDataValues = new HashMap<>(parameter.enumData().values());
                            enumDataValues.entrySet().removeIf(entry -> {
                                if (!gameSession.areCommandsEnabled() && entry.getValue().contains(CommandData.EnumData.FLAG_CHEATS_ENABLED)) {
                                    return true;
                                }
                                if (entry.getValue().contains(CommandData.EnumData.FLAG_OPERATOR_PERMISSIONS) && gameSession.getCommandPermission() < CommandPermissions.OPERATOR) {
                                    return true;
                                }
                                return entry.getValue().contains(CommandData.EnumData.FLAG_HOST_PERMISSIONS) && gameSession.getCommandPermission() < CommandPermissions.HOST;
                            });
                            final Set<String> values = new HashSet<>(enumDataValues.keySet());
                            enumDataValues.entrySet().removeIf(entry -> entry.getValue().contains(CommandData.EnumData.FLAG_HIDE_FROM_COMPLETIONS));
                            argumentType = EnumArgumentType.valuesAndCompletions(values, enumDataValues.keySet());
                        } else {
                            argumentType = EnumArgumentType.values(parameter.enumData().values().keySet());
                        }

                        argument = argument(parameter.name() + ": " + parameter.enumData().name(), argumentType).suggests(argumentType::listSuggestions);
                    } else if (parameter.subCommandData() != null) {
                        // TODO: Enhancement: Sub commands
                        continue;
                    } else if (parameter.postfix() != null) {
                        // TODO: Enhancement: Postfix support
                        continue;
                    } else if (parameter.type() == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid command parameter: " + parameter);
                        argument = argument(parameter.name() + ": unknown", StringArgumentType.greedyString());
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
                        // TODO: Enhancement: Implement target argument type
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
                            argument.executes(action);
                        }
                    } else {
                        argument.executes(action);
                    }
                    last = argument;
                }
                if (hasRedirect) {
                    cmdBuilder.redirect(this.dispatcher.getRoot());
                } else if (last != null) {
                    cmdBuilder.then(last);
                } else {
                    cmdBuilder.executes(action);
                }
            }

            final LiteralCommandNode<UserConnection> node = new BedrockLiteralCommandNode<>(command, cmdBuilder.build());
            this.dispatcher.getRoot().addChild(node);
            if (command.alias() != null && command.alias().values().size() > 1) {
                for (String alias : command.alias().values().keySet()) {
                    if (!alias.equals(node.getName())) {
                        this.dispatcher.register(literal(alias).redirect(node));
                    }
                }
            }
        }

        if (this.dispatcher.getRoot().getChild("help") == null) {
            final ResourcePacksStorage resourcePacksStorage = this.getUser().get(ResourcePacksStorage.class);
            final Function<String, String> translator = resourcePacksStorage.getTranslationLookup();
            final LiteralArgumentBuilder<UserConnection> cmdBuilder = literal("help");
            cmdBuilder.executes(cmd -> {
                PacketFactory.sendSystemChat(cmd.getSource(), TextUtil.stringToGson("§c" + BedrockTranslator.translate("%commands.generic.usage", translator, new Object[]{"/help <command>"})));
                return RESULT_CANCEL;
            });
            cmdBuilder.then(argument("command", StringArgumentType.greedyString()).suggests((context, builder) -> SuggestionsUtil.suggestMatching(this.dispatcher.getRoot().getChildren().stream().map(c -> {
                final String description;
                if (c instanceof BedrockLiteralCommandNode) {
                    description = ((BedrockLiteralCommandNode<UserConnection>) c).getCommandData().description();
                } else if (c.getName().equals("help") || c.getName().equals("?")) {
                    description = "commands.help.description";
                } else {
                    description = null;
                }
                return new Pair<>(c.getName(), description != null ? BedrockTranslator.translate(description, translator, new Object[0], TranslatorOptions.IGNORE_STARTING_PERCENT) : null);
            }), builder)).executes(cmd -> {
                final String commandName = StringArgumentType.getString(cmd, "command");
                CommandNode<UserConnection> node = this.dispatcher.getRoot().getChild(commandName);
                final List<String> lines = new ArrayList<>();
                if (node != null) {
                    while (node.getRedirect() != null) {
                        node = node.getRedirect();
                    }
                    lines.add(resourcePacksStorage.getTranslations().get("commands.generic.usage.noparam"));
                    final String[] usage = this.dispatcher.getAllUsage(node, cmd.getSource(), true);
                    if (usage.length == 0) {
                        lines.add("- /" + node.getName());
                    } else {
                        for (int i = 0; i < usage.length; i++) {
                            usage[i] = "- /" + node.getName() + " " + usage[i];
                        }
                    }
                    Collections.addAll(lines, usage);
                } else {
                    lines.add("§c" + BedrockTranslator.translate("%commands.generic.unknown", translator, new Object[]{commandName}));
                }

                for (String line : lines) {
                    PacketFactory.sendSystemChat(cmd.getSource(), TextUtil.stringToGson(line));
                }
                return RESULT_CANCEL;
            }));
            this.dispatcher.register(literal("?").redirect(this.dispatcher.register(cmdBuilder)));
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
