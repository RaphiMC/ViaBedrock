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
package net.raphimc.viabedrock.protocol.rewriter.item;

import com.viaversion.nbt.tag.IntTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import net.raphimc.viabedrock.api.model.container.dynamic.BundleContainer;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.Arrays;

public class BundleItemRewriter implements ItemRewriter.NbtRewriter {

    @Override
    public void toJava(UserConnection user, BedrockItem bedrockItem, Item javaItem) {
        if (bedrockItem.tag() == null) return;

        final IntTag bundleIdTag = bedrockItem.tag().getIntTag("bundle_id");
        if (bundleIdTag == null || bundleIdTag.asInt() == 0) return;

        final FullContainerName containerName = new FullContainerName(ContainerEnumName.DynamicContainer, bundleIdTag.asInt());
        final BundleContainer bundleContainer = user.get(InventoryTracker.class).getDynamicContainer(containerName);
        if (bundleContainer == null) return;

        Item[] javaItems = bundleContainer.getJavaBundleItems();
        for (int i = 0; i < javaItems.length; i++) {
            if (javaItems[i].isEmpty()) {
                javaItems = Arrays.copyOfRange(javaItems, 0, i);
                break;
            }
        }
        for (int i = 0; i < javaItems.length / 2; i++) {
            final Item temp = javaItems[i];
            javaItems[i] = javaItems[javaItems.length - i - 1];
            javaItems[javaItems.length - i - 1] = temp;
        }
        javaItem.dataContainer().set(VersionedTypes.V1_21_9.structuredDataKeys.bundleContents, javaItems);
    }

}
