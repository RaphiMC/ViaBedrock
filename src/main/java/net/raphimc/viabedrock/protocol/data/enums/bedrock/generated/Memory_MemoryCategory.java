// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Memory_MemoryCategory {

    unknown(0),
    invalid_sizeunknown(1),
    actor(2),
    actoranimation(3),
    actorrendering(4),
    blocktickingqueues(5),
    biome_storage(6),
    blobs(7),
    cereal(8),
    circuitsystem(9),
    client(10),
    commands(11),
    dbstorage(12),
    debug(13),
    documentation(14),
    ecssystems(15),
    fmod(16),
    fonts(17),
    imgui(18),
    input(19),
    jsonui(20),
    jsonui_controlfactory_json(21),
    jsonui_controltree(22),
    jsonui_controltree_controlelement(23),
    jsonui_controltree_populatedatabinding(24),
    jsonui_controltree_populatefocus(25),
    jsonui_controltree_populatelayout(26),
    jsonui_controltree_populateother(27),
    jsonui_controltree_populatesprite(28),
    jsonui_controltree_populatetext(29),
    jsonui_controltree_populatetts(30),
    jsonui_controltree_visibility(31),
    jsonui_createui(32),
    jsonui_defs(33),
    jsonui_layoutmanager(34),
    jsonui_layoutmanager_removedependencies(35),
    jsonui_layoutmanager_initvariable(36),
    languages(37),
    level(38),
    levelstructures(39),
    levelchunk(40),
    levelchunkgen(41),
    levelchunkgenthreadlocal(42),
    lightvolumemanager(43),
    network(44),
    marketplace(45),
    material_dragoncompileddefinition(46),
    material_dragonmaterial(47),
    material_dragonresource(48),
    material_dragonuniformmap(49),
    material_rendermaterial(50),
    material_rendermaterialgroup(51),
    material_variationmanager(52),
    molang(53),
    oreui(54),
    oreui_client(55),
    persona_pieces(56),
    persona_animations(57),
    persona_characters(58),
    persona_skinpacks(59),
    persona_repo(60),
    player(61),
    renderchunk(62),
    renderchunk_indexbuffer(63),
    renderchunk_vertexbuffer(64),
    rendering(65),
    rendering_bgfxinit(66),
    rendering_bgfxstartframe(67),
    rendering_blocktessellator(68),
    rendering_endframe(69),
    rendering_graphicstasksinit(70),
    rendering_library(71),
    rendering_polygonoperatorpool(72),
    rendering_pbrtexturedata(73),
    rendering_renderregistry(74),
    rendering_setup(75),
    rendering_vertices(76),
    requestlog(77),
    resourcepacks(78),
    sound(79),
    subchunk_biomedata(80),
    subchunk_blockdata(81),
    subchunk_lightdata(82),
    textures(83),
    weatherrenderer(84),
    world_generator(85),
    tasks(86),
    test(87),
    test_loadtesttags(88),
    scripting(89),
    scripting_runtime(90),
    scripting_context(91),
    scripting_context_bindings_mc(92),
    scripting_context_bindings_gt(93),
    scripting_context_run(94),
    datadrivenui(95),
    datadrivenui_defs(96),
    gameface(97),
    gameface_system(98),
    gameface_dom(99),
    gameface_css(100),
    gameface_display(101),
    gameface_tempallocator(102),
    gameface_poolallocator(103),
    gameface_dump(104),
    gameface_media(105),
    gameface_json(106),
    gameface_scriptengine(107),
    gameface_script(108),
    gameface_layout(109),
    ;

    private static final Int2ObjectMap<Memory_MemoryCategory> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Memory_MemoryCategory value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Memory_MemoryCategory getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Memory_MemoryCategory getByValue(final int value, final Memory_MemoryCategory fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Memory_MemoryCategory getByName(final String name) {
        for (Memory_MemoryCategory value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Memory_MemoryCategory getByName(final String name, final Memory_MemoryCategory fallback) {
        for (Memory_MemoryCategory value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Memory_MemoryCategory(final Memory_MemoryCategory value) {
        this(value.value);
    }

    Memory_MemoryCategory(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
