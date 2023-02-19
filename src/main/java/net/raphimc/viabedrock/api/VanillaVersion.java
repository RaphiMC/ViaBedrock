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
package net.raphimc.viabedrock.api;

public enum VanillaVersion {

    v0_13_0("0.13.0"),
    v0_13_1("0.13.1"),
    v0_13_2("0.13.2"),
    v0_14_0("0.14.0"),
    v0_14_2("0.14.2"),
    v0_15_0("0.15.0"),
    v0_15_4("0.15.4"),
    v0_15_6("0.15.6"),
    v0_15_7("0.15.7"),
    v0_15_8("0.15.8"),
    v0_15_9("0.15.9"),
    v0_15_10("0.15.10"),
    v0_16_0("0.16.0"),
    v0_16_1("0.16.1"),
    v1_0_0("1.0.0"),
    v1_1_5("1.1.5"),
    v1_2_2("1.2.2"),
    v1_2_3("1.2.3"),
    v1_2_5("1.2.5"),
    v1_2_8("1.2.8"),
    v1_2_10("1.2.10"),
    v1_4_2("1.4.2"),
    v1_5_0("1.5.0"),
    v1_5_2("1.5.2"),
    v1_5_3("1.5.3"),
    v1_6_0("1.6.0"),
    v1_6_1("1.6.1"),
    v1_7_0("1.7.0"),
    v1_7_1("1.7.1"),
    v1_8_0("1.8.0"),
    v1_8_1("1.8.1"),
    v1_9_0("1.9.0"),
    v1_10_0("1.10.0"),
    v1_10_1("1.10.1"),
    v1_11_0("1.11.0"),
    v1_11_1("1.11.1"),
    v1_11_3("1.11.3"),
    v1_11_4("1.11.4"),
    v1_12_0("1.12.0"),
    v1_12_1("1.12.1"),
    v1_13_0("1.13.0"),
    v1_13_1("1.13.1"),
    v1_14_0("1.14.0"),
    v1_14_1("1.14.1"),
    v1_14_2("1.14.2"),
    v1_14_20("1.14.20"),
    v1_14_25("1.14.25"),
    v1_14_30("1.14.30"),
    v1_14_60("1.14.60"),
    v1_15_0("1.15.0"),
    v1_16_0("1.16.0"),
    v1_16_1("1.16.1"),
    v1_16_10("1.16.10"),
    v1_16_20("1.16.20"),
    v1_16_40("1.16.40"),
    v1_16_100("1.16.100"),
    v1_16_200("1.16.200"),
    v1_16_201("1.16.201"),
    v1_16_210("1.16.210"),
    v1_16_220("1.16.220"),
    v1_16_221("1.16.221"),
    v1_16_230("1.16.230"),
    v1_17_0("1.17.0"),
    v1_17_2("1.17.2"),
    v1_17_10("1.17.10"),
    v1_17_11("1.17.11"),
    v1_17_20("1.17.20"),
    v1_17_30("1.17.30"),
    v1_17_32("1.17.32"),
    v1_17_34("1.17.34"),
    v1_17_40("1.17.40"),
    v1_17_41("1.17.41"),
    v1_18_0("1.18.0"),
    v1_18_1("1.18.1"),
    v1_18_2("1.18.2"),
    v1_18_10("1.18.10"),
    v1_18_12("1.18.12"),
    v1_18_20("1.18.20"),
    v1_18_30("1.18.30"),
    v1_18_31("1.18.31"),
    v1_19_0("1.19.0"),
    v1_19_2("1.19.2"),
    v1_19_10("1.19.10"),
    v1_19_11("1.19.11"),
    v1_19_20("1.19.20"),
    v1_19_21("1.19.21"),
    v1_19_22("1.19.22"),
    v1_19_30("1.19.30"),
    v1_19_31("1.19.31"),
    v1_19_40("1.19.40"),
    v1_19_41("1.19.41"),
    v1_19_50("1.19.50"),
    v1_19_51("1.19.51"),
    v1_19_60("1.19.60"),
    v1_19_62("1.19.62"),
    v1_19_70("1.19.70"),
    LATEST("*");

    private final String version;

    VanillaVersion(final String version) {
        this.version = version;
    }

    public static VanillaVersion fromString(final String version) {
        for (final VanillaVersion vanillaVersion : VanillaVersion.values()) {
            if (vanillaVersion.version.equals(version)) {
                return vanillaVersion;
            }
        }

        return null;
    }

}
