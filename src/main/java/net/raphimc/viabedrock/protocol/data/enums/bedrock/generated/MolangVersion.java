// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MolangVersion {

    invalid(-1),
    beforeversioning(0),
    initial(1),
    fixeditemremainingusedurationquery(2),
    expressionerrormessages(3),
    unexpectedoperatorerrors(4),
    conditionaloperatorassociativity(5),
    comparisonandlogicaloperatorprecedence(6),
    dividebynegativevalue(7),
    fixedcapeflapamountquery(8),
    queryblockpropertyrenamedtostate(9),
    deprecateoldblockquerynames(10),
    deprecatedsnifferandcamelqueries(11),
    leafsupportinginfirstsolidblockbelow(12),
    numvalidversions(14),
    latest(13),
    hardcodedmolang(13),
    ;

    private static final Int2ObjectMap<MolangVersion> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MolangVersion value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MolangVersion getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MolangVersion getByValue(final int value, final MolangVersion fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MolangVersion getByName(final String name) {
        for (MolangVersion value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MolangVersion getByName(final String name, final MolangVersion fallback) {
        for (MolangVersion value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MolangVersion(final MolangVersion value) {
        this(value.value);
    }

    MolangVersion(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
