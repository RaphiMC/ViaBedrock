package net.raphimc.viabedrock.generator.mixin;

import net.minecraft.core.RegistrySynchronization.PackedRegistryEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.raphimc.viabedrock.generator.DumpOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(ClientboundRegistryDataPacket.class)
public class DumpRegistries {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void dump(final CallbackInfo callback) {
        final ClientboundRegistryDataPacket packet = (ClientboundRegistryDataPacket) (Object) this;
        final CompoundTag entries = new CompoundTag();
        for (PackedRegistryEntry entry : packet.entries()) {
            entry.data().ifPresent(tag -> entries.put(entry.id().toString(), tag));
        }
        try {
            mergeRegistry(packet.registry().identifier().toString(), entries);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to dump Java Edition registries", exception);
        }
    }

    private static synchronized void mergeRegistry(final String name, final CompoundTag entries) throws IOException {
        final Path output = DumpOutput.DIRECTORY.resolve("registries.nbt");
        final CompoundTag root = Files.exists(output)
                ? NbtIo.readCompressed(output, NbtAccounter.unlimitedHeap())
                : new CompoundTag();
        root.put(name, entries);
        DumpOutput.writeNbt("registries.nbt", root);
    }
}
