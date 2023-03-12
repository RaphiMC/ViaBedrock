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
package net.raphimc.viabedrock.protocol.model;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

public class SkinData {

    private final String skinId;
    private final String playFabId;
    private final String skinResourcePatch;
    private final BufferedImage skinData;
    private final List<AnimationData> animations;
    private final BufferedImage capeData;
    private final String geometryData;
    private final String geometryDataEngineVersion;
    private final String animationData;
    private final boolean premium;
    private final boolean persona;
    private final boolean capeOnClassic;
    private final boolean primaryUser;
    private final String capeId;
    private final String fullSkinId;
    private final String armSize;
    private final String skinColor;
    private final List<PersonaPieceData> personaPieces;
    private final List<PersonaPieceTintData> tintColors;
    private final boolean overridingPlayerAppearance;

    public SkinData(final String skinId, final String playFabId, final String skinResourcePatch, final BufferedImage skinData, final List<AnimationData> animations, final BufferedImage capeData, final String geometryData, final String geometryDataEngineVersion, final String animationData, final boolean premium, final boolean persona, final boolean capeOnClassic, final boolean primaryUser, final String capeId, final String fullSkinId, final String armSize, final String skinColor, final List<PersonaPieceData> personaPieces, final List<PersonaPieceTintData> tintColors, final boolean overridingPlayerAppearance) {
        this.skinId = skinId;
        this.playFabId = playFabId;
        this.skinResourcePatch = skinResourcePatch;
        this.skinData = skinData;
        this.animations = animations;
        this.capeData = capeData;
        this.geometryData = geometryData;
        this.geometryDataEngineVersion = geometryDataEngineVersion;
        this.animationData = animationData;
        this.premium = premium;
        this.persona = persona;
        this.capeOnClassic = capeOnClassic;
        this.primaryUser = primaryUser;
        this.capeId = capeId;
        this.fullSkinId = fullSkinId;
        this.armSize = armSize;
        this.skinColor = skinColor;
        this.personaPieces = personaPieces;
        this.tintColors = tintColors;
        this.overridingPlayerAppearance = overridingPlayerAppearance;
    }

    public String skinId() {
        return this.skinId;
    }

    public String playFabId() {
        return this.playFabId;
    }

    public String skinResourcePatch() {
        return this.skinResourcePatch;
    }

    public BufferedImage skinData() {
        return this.skinData;
    }

    public List<AnimationData> animations() {
        return this.animations;
    }

    public BufferedImage capeData() {
        return this.capeData;
    }

    public String geometryData() {
        return this.geometryData;
    }

    public String geometryDataEngineVersion() {
        return this.geometryDataEngineVersion;
    }

    public String animationData() {
        return this.animationData;
    }

    public boolean premium() {
        return this.premium;
    }

    public boolean persona() {
        return this.persona;
    }

    public boolean capeOnClassic() {
        return this.capeOnClassic;
    }

    public boolean primaryUser() {
        return this.primaryUser;
    }

    public String capeId() {
        return this.capeId;
    }

    public String fullSkinId() {
        return this.fullSkinId;
    }

    public String armSize() {
        return this.armSize;
    }

    public String skinColor() {
        return this.skinColor;
    }

    public List<PersonaPieceData> personaPieces() {
        return this.personaPieces;
    }

    public List<PersonaPieceTintData> tintColors() {
        return this.tintColors;
    }

    public boolean overridingPlayerAppearance() {
        return this.overridingPlayerAppearance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkinData skinData1 = (SkinData) o;
        return premium == skinData1.premium && persona == skinData1.persona && capeOnClassic == skinData1.capeOnClassic && primaryUser == skinData1.primaryUser && overridingPlayerAppearance == skinData1.overridingPlayerAppearance && Objects.equals(skinId, skinData1.skinId) && Objects.equals(playFabId, skinData1.playFabId) && Objects.equals(skinResourcePatch, skinData1.skinResourcePatch) && Objects.equals(skinData, skinData1.skinData) && Objects.equals(animations, skinData1.animations) && Objects.equals(capeData, skinData1.capeData) && Objects.equals(geometryData, skinData1.geometryData) && Objects.equals(geometryDataEngineVersion, skinData1.geometryDataEngineVersion) && Objects.equals(animationData, skinData1.animationData) && Objects.equals(capeId, skinData1.capeId) && Objects.equals(fullSkinId, skinData1.fullSkinId) && Objects.equals(armSize, skinData1.armSize) && Objects.equals(skinColor, skinData1.skinColor) && Objects.equals(personaPieces, skinData1.personaPieces) && Objects.equals(tintColors, skinData1.tintColors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skinId, playFabId, skinResourcePatch, skinData, animations, capeData, geometryData, geometryDataEngineVersion, animationData, premium, persona, capeOnClassic, primaryUser, capeId, fullSkinId, armSize, skinColor, personaPieces, tintColors, overridingPlayerAppearance);
    }

