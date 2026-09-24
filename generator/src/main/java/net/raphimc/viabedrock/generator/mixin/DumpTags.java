package net.raphimc.viabedrock.generator.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.raphimc.viabedrock.generator.DumpOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Map;

@Mixin(ClientboundUpdateTagsPacket.class)
public class DumpTags {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void dump(final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags,
                      final CallbackInfo callback) {
        final CompoundTag root = new CompoundTag();
        for (Map.Entry<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> registry : tags.entrySet()) {
            final CompoundTag entries = new CompoundTag();
            final Map<Identifier, IntList> tagsInRegistry = ((NetworkPayloadAccessor) (Object) registry.getValue()).viabedrock$getTags();
            for (Map.Entry<Identifier, IntList> tag : tagsInRegistry.entrySet()) {
                entries.put(tag.getKey().toString(), new IntArrayTag(tag.getValue().toIntArray()));
            }
            root.put(registry.getKey().identifier().toString(), entries);
        }
        try {
            DumpOutput.writeNbt("tags.nbt", root);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to dump Java Edition tags", exception);
        }
    }
}
