// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorType {

    undefined(1),
    mob(256),
    pathfindermob(768),
    monster(2816),
    animal(4864),
    tamableanimal(21248),
    ambient(33024),
    undeadmonster(68352),
    zombiemonster(199424),
    arthropod(264960),
    minecart(524288),
    skeletonmonster(1116928),
    equineanimal(2118400),
    projectile(4194304),
    abstractarrow(8388608),
    wateranimal(8960),
    villagerbase(16777984),
    chicken(4874),
    cow(4875),
    pig(4876),
    sheep(4877),
    wolf(21262),
    villager(16777999),
    mushroomcow(4880),
    squid(8977),
    rabbit(4882),
    bat(33043),
    irongolem(788),
    snowgolem(789),
    ocelot(21270),
    horse(2118423),
    polarbear(4892),
    llama(4893),
    parrot(21278),
    dolphin(8991),
    donkey(2118424),
    mule(2118425),
    skeletonhorse(2183962),
    zombiehorse(2183963),
    zombie(199456),
    creeper(2849),
    skeleton(1116962),
    spider(264995),
    pigzombie(68388),
    slime(2853),
    enderman(2854),
    silverfish(264999),
    cavespider(265000),
    ghast(2857),
    lavaslime(2858),
    blaze(2859),
    zombievillager(199468),
    witch(2861),
    stray(1116974),
    husk(199471),
    witherskeleton(1116976),
    guardian(2865),
    elderguardian(2866),
    npc(307),
    witherboss(68404),
    dragon(2869),
    shulker(2870),
    endermite(265015),
    agent(312),
    vindicator(2873),
    phantom(68410),
    illagerbeast(2875),
    armorstand(317),
    tripodcamera(318),
    player(319),
    itementity(64),
    primedtnt(65),
    fallingblock(66),
    movingblock(67),
    experiencepotion(4194372),
    experience(69),
    eyeofender(70),
    endercrystal(71),
    fireworksrocket(72),
    trident(12582985),
    turtle(4938),
    cat(21323),
    shulkerbullet(4194380),
    fishinghook(77),
    chalkboard(78),
    dragonfireball(4194383),
    arrow(12582992),
    snowball(4194385),
    thrownegg(4194386),
    painting(83),
    largefireball(4194389),
    thrownpotion(4194390),
    enderpearl(4194391),
    leashknot(88),
    witherskull(4194393),
    boatrideable(90),
    witherskulldangerous(4194395),
    lightningbolt(93),
    smallfireball(4194398),
    areaeffectcloud(95),
    lingeringpotion(4194405),
    llamaspit(4194406),
    evocationfang(4194407),
    evocationillager(2920),
    vex(2921),
    minecartrideable(524372),
    minecarthopper(524384),
    minecarttnt(524385),
    minecartchest(524386),
    minecartfurnace(524387),
    minecartcommandblock(524388),
    icebomb(4194410),
    balloon(107),
    pufferfish(9068),
    salmon(9069),
    drowned(199534),
    tropicalfish(9071),
    fish(9072),
    panda(4977),
    pillager(2930),
    villagerv2(16778099),
    zombievillagerv2(199540),
    shield(117),
    wanderingtrader(886),
    lectern(119),
    elderguardianghost(2936),
    fox(4985),
    bee(378),
    piglin(379),
    hoglin(4988),
    strider(4989),
    zoglin(68478),
    piglinbrute(383),
    goat(4992),
    glowsquid(9089),
    axolotl(4994),
    warden(2947),
    frog(4996),
    tadpole(9093),
    allay(390),
    chestboatrideable(218),
    traderllama(5021),
    camel(5002),
    sniffer(5003),
    breeze(2956),
    breezewindchargeprojectile(4194445),
    armadillo(5006),
    windchargeprojectile(4194447),
    bogged(1117072),
    ominousitemspawner(145),
    creaking(2962),
    happyghast(5011),
    coppergolem(916),
    nautilus(9109),
    zombienautilus(74646),
    parched(1117079),
    camelhusk(70552),
    sulfurcube(921),
    cushion(154),
    ;

    private static final Int2ObjectMap<ActorType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ActorType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorType getByValue(final int value, final ActorType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ActorType getByName(final String name) {
        for (ActorType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ActorType getByName(final String name, final ActorType fallback) {
        for (ActorType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ActorType(final ActorType value) {
        this(value.value);
    }

    ActorType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
