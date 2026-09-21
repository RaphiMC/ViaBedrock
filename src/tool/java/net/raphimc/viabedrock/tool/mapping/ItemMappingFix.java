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
package net.raphimc.viabedrock.tool.mapping;

/**
 * A suggested change to one java_id in the item mappings.
 *
 * @param bedrockIdentifier The bedrock item
 * @param path              Where the java_id sits, either {@code block/<block state>} or {@code meta/<meta>}
 * @param currentJavaId     What it maps to now
 * @param suggestedJavaId   What the rest of the data suggests
 * @param reason            Why, for example which sibling item it was derived from
 */
public record ItemMappingFix(String bedrockIdentifier, String path, String currentJavaId, String suggestedJavaId, String reason) {

    public String key() {
        return this.bedrockIdentifier + " " + this.path;
    }

}
