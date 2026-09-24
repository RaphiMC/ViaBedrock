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

import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.model.BlockState;

/**
 * A proposed mapping for one bedrock block state.
 *
 * @param bedrockBlockState The state which has no mapping
 * @param javaBlockState    The proposed java state, or null when nothing could be validated
 * @param strategy          How the proposal was found
 * @param template          The bedrock block the proposal was derived from, when there was one
 * @param score             How much the strategy is trusted, between 0 and 1
 * @param note              Extra context for the reviewer, for example which properties had to be defaulted
 */
public record BlockStateProposal(BedrockBlockState bedrockBlockState, BlockState javaBlockState, String strategy, String template, double score, String note) {

    public boolean resolved() {
        return this.javaBlockState != null;
    }

}
