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
package net.raphimc.viabedrock.protocol.model.inventory;

import java.util.List;

/**
 * A single request of the Bedrock ItemStackRequest packet.
 *
 * @param requestId         The client generated request id. Client generated ids are negative and odd.
 * @param actions           The actions which make up this request
 * @param filterStrings     Strings which have to be filtered by the server (anvil/cartography/book text)
 * @param filterStringCause The reason why the strings have to be filtered
 */
public record ItemStackRequest(int requestId, List<ItemStackRequestAction> actions, String[] filterStrings, int filterStringCause) {

    public ItemStackRequest(final int requestId, final List<ItemStackRequestAction> actions) {
        this(requestId, actions, new String[0], 0);
    }

}
