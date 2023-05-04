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
package net.raphimc.viabedrock.api.chunk.block_state_upgrader;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class BlockStateUpgradeSchema {

    private final int version;
    protected final List<Consumer<CompoundTag>> actions = new ArrayList<>();

    public BlockStateUpgradeSchema(final int version) {
        this.version = version;
    }

    public BlockStateUpgradeSchema(final int majorVersion, final int minorVersion, final int patchVersion, final int revisionVersion) {
        this.version = majorVersion << 24 | minorVersion << 16 | patchVersion << 8 | revisionVersion;
    }

    public void upgrade(final CompoundTag tag) {
        final IntTag version = tag.get("version");
        if (version != null && (version.asInt() > this.version || version.asInt() < 0)) {
            return;
        }

        try {
            for (Consumer<CompoundTag> action : this.actions) {
                action.accept(tag);
            }
        } catch (JsonBlockStateUpgradeSchema.StopUpgrade ignored) {
        }

        tag.put("version", new IntTag(this.version));
    }

    public int version() {
        return this.version;
    }

    protected static final class StopUpgrade extends RuntimeException {

        public static final StopUpgrade INSTANCE = new StopUpgrade();

        StopUpgrade() {
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

    }

}
