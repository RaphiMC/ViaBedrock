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

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntImmutablePair;
import com.viaversion.viaversion.libs.fastutil.ints.IntIntPair;
import com.viaversion.viaversion.util.Pair;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.lenni0451.mcstructs_bedrock.text.utils.TranslatorOptions;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.CommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CommandsStorage extends StoredObject {

    private final CommandData[] commands;

    public CommandsStorage(final UserConnection user, final CommandData[] commands) {
        super(user);

        this.commands = commands;
    }

    // TODO: Enhancement: Add tab completion for arguments
    public Pair<IntIntPair, List<Pair<String, String>>> complete(final String message) {
        final Function<String, String> translator = k -> BedrockProtocol.MAPPINGS.getTranslations().getOrDefault(k, k);

        final List<Pair<String, String>> completions = new ArrayList<>();
        int startIndex = 0;
        int endIndex = 0;

        final String[] split = message.split(" ", 2);
        final String cmdString = split[0];
        final String args = split.length > 1 ? split[1] : "";

        CommandData cmd = null;
        for (CommandData commandData : this.commands) {
            for (String alias : commandData.alias().values().keySet()) {
                if (alias.equals(cmdString)) {
                    cmd = commandData;
                }
                if (args.isEmpty() && !message.endsWith(" ") && alias.startsWith(cmdString)) {
                    completions.add(new Pair<>(alias, BedrockTranslator.translate(commandData.description(), translator, new Object[0], TranslatorOptions.IGNORE_STARTING_PERCENT)));
                }
            }
        }

        if (!completions.isEmpty()) {
            startIndex = 1;
            endIndex = startIndex + cmdString.length();
        } else {

        }

        return new Pair<>(new IntIntImmutablePair(startIndex, endIndex), completions);
    }

}
