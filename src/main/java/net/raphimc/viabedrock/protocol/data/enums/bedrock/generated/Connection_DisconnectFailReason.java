// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Connection_DisconnectFailReason {

    unknown(0),
    cantconnectnointernet(1),
    nopermissions(2),
    unrecoverableerror(3),
    thirdpartyblocked(4),
    thirdpartynointernet(5),
    thirdpartybadip(6),
    thirdpartynoserverorserverlocked(7),
    versionmismatch(8),
    skinissue(9),
    invitesessionnotfound(10),
    edulevelsettingsmissing(11),
    localservernotfound(12),
    legacydisconnect(13),
    internal_userleavegameattempted(14),
    platformlockedskinserror(15),
    realmsworldunassigned(16),
    realmsservercantconnect(17),
    realmsserverhidden(18),
    realmsserverdisabledbeta(19),
    realmsserverdisabled(20),
    crossplatformdisabled(21),
    testonly_cantconnect(22),
    sessionnotfound(23),
    clientsettingsincompatiblewithserver(24),
    serverfull(25),
    invalidplatformskin(26),
    editionversionmismatch(27),
    editionmismatch(28),
    levelnewerthanexeversion(29),
    internal_nofailoccurred(30),
    bannedskin(31),
    timeout(32),
    servernotfound(33),
    outdatedserver(34),
    outdatedclient(35),
    nopremiumplatform(36),
    multiplayerdisabled(37),
    nowifi(38),
    worldcorruption(39),
    noreason(40),
    disconnected(41),
    invalidplayer(42),
    loggedinotherlocation(43),
    serveridconflict(44),
    notallowed(45),
    notauthenticated(46),
    invalidtenant(47),
    unknownpacket(48),
    unexpectedpacket(49),
    invalidcommandrequestpacket(50),
    hostsuspended(51),
    loginpacketnorequest(52),
    loginpacketnocert(53),
    missingclient(54),
    kicked(55),
    kickedforexploit(56),
    kickedforidle(57),
    resourcepackproblem(58),
    incompatiblepack(59),
    outofstorage(60),
    invalidlevel(61),
    disconnectpacket(62),
    blockmismatch(63),
    invalidheights(64),
    invalidwidths(65),
    connectionlost(66),
    zombieconnection(67),
    shutdown(68),
    reasonnotset(69),
    loadingstatetimeout(70),
    resourcepackloadingfailed(71),
    searchingforsessionloadingscreenfailed(72),
    nethernetprotocolversion(73),
    subsystemstatuserror(74),
    emptyauthfromdiscovery(75),
    emptyurlfromdiscovery(76),
    expiredauthfromdiscovery(77),
    unknownsignalservicesigninfailure(78),
    xbljoinlobbyfailure(79),
    unspecifiedclientinstancedisconnection(80),
    nethernetsessionnotfound(81),
    nethernetcreatepeerconnection(82),
    nethernetice(83),
    nethernetconnectrequest(84),
    nethernetconnectresponse(85),
    nethernetnegotiationtimeout(86),
    nethernetinactivitytimeout(87),
    staleconnectionbeingreplaced(88),
    realmssessionnotfound(89),
    badpacket(90),
    nethernetfailedtocreateoffer(91),
    nethernetfailedtocreateanswer(92),
    nethernetfailedtosetlocaldescription(93),
    nethernetfailedtosetremotedescription(94),
    nethernetnegotiationtimeoutwaitingforresponse(95),
    nethernetnegotiationtimeoutwaitingforaccept(96),
    nethernetincomingconnectionignored(97),
    nethernetsignalingparsingfailure(98),
    nethernetsignalingunknownerror(99),
    nethernetsignalingunicastdeliveryfailed(100),
    nethernetsignalingbroadcastdeliveryfailed(101),
    nethernetsignalinggenericdeliveryfailed(102),
    editormismatcheditorworld(103),
    editormismatchvanillaworld(104),
    worldtransfernotprimaryclient(105),
    internal_requestservershutdown(106),
    clientgamesetupcancelled(107),
    clientgamesetupfailed(108),
    novenue(109),
    nethernetsignalingsigninfailed(110),
    sessionaccessdenied(111),
    servicesigninissue(112),
    nethernetnosignalingchannel(113),
    nethernetnotloggedin(114),
    nethernetclientsignalingerror(115),
    subclientlogindisabled(116),
    deeplinktryingtoopendemoworldwhilesignedin(117),
    asyncjointaskdenied(118),
    realmstimelinerequired(119),
    guestwithouthost(120),
    failedtojoinexperience(121),
    nethernetdatachannelclosed(122),
    discoveryenvironmentmismatch(123),
    hostwithoutkeys(124),
    hostsignedout(125),
    scriptwatchdogexception(126),
    scriptmemorylimitexceeded(127),
    storagelowduringgameplay(128),
    storagefullduringgameplay(129),
    levelstoragecorruption(130),
    editionmismatchvanillatoedu(131),
    editionmismatchedutovanilla(132),
    editormismatcheditortovanilla(133),
    editormismatchvanillatoeditor(134),
    denylisted(135),
    noncemissing(136),
    noncenotfound(137),
    nonceexpired(138),
    noncenotvalid(139),
    hostdisconnected(140),
    editorjoinintentpolicyfailure(141),
    nethernetidentitynotallowed(142),
    invalidname(143),
    expiredtoken(144),
    hostacceptsnotypeofauth(145),
    notauthenticatedfastfail(146),
    editornotallowed(147),
    missingstructuredata(148),
    unsupportedtransport(149),
    ;

    private static final Int2ObjectMap<Connection_DisconnectFailReason> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Connection_DisconnectFailReason value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Connection_DisconnectFailReason getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Connection_DisconnectFailReason getByValue(final int value, final Connection_DisconnectFailReason fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Connection_DisconnectFailReason getByName(final String name) {
        for (Connection_DisconnectFailReason value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Connection_DisconnectFailReason getByName(final String name, final Connection_DisconnectFailReason fallback) {
        for (Connection_DisconnectFailReason value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Connection_DisconnectFailReason(final Connection_DisconnectFailReason value) {
        this(value.value);
    }

    Connection_DisconnectFailReason(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
