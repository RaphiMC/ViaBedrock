package net.raphimc.viabedrock.generator;

import net.fabricmc.api.ModInitializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ViaBedrockDataGenerator implements ModInitializer {

    @Override
    public void onInitialize() {
        try {
            DumpOutput.prepare();
            dumpHeightmapBlockStates();
            EntityMetadataDumper.dump();
            EffectRegistry.dump();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to generate Java Edition data", exception);
        }
    }

    private static void dumpHeightmapBlockStates() throws IOException {
        final List<Integer> blockStates = new ArrayList<>();
        for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
            if (Heightmap.Types.MOTION_BLOCKING.isOpaque().test(state)) {
                blockStates.add(Block.BLOCK_STATE_REGISTRY.getId(state));
            }
        }

        final CompoundTag root = new CompoundTag();
        root.putIntArray("motion_blocking", blockStates.stream().mapToInt(Integer::intValue).toArray());
        DumpOutput.writeNbt("heightmap_blockstates.nbt", root);
    }
}