    @Override
    public String toString() {
        return "SkinData{" +
                "skinId='" + skinId + '\'' +
                ", playFabId='" + playFabId + '\'' +
                ", skinResourcePatch='" + skinResourcePatch + '\'' +
                ", skinData=" + skinData +
                ", animations=" + animations +
                ", capeData=" + capeData +
                ", geometryData='" + geometryData + '\'' +
                ", geometryDataEngineVersion='" + geometryDataEngineVersion + '\'' +
                ", animationData='" + animationData + '\'' +
                ", premium=" + premium +
                ", persona=" + persona +
                ", capeOnClassic=" + capeOnClassic +
                ", primaryUser=" + primaryUser +
                ", capeId='" + capeId + '\'' +
                ", fullSkinId='" + fullSkinId + '\'' +
                ", armSize='" + armSize + '\'' +
                ", skinColor='" + skinColor + '\'' +
                ", personaPieces=" + personaPieces +
                ", tintColors=" + tintColors +
                ", overridingPlayerAppearance=" + overridingPlayerAppearance +
                '}';
    }

    public static class AnimationData {

        private final BufferedImage image;
        private final int type;
        private final float frames;
        private final int expression;

        public AnimationData(final BufferedImage image, final int type, final float frames, final int expression) {
            this.image = image;
            this.type = type;
            this.frames = frames;
            this.expression = expression;
        }

        public BufferedImage image() {
            return this.image;
        }

        public int type() {
            return this.type;
        }

        public float frames() {
            return this.frames;
        }

        public int expression() {
            return this.expression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AnimationData that = (AnimationData) o;
            return type == that.type && Float.compare(that.frames, frames) == 0 && expression == that.expression && Objects.equals(image, that.image);
        }

        @Override
        public int hashCode() {
            return Objects.hash(image, type, frames, expression);
        }

        @Override
        public String toString() {
            return "AnimationData{" +
                    "image=" + image +
                    ", type=" + type +
                    ", frames=" + frames +
                    ", expression=" + expression +
                    '}';
        }

    }

    public static class PersonaPieceData {

        private final String id;
        private final String type;
        private final String packId;
        private final boolean defaultPiece;
        private final String productId;

        public PersonaPieceData(final String id, final String type, final String packId, final boolean defaultPiece, final String productId) {
            this.id = id;
            this.type = type;
            this.packId = packId;
            this.defaultPiece = defaultPiece;
            this.productId = productId;
        }

        public String id() {
            return this.id;
        }

        public String type() {
            return this.type;
        }

        public String packId() {
            return this.packId;
        }

        public boolean isDefault() {
            return this.defaultPiece;
        }

        public String productId() {
            return this.productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonaPieceData that = (PersonaPieceData) o;
            return defaultPiece == that.defaultPiece && Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(packId, that.packId) && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type, packId, defaultPiece, productId);
        }

        @Override
        public String toString() {
            return "PersonaPieceData{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", packId='" + packId + '\'' +
                    ", defaultPiece=" + defaultPiece +
                    ", productId='" + productId + '\'' +
                    '}';
        }

    }

    public static class PersonaPieceTintData {

        private final String type;
        private final List<String> colors;

        public PersonaPieceTintData(final String type, final List<String> colors) {
            this.type = type;
            this.colors = colors;
        }

        public String type() {
            return this.type;
        }

        public List<String> colors() {
            return this.colors;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonaPieceTintData that = (PersonaPieceTintData) o;
            return Objects.equals(type, that.type) && Objects.equals(colors, that.colors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, colors);
        }

        @Override
        public String toString() {
            return "PersonaPieceTintData{" +
                    "type='" + type + '\'' +
                    ", colors=" + colors +
                    '}';
        }

    }

}
