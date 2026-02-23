# ViaBedrock Update Process
This file lists the steps necessary to update ViaBedrock.

1. Add new vanilla packs to `ResourcePacksStorage#VANILLA_PACK_NAMES`
2. Update and run `BedrockDataEnumGenerator` and `JavaDataEnumGenerator` to generate the data enums
3. Update `assets/viabedrock/data` (See `Data Asset Sources.md`) and `BedrockMappingData`
4. Update data in the `protocol/data` package
5. Run `MappingDataGenerator` to generate the mapping classes
6. Update hardcoded blockstates: Search all files for `new BlockState("`
7. Replace `VersionedTypes.V1_21_11` and `EntityTypes1_21_11` with the new type
8. Replace `ClientboundPackets1_21_11` and `ServerboundPackets1_21_6` with the new packet enum
9. Replace `ClientboundConfigurationPackets1_21_9` and `ServerboundConfigurationPackets1_21_9` with the new packet enum
10. Check `StructuredDataKey` usages and update them to new Minecraft version if needed
11. Update changed packet contents
12. Update rewriters
