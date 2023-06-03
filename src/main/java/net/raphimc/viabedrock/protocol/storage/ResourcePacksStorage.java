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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.api.model.ResourcePack;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.*;
import java.util.function.Function;

public class ResourcePacksStorage extends StoredObject {

    private final Map<UUID, ResourcePack> packs = new HashMap<>();
    private final Set<UUID> preloadedPacks = new HashSet<>();
    private final List<UUID> resourcePackStack = new ArrayList<>();
    private final List<UUID> behaviourPackStack = new ArrayList<>();

    private boolean completedTransfer;

    private Map<String, String> translations;

    public ResourcePacksStorage(final UserConnection user) {
        super(user);

        this.addPreloadedPack(BedrockProtocol.MAPPINGS.getVanillaResourcePack());
    }

    public boolean hasPack(final UUID packId) {
        return this.packs.containsKey(packId);
    }

    public ResourcePack getPack(final UUID packId) {
        return this.packs.get(packId);
    }

    public Collection<ResourcePack> getPacks() {
        return this.packs.values();
    }

    public boolean areAllPacksDecompressed() {
        return this.packs.values().stream().allMatch(ResourcePack::isDecompressed);
    }

    public void addPack(final ResourcePack pack) {
        this.packs.put(pack.packId(), pack);
    }

    public boolean isPreloaded(final UUID packId) {
        return this.preloadedPacks.contains(packId);
    }

    public void addPreloadedPack(final ResourcePack pack) {
        this.packs.put(pack.packId(), pack);
        this.preloadedPacks.add(pack.packId());
        this.resourcePackStack.add(pack.packId());
    }

    public void iterateResourcePacksTopToBottom(final Function<ResourcePack, Boolean> function) {
        for (UUID packId : this.resourcePackStack) {
            final ResourcePack pack = this.packs.get(packId);
            if (pack == null) continue;

            if (!function.apply(pack)) break;
        }
    }

    public void iterateResourcePacksBottomToTop(final Function<ResourcePack, Boolean> function) {
        for (int i = this.resourcePackStack.size() - 1; i >= 0; i--) {
            final ResourcePack pack = this.packs.get(this.resourcePackStack.get(i));
            if (pack == null) continue;

            if (!function.apply(pack)) break;
        }
    }

    public void setPackStack(final UUID[] resourcePackStack, final UUID[] behaviourPackStack) {
        this.resourcePackStack.addAll(0, Arrays.asList(resourcePackStack));
        this.behaviourPackStack.addAll(0, Arrays.asList(behaviourPackStack));

        this.translations = new HashMap<>();
        this.iterateResourcePacksBottomToTop(pack -> {
            if (pack.content().containsKey("texts/en_US.lang")) {
                this.translations.putAll(pack.content().getLang("texts/en_US.lang"));
            }
            return true;
        });
        this.translations = Collections.unmodifiableMap(this.translations);
    }

    public boolean hasCompletedTransfer() {
        return this.completedTransfer;
    }

    public void setCompletedTransfer() {
        this.completedTransfer = true;
    }

    public Map<String, String> getTranslations() {
        return this.translations;
    }

}
