// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MolangVersion {

    Invalid(-1),
    BeforeVersioning(0),
    Initial(1),
    FixedItemRemainingUseDurationQuery(2),
    ExpressionErrorMessages(3),
    UnexpectedOperatorErrors(4),
    ConditionalOperatorAssociativity(5),
    ComparisonAndLogicalOperatorPrecedence(6),
    DivideByNegativeValue(7),
    FixedCapeFlapAmountQuery(8),
    QueryBlockPropertyRenamedToState(9),
    DeprecateOldBlockQueryNames(10),
    DeprecatedSnifferAndCamelQueries(11),
    LeafSupportingInFirstSolidBlockBelow(12),
    Latest(13),
    HardcodedMolang(Latest);

    private static final Int2ObjectMap<MolangVersion> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MolangVersion value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static MolangVersion getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MolangVersion getByValue(final int value, final MolangVersion fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
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
