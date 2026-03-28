// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Memory_MemoryCategory {

    Unknown(0),
    Invalid_SizeUnknown(1),
    Actor(2),
    ActorAnimation(3),
    ActorRendering(4),
    BlockTickingQueues(5),
    Biome_Storage(6),
    Cereal(7),
    CircuitSystem(8),
    Client(9),
    Commands(10),
    DBStorage(11),
    Debug(12),
    Documentation(13),
    ECSSystems(14),
    FMOD(15),
    Fonts(16),
    ImGui(17),
    Input(18),
    JsonUI(19),
    JsonUI_ControlFactory_Json(20),
    JsonUI_ControlTree(21),
    JsonUI_ControlTree_ControlElement(22),
    JsonUI_ControlTree_PopulateDataBinding(23),
    JsonUI_ControlTree_PopulateFocus(24),
    JsonUI_ControlTree_PopulateLayout(25),
    JsonUI_ControlTree_PopulateOther(26),
    JsonUI_ControlTree_PopulateSprite(27),
    JsonUI_ControlTree_PopulateText(28),
    JsonUI_ControlTree_PopulateTTS(29),
    JsonUI_ControlTree_Visibility(30),
    JsonUI_CreateUI(31),
    JsonUI_Defs(32),
    JsonUI_LayoutManager(33),
    JsonUI_LayoutManager_RemoveDependencies(34),
    JsonUI_LayoutManager_InitVariable(35),
    Languages(36),
    Level(37),
    LevelStructures(38),
    LevelChunk(39),
    LevelChunkGen(40),
    LevelChunkGenThreadLocal(41),
    LightVolumeManager(42),
    Network(43),
    Marketplace(44),
    Material_DragonCompiledDefinition(45),
    Material_DragonMaterial(46),
    Material_DragonResource(47),
    Material_DragonUniformMap(48),
    Material_RenderMaterial(49),
    Material_RenderMaterialGroup(50),
    Material_VariationManager(51),
    Molang(52),
    OreUI(53),
    Persona(54),
    Player(55),
    RenderChunk(56),
    RenderChunk_IndexBuffer(57),
    RenderChunk_VertexBuffer(58),
    Rendering(59),
    Rendering_Library(60),
    RequestLog(61),
    ResourcePacks(62),
    Sound(63),
    SubChunk_BiomeData(64),
    SubChunk_BlockData(65),
    SubChunk_LightData(66),
    Textures(67),
    VR(68),
    WeatherRenderer(69),
    World_Generator(70),
    Tasks(71),
    Test(72),
    Scripting(73),
    Scripting_Runtime(74),
    Scripting_Context(75),
    Scripting_Context_Bindings_MC(76),
    Scripting_Context_Bindings_GT(77),
    Scripting_Context_Run(78),
    DataDrivenUI(79),
    DataDrivenUI_Defs(80),
    Gameface(81),
    Gameface_System(82),
    Gameface_DOM(83),
    Gameface_CSS(84),
    Gameface_Display(85),
    Gameface_TempAllocator(86),
    Gameface_PoolAllocator(87),
    Gameface_Dump(88),
    Gameface_Media(89),
    Gameface_JSON(90),
    Gameface_ScriptEngine(91),
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
