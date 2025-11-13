// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CommandRegistry_HardNonTerminal {

    Epsilon(0),
    Int(0x100001),
    Float(0x100002),
    Val(0x100003),
    RVal(0x100004),
    WildcardInt(0x100005),
    Operator(0x100006),
    CompareOperator(0x100007),
    Selection(0x100008),
    StandaloneSelection(0x100009),
    WildcardSelection(0x10000a),
    NonIdSelector(0x10000b),
    ScoresArg(0x10000c),
    ScoresArgs(0x10000d),
    ScoreSelectParam(0x10000e),
    ScoreSelector(0x10000f),
    TagSelector(0x100010),
    FilePath(0x100011),
    FilePathVal(0x100012),
    FilePathCont(0x100013),
    IntegerRangeVal(0x100014),
    IntegerRangePostVal(0x100015),
    IntegerRange(0x100016),
    FullIntegerRange(0x100017),
    RationalRangeVal(0x100018),
    RationalRangePostVal(0x100019),
    RationalRange(0x10001a),
    FullRationalRange(0x10001b),
    SelArgs(0x10001c),
    Args(0x10001d),
    Arg(0x10001e),
    MArg(0x10001f),
    MValue(0x100020),
    NameArg(0x100021),
    TypeArg(0x100022),
    FamilyArg(0x100023),
    HasPermissionArg(0x100024),
    HasPermissionArgs(0x100025),
    HasPermissionSelector(0x100026),
    HasPermissionElement(0x100027),
    HasPermissionElements(0x100028),
    TagArg(0x100029),
    HasItemElement(0x10002a),
    HasItemElements(0x10002b),
    HasItemArg(0x10002c),
    HasItemArgs(0x10002d),
    HasItemSelector(0x10002e),
    EquipmentSlotEnum(0x10002f),
    PropertyValue(0x100030),
    HasPropertyParamValue(0x100031),
    HasPropertyParamEnumValue(0x100032),
    HasPropertyArg(0x100033),
    HasPropertyArgs(0x100034),
    HasPropertyElement(0x100035),
    HasPropertyElements(0x100036),
    HasPropertySelector(0x100037),
    Id(0x100038),
    IdCont(0x100039),
    CoordXInt(0x10003a),
    CoordYInt(0x10003b),
    CoordZInt(0x10003c),
    CoordXFloat(0x10003d),
    CoordYFloat(0x10003e),
    CoordZFloat(0x10003f),
    Position(0x100040),
    PositionFloat(0x100041),
    MessageExp(0x100042),
    Message(0x100043),
    MessageRoot(0x100044),
    PostSelector(0x100045),
    RawText(0x100046),
    RawTextCont(0x100047),
    JsonValue(0x100048),
    JsonField(0x100049),
    JsonObject(0x10004a),
    JsonObjectFields(0x10004b),
    JsonObjectCont(0x10004c),
    JsonArray(0x10004d),
    JsonArrayValues(0x10004e),
    JsonArrayCont(0x10004f),
    BlockState(0x100050),
    BlockStateKey(0x100051),
    BlockStateValue(0x100052),
    BlockStateValues(0x100053),
    BlockStateArray(0x100054),
    BlockStateArrayCont(0x100055),
    Command(0x100056),
    SlashCommand(0x100057),
    CodeBuilderArg(0x100058),
    CodeBuilderArgs(0x100059),
    CodeBuilderSelectParam(0x10005a),
    CodeBuilderSelector(0x10005b);

    private static final Int2ObjectMap<CommandRegistry_HardNonTerminal> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CommandRegistry_HardNonTerminal value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CommandRegistry_HardNonTerminal getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandRegistry_HardNonTerminal getByValue(final int value, final CommandRegistry_HardNonTerminal fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CommandRegistry_HardNonTerminal(final CommandRegistry_HardNonTerminal value) {
        this(value.value);
    }

    CommandRegistry_HardNonTerminal(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
