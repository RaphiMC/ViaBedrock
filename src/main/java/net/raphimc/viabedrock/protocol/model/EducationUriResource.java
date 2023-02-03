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
package net.raphimc.viabedrock.protocol.model;

import java.util.Objects;

public class EducationUriResource {

    private final String buttonName;
    private final String linkUri;

    public EducationUriResource(String buttonName, String linkUri) {
        this.buttonName = buttonName;
        this.linkUri = linkUri;
    }

    public EducationUriResource(EducationUriResource resource) {
        this.buttonName = resource.buttonName;
        this.linkUri = resource.linkUri;
    }

    public String buttonName() {
        return this.buttonName;
    }

    public String linkUri() {
        return this.linkUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EducationUriResource that = (EducationUriResource) o;
        return Objects.equals(buttonName, that.buttonName) && Objects.equals(linkUri, that.linkUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buttonName, linkUri);
    }

    @Override
    public String toString() {
        return "EducationSharedUriResource{" +
                "buttonName='" + buttonName + '\'' +
                ", linkUri='" + linkUri + '\'' +
                '}';
    }
}
