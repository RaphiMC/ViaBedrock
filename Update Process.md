# ViaBedrock Update Process

This file lists the steps necessary to update ViaBedrock.

1. Update data assets (See `Data Asset Sources.md`) and BedrockMappingData
2. Update ProtocolConstants class
3. Update the EnumGeneratorTask commit hash in build.gradle
4. Update hardcoded blockstates: Search all files for `new BlockState("`
5. Update data in the `protocol/data` package
6. Replace `Types1_21` and `EntityTypes1_20_5` with the new type
7. Replace `ClientboundPackets1_21` and `ServerboundPackets1_20_5` with the new packet enum
8. Replace `ClientboundConfigurationPackets1_21` and `ServerboundConfigurationPackets1_20_5` with the new packet enum
9. Update changed packet contents
10. Update rewriters
11. Done!
