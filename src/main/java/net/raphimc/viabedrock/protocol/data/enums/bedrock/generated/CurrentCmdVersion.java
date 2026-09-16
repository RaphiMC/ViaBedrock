// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CurrentCmdVersion {

    invalid(-1),
    initial(1),
    tprotationclamping(2),
    newbedrockcmdsystem(3),
    executeusesvec3(4),
    clonefixes(5),
    updateaquatic(6),
    entityselectorusesvec3(7),
    containersdontdropitemsanymore(8),
    filtersobeydimensions(9),
    executeandblockcommandandselfselectorfixes(10),
    instanteffectsuseticks(11),
    dontregisterbrokenfunctioncommands(12),
    clearspawnpointcommand(13),
    cloneandteleportrotationfixes(14),
    teleportdimensionfixes(15),
    cloneupdateblockandtimefixes(16),
    cloneintersectfix(17),
    functionexecuteorderandchestslotfix(18),
    nontickingareasnolongerconsideredloaded(19),
    spreadplayershazardandresolveplayerbynamefix(20),
    newexecutecommandsyntaxexperimentandchestloottablefixandteleportfacingverticalunclampedandlocatebiomeandfeaturemerged(21),
    waterloggingaddedtostructurecommand(22),
    selectordistancefilteredandrelativerotationfix(23),
    newsummoncommandaddedrotationoptionsandbubblecolumnclonefixandexecuteindimensionteleportfixandnewexecuterotationfix(24),
    newexecutecommandreleaseenchantcommandlevelfixandhasitemdatafixandcommanddeferral(25),
    executeifscorefixes(26),
    replaceitemandlootreplaceblockcommandsdonotplaceitemsintocauldronsfix(27),
    changestocommandoriginrotation(28),
    removeauxvalueparameterfromblockcommands(29),
    volumeselectorfixes(30),
    enablesummonrotation(31),
    summoncommanddefaultrotation(32),
    positionaldimensionfiltering(33),
    commandselectorhasitemfilternolongercallssameitemfunction(34),
    agentsweepingblocktest(34),
    blockstateequals(35),
    commandpositionfix(35),
    commandselectorhasitemfilterusesdataasdamageforselectingdamageableitems(36),
    executedetectconditionsubcommandnotallownonloadedblocks(37),
    removesuicidekeyword(38),
    clonecontainerblockentityremovalfix(39),
    stopsoundmusicfix(40),
    spreadplayersstuckingroundfixandmaxheightparameter(41),
    locatestructureoutput(42),
    postblockflattening(43),
    testforblockcommanddoesnotignoreblockstate(44),
    count(53),
    latest(52),
    ;

    private static final Int2ObjectMap<CurrentCmdVersion> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CurrentCmdVersion value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CurrentCmdVersion getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CurrentCmdVersion getByValue(final int value, final CurrentCmdVersion fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CurrentCmdVersion getByName(final String name) {
        for (CurrentCmdVersion value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CurrentCmdVersion getByName(final String name, final CurrentCmdVersion fallback) {
        for (CurrentCmdVersion value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CurrentCmdVersion(final CurrentCmdVersion value) {
        this(value.value);
    }

    CurrentCmdVersion(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
