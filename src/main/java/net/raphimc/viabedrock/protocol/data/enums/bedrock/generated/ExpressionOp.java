// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ExpressionOp {

    Unknown(-1),
    LeftBrace(0),
    RightBrace(1),
    LeftBracket(2),
    RightBracket(3),
    LeftParenthesis(4),
    RightParenthesis(5),
    Negate(6),
    LogicalNot(7),
    Abs(8),
    Add(9),
    Acos(10),
    Asin(11),
    Atan(12),
    Atan2(13),
    Ceil(14),
    Clamp(15),
    CopySign(16),
    Cos(17),
    DieRoll(18),
    DieRollInt(19),
    Div(20),
    Exp(21),
    Floor(22),
    HermiteBlend(23),
    Lerp(24),
    LerpRotate(25),
    Ln(26),
    Max(27),
    Min(28),
    MinAngle(29),
    Mod(30),
    Mul(31),
    Pow(32),
    Random(33),
    RandomInt(34),
    Round(35),
    Sin(36),
    Sign(37),
    Sqrt(38),
    Trunc(39),
    QueryFunction(40),
    ArrayVariable(41),
    ContextVariable(42),
    EntityVariable(43),
    TempVariable(44),
    MemberAccessor(45),
    HashedStringHash(46),
    GeometryVariable(47),
    MaterialVariable(48),
    TextureVariable(49),
    LessThan(50),
    LessEqual(51),
    GreaterEqual(52),
    GreaterThan(53),
    LogicalEqual(54),
    LogicalNotEqual(55),
    LogicalOr(56),
    LogicalAnd(57),
    NullCoalescing(58),
    Conditional(59),
    ConditionalElse(60),
    Float(61),
    Pi(62),
    Array(63),
    Geometry(64),
    Material(65),
    Texture(66),
    Loop(67),
    ForEach(68),
    Break(69),
    Continue(70),
    Assignment(71),
    Pointer(72),
    Semicolon(73),
    Return(74),
    Comma(75),
    This(76),
    Internal_NonEvaluatedArray(77),
    InverseLerp(78),
    EaseInQuad(79),
    EaseOutQuad(80),
    EaseInOutQuad(81),
    EaseInCubic(82),
    EaseOutCubic(83),
    EaseInOutCubic(84),
    EaseInQuart(85),
    EaseOutQuart(86),
    EaseInOutQuart(87),
    EaseInQuint(88),
    EaseOutQuint(89),
    EaseInOutQuint(90),
    EaseInSine(91),
    EaseOutSine(92),
    EaseInOutSine(93),
    EaseInExpo(94),
    EaseOutExpo(95),
    EaseInOutExpo(96),
    EaseInCirc(97),
    EaseOutCirc(98),
    EaseInOutCirc(99),
    EaseInBounce(100),
    EaseOutBounce(101),
    EaseInOutBounce(102),
    EaseInBack(103),
    EaseOutBack(104),
    EaseInOutBack(105),
    EaseInElastic(106),
    EaseOutElastic(107),
    EaseInOutElastic(108),
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
