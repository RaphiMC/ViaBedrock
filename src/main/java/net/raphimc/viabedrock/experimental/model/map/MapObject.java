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
package net.raphimc.viabedrock.experimental.model.map;

import com.viaversion.viaversion.api.minecraft.BlockPosition;

import java.util.ArrayList;
import java.util.List;

public class MapObject {

    private long id;
    private byte dimension;
    private boolean locked;
    private BlockPosition origin;
    private final List<Long> trackedEntities;
    private byte scale;
    private final List<MapTrackedObject> trackedObjects;
    private final List<MapDecoration> decorations;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int[] colors;

    private final int java_id;

    public MapObject(long id, int java_id) {
        this.id = id;

        this.trackedEntities = new ArrayList<>();
        this.trackedObjects = new ArrayList<>();
        this.decorations = new ArrayList<>();

        this.java_id = java_id;
    }

    public MapObject(long id, byte dimension, boolean locked, BlockPosition origin, List<Long> trackedEntities,
                     byte scale, List<MapTrackedObject> trackedObjects, List<MapDecoration> decorations,
                     int width, int height, int xOffset, int yOffset, int[] colors, int java_id) {
        this.id = id;
        this.dimension = dimension;
        this.locked = locked;
        this.origin = origin;
        this.trackedEntities = trackedEntities;
        this.scale = scale;
        this.trackedObjects = trackedObjects;
        this.decorations = decorations;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.colors = colors;

        this.java_id = java_id;
    }

    public long getId() {
        return id;
    }

    public int getJavaId() {
        return java_id;
    }

    public byte getDimension() {
        return dimension;
    }

    public boolean isLocked() {
        return locked;
    }

    public BlockPosition getOrigin() {
        return origin;
    }

    public List<Long> getTrackedEntities() {
        return trackedEntities;
    }

    public byte getScale() {
        return scale;
    }

    public List<MapTrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public List<MapDecoration> getDecorations() {
        return decorations;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int[] getColors() {
        return colors;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDimension(byte dimension) {
        this.dimension = dimension;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setOrigin(BlockPosition origin) {
        this.origin = origin;
    }

    public void setScale(byte scale) {
        this.scale = scale;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }
}
