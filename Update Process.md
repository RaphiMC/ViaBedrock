# ViaBedrock Update Process

This file lists the steps necessary to update ViaBedrock.

1. Update data assets (See `Data Asset Sources.md`) and BedrockMappingData
2. Update ProtocolConstants class
3. Update hardcoded blockstates: Search all files for `new BlockState("`
4. Update data in `protocol/data` and `protocol/model` packages
5. Replace `Types1_20_2` with the new type
6. Replace `EntityTypes1_19_4` and `Protocol1_19_4To1_19_3.class` with the new type
7. Replace `ClientboundPackets1_20_2` and `ServerboundPackets1_20_2` with the new packet enum
8. Replace `ClientboundConfigurationPackets1_20_2` and `ServerboundConfigurationPackets1_20_2` with the new packet enum
9. Update changed packet contents
10. Update rewriters
11. Done!
