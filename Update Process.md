# ViaBedrock Update Process
This file lists the steps necessary to update ViaBedrock.

## Tools
The generators live in the `tool` source set and are run through Gradle:

```bash
./gradlew generateJavaDataEnums
./gradlew generateBedrockDataEnums --args="--protocol-docs=/path/to/protocol-docs"
./gradlew sortJson --args="--file=data/custom/item_mappings.json"
```

Run `./gradlew tasks --group="viabedrock tools"` for the full list and the options each tool takes.
Every option can also be set as the system property `viabedrock.tool.<option>` or as the environment variable
`VIABEDROCK_TOOL_<OPTION>`, which is the easiest way to keep machine specific paths out of the command line:

```bash
export VIABEDROCK_TOOL_CLIENT_DATA="C:\XboxGames\Minecraft for Windows\Content\data"
export VIABEDROCK_TOOL_PROTOCOL_DOCS="$HOME/Projects/protocol-docs"
```

The Bedrock client tools read extracted packs from `run/client-data/resource_packs_unpacked` by default.
The enum generator reads metadata from `run/protocol-docs` by default.
All generators write to `src/main/java` and `src/main/resources`. Review their output with `git diff`.

## Mapping tools
The curated mapping files are large enough that finding what an update left behind by hand is the slow part. These
Gradle tasks do that part, and every proposal they make is checked against the real java block state list before it is
written, so a proposal is either something java actually has or it is reported as unresolved.

```bash
./gradlew reportMappingGaps      # what is missing, stale or pointing at something java dropped
./gradlew reportMappingMistakes  # what loads fine but disagrees with the rest of the data
./gradlew syncDerivedMappings    # adds predictable block tags and potted states
./gradlew proposeMappings        # writes block and identity proposals to run/mapping-proposals
./gradlew selfTestMappings       # hides each mapped block and checks the proposer reproduces it by hand
./gradlew applyMappingProposals  # merges the reviewed proposals into the data assets
./gradlew validateMappings       # checks derived data and loads the mapping data
```

After updating the Bedrock palette and Java registry, run `syncDerivedMappings`. It adds sign and shelf block tags,
and directly named potted blocks. Other custom mappings remain curated.
`reportMappingGaps` checks these derived entries, and `validateMappings` fails when any are missing or disagree.

Run the report and proposer. Read `run/mapping-proposals/block_states.txt`.
Remove or edit incorrect entries in `run/mapping-proposals/block_states.json`.
Then apply the proposals, validate the mappings, and review the git diff.
The apply task checks proposals against the current Bedrock and Java data. Pass `--replace` only when you intend to
change an existing block state mapping.
For missing entities, effects, particles, and sounds, review `run/mapping-proposals/identifiers.json`.
The proposer adds an entry only when both editions use the same identifier. Apply a reviewed category with
`./gradlew applyMappingProposals --args="--category=entities"`. Run `reportMappingMistakes` before applying item fixes;
it records the current item IDs so the apply task can reject old proposals.

`reportMappingMistakes` is the other half. Everything it lists already loads, so nothing here is proof of a bug, but
each finding is a mapping that disagrees with the rest of the data:

- `identity-available` is a mapping pointing somewhere else although java has an item or block of exactly that name
  and nothing else claims it. That is what a family copied from an older one looks like once java catches up, like
  every `poplar_*` item sitting on `pale_oak_*`.
- `family-outlier` is a block translating its properties differently from the blocks which have the same properties on
  both sides. Bedrock does have blocks which genuinely differ, so read before changing.
- `ignored-property` is a bedrock property which never changes the java state although java has a property of that
  name.

Item suggestions land in `run/mapping-proposals/item_fixes.json` and go in with
`./gradlew applyMappingProposals --args="--category=items"`, which only ever rewrites `java_id` and leaves
`java_tag` and `java_name` alone.

Proposals come from three strategies, and the report says which one produced each block:

- `analogy` copies a block which has the same bedrock properties and is already fully mapped, then rewrites the java
  identifier the same way the bedrock identifier differs. This is how `oak_stairs` maps `lime_wool_stairs`. It is the
  strategy to trust, the self test reproduces 99.7% of hand written mappings with it.
- `properties` matches bedrock property names against the java ones, using name aliases learned from the mappings that
  already exist, and falls back to the value the rest of the file uses. This is what handles blocks which only gained
  properties, like the panes and bars which grew `connection_*`.
- `previous-mapping` reuses the java block the old mapping of the same bedrock block pointed at. It is what keeps
  `trip_wire` on java's `tripwire` and the education edition `hard_*` panes on their plain counterparts.

## Java Edition Update
1. Add missing enums to `JavaDataEnumGenerator` and run `./gradlew generateJavaDataEnums`
2. Update `assets/viabedrock/data` (See `Data Asset Sources.md`) and `BedrockMappingData`
3. Update data in the `protocol/data` package, including the version constants in `ProtocolConstants`
4. Run `./gradlew generateMappingData` to generate the mapping classes
5. Update hardcoded blockstates: Search all files for `new BlockState("`
6. Replace `VersionedTypes.V26_3` and `EntityTypes26_3` with the new type
7. Replace `ClientboundPackets26_3` and `ServerboundPackets26_3` with the new packet enum
8. Replace `ClientboundConfigurationPackets26_3` and `ServerboundConfigurationPackets1_21_9` with the new packet enum
9. Check `StructuredDataKey` usages and update them to new Minecraft version if needed
10. Update changed packet contents
11. Update rewriters

## Bedrock Edition Update
1. Add new vanilla resource packs to `assets/viabedrock/data/custom/vanilla_resource_packs.json`.
   `./gradlew generateBedrockPacks` prints the packs which are missing from that file.
2. Download the enum metadata from a [Mojang/bedrock-protocol-docs release](https://github.com/Mojang/bedrock-protocol-docs/releases).
   Extract it to `run/protocol-docs` and run `./gradlew generateBedrockDataEnums`.
3. Update manual enums.
4. Extract the client resource packs with [brarchive-extractor](https://github.com/LPaicen/brarchive-extractor).
   The tools read the extracted packs from `run/client-data/resource_packs_unpacked` by default.
5. Update `assets/viabedrock/data` (see `Data Asset Sources.md`) and `BedrockMappingData`.
   `./gradlew generateBedrockSoundLists` and `./gradlew generateBedrockParticleList` generate the sound and particle
   files from the client. New block state upgrade schemas are applied to the mappings with
   `./gradlew upgradeBlockStateMappings --args="--from=<first new schema>"`.
6. Update data in the `protocol/data` package, including the version constants in `ProtocolConstants`.
7. Run `./gradlew generateMappingData` to generate the mapping classes.
8. Update hardcoded block states. Search all files for `new BlockState("`.
9. Update fields in `SkinProvider`.
10. Update changed packet contents.
11. Update rewriters.
