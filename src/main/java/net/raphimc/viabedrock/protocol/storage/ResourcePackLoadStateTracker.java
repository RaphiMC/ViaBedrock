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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.resourcepack.content.ZipContent;
import net.raphimc.viabedrock.api.resourcepack.http.BedrockPackDownloader;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ResourcePackResponse;
import net.raphimc.viabedrock.protocol.provider.ResourcePackProvider;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ResourcePackLoadStateTracker extends StoredObject {

    private final Map<ResourcePack.Key, Info> requests = new HashMap<>();
    private final Map<ResourcePack.Key, ResourcePack> resourcePacks = new ConcurrentHashMap<>();
    private final AtomicInteger remainingResourcePackCount = new AtomicInteger();
    private final ExecutorService executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> {
        final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        thread.setName("ViaBedrock ResourcePack Executor");
        return thread;
    }, null, true);
    private final CompletableFuture<Void> loadFuture = new CompletableFuture<>();
    private boolean javaClientAccepted;

    public ResourcePackLoadStateTracker(final UserConnection user, final ResourcePackLoadStateTracker.Info[] infos) {
        super(user);
        for (Info info : infos) {
            this.requests.put(info.key(), info);
        }
        this.remainingResourcePackCount.set(this.requests.size());
    }

    public Info getRequest(final ResourcePack.Key key) {
        return this.requests.get(key);
    }

    public void addRemoteResourcePack(final ResourcePack resourcePack) {
        try {
            Via.getManager().getProviders().get(ResourcePackProvider.class).save(resourcePack);
        } catch (Throwable e) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to save resource pack: " + resourcePack.key(), e);
        }
        this.addLocalResourcePack(resourcePack);
    }

    public void addLocalResourcePack(final ResourcePack resourcePack) {
        this.resourcePacks.put(resourcePack.key(), resourcePack);
        if (this.remainingResourcePackCount.decrementAndGet() == 0) {
            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "All resource packs have been loaded");
            this.loadFuture.complete(null);
        }
    }

    public ResourcePack getResourcePack(final ResourcePack.Key key) {
        return this.resourcePacks.get(key);
    }

    public CompletableFuture<Void> loadRequestedResourcePacks() {
        final List<Callable<Void>> asyncTasks = new ArrayList<>();
        final List<ResourcePack.Key> downloadList = Collections.synchronizedList(new ArrayList<>());
        for (Info info : this.requests.values()) {
            if (BedrockProtocol.MAPPINGS.getBedrockResourcePacks().containsKey(info.key())) {
                this.addLocalResourcePack(BedrockProtocol.MAPPINGS.getBedrockResourcePacks().get(info.key()));
            } else if (Via.getManager().getProviders().get(ResourcePackProvider.class).has(info.key())) {
                asyncTasks.add(() -> {
                    try {
                        this.addLocalResourcePack(Via.getManager().getProviders().get(ResourcePackProvider.class).load(info.key()));
                    } catch (Throwable e) {
                        if (!(e.getCause() instanceof InterruptedException)) {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to load resource pack: " + info.key(), e);
                            downloadList.add(info.key());
                        }
                    }
                    return null;
                });
            } else if (info.httpUrl() != null) {
                asyncTasks.add(() -> {
                    try {
                        final BedrockPackDownloader downloader = new BedrockPackDownloader(info.httpUrl());
                        downloader.getContentLength(); // Check if the pack is available before downloading
                        this.addRemoteResourcePack(new ResourcePack(new ZipContent(downloader.download())));
                    } catch (Throwable e) {
                        if (!(e.getCause() instanceof InterruptedException)) {
                            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to download resource pack: " + info.key(), e);
                            downloadList.add(info.key());
                        }
                    }
                    return null;
                });
            } else {
                downloadList.add(info.key());
            }
        }
        CompletableFuture.runAsync(() -> {
            try {
                for (Future<Void> future : this.executor.invokeAll(asyncTasks, 2, TimeUnit.MINUTES)) {
                    future.get();
                }
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, this.executor).thenRun(() -> {
            if (!downloadList.isEmpty()) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Downloading " + downloadList.size() + " resource packs over the game protocol");
                final PacketWrapper resourcePackClientResponse = PacketWrapper.create(ServerboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE, this.user());
                resourcePackClientResponse.write(Types.BYTE, (byte) ResourcePackResponse.Downloading.getValue()); // status
                resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, downloadList.stream().map(ResourcePack.Key::toString).toArray(String[]::new)); // downloading packs
                resourcePackClientResponse.scheduleSendToServer(BedrockProtocol.class);
            } else {
                this.loadFuture.complete(null);
            }
        }).exceptionally(e -> {
            this.loadFuture.completeExceptionally(e);
            return null;
        });
        return this.loadFuture;
    }

    public void loadUnrequestedResourcePacks(final ResourcePack.Key[] keys) {
        for (ResourcePack.Key key : keys) {
            if (BedrockProtocol.MAPPINGS.getBedrockResourcePacks().containsKey(key)) {
                this.resourcePacks.put(key, BedrockProtocol.MAPPINGS.getBedrockResourcePacks().get(key));
            } else if (Via.getManager().getProviders().get(ResourcePackProvider.class).has(key)) {
                try {
                    this.resourcePacks.put(key, Via.getManager().getProviders().get(ResourcePackProvider.class).load(key));
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to load resource pack: " + key, e);
                }
            }
        }
    }

    @Override
    public void onRemove() {
        this.executor.shutdownNow();
    }

    public boolean hasJavaClientAccepted() {
        return this.javaClientAccepted;
    }

    public void setJavaClientAccepted() {
        this.javaClientAccepted = true;
    }

    public record Info(ResourcePack.Key key, byte[] contentKey, String contentId, URL httpUrl) {
    }

}
