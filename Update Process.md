# ViaBedrock Update Process

This file lists the steps necessary to update ViaBedrock.

1. Update data assets (See `Data Asset Sources.md`) and BedrockMappingData
2. Update ProtocolConstants class
3. Update the EnumGeneratorTask commit hash in build.gradle
4. Update hardcoded blockstates: Search all files for `new BlockState("`
5. Update data in the `protocol/data` package
6. Replace `Types1_20_3` with the new type
7. Replace `EntityTypes1_20_3` and `Protocol1_20_3To1_20_2.class` with the new type
8. Replace `ClientboundPackets1_20_3` and `ServerboundPackets1_20_3` with the new packet enum
9. Replace `ClientboundConfigurationPackets1_20_3` and `ServerboundConfigurationPackets1_20_2` with the new packet enum
10. Update changed packet contents
11. Update rewriters
12. Done!

Bedrock 1.20.70 -> Java 1.20.5
* Items
  * minecraft:armadillo_scute
  * minecraft:armadillo_spawn_egg
  * minecraft:wolf_armor
  * minecraft:bogged_spawn_egg
  * minecraft:vault
  * minecraft:wind_charge
* Check BlockEntities
* Check block_id in custom blocks
