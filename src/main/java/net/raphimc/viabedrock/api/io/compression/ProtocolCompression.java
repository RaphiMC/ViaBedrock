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
package net.raphimc.viabedrock.api.io.compression;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.PacketCompressionAlgorithm;

public class ProtocolCompression {

    private final CompressionAlgorithm preferredCompressionAlgorithm;
    private final int threshold;
    private ZLibCompression zLibCompression;
    private SnappyCompression snappyCompression;

    public ProtocolCompression(final PacketCompressionAlgorithm preferredCompressionAlgorithm, final int threshold) {
        this.preferredCompressionAlgorithm = this.getCompressionAlgorithm(preferredCompressionAlgorithm);
        this.threshold = threshold;
    }

    public void end() {
        if (this.zLibCompression != null) {
            this.zLibCompression.end();
        }
        if (this.snappyCompression != null) {
            this.snappyCompression.end();
        }
    }

    public CompressionAlgorithm getCompressionAlgorithmForSize(final int inputSize) {
        if (inputSize < this.threshold) {
            return NoopCompression.INSTANCE;
        } else {
            return this.preferredCompressionAlgorithm;
        }
    }

    public CompressionAlgorithm getCompressionAlgorithm(final PacketCompressionAlgorithm algorithm) {
        return switch (algorithm) {
            case None -> NoopCompression.INSTANCE;
            case ZLib -> {
                if (this.zLibCompression == null) {
                    this.zLibCompression = new ZLibCompression();
                }
                yield this.zLibCompression;
            }
            case Snappy -> {
                if (this.snappyCompression == null) {
                    this.snappyCompression = new SnappyCompression();
                }
                yield this.snappyCompression;
            }
            default -> throw new IllegalStateException("Unhandled PacketCompressionAlgorithm " + algorithm);
        };
    }

}
