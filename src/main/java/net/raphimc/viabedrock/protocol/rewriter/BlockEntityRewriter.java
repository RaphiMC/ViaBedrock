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
package net.raphimc.viabedrock.protocol.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.rewriter.blockentity.*;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BlockEntityRewriter {

    private static final Map<String, Rewriter> BLOCK_ENTITY_REWRITERS = new HashMap<>();
    private static final Rewriter NULL_REWRITER = (user, bedrockBlockEntity) -> null;

    static {
        BLOCK_ENTITY_REWRITERS.put("bed", new BedBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("cauldron", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("command_block", new CommandBlockBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("enchanting_table", new EnchantingTableBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("hanging_sign", new SignBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("lectern", new LecternBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("sign", new SignBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("spore_blossom", NULL_REWRITER);
    }

    public static BlockEntity toJava(final UserConnection user, final int bedrockBlockStateId, final BedrockBlockEntity bedrockBlockEntity) {
        final BlockStateRewriter blockStateRewriter = user.get(BlockStateRewriter.class);
        final String tag = blockStateRewriter.tag(bedrockBlockStateId);

        if (BLOCK_ENTITY_REWRITERS.containsKey(tag)) {
            final BlockEntity javaBlockEntity = BLOCK_ENTITY_REWRITERS.get(tag).toJava(user, bedrockBlockEntity);
            if (javaBlockEntity == null) return null;

            if (javaBlockEntity.tag() != null) {
                final int typeId = BedrockProtocol.MAPPINGS.getBlockEntities().getOrDefault(tag, -1);
                if (typeId == -1) throw new IllegalStateException("Unknown block entity type: " + tag);

                return javaBlockEntity.withTypeId(typeId);
            }

            return javaBlockEntity;
        }

        return null;
    }

    public interface Rewriter {
        BlockEntity toJava(final UserConnection user, final BedrockBlockEntity bedrockBlockEntity);

        default void copy(final CompoundTag oldTag, final CompoundTag newTag, final String key, final Class<?> expectedType) {
            copy(oldTag, newTag, key, key, expectedType);
        }

        default void copy(final CompoundTag oldTag, final CompoundTag newTag, final String oldKey, final String newKey, final Class<?> expectedType) {
            if (expectedType.isInstance(oldTag.get(oldKey))) {
                newTag.put(newKey, oldTag.get(oldKey));
            }
        }

        default CompoundTag rewriteSlot(final UserConnection user, final CompoundTag itemTag) {
            return user.get(ItemRewriter.class).javaTag(itemTag);
        }

        default StringTag rewriteCustomName(final UserConnection user, final StringTag textTag) {
            final Function<String, String> translator = k -> user.get(ResourcePacksStorage.class).getTranslations().getOrDefault(k, k);
            return new StringTag(TextUtil.stringToJson(BedrockTranslator.translate(textTag.getValue(), translator, new Object[0])));
        }

        default void translateCustomName(final UserConnection user, final CompoundTag oldTag, final CompoundTag newTag) {
            if (oldTag.get("CustomName") instanceof StringTag) {
                newTag.put("CustomName", rewriteCustomName(user, oldTag.get("CustomName")));
            }
        }
    }

}
