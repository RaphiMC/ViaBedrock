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
package net.raphimc.viabedrock.protocol.rewriter.blockentity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.rewriter.BlockEntityRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CommandBlockBlockEntityRewriter implements BlockEntityRewriter.Rewriter {

    @Override
    public BlockEntity toJava(UserConnection user, BedrockBlockEntity bedrockBlockEntity) {
        final CompoundTag bedrockTag = bedrockBlockEntity.tag();
        final CompoundTag javaTag = new CompoundTag();

        copy(bedrockTag, javaTag, "Command", StringTag.class);
        copy(bedrockTag, javaTag, "SuccessCount", IntTag.class);
        copy(bedrockTag, javaTag, "TrackOutput", ByteTag.class);
        copy(bedrockTag, javaTag, "LastExecution", LongTag.class);
        copy(bedrockTag, javaTag, "powered", ByteTag.class);
        copy(bedrockTag, javaTag, "conditionMet", ByteTag.class);
        copy(bedrockTag, javaTag, "auto", ByteTag.class);
        translateCustomName(user, bedrockTag, javaTag);

        if (bedrockTag.get("LastOutput") instanceof StringTag) {
            final Function<String, String> translator = k -> user.get(ResourcePacksStorage.class).getTranslations().getOrDefault(k, k);

            final List<String> lastOutputParams = new ArrayList<>();
            if (bedrockTag.get("LastOutputParams") instanceof ListTag) {
                final ListTag bedrockLastOutputParams = bedrockTag.get("LastOutputParams");
                if (bedrockLastOutputParams.getElementType().equals(StringTag.class)) {
                    for (Tag bedrockLastOutputParam : bedrockLastOutputParams) {
                        lastOutputParams.add(((StringTag) bedrockLastOutputParam).getValue());
                    }
                }
            }

            final String bedrockLastOutput = bedrockTag.<StringTag>get("LastOutput").getValue();
            final String javaLastOutput = TextUtil.stringToJson(BedrockTranslator.translate(bedrockLastOutput, translator, lastOutputParams.toArray()));
            javaTag.put("LastOutput", new StringTag(javaLastOutput));
        }

        return new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, javaTag);
    }

}
