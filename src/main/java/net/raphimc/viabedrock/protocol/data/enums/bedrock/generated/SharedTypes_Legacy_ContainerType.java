// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_Legacy_ContainerType {

    none(-9),
    inventory(-1),
    container(0),
    workbench(1),
    furnace(2),
    enchantment(3),
    brewing_stand(4),
    anvil(5),
    dispenser(6),
    dropper(7),
    hopper(8),
    cauldron(9),
    minecart_chest(10),
    minecart_hopper(11),
    horse(12),
    beacon(13),
    structure_editor(14),
    trade(15),
    command_block(16),
    jukebox(17),
    armor(18),
    hand(19),
    compound_creator(20),
    element_constructor(21),
    material_reducer(22),
    lab_table(23),
    loom(24),
    lectern(25),
    grindstone(26),
    blast_furnace(27),
    smoker(28),
    stonecutter(29),
    cartography(30),
    hud(31),
    jigsaw_editor(32),
    smithing_table(33),
    chest_boat(34),
    decorated_pot(35),
    crafter(36),
    ;

    private static final Int2ObjectMap<SharedTypes_Legacy_ContainerType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_Legacy_ContainerType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_Legacy_ContainerType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_Legacy_ContainerType getByValue(final int value, final SharedTypes_Legacy_ContainerType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_Legacy_ContainerType getByName(final String name) {
        for (SharedTypes_Legacy_ContainerType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_Legacy_ContainerType getByName(final String name, final SharedTypes_Legacy_ContainerType fallback) {
        for (SharedTypes_Legacy_ContainerType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_Legacy_ContainerType(final SharedTypes_Legacy_ContainerType value) {
        this(value.value);
    }

    SharedTypes_Legacy_ContainerType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
