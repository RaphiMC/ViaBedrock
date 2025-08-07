# ViaBedrock Update Process

This file lists the steps necessary to update ViaBedrock.

1. Update ProtocolConstants class
2. Add new vanilla packs to `ResourcePacksStorage#VANILLA_PACK_NAMES`
3. Update and run BedrockDataEnumGenerator to generate the bedrock data enums
4. Update data assets (See `Data Asset Sources.md`) and BedrockMappingData
5. Update hardcoded blockstates: Search all files for `new BlockState("`
6. Update data in the `protocol/data` package
7. Replace `VersionedTypes.V1_21_6` and `EntityTypes1_21_6` with the new type
8. Replace `ClientboundPackets1_21_6` and `ServerboundPackets1_21_6` with the new packet enum
9. Replace `ClientboundConfigurationPackets1_21_6` and `ServerboundConfigurationPackets1_21_6` with the new packet enum
10. Check `StructuredDataKey` usages and update them to new Minecraft version if needed
11. Update changed packet contents
12. Update rewriters
13. Done!

Waiting for the next Java Edition update:
- entity_mappings.json
  - minecraft:copper_golem
- item_mappings.json
  - minecraft:copper_helmet
  - minecraft:copper_leggings
  - minecraft:copper_shovel
  - minecraft:copper_pickaxe
  - minecraft:waxed_exposed_copper_chest
  - minecraft:exposed_copper_chest
  - minecraft:weathered_copper_chest
  - minecraft:copper_nugget
  - minecraft:waxed_weathered_copper_chest
  - minecraft:copper_chestplate
  - minecraft:oxidized_copper_chest
  - minecraft:copper_golem_spawn_egg
  - minecraft:copper_sword
  - minecraft:copper_chest
  - minecraft:copper_axe
  - minecraft:waxed_copper_chest
  - minecraft:copper_hoe
  - minecraft:waxed_oxidized_copper_chest
  - minecraft:copper_boots
- Check block entity list and rewrites
