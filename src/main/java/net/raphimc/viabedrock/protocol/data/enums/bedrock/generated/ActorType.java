// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorType {

    Undefined(1),
    TypeMask(0x000000ff),
    Mob(0x00000100),
    PathfinderMob(0x00000200 | Mob.getValue()),
    Monster(0x00000800 | PathfinderMob.getValue()),
    Animal(0x00001000 | PathfinderMob.getValue()),
    TamableAnimal(0x00004000 | Animal.getValue()),
    Ambient(0x00008000 | Mob.getValue()),
    UndeadMob(0x00010000 | Monster.getValue()),
    ZombieMonster(0x00020000 | UndeadMob.getValue()),
    Arthropod(0x00040000 | Monster.getValue()),
    Minecart(0x00080000),
    SkeletonMonster(0x00100000 | UndeadMob.getValue()),
    EquineAnimal(0x00200000 | TamableAnimal.getValue()),
    Projectile(0x00400000),
    AbstractArrow(0x00800000),
    WaterAnimal(0x00002000 | PathfinderMob.getValue()),
    VillagerBase(0x01000000 | PathfinderMob.getValue()),
    Chicken(10 | Animal.getValue()),
    Cow(11 | Animal.getValue()),
    Pig(12 | Animal.getValue()),
    Sheep(13 | Animal.getValue()),
    Wolf(14 | TamableAnimal.getValue()),
    Villager(15 | VillagerBase.getValue()),
    MushroomCow(16 | Animal.getValue()),
    Squid(17 | WaterAnimal.getValue()),
    Rabbit(18 | Animal.getValue()),
    Bat(19 | Ambient.getValue()),
    IronGolem(20 | PathfinderMob.getValue()),
    SnowGolem(21 | PathfinderMob.getValue()),
    Ocelot(22 | TamableAnimal.getValue()),
    Horse(23 | EquineAnimal.getValue()),
    PolarBear(28 | Animal.getValue()),
    Llama(29 | Animal.getValue()),
    Parrot(30 | TamableAnimal.getValue()),
    Dolphin(31 | WaterAnimal.getValue()),
    Donkey(24 | EquineAnimal.getValue()),
    Mule(25 | EquineAnimal.getValue()),
    SkeletonHorse(26 | EquineAnimal.getValue() | UndeadMob.getValue()),
    ZombieHorse(27 | EquineAnimal.getValue() | UndeadMob.getValue()),
    Zombie(32 | ZombieMonster.getValue()),
    Creeper(33 | Monster.getValue()),
    Skeleton(34 | SkeletonMonster.getValue()),
    Spider(35 | Arthropod.getValue()),
    PigZombie(36 | UndeadMob.getValue()),
    Slime(37 | Monster.getValue()),
    EnderMan(38 | Monster.getValue()),
    Silverfish(39 | Arthropod.getValue()),
    CaveSpider(40 | Arthropod.getValue()),
    Ghast(41 | Monster.getValue()),
    LavaSlime(42 | Monster.getValue()),
    Blaze(43 | Monster.getValue()),
    ZombieVillager(44 | ZombieMonster.getValue()),
    Witch(45 | Monster.getValue()),
    Stray(46 | SkeletonMonster.getValue()),
    Husk(47 | ZombieMonster.getValue()),
    WitherSkeleton(48 | SkeletonMonster.getValue()),
    Guardian(49 | Monster.getValue()),
    ElderGuardian(50 | Monster.getValue()),
    Npc(51 | Mob.getValue()),
    WitherBoss(52 | UndeadMob.getValue()),
    Dragon(53 | Monster.getValue()),
    Shulker(54 | Monster.getValue()),
    Endermite(55 | Arthropod.getValue()),
    Agent(56 | Mob.getValue()),
    Vindicator(57 | Monster.getValue()),
    Phantom(58 | UndeadMob.getValue()),
    IllagerBeast(59 | Monster.getValue()),
    ArmorStand(61 | Mob.getValue()),
    TripodCamera(62 | Mob.getValue()),
    Player(63 | Mob.getValue()),
    ItemEntity(64),
    PrimedTnt(65),
    FallingBlock(66),
    MovingBlock(67),
    ExperiencePotion(68 | Projectile.getValue()),
    Experience(69),
    EyeOfEnder(70),
    EnderCrystal(71),
    FireworksRocket(72),
    Trident(73 | Projectile.getValue() | AbstractArrow.getValue()),
    Turtle(74 | Animal.getValue()),
    Cat(75 | TamableAnimal.getValue()),
    ShulkerBullet(76 | Projectile.getValue()),
    FishingHook(77),
    Chalkboard(78),
    DragonFireball(79 | Projectile.getValue()),
    Arrow(80 | Projectile.getValue() | AbstractArrow.getValue()),
    Snowball(81 | Projectile.getValue()),
    ThrownEgg(82 | Projectile.getValue()),
    Painting(83),
    LargeFireball(85 | Projectile.getValue()),
    ThrownPotion(86 | Projectile.getValue()),
    Enderpearl(87 | Projectile.getValue()),
    LeashKnot(88),
    WitherSkull(89 | Projectile.getValue()),
    BoatRideable(90),
    WitherSkullDangerous(91 | Projectile.getValue()),
    LightningBolt(93),
    SmallFireball(94 | Projectile.getValue()),
    AreaEffectCloud(95),
    LingeringPotion(101 | Projectile.getValue()),
    LlamaSpit(102 | Projectile.getValue()),
    EvocationFang(103 | Projectile.getValue()),
    EvocationIllager(104 | Monster.getValue()),
    Vex(105 | Monster.getValue()),
    MinecartRideable(84 | Minecart.getValue()),
    MinecartHopper(96 | Minecart.getValue()),
    MinecartTNT(97 | Minecart.getValue()),
    MinecartChest(98 | Minecart.getValue()),
    MinecartFurnace(99 | Minecart.getValue()),
    MinecartCommandBlock(100 | Minecart.getValue()),
    IceBomb(106 | Projectile.getValue()),
    Balloon(107),
    Pufferfish(108 | WaterAnimal.getValue()),
    Salmon(109 | WaterAnimal.getValue()),
    Drowned(110 | ZombieMonster.getValue()),
    Tropicalfish(111 | WaterAnimal.getValue()),
    Fish(112 | WaterAnimal.getValue()),
    Panda(113 | Animal.getValue()),
    Pillager(114 | Monster.getValue()),
    VillagerV2(115 | VillagerBase.getValue()),
    ZombieVillagerV2(116 | ZombieMonster.getValue()),
    Shield(117),
    WanderingTrader(118 | PathfinderMob.getValue()),
    Lectern(119),
    ElderGuardianGhost(120 | Monster.getValue()),
    Fox(121 | Animal.getValue()),
    Bee(122 | Mob.getValue()),
    Piglin(123 | Mob.getValue()),
    Hoglin(124 | Animal.getValue()),
    Strider(125 | Animal.getValue()),
    Zoglin(126 | UndeadMob.getValue()),
    PiglinBrute(127 | Mob.getValue()),
    Goat(128 | Animal.getValue()),
    GlowSquid(129 | WaterAnimal.getValue()),
    Axolotl(130 | Animal.getValue()),
    Warden(131 | Monster.getValue()),
    Frog(132 | Animal.getValue()),
    Tadpole(133 | WaterAnimal.getValue()),
    Allay(134 | Mob.getValue()),
    ChestBoatRideable(136 | BoatRideable.getValue()),
    TraderLlama(137 | Llama.getValue()),
    Camel(138 | Animal.getValue()),
    Sniffer(139 | Animal.getValue()),
    Breeze(140 | Monster.getValue()),
    BreezeWindChargeProjectile(141 | Projectile.getValue()),
    Armadillo(142 | Animal.getValue()),
    WindChargeProjectile(143 | Projectile.getValue()),
    Bogged(144 | SkeletonMonster.getValue()),
    OminousItemSpawner(145),
    Creaking(146 | Monster.getValue()),
    HappyGhast(147 | Animal.getValue()),
    CopperGolem(148 | PathfinderMob.getValue());

    private static final Int2ObjectMap<ActorType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ActorType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorType getByValue(final int value, final ActorType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ActorType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
