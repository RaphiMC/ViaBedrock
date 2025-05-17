# ViaBedrock Update Process

This file lists the steps necessary to update ViaBedrock.

1. Update data assets (See `Data Asset Sources.md`) and BedrockMappingData
2. Update ProtocolConstants class
3. Update the EnumGeneratorTask commit hash in build.gradle
4. Update hardcoded blockstates: Search all files for `new BlockState("`
5. Add new vanilla packs to `ResourcePacksStorage#VANILLA_PACK_NAMES`
6. Update data in the `protocol/data` package
7. Replace `VersionedTypes.V1_21_5` and `EntityTypes1_21_5` with the new type
8. Replace `ClientboundPackets1_21_5` and `ServerboundPackets1_21_5` with the new packet enum
9. Replace `ClientboundConfigurationPackets1_21` and `ServerboundConfigurationPackets1_20_5` with the new packet enum
10. Check `StructuredDataKey` usages and update them to new Minecraft version if needed
11. Update changed packet contents
12. Update rewriters
13. Done!

Next Java Edition update:
* Experimental feature mappings
  * locator_bar
  * experimental_graphics
  * y_2025_drop_2
* Entity mappings
  * happy_ghast
* Item mappings
  * black_harness
  * pink_harness
  * gray_harness
  * orange_harness
  * light_gray_harness
  * blue_harness
  * lime_harness
  * green_harness
  * brown_harness
  * purple_harness
  * red_harness
  * cyan_harness
  * white_harness
  * yellow_harness
  * magenta_harness
  * light_blue_harness
  * happy_ghast_spawn_egg
  * dried_ghast
* Blockstate mappings
  * dried_ghast
* Packets
  * Locator bar packet
