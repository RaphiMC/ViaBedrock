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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.resourcepack.*;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ResourcePackResponse;
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

    public static final List<String> VANILLA_PACK_NAMES = List.of(
            "vanilla",
            "vanilla_1.14",
            "vanilla_1.15",
            "vanilla_1.16",
            "vanilla_1.16.100",
            "vanilla_1.16.200",
            "vanilla_1.16.210",
            "vanilla_1.16.220",
            "vanilla_1.17.0",
            "vanilla_1.17.10",
            "vanilla_1.17.20",
            "vanilla_1.17.30",
            "vanilla_1.17.40",
            "vanilla_1.18.0",
            "vanilla_1.18.10",
            "vanilla_1.18.20",
            "vanilla_1.18.30",
            "vanilla_1.19.0",
            "vanilla_1.19.10",
            "vanilla_1.19.20",
            "vanilla_1.19.30",
            "vanilla_1.19.40",
            "vanilla_1.19.50",
            "vanilla_1.19.60",
            "vanilla_1.19.70",
            "vanilla_1.19.80",
            "vanilla_1.20.0",
            "vanilla_1.20.10",
            "vanilla_1.20.20",
            "vanilla_1.20.30",
            "vanilla_1.20.40",
            "vanilla_1.20.50",
            "vanilla_1.20.60",
            "vanilla_1.20.70",
            "vanilla_1.20.80",
            "vanilla_1.21.0",
            "vanilla_1.21.10",
            "vanilla_1.21.20",
            "vanilla_1.21.30",
            "vanilla_1.21.40",
            "vanilla_1.21.50",
            "vanilla_1.21.60",
            "vanilla_1.21.70",
            "vanilla_1.21.80",
            "vanilla_1.21.90",
            "vanilla_1.21.100",
            "vanilla_1.21.110",
            "vanilla_1.21.120",
            "vanilla_1.21.130"
    );

    private final Map<UUID, ResourcePack> packs = new HashMap<>();
    private final List<ResourcePack> packStackTopToBottom = new ArrayList<>();
    private final List<ResourcePack> packStackBottomToTop = new ArrayList<>();

    private boolean javaClientWaitingForPack;
    private boolean loadedOnJavaClient;
    private final Map<String, Object> converterData = new HashMap<>();

    private TextDefinitions texts;
    private BlockDefinitions blocks;
    private ItemDefinitions items;
    private AttachableDefinitions attachables;
    private TextureDefinitions textures;
    private SoundDefinitions sounds;
    private ParticleDefinitions particles;
    private EntityDefinitions entities;
    private ModelDefinitions models;
    private FogDefinitions fogs;
    private BiomeDefinitions biomes;
    private RenderControllerDefinitions renderControllers;

    public ResourcePacksStorage(final UserConnection user) {
        super(user);
    }

    public void sendResponseIfAllDownloadsCompleted() {
        if (this.packs.values().stream().allMatch(ResourcePack::isDecompressed)) {
            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "All packs have been downloaded and decompressed");
            final PacketWrapper resourcePackClientResponse = PacketWrapper.create(ServerboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE, this.user());
            resourcePackClientResponse.write(Types.BYTE, (byte) ResourcePackResponse.DownloadingFinished.getValue()); // status
            resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // resource pack ids
            resourcePackClientResponse.sendToServer(BedrockProtocol.class);
        }
    }

    public CompletableFuture<Void> runHttpTask(final Collection<ResourcePack> packs, final Consumer<ResourcePack> task, final BiConsumer<ResourcePack, Throwable> errorHandler) {
        final List<Runnable> tasks = new ArrayList<>();
        for (ResourcePack pack : packs) {
            if (pack.cdnUrl() == null) continue;
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
        this.user().getChannel().closeFuture().addListener(future -> httpExecutor.shutdownNow());

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

    public void setPackStack(final UUID[] resourcePackStack, final UUID[] behaviourPackStack) {
        this.packStackTopToBottom.clear();
        Arrays.stream(behaviourPackStack).map(this.packs::get).filter(Objects::nonNull).forEach(this.packStackTopToBottom::add);
        Arrays.stream(resourcePackStack).map(this.packs::get).filter(Objects::nonNull).forEach(this.packStackTopToBottom::add);
        if (BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePacks() != null) { // null if ran from ResourcePackConverterTest
            for (int i = VANILLA_PACK_NAMES.size() - 1; i >= 0; i--) {
                this.packStackTopToBottom.add(BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePacks().get(VANILLA_PACK_NAMES.get(i)));
            }
        }
        this.packStackBottomToTop.clear();
        this.packStackBottomToTop.addAll(this.packStackTopToBottom);
        Collections.reverse(this.packStackBottomToTop);

        this.texts = new TextDefinitions(this);
        this.blocks = new BlockDefinitions(this);
        this.items = new ItemDefinitions(this);
        this.attachables = new AttachableDefinitions(this);
        this.textures = new TextureDefinitions(this);
        this.sounds = new SoundDefinitions(this);
        this.particles = new ParticleDefinitions(this);
        this.entities = new EntityDefinitions(this);
        this.models = new ModelDefinitions(this);
        this.fogs = new FogDefinitions(this);
        this.biomes = new BiomeDefinitions(this);
        this.renderControllers = new RenderControllerDefinitions(this);
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

    public Map<String, Object> getConverterData() {
        return this.converterData;
    }

    public boolean hasFinishedLoading() {
        return this.renderControllers != null;
    }

    public TextDefinitions getTexts() {
        return this.texts;
    }

    public BlockDefinitions getBlocks() {
        return this.blocks;
    }

    public ItemDefinitions getItems() {
        return this.items;
    }

    public AttachableDefinitions getAttachables() {
        return this.attachables;
    }

    public TextureDefinitions getTextures() {
        return this.textures;
    }

    public SoundDefinitions getSounds() {
        return this.sounds;
    }

    public ParticleDefinitions getParticles() {
        return this.particles;
    }

    public EntityDefinitions getEntities() {
        return this.entities;
    }

    public ModelDefinitions getModels() {
        return this.models;
    }

    public FogDefinitions getFogs() {
        return this.fogs;
    }

    public BiomeDefinitions getBiomes() {
        return this.biomes;
    }

    public RenderControllerDefinitions getRenderControllers() {
        return this.renderControllers;
    }

}
