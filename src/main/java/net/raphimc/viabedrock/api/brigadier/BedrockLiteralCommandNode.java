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
package net.raphimc.viabedrock.api.brigadier;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.raphimc.viabedrock.protocol.model.CommandData;

public class BedrockLiteralCommandNode<S> extends LiteralCommandNode<S> {

    private final CommandData commandData;

    public BedrockLiteralCommandNode(final CommandData commandData, final LiteralCommandNode<S> node) {
        super(node.getLiteral(), node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());
        node.getChildren().forEach(this::addChild);

        this.commandData = commandData;
    }

    public CommandData getCommandData() {
        return this.commandData;
    }

}
