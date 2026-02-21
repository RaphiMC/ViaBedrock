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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.model.Position3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BreakingTracker extends StoredObject {
    private final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    private final Map<Position3f, BlockCrackingInfo> blockCrackingInfos = new ConcurrentHashMap<>();

    public BreakingTracker(UserConnection user) {
        super(user);
    }

    public void tick() {
        for (Map.Entry<Position3f, BlockCrackingInfo> entry : this.blockCrackingInfos.entrySet()) {
            final Position3f position3f = entry.getKey();
            final BlockCrackingInfo info = entry.getValue();

            // Clamped this to 0-10 instead of 0-9 because we don't want the animation to be stuck at stage 9 (we want it to stop after that).
            // We're doing this because breaking animation doesn't seem to actually be cleared from the client cache until StopBlockCracking is sent.
            int progress = (int) Math.max(0, Math.min(10, ((System.currentTimeMillis() - info.startTime()) / (double) ((65535 / info.breakTime()) * 50)) * 10));
            if (progress != info.prevProgress()) {
                PacketFactory.sendJavaBlockDestroyProgress(user(), info.breakId(), new BlockPosition((int) position3f.x(), (int) position3f.y(), (int) position3f.z()), progress);
            }
            info.prevProgress(progress);
        }
    }

    public void stopCracking(Position3f position3f) {
        Map.Entry<Position3f, BlockCrackingInfo> entry = crackingInfoFromPosition(position3f);
        if (entry == null) {
            return;
        }

        this.blockCrackingInfos.remove(entry.getKey());

        final BlockCrackingInfo info = entry.getValue();
        PacketFactory.sendJavaBlockDestroyProgress(user(), info.breakId(), new BlockPosition((int) position3f.x(), (int) position3f.y(), (int) position3f.z()), 10);
    }

    public void updateCrackingInfo(Position3f position3f, long breakTime, boolean update) {
        Map.Entry<Position3f, BlockCrackingInfo> entry = crackingInfoFromPosition(position3f);
        // The cracking animation won't start regardless of the "UpdateBlockCracking" info until the client actually receive "StartBlockCracking".
        if (entry == null && update) {
            return;
        }

        BlockCrackingInfo info = entry == null ? null : entry.getValue();
        if (info == null) {
            this.blockCrackingInfos.put(position3f, info = new BlockCrackingInfo(breakTime));

            // Lets the client know that we want to start cracking this block.
            PacketFactory.sendJavaBlockDestroyProgress(user(), info.breakId(), new BlockPosition((int) position3f.x(), (int) position3f.y(), (int) position3f.z()), 0);
        } else {
            info.breakTime(breakTime);

            // If the cracking animation has finished, we start over.
            if (info.prevProgress() >= 9) {
                info.startTime(System.currentTimeMillis());
                info.prevProgress(0);
                PacketFactory.sendJavaBlockDestroyProgress(user(), info.breakId(), new BlockPosition((int) position3f.x(), (int) position3f.y(), (int) position3f.z()), 0);
            }
        }
    }

    private Map.Entry<Position3f, BlockCrackingInfo> crackingInfoFromPosition(Position3f position3f) {
        for (Map.Entry<Position3f, BlockCrackingInfo> entry : this.blockCrackingInfos.entrySet()) {
            final Position3f position3f1 = entry.getKey();
            if (position3f1.x() == position3f.x() && position3f1.y() == position3f.y() && position3f1.z() == position3f.z()) {
                return entry;
            }
        }

        return null;
    }

    private class BlockCrackingInfo {
        private final int breakId;
        private long breakTime, startTime;
        private int prevProgress = 0;

        public BlockCrackingInfo(long breakTime) {
            this.breakId = ID_COUNTER.getAndIncrement();
            this.breakTime = breakTime;
            this.startTime = System.currentTimeMillis();
        }

        public int breakId() {
            return this.breakId;
        }

        public int prevProgress() {
            return this.prevProgress;
        }

        public void prevProgress(int prevProgress) {
            this.prevProgress = prevProgress;
        }

        public long breakTime() {
            return this.breakTime;
        }

        public void breakTime(long time) {
            this.breakTime = time;
        }

        public long startTime() {
            return this.startTime;
        }

        public void startTime(long time) {
            this.startTime = time;
        }
    }
}
