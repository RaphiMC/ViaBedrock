// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MinecraftPacketIds {

    keepalive(0),
    login(1),
    playstatus(2),
    servertoclienthandshake(3),
    clienttoserverhandshake(4),
    disconnect(5),
    resourcepacksinfo(6),
    resourcepackstack(7),
    resourcepackclientresponse(8),
    text(9),
    settime(10),
    startgame(11),
    addplayer(12),
    addactor(13),
    removeactor(14),
    additemactor(15),
    serverplayerpostmoveposition(16),
    takeitemactor(17),
    moveabsoluteactor(18),
    moveplayer(19),
    passengerjump(20),
    updateblock(21),
    addpainting(22),
    ticksync(23),
    levelsoundeventv1(24),
    levelevent(25),
    tileevent(26),
    actorevent(27),
    mobeffect(28),
    updateattributes(29),
    inventorytransaction(30),
    playerequipment(31),
    mobarmorequipment(32),
    interact(33),
    blockpickrequest(34),
    actorpickrequest(35),
    playeraction(36),
    actorfall(37),
    hurtarmor(38),
    setactordata(39),
    setactormotion(40),
    setactorlink(41),
    sethealth(42),
    setspawnposition(43),
    animate(44),
    respawn(45),
    containeropen(46),
    containerclose(47),
    playerhotbar(48),
    inventorycontent(49),
    inventoryslot(50),
    containersetdata(51),
    craftingdata(52),
    craftingevent(53),
    guidatapickitem(54),
    adventuresettings(55),
    blockactordata(56),
    playerinput(57),
    fullchunkdata(58),
    setcommandsenabled(59),
    setdifficulty(60),
    changedimension(61),
    setplayergametype(62),
    playerlist(63),
    simpleevent(64),
    legacytelemetryevent(65),
    spawnexperienceorb(66),
    mapdata(67),
    mapinforequest(68),
    requestchunkradius(69),
    chunkradiusupdated(70),
    itemframedropitem(71),
    gameruleschanged(72),
    camera(73),
    bossevent(74),
    showcredits(75),
    availablecommands(76),
    commandrequest(77),
    commandblockupdate(78),
    commandoutput(79),
    updatetrade(80),
    updateequip(81),
    resourcepackdatainfo(82),
    resourcepackchunkdata(83),
    resourcepackchunkrequest(84),
    transfer(85),
    playsound(86),
    stopsound(87),
    settitle(88),
    addbehaviortree(89),
    structureblockupdate(90),
    showstoreoffer(91),
    purchasereceipt(92),
    playerskin(93),
    subclientlogin(94),
    automationclientconnect(95),
    setlasthurtby(96),
    bookedit(97),
    npcrequest(98),
    phototransfer(99),
    showmodalform(100),
    modalformresponse(101),
    serversettingsrequest(102),
    serversettingsresponse(103),
    showprofile(104),
    setdefaultgametype(105),
    removeobjective(106),
    setdisplayobjective(107),
    setscore(108),
    labtable(109),
    updateblocksynced(110),
    movedeltaactor(111),
    setscoreboardidentity(112),
    setlocalplayerasinit(113),
    updatesoftenum(114),
    ping(115),
    blockpalette(116),
    scriptcustomevent(117),
    spawnparticleeffect(118),
    availableactoridlist(119),
    levelsoundeventv2(120),
    networkchunkpublisherupdate(121),
    biomedefinitionlist(122),
    levelsoundevent(123),
    leveleventgeneric(124),
    lecternupdate(125),
    videostreamconnect(126),
    addentity(127),
    removeentity(128),
    clientcachestatus(129),
    onscreentextureanimation(130),
    mapcreatelockedcopy(131),
    structuretemplatedataexportrequest(132),
    structuretemplatedataexportresponse(133),
    clientcacheblobstatuspacket(135),
    clientcachemissresponsepacket(136),
    educationsettingspacket(137),
    emote(138),
    multiplayersettingspacket(139),
    settingscommandpacket(140),
    anvildamage(141),
    completedusingitem(142),
    networksettings(143),
    playerauthinputpacket(144),
    creativecontent(145),
    playerenchantoptions(146),
    itemstackrequest(147),
    itemstackresponse(148),
    playerarmordamage(149),
    codebuilderpacket(150),
    updateplayergametype(151),
    emotelist(152),
    positiontrackingdbserverbroadcast(153),
    positiontrackingdbclientrequest(154),
    debuginfopacket(155),
    packetviolationwarning(156),
    motionpredictionhints(157),
    triggeranimation(158),
    camerashake(159),
    playerfogsetting(160),
    correctplayermovepredictionpacket(161),
    itemregistrypacket(162),
    filtertextpacket(163),
    clientbounddebugrendererpacket(164),
    syncactorproperty(165),
    addvolumeentitypacket(166),
    removevolumeentitypacket(167),
    simulationtypepacket(168),
    npcdialoguepacket(169),
    eduuriresourcepacket(170),
    createphotopacket(171),
    updatesubchunkblocks(172),
    photoinforequest(173),
    subchunkpacket(174),
    subchunkrequestpacket(175),
    playerstartitemcooldown(176),
    scriptmessagepacket(177),
    codebuildersourcepacket(178),
    tickingareasloadstatus(179),
    dimensiondatapacket(180),
    agentaction(181),
    changemobproperty(182),
    lessonprogresspacket(183),
    requestabilitypacket(184),
    requestpermissionspacket(185),
    toastrequest(186),
    updateabilitiespacket(187),
    updateadventuresettingspacket(188),
    deathinfo(189),
    editornetworkpacket(190),
    featureregistrypacket(191),
    serverstats(192),
    requestnetworksettings(193),
    gametestrequestpacket(194),
    gametestresultspacket(195),
    playerclientinputpermissions(196),
    clientcheatabilitypacket(197),
    camerapresets(198),
    unlockedrecipes(199),
    titlespecificpacketsstart(200),
    titlespecificpacketsend(299),
    camerainstruction(300),
    compressedbiomedefinitionlist(301),
    trimdata(302),
    opensign(303),
    agentanimation(304),
    refreshentitlementspacket(305),
    playertogglecrafterslotrequestpacket(306),
    setplayerinventoryoptions(307),
    sethudpacket(308),
    awardachievementpacket(309),
    clientboundclosescreen(310),
    clientboundloadingscreenpacket(311),
    serverboundloadingscreenpacket(312),
    jigsawstructuredatapacket(313),
    currentstructurefeaturepacket(314),
    serverbounddiagnosticspacket(315),
    cameraaimassist(316),
    containerregistrycleanup(317),
    movementeffect(318),
    setmovementauthoritymode(319),
    cameraaimassistactorpriority(339),
    cameraaimassistpresets(320),
    clientcameraaimassist(321),
    clientmovementpredictionsyncpacket(322),
    updateclientoptions(323),
    playervideocapturepacket(324),
    playerupdateentityoverridespacket(325),
    playerlocation(326),
    syncworldclocks(344),
    sendpartydestinationcookie(349),
    partydestinationcookieresponse(350),
    setplayerfurnaceoptions(351),
    recordstarted(352),
    ;

    private static final Int2ObjectMap<MinecraftPacketIds> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MinecraftPacketIds value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MinecraftPacketIds getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MinecraftPacketIds getByValue(final int value, final MinecraftPacketIds fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MinecraftPacketIds getByName(final String name) {
        for (MinecraftPacketIds value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MinecraftPacketIds getByName(final String name, final MinecraftPacketIds fallback) {
        for (MinecraftPacketIds value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MinecraftPacketIds(final MinecraftPacketIds value) {
        this(value.value);
    }

    MinecraftPacketIds(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
