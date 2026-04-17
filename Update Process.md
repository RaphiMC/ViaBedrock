# ViaBedrock Update Process
This file lists the steps necessary to update ViaBedrock.

## Java Edition Update
1. Update and run `JavaDataEnumGenerator` to generate the data enums
2. Update `assets/viabedrock/data` (See `Data Asset Sources.md`) and `BedrockMappingData`
3. Update data in the `protocol/data` package
4. Run `MappingDataGenerator` to generate the mapping classes
5. Update hardcoded blockstates: Search all files for `new BlockState("`
6. Replace `VersionedTypes.V26_1` and `EntityTypes1_21_11` with the new type
7. Replace `ClientboundPackets26_1` and `ServerboundPackets26_1` with the new packet enum
8. Replace `ClientboundConfigurationPackets1_21_9` and `ServerboundConfigurationPackets1_21_9` with the new packet enum
9. Check `StructuredDataKey` usages and update them to new Minecraft version if needed
10. Update changed packet contents
11. Update rewriters

## Bedrock Edition Update
1. Add new vanilla resource packs to `assets/viabedrock/data/custom/vanilla_resource_packs.json`
2. Update and run `BedrockDataEnumGenerator` to generate the data enums
3. Update `assets/viabedrock/data` (See `Data Asset Sources.md`) and `BedrockMappingData`
4. Update data in the `protocol/data` package
5. Run `MappingDataGenerator` to generate the mapping classes
6. Update hardcoded blockstates: Search all files for `new BlockState("`
7. Update changed packet contents
8. Update rewriters
