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
package net.raphimc.viabedrock.protocol.storage;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.resourcepack.ItemDefinitions;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.model.resourcepack.TextDefinitions;
import net.raphimc.viabedrock.api.model.resourcepack.TextureDefinitions;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ResourcePackResponse;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ResourcePacksStorage extends StoredObject {

    private final Map<UUID, ResourcePack> packs = new HashMap<>();
    private final Set<UUID> preloadedPacks = new LinkedHashSet<>();
    private final List<ResourcePack> packStackTopToBottom = new ArrayList<>();
    private final List<ResourcePack> packStackBottomToTop = new ArrayList<>();

    private boolean javaClientWaitingForPack;
    private boolean loadedOnJavaClient;

    private TextDefinitions texts;
    private ItemDefinitions items;
    private TextureDefinitions textures;

    public ResourcePacksStorage(final UserConnection user) {
        super(user);

        if (BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePack() != null) { // null if ran from ResourcePackConverterTest
            this.addPreloadedPack(BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePack());
        }
    }

    public void sendResponseIfAllDownloadsCompleted() {
        if (this.packs.values().stream().allMatch(ResourcePack::isDecompressed)) {
            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "All packs have been downloaded and decompressed");
            final PacketWrapper resourcePackClientResponse = PacketWrapper.create(ServerboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE, this.getUser());
            resourcePackClientResponse.write(Types.BYTE, (byte) ResourcePackResponse.DownloadingFinished.getValue()); // status
            resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // resource pack ids
            resourcePackClientResponse.sendToServer(BedrockProtocol.class);
        }
    }

    public CompletableFuture<Void> runHttpTask(final Collection<ResourcePack> packs, final Consumer<ResourcePack> task, final BiConsumer<ResourcePack, Throwable> errorHandler) {
        final List<Runnable> tasks = new ArrayList<>();
        for (ResourcePack pack : packs) {
            if (pack.url() == null) continue;
            tasks.add(() -> {
                try {
                    task.accept(pack);
                } catch (Throwable e) {
                    if (e.getCause() instanceof InterruptedException) return;
                    errorHandler.accept(pack, e);
                }
            });
        }
        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        final ExecutorService httpExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ViaBedrock-Pack-Downloader-%d").build());
        this.getUser().getChannel().closeFuture().addListener(future -> httpExecutor.shutdownNow());

        for (Runnable runnable : tasks) {
            httpExecutor.execute(runnable);
        }
        httpExecutor.shutdown();

        return CompletableFuture.runAsync(() -> {
            try {
                if (!httpExecutor.awaitTermination(5, TimeUnit.MINUTES)) {
                    httpExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                httpExecutor.shutdownNow();
            }
        });
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

    public void addPack(final ResourcePack pack) {
        this.packs.put(pack.packId(), pack);
    }

    public boolean isPreloaded(final UUID packId) {
        return this.preloadedPacks.contains(packId);
    }

    public void addPreloadedPack(final ResourcePack pack) {
        this.packs.put(pack.packId(), pack);
        this.preloadedPacks.add(pack.packId());
    }

    public void setPackStack(final UUID[] resourcePackStack, final UUID[] behaviourPackStack) {
        this.packStackTopToBottom.clear();
        Arrays.stream(behaviourPackStack).map(this.packs::get).filter(Objects::nonNull).forEach(this.packStackTopToBottom::add);
        Arrays.stream(resourcePackStack).map(this.packs::get).filter(Objects::nonNull).forEach(this.packStackTopToBottom::add);
        this.preloadedPacks.stream().map(this.packs::get).forEach(this.packStackTopToBottom::add);
        this.packStackBottomToTop.clear();
        this.packStackBottomToTop.addAll(this.packStackTopToBottom);
        Collections.reverse(this.packStackBottomToTop);

        this.texts = new TextDefinitions(this);
        this.items = new ItemDefinitions(this);
        this.textures = new TextureDefinitions(this);
    }

    public List<ResourcePack> getPackStackTopToBottom() {
        return this.packStackTopToBottom;
    }

    public List<ResourcePack> getPackStackBottomToTop() {
        return this.packStackBottomToTop;
    }

    public boolean isJavaClientWaitingForPack() {
        return this.javaClientWaitingForPack;
    }

    public void setJavaClientWaitingForPack(final boolean state) {
        this.javaClientWaitingForPack = state;
    }

    public boolean isLoadedOnJavaClient() {
        return this.loadedOnJavaClient;
    }

    public void setLoadedOnJavaClient() {
        this.javaClientWaitingForPack = false;
        this.loadedOnJavaClient = true;
    }

    public boolean hasFinishedLoading() {
        return this.texts != null;
    }

    public TextDefinitions getTexts() {
        return this.texts;
    }

    public ItemDefinitions getItems() {
        return this.items;
    }

    public TextureDefinitions getTextures() {
        return this.textures;
    }

}
