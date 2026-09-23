package net.raphimc.viabedrock.generator;

import com.google.gson.JsonArray;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import java.io.IOException;

public final class EffectRegistry {

    private EffectRegistry() {
    }

    public static void dump() throws IOException {
        final JsonArray effects = new JsonArray();
        for (int id = 0; id < BuiltInRegistries.MOB_EFFECT.size(); id++) {
            final Reference<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.get(id).orElseThrow();
            effects.add(BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).toString());
        }
        DumpOutput.writeJson("effects.json", effects);
    }
}
