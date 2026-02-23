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
    Balancer(5),
    BlockTickingQueues(6),
    Biome_Storage(7),
    Cereal(8),
    CircuitSystem(9),
    Client(10),
    Commands(11),
    DBStorage(12),
    Debug(13),
    Documentation(14),
    ECSSystems(15),
    FMOD(16),
    Fonts(17),
    ImGui(18),
    Input(19),
    JsonUI(20),
    JsonUI_ControlFactory_Json(21),
    JsonUI_ControlTree(22),
    JsonUI_ControlTree_ControlElement(23),
    JsonUI_ControlTree_PopulateDataBinding(24),
    JsonUI_ControlTree_PopulateFocus(25),
    JsonUI_ControlTree_PopulateLayout(26),
    JsonUI_ControlTree_PopulateOther(27),
    JsonUI_ControlTree_PopulateSprite(28),
    JsonUI_ControlTree_PopulateText(29),
    JsonUI_ControlTree_PopulateTTS(30),
    JsonUI_ControlTree_Visibility(31),
    JsonUI_CreateUI(32),
    JsonUI_Defs(33),
    JsonUI_LayoutManager(34),
    JsonUI_LayoutManager_RemoveDependencies(35),
    JsonUI_LayoutManager_InitVariable(36),
    Languages(37),
    Level(38),
    LevelStructures(39),
    LevelChunk(40),
    LevelChunkGen(41),
    LevelChunkGenThreadLocal(42),
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
