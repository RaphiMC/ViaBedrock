// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ExpressionOp {

    unknown(-1),
    leftbrace(0),
    rightbrace(1),
    leftbracket(2),
    rightbracket(3),
    leftparenthesis(4),
    rightparenthesis(5),
    negate(6),
    logicalnot(7),
    abs(8),
    add(9),
    acos(10),
    asin(11),
    atan(12),
    atan2(13),
    ceil(14),
    clamp(15),
    copysign(16),
    cos(17),
    dieroll(18),
    dierollint(19),
    div(20),
    exp(21),
    floor(22),
    hermiteblend(23),
    lerp(24),
    lerprotate(25),
    ln(26),
    max(27),
    min(28),
    minangle(29),
    mod(30),
    mul(31),
    pow(32),
    random(33),
    randomint(34),
    round(35),
    sin(36),
    sign(37),
    sqrt(38),
    trunc(39),
    queryfunction(40),
    arrayvariable(41),
    contextvariable(42),
    entityvariable(43),
    tempvariable(44),
    memberaccessor(45),
    hashedstringhash(46),
    geometryvariable(47),
    materialvariable(48),
    texturevariable(49),
    lessthan(50),
    lessequal(51),
    greaterequal(52),
    greaterthan(53),
    logicalequal(54),
    logicalnotequal(55),
    logicalor(56),
    logicaland(57),
    nullcoalescing(58),
    conditional(59),
    conditionalelse(60),
    float(61),
    pi(62),
    array(63),
    geometry(64),
    material(65),
    texture(66),
    loop(67),
    foreach(68),
    break(69),
    continue(70),
    assignment(71),
    pointer(72),
    semicolon(73),
    return(74),
    comma(75),
    this(76),
    internal_nonevaluatedarray(77),
    inverselerp(78),
    easeinquad(79),
    easeoutquad(80),
    easeinoutquad(81),
    easeincubic(82),
    easeoutcubic(83),
    easeinoutcubic(84),
    easeinquart(85),
    easeoutquart(86),
    easeinoutquart(87),
    easeinquint(88),
    easeoutquint(89),
    easeinoutquint(90),
    easeinsine(91),
    easeoutsine(92),
    easeinoutsine(93),
    easeinexpo(94),
    easeoutexpo(95),
    easeinoutexpo(96),
    easeincirc(97),
    easeoutcirc(98),
    easeinoutcirc(99),
    easeinbounce(100),
    easeoutbounce(101),
    easeinoutbounce(102),
    easeinback(103),
    easeoutback(104),
    easeinoutback(105),
    easeinelastic(106),
    easeoutelastic(107),
    easeinoutelastic(108),
    ;

    private static final Int2ObjectMap<ExpressionOp> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ExpressionOp value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ExpressionOp getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ExpressionOp getByValue(final int value, final ExpressionOp fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ExpressionOp getByName(final String name) {
        for (ExpressionOp value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ExpressionOp getByName(final String name, final ExpressionOp fallback) {
        for (ExpressionOp value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ExpressionOp(final ExpressionOp value) {
        this(value.value);
    }

    ExpressionOp(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
