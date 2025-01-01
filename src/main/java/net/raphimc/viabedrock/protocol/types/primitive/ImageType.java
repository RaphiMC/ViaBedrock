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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.awt.image.BufferedImage;

public class ImageType extends Type<BufferedImage> {

    public ImageType() {
        super(BufferedImage.class);
    }

    @Override
    public BufferedImage read(ByteBuf buffer) {
        final int width = buffer.readIntLE();
        final int height = buffer.readIntLE();
        final byte[] data = BedrockTypes.BYTE_ARRAY.read(buffer);

        if (width <= 0 || height <= 0 || data.length == 0 || data.length != width * height * 4) {
            return null;
        }

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int index = (y * width + x) * 4;
                final int argb = ((data[index + 3] & 0xFF) << 24) | ((data[index] & 0xFF) << 16) | ((data[index + 1] & 0xFF) << 8) | data[index + 2] & 0xFF;
                image.setRGB(x, y, argb);
            }
        }
        return image;
    }

    @Override
    public void write(ByteBuf buffer, BufferedImage value) {
        if (value == null) {
            buffer.writeIntLE(0);
            buffer.writeIntLE(0);
            BedrockTypes.BYTE_ARRAY.write(buffer, new byte[0]);
            return;
        }

        buffer.writeIntLE(value.getWidth());
        buffer.writeIntLE(value.getHeight());
        BedrockTypes.BYTE_ARRAY.write(buffer, getImageData(value));
    }

    public static byte[] getImageData(final BufferedImage image) {
        final byte[] data = new byte[image.getWidth() * image.getHeight() * 4];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int argb = image.getRGB(x, y);
                final int index = (y * image.getWidth() + x) * 4;
                data[index] = (byte) ((argb >> 16) & 0xFF);
                data[index + 1] = (byte) ((argb >> 8) & 0xFF);
                data[index + 2] = (byte) (argb & 0xFF);
                data[index + 3] = (byte) ((argb >> 24) & 0xFF);
            }
        }
        return data;
    }

}
