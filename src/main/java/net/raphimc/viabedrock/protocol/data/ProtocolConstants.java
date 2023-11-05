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
package net.raphimc.viabedrock.protocol.data;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

public class ProtocolConstants {

    public static final ProtocolVersion JAVA_VERSION = ProtocolVersion.v1_20_2;
    public static final int JAVA_PACK_VERSION = 18;
    public static final TextComponentSerializer JAVA_TEXT_COMPONENT_SERIALIZER = TextComponentSerializer.V1_19_4;
    public static final int JAVA_PAINTING_VARIANT_ID = 8;

    public static final String BEDROCK_VERSION_NAME = "1.20.40";
    public static final int BEDROCK_PROTOCOL_VERSION = 622;
    public static final int BEDROCK_COMMAND_VERSION = 36;
    public static final short BEDROCK_REQUEST_CHUNK_RADIUS_MAX_RADIUS = 28;

}
