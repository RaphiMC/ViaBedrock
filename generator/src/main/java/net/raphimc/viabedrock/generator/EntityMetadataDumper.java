package net.raphimc.viabedrock.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class EntityMetadataDumper {

    private EntityMetadataDumper() {
    }

    public static void dump() throws IOException {
        final Set<Class<?>> classes = new LinkedHashSet<>();
        for (Field field : EntityTypes.class.getDeclaredFields()) {
            if (!EntityType.class.isAssignableFrom(field.getType())) {
                continue;
            }
            final Type genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType parameterizedType)
                || !(parameterizedType.getActualTypeArguments()[0] instanceof Class<?> entityClass)) {
                throw new IllegalStateException("Cannot find entity class for " + field.getName());
            }
            for (Class<?> type = entityClass; type != null; type = type.getSuperclass()) {
                classes.add(type);
            }
        }

        final Map<String, JsonArray> sortedFields = new TreeMap<>();
        for (Class<?> type : classes) {
            final List<String> fields = getDataFields(type);
            if (fields.isEmpty()) {
                continue;
            }
            final JsonArray names = new JsonArray();
            fields.forEach(names::add);
            sortedFields.put(toViaBedrockName(type), names);
        }
        final JsonObject root = new JsonObject();
        sortedFields.forEach(root::add);
        DumpOutput.writeJson("entity_data_fields.json", root);
    }

    private static List<String> getDataFields(final Class<?> type) {
        final List<String> names = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (EntityDataAccessor.class.isAssignableFrom(field.getType())) {
                names.add(field.getName()
                    .replaceFirst("DATA_ID_", "")
                    .replaceFirst("DATA_", "")
                    .replaceFirst("ID_", "")
                    .replace("_ID", ""));
            }
        }
        return names;
    }

    private static String toViaBedrockName(final Class<?> type) {
        String name = String.join("_", type.getSimpleName().split("(?<=[a-z])(?=[A-Z])"))
            .toUpperCase(Locale.ROOT);
        if (!name.equals("LIVING_ENTITY")) {
            name = name.replace("_ENTITY", "");
        }
        if (name.startsWith("MINECART_")) {
            name = name.substring("MINECART_".length()) + "_MINECART";
        }
        name = name.replace("THROWN_", "").replace("THROWABLE_", "").replace("PRIMED_", "");
        return switch (name) {
            case "LEASH_FENCE_KNOT" -> "LEASH_KNOT";
            case "FISHING_HOOK" -> "FISHING_BOBBER";
            case "AGEABLE_MOB" -> "ABSTRACT_AGEABLE";
            case "MUSHROOM_COW" -> "MOOSHROOM";
            case "ENDER_MAN" -> "ENDERMAN";
            case "WITHER_BOSS" -> "WITHER";
            case "RAIDER" -> "ABSTRACT_RAIDER";
            case "HANGING" -> "HANGING_ENTITY";
            default -> name;
        };
    }
}
