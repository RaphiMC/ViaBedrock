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
package net.raphimc.viabedrock.protocol.data.enums.bedrock;

public enum DeviceOS {

    UNKNOWN("Unknown"),
    ANDROID("Android"),
    IOS("iOS"),
    MAC_OS("macOS"),
    FIRE_OS("FireOS"),
    GEAR_VR("Gear VR"),
    HOLOLENS("Hololens"),
    UWP("UWP"),
    WIN32("Windows x86"),
    DEDICATED("Dedicated"),
    TVOS("Apple TV"),
    PS4("PS4"),
    SWITCH("Switch"),
    XBOX_ONE("Xbox One"),
    WINDOWS_PHONE("Windows Phone"),
    LINUX("Linux");

    private final String name;

    DeviceOS(final String name) {
        this.name = name;
    }

    public static DeviceOS fromId(final int id) {
        return id >= 0 && id < values().length ? values()[id] : UNKNOWN;
    }

    public String getDisplayName() {
        return this.name;
    }

}
