// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemStackNetResult {

    success(0),
    error(1),
    invalidrequestactiontype(2),
    actionrequestnotallowed(3),
    screenhandlerendrequestfailed(4),
    itemrequestactionhandlercommitfailed(5),
    invalidrequestcraftactiontype(6),
    invalidcraftrequest(7),
    invalidcraftrequestscreen(8),
    invalidcraftresult(9),
    invalidcraftresultindex(10),
    invalidcraftresultitem(11),
    invaliditemnetid(12),
    missingcreatedoutputcontainer(13),
    failedtosetcreateditemoutputslot(14),
    requestalreadyinprogress(15),
    failedtoinitsparsecontainer(16),
    resulttransferfailed(17),
    expecteditemslotnotfullyconsumed(18),
    expectedanywhereitemnotfullyconsumed(19),
    itemalreadyconsumedfromslot(20),
    consumedtoomuchfromslot(21),
    mismatchslotexpectedconsumeditem(22),
    mismatchslotexpectedconsumeditemnetidvariant(23),
    failedtomatchexpectedslotconsumeditem(24),
    failedtomatchexpectedallowedanywhereconsumeditem(25),
    consumeditemoutofallowedslotrange(26),
    consumeditemnotallowed(27),
    playernotincreativemode(28),
    invalidexperimentalreciperequest(29),
    failedtocraftcreative(30),
    failedtogetlevelrecipe(31),
    failedtofindrecipebynetid(32),
    mismatchedcraftingsize(33),
    missinginputsparsecontainer(34),
    mismatchedrecipeforinputgriditems(35),
    emptycraftresults(36),
    failedtoenchant(37),
    missinginputitem(38),
    insufficientplayerleveltoenchant(39),
    missingmaterialitem(40),
    missingactor(41),
    unknownprimaryeffect(42),
    primaryeffectoutofrange(43),
    primaryeffectunavailable(44),
    secondaryeffectoutofrange(45),
    secondaryeffectunavailable(46),
    dstcontainerequaltocreatedoutputcontainer(47),
    dstcontainerandslotequaltosrccontainerandslot(48),
    failedtovalidatesrcslot(49),
    failedtovalidatedstslot(50),
    invalidadjustedamount(51),
    invaliditemsettype(52),
    invalidtransferamount(53),
    cannotswapitem(54),
    cannotplaceitem(55),
    unhandleditemsettype(56),
    invalidremovedamount(57),
    invalidregion(58),
    cannotdropitem(59),
    cannotdestroyitem(60),
    invalidsourcecontainer(61),
    itemnotconsumed(62),
    invalidnumcrafts(63),
    invalidcraftresultstacksize(64),
    cannotremoveitem(65),
    cannotconsumeitem(66),
    screenstackerror(67),
    ;

    private static final Int2ObjectMap<ItemStackNetResult> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemStackNetResult value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ItemStackNetResult getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemStackNetResult getByValue(final int value, final ItemStackNetResult fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ItemStackNetResult getByName(final String name) {
        for (ItemStackNetResult value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ItemStackNetResult getByName(final String name, final ItemStackNetResult fallback) {
        for (ItemStackNetResult value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ItemStackNetResult(final ItemStackNetResult value) {
        this(value.value);
    }

    ItemStackNetResult(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
