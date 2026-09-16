// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ContainerEnumName {

    anvilinputcontainer(0),
    anvilmaterialcontainer(1),
    anvilresultpreviewcontainer(2),
    smithingtableinputcontainer(3),
    smithingtablematerialcontainer(4),
    smithingtableresultpreviewcontainer(5),
    armorcontainer(6),
    levelentitycontainer(7),
    beaconpaymentcontainer(8),
    brewingstandinputcontainer(9),
    brewingstandresultcontainer(10),
    brewingstandfuelcontainer(11),
    combinedhotbarandinventorycontainer(12),
    craftinginputcontainer(13),
    craftingoutputpreviewcontainer(14),
    recipeconstructioncontainer(15),
    recipenaturecontainer(16),
    recipeitemscontainer(17),
    recipefoodcontainer(64),
    recipeblockscontainer(65),
    recipefurnaceitemscontainer(66),
    recipesearchcontainer(18),
    recipesearchbarcontainer(19),
    recipeequipmentcontainer(20),
    recipebookcontainer(21),
    enchantinginputcontainer(22),
    enchantingmaterialcontainer(23),
    furnacefuelcontainer(24),
    furnaceingredientcontainer(25),
    furnaceresultcontainer(26),
    horseequipcontainer(27),
    hotbarcontainer(28),
    inventorycontainer(29),
    shulkerboxcontainer(30),
    tradeingredient1container(31),
    tradeingredient2container(32),
    traderesultpreviewcontainer(33),
    offhandcontainer(34),
    compoundcreatorinput(35),
    compoundcreatoroutputpreview(36),
    elementconstructoroutputpreview(37),
    materialreducerinput(38),
    materialreduceroutput(39),
    labtableinput(40),
    loominputcontainer(41),
    loomdyecontainer(42),
    loommaterialcontainer(43),
    loomresultpreviewcontainer(44),
    blastfurnaceingredientcontainer(45),
    smokeringredientcontainer(46),
    trade2ingredient1container(47),
    trade2ingredient2container(48),
    trade2resultpreviewcontainer(49),
    grindstoneinputcontainer(50),
    grindstoneadditionalcontainer(51),
    grindstoneresultpreviewcontainer(52),
    stonecutterinputcontainer(53),
    stonecutterresultpreviewcontainer(54),
    cartographyinputcontainer(55),
    cartographyadditionalcontainer(56),
    cartographyresultpreviewcontainer(57),
    barrelcontainer(58),
    cursorcontainer(59),
    createdoutputcontainer(60),
    smithingtabletemplatecontainer(61),
    crafterlevelentitycontainer(62),
    dynamiccontainer(63),
    ;

    private static final Int2ObjectMap<ContainerEnumName> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ContainerEnumName value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ContainerEnumName getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ContainerEnumName getByValue(final int value, final ContainerEnumName fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ContainerEnumName getByName(final String name) {
        for (ContainerEnumName value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ContainerEnumName getByName(final String name, final ContainerEnumName fallback) {
        for (ContainerEnumName value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ContainerEnumName(final ContainerEnumName value) {
        this(value.value);
    }

    ContainerEnumName(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
