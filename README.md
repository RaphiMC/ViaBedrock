# ViaBedrock
ViaVersion addon to add support for Minecraft: Bedrock Edition servers.

ViaBedrock aims to be as compatible and accurate as possible with the Minecraft: Bedrock Edition protocol.

## Usage
**ViaBedrock is in very early stages of development and NOT intended for regular use yet.**

If you want to try it out anyway you can download the latest dev build
[here](https://build.lenni0451.net/job/ViaBedrock) (Make sure to download the ViaProxy-ViaBedrockPlugin jar file).

To use the ViaProxy implementation of ViaBedrock you have to download the latest [ViaProxy](https://build.lenni0451.net/job/ViaProxy/) dev build first and put the ViaProxy-ViaBedrockPlugin jar file into the plugins folder of ViaProxy.

**Do not report any bugs yet. There are still a lot of things which are not implemented yet.**

If you want to talk about ViaBedrock or learn more about it you can join my [Discord](https://discord.gg/dCzT9XHEWu).

## Credits
ViaBedrock would not have been possible without the following projects:
- [ViaVersion](https://github.com/ViaVersion/ViaVersion): Provides the base for translating packets
- [CloudburstMC Protocol](https://github.com/CloudburstMC/Protocol): Documentation of the Bedrock Edition protocol
- [wiki.vg](https://wiki.vg/Bedrock_Protocol): Documentation of the Bedrock Edition protocol
- [mcrputil](https://github.com/valaphee/mcrputil): Documentation of Bedrock Edition resource pack encryption

Additionally ViaBedrock uses assets and data dumps from other projects: See the `Data Asset Sources.md` file for more information.

## Features
- [x] Pinging
- [x] Joining
- [x] Xbox Live Auth
- [x] Chat / Commands
- [x] Chunks
- [x] Chunk caching
- [x] Block updates
- [x] Biomes
- [x] Players
- [x] Entities
- [ ] Entity interactions
- [ ] Entity metadata
- [ ] Entity attributes
- [ ] Entity mounts
- [x] Client-Authoritative Movement
- [ ] Server-Authoritative Movement
- [ ] Client-Authoritative Inventory
- [ ] Server-Authoritative Inventory
- [ ] Block breaking
- [ ] Block placing
- [ ] Respawning
- [x] Dimension switching
- [ ] Form GUIs
- [ ] Scoreboard
- [ ] Titles
- [ ] Bossbar
- [x] Player list
- [ ] Sounds
- [ ] Particles
- [x] Player Skins (Requires [BedrockSkinUtility](https://github.com/Camotoy/BedrockSkinUtility) mod)
- [] Very basic resource pack conversion
