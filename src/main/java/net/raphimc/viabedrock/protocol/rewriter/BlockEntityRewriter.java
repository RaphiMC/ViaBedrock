/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.rewriter.blockentity.*;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BlockEntityRewriter {

    private static final Map<String, Rewriter> BLOCK_ENTITY_REWRITERS = new HashMap<>();

    private static final Rewriter NULL_REWRITER = (user, bedrockBlockEntity) -> null;
    private static final Rewriter NOOP_REWRITER = (user, bedrockBlockEntity) -> new BlockEntityImpl(bedrockBlockEntity.packedXZ(), bedrockBlockEntity.y(), -1, new CompoundTag());

    static {
        // TODO: Enhancement: Add missing block entities
        BLOCK_ENTITY_REWRITERS.put("brewing_stand", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("calibrated_sculk_sensor", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("campfire", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("note_block", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("piston", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("moving_block", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("sculk_sensor", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("sculk_shrieker", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("trial_spawner", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("vault", NOOP_REWRITER);

        BLOCK_ENTITY_REWRITERS.put("banner", new BannerBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("barrel", new LootableContainerBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("beacon", new BeaconBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("bed", new BedBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("beehive", new BeehiveBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("bell", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("blast_furnace", new FurnaceBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("brushable_block", new BrushableBlockBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("cauldron", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("chest", new LootableContainerBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("chiseled_bookshelf", new ChiseledBookshelfBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("command_block", new CommandBlockBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("comparator", new ComparatorBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("conduit", new ConduitBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("crafter", new CrafterBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("daylight_detector", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("decorated_pot", new DecoratedPotBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("dispenser", new LootableContainerBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("dropper", new LootableContainerBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("enchanting_table", new NamedBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("end_gateway", new EndGatewayBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("end_portal", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("ender_chest", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("flower_pot", new FlowerPotBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("furnace", new FurnaceBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("hanging_sign", new SignBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("hopper", new HopperBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("item_frame", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("jigsaw", new JigsawBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("jukebox", new JukeboxBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("lectern", new LecternBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("lodestone", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("mob_spawner", new MobSpawnerBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("nether_reactor", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("sculk_catalyst", NOOP_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("shulker_box", new LootableContainerBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("sign", new SignBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("skull", new SkullBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("smoker", new FurnaceBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("spore_blossom", NULL_REWRITER);
        BLOCK_ENTITY_REWRITERS.put("structure_block", new StructureBlockBlockEntityRewriter());
        BLOCK_ENTITY_REWRITERS.put("trapped_chest", new LootableContainerBlockEntityRewriter());
    }

    public static BlockEntity toJava(final UserConnection user, final int bedrockBlockStateId, final BedrockBlockEntity bedrockBlockEntity) {
        final BlockStateRewriter blockStateRewriter = user.get(BlockStateRewriter.class);
        if (bedrockBlockStateId == blockStateRewriter.bedrockId(BedrockBlockState.AIR)) {
            return null;
        }

        final String tag = blockStateRewriter.tag(bedrockBlockStateId);
        if (BLOCK_ENTITY_REWRITERS.containsKey(tag)) {
            final BlockEntity javaBlockEntity = BLOCK_ENTITY_REWRITERS.get(tag).toJava(user, bedrockBlockEntity);
            if (javaBlockEntity == null) return null;

            if (javaBlockEntity.tag() != null) {
                final int typeId = BedrockProtocol.MAPPINGS.getJavaBlockEntities().getOrDefault(tag, -1);
                if (typeId == -1) throw new IllegalStateException("Unknown java block entity type: " + tag);

                return javaBlockEntity.withTypeId(typeId);
            }

            return javaBlockEntity;
        } else {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block entity translation for " + bedrockBlockStateId + " (" + bedrockBlockEntity.tag() + ")");
        }

        return null;
    }

    public static boolean isJavaBlockEntity(final String tag) {
        return !NULL_REWRITER.equals(BLOCK_ENTITY_REWRITERS.get(tag));
    }

    public interface Rewriter {

        BlockEntity toJava(final UserConnection user, final BedrockBlockEntity bedrockBlockEntity);

        default void copy(final CompoundTag fromTag, final CompoundTag toTag, final String key, final Class<?> expectedType) {
            this.copy(fromTag, toTag, key, key, expectedType);
        }

        default void copy(final CompoundTag fromTag, final CompoundTag toTag, final String fromKey, final String toKey, final Class<?> expectedType) {
            if (expectedType.isInstance(fromTag.get(fromKey))) {
                toTag.put(toKey, fromTag.get(fromKey).copy());
            }
        }

        default void copyCustomName(final UserConnection user, final CompoundTag fromTag, final CompoundTag toTag) {
            if (fromTag.get("CustomName") instanceof StringTag customNameTag) {
                toTag.put("CustomName", this.rewriteCustomName(user, customNameTag));
            }
        }

        default void copyItemList(final UserConnection user, final CompoundTag fromTag, final CompoundTag toTag) {
            if (fromTag.get("Items") instanceof ListTag<?> itemsTag && CompoundTag.class.equals(itemsTag.getElementType())) {
                toTag.put("Items", this.rewriteItemList(user, (ListTag<CompoundTag>) itemsTag));
            }
        }

        default void copyItem(final UserConnection user, final CompoundTag fromTag, final CompoundTag toTag, final String key) {
            this.copyItem(user, fromTag, toTag, key, key);
        }

        default void copyItem(final UserConnection user, final CompoundTag fromTag, final CompoundTag toTag, final String fromKey, final String toKey) {
            if (fromTag.get(fromKey) instanceof CompoundTag itemTag) {
                toTag.put(toKey, this.rewriteItem(user, itemTag));
            }
        }

        default CompoundTag rewriteItem(final UserConnection user, final CompoundTag bedrockItemTag) {
            return user.get(ItemRewriter.class).javaItem(bedrockItemTag);
        }

        default ListTag<?> rewriteItemList(final UserConnection user, final ListTag<CompoundTag> bedrockItemList) {
            final ListTag<CompoundTag> javaItemList = new ListTag<>(CompoundTag.class);
            final ItemRewriter itemRewriter = user.get(ItemRewriter.class);
            for (final CompoundTag bedrockItemTag : bedrockItemList) {
                final CompoundTag javaItemTag = itemRewriter.javaItem(bedrockItemTag);
                this.copy(bedrockItemTag, javaItemTag, "Slot", ByteTag.class);
                javaItemList.add(javaItemTag);
            }
            return javaItemList;
        }

        default StringTag rewriteCustomName(final UserConnection user, final StringTag textTag) {
            return new StringTag(TextUtil.stringToJson(user.get(ResourcePacksStorage.class).translate(textTag.getValue())));
        }

    }

}
