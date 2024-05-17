/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.Experiment;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ExperimentType extends Type<Experiment> {

    public ExperimentType() {
        super(Experiment.class);
    }

    @Override
    public Experiment read(ByteBuf buffer) {
        return new Experiment(BedrockTypes.STRING.read(buffer), buffer.readBoolean());
    }

    @Override
    public void write(ByteBuf buffer, Experiment value) {
        BedrockTypes.STRING.write(buffer, value.name());
        buffer.writeBoolean(value.enabled());
    }

}
