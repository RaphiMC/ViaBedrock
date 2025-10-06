/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.util;

import java.util.List;
import java.util.Locale;

public class ServerBlacklist {

    private static final List<String> BLACKLISTED_HOSTS = List.of(
            "hivebedrock.network",
            "geo.hivebedrock.network",
            "ca.hivebedrock.network",
            "fr.hivebedrock.network",
            "sg.hivebedrock.network"
    );

    public static boolean isBlacklisted(final String hostname) {
        return BLACKLISTED_HOSTS.contains(hostname.toLowerCase(Locale.ROOT));
    }

}
