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

import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayerAuthInputPacket_InputData;
import net.raphimc.viabedrock.protocol.model.Position2f;
import net.raphimc.viabedrock.protocol.model.Position3f;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {

    public static int ceil(final float f) {
        final int i = (int) f;
        return f > i ? i + 1 : i;
    }

    public static float clamp(final float value, final float min, final float max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public static int clamp(final int value, final int min, final int max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public static float lerp(final float progress, final float start, final float end) {
        return start + progress * (end - start);
    }

    public static int getOrFallback(final int value, final int min, final int max, final int fallback) {
        return value < min || value > max ? fallback : value;
    }

    public static byte float2Byte(final float f) {
        return (byte) (f * 256F / 360F);
    }

    public static float byte2Float(final byte b) {
        return b * 360F / 256F;
    }

    public static boolean roughlyEquals(final float a, final float b) {
        return roughlyEquals(a, b, 1E-4F);
    }

    public static boolean roughlyEquals(final float a, final float b, final float epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    public static float randomFloatInclusive(final float min, final float max) {
        if (min == max) {
            return min;
        } else {
            return min + ThreadLocalRandom.current().nextFloat() * (max - min);
        }
    }

    public static Position2f calculateMovementDirections(final Set<PlayerAuthInputPacket_InputData> authInputData, final boolean sneaking) {
        final float[] directions = new float[2];
        directions[0] = authInputData.contains(PlayerAuthInputPacket_InputData.Left) ? 1F : authInputData.contains(PlayerAuthInputPacket_InputData.Right) ? -1F : 0F;
        directions[1] = authInputData.contains(PlayerAuthInputPacket_InputData.Up) ? 1F : authInputData.contains(PlayerAuthInputPacket_InputData.Down) ? -1F : 0F;
        final boolean both = directions[0] != 0F && directions[1] != 0F;
        for (int i = 0; i < directions.length; i++) {
            if (both) {
                directions[i] *= (float) (1D / Math.sqrt(2));
            }
            if (sneaking) {
                directions[i] *= 0.3F;
            }
        }
        return new Position2f(directions[0], directions[1]);
    }

    public static Position3f calculateCameraOrientation(final float yaw, final float pitch) {
        final float yawRad = (float) Math.toRadians(yaw);
        final float pitchRad = (float) Math.toRadians(pitch);
        final float x = -MathUtil.clamp((float) Math.sin(yawRad) * (float) Math.cos(pitchRad), -1F, 1F);
        final float y = -MathUtil.clamp((float) Math.sin(pitchRad), -1F, 1F);
        final float z = MathUtil.clamp((float) Math.cos(yawRad) * (float) Math.cos(pitchRad), -1F, 1F);
        return new Position3f(x, y, z);
    }

}
