// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MinecraftEventing_AchievementIds {

    chestfullofcobblestone(7),
    diamondforyou(10),
    ironbelly(20),
    ironman(21),
    onarail(29),
    overkill(30),
    returntosender(37),
    sniperduel(38),
    stayinfrosty(39),
    takeinventory(40),
    maproom(50),
    freightstation(52),
    smelteverything(53),
    tasteofyourownmedicine(54),
    whenpigsfly(56),
    inception(58),
    artificialselection(60),
    freediver(61),
    spawnthewither(62),
    beaconator(63),
    greatview(64),
    supersonic(65),
    theendagain(66),
    treasurehunter(67),
    shootingstar(68),
    fashionshow(69),
    selfpublishedauthor(71),
    alternativefuel(72),
    sleepwiththefishes(73),
    castaway(74),
    imamarinebiologist(75),
    sailthe7seas(76),
    megold(77),
    ahoy(78),
    atlantis(79),
    onepickletwopickleseapicklefour(80),
    doabarrelroll(81),
    moskstraumen(82),
    echolocation(83),
    wherehaveyoubeen(84),
    topoftheworld(85),
    fruitontheloom(86),
    soundthealarm(87),
    buylowsellhigh(88),
    disenchanted(89),
    timeforstew(90),
    beeourguest(91),
    totalbeelocation(92),
    stickysituation(93),
    covermeindebris(94),
    floatyourgoat(95),
    friend(96),
    waxonwaxoff(97),
    striderriddeninlavainoverworld(98),
    goathornacquired(99),
    jukeboxusedinmeadows(100),
    tradedatworldheight(101),
    survivedfallfromworldheight(102),
    sneakclosetosculksensor(103),
    itspreads(104),
    birthdaysong(105),
    withourpowerscombined(106),
    plantingthepast(107),
    carefulrestoration(108),
    revaulting(109),
    crafterscraftingcrafters(110),
    whoneedsrockets(111),
    overoverkill(112),
    hearttransplanter(113),
    stayhydrated(114),
    mobkabob(115),
    adventuringtime(116),
    uhoh(117),
    gettingwood(118),
    benchmaking(119),
    timetomine(120),
    hottopic(121),
    acquirehardware(122),
    gettinganupgrade(123),
    monsterhunter(124),
    diamonds(125),
    plethoraofcats(126),
    ;

    private static final Int2ObjectMap<MinecraftEventing_AchievementIds> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MinecraftEventing_AchievementIds value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MinecraftEventing_AchievementIds getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MinecraftEventing_AchievementIds getByValue(final int value, final MinecraftEventing_AchievementIds fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MinecraftEventing_AchievementIds getByName(final String name) {
        for (MinecraftEventing_AchievementIds value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MinecraftEventing_AchievementIds getByName(final String name, final MinecraftEventing_AchievementIds fallback) {
        for (MinecraftEventing_AchievementIds value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MinecraftEventing_AchievementIds(final MinecraftEventing_AchievementIds value) {
        this(value.value);
    }

    MinecraftEventing_AchievementIds(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
