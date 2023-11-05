# ViaBedrock Update Process

This file lists the steps necessary to update ViaBedrock.

1. Update data assets (See `Data Asset Sources.md`) and BedrockMappingData
2. Update ProtocolConstants class
3. Update hardcoded blockstates: Search all files for `new BlockState("`
4. Replace `Types1_20_2` with the new type
5. Replace `EntityTypes1_19_4` and `Protocol1_19_4To1_19_3.class` with the new type
6. Replace `ClientboundPackets1_20_2` and `ServerboundPackets1_20_2` with the new packet enum
7. Replace `ClientboundConfigurationPackets1_20_2` and `ServerboundConfigurationPackets1_20_2` with the new packet enum
8. Update changed packet contents
9. Update rewriters
10. Done!
