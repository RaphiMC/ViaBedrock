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

import com.viaversion.viaversion.api.connection.StorableObject;

public class ClientSettingsStorage implements StorableObject {

    private final String locale;
    private final int viewDistance;
    private final int chatVisibility;
    private final boolean chatColors;
    private final short skinParts;
    private final int mainHand;
    private final boolean textFiltering;
    private final boolean serverListing;

    public ClientSettingsStorage(final String locale, final int viewDistance, final int chatVisibility, final boolean chatColors, final short skinParts, final int mainHand, final boolean textFiltering, final boolean serverListing) {
        this.locale = locale;
        this.viewDistance = viewDistance;
        this.chatVisibility = chatVisibility;
        this.chatColors = chatColors;
        this.skinParts = skinParts;
        this.mainHand = mainHand;
        this.textFiltering = textFiltering;
        this.serverListing = serverListing;
    }

    public String getLocale() {
        return this.locale;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getChatVisibility() {
        return this.chatVisibility;
    }

    public boolean isChatColors() {
        return this.chatColors;
    }

    public short getSkinParts() {
        return this.skinParts;
    }

    public int getMainHand() {
        return this.mainHand;
    }

    public boolean isTextFiltering() {
        return this.textFiltering;
    }

    public boolean isServerListing() {
        return this.serverListing;
    }

}
