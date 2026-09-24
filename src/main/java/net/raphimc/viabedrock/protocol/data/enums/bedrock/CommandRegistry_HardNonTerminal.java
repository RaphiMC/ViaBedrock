/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.protocol.data.enums.bedrock;

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
    WildcardSelection(0x10000A),
    NonIdSelector(0x10000B),
    ScoresArg(0x10000C),
    ScoresArgs(0x10000D),
    ScoreSelectParam(0x10000E),
    ScoreSelector(0x10000F),
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
    RationalRange(0x10001A),
    FullRationalRange(0x10001B),
    SelArgs(0x10001C),
    Args(0x10001D),
    Arg(0x10001E),
    MArg(0x10001F),
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
    HasItemElement(0x10002A),
    HasItemElements(0x10002B),
    HasItemArg(0x10002C),
    HasItemArgs(0x10002D),
    HasItemSelector(0x10002E),
    EquipmentSlotEnum(0x10002F),
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
    CoordXInt(0x10003A),
    CoordYInt(0x10003B),
    CoordZInt(0x10003C),
    CoordXFloat(0x10003D),
    CoordYFloat(0x10003E),
    CoordZFloat(0x10003F),
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
    JsonObject(0x10004A),
    JsonObjectFields(0x10004B),
    JsonObjectCont(0x10004C),
    JsonArray(0x10004D),
    JsonArrayValues(0x10004E),
    JsonArrayCont(0x10004F),
    BlockState(0x100050),
    BlockStateKey(0x100051),
    BlockStateValue(0x100052),
    BlockStateValues(0x100053),
    BlockStateArray(0x100054),
    BlockStateArrayCont(0x100055),
    ClockTimeMarkerName(0x100056),
    Command(0x100057),
    SlashCommand(0x100058),
    CodeBuilderArg(0x100059),
    CodeBuilderArgs(0x10005A),
    CodeBuilderSelectParam(0x10005B),
    CodeBuilderSelector(0x10005C);

    private static final Int2ObjectMap<CommandRegistry_HardNonTerminal> BY_VALUE = new Int2ObjectOpenHashMap<>();

    private final int value;

    static {
        for (CommandRegistry_HardNonTerminal value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CommandRegistry_HardNonTerminal getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandRegistry_HardNonTerminal getByValue(final int value, final CommandRegistry_HardNonTerminal fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CommandRegistry_HardNonTerminal getByName(final String name) {
        for (CommandRegistry_HardNonTerminal value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CommandRegistry_HardNonTerminal getByName(final String name, final CommandRegistry_HardNonTerminal fallback) {
        for (CommandRegistry_HardNonTerminal value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

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
