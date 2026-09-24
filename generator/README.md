# ViaBedrock data generator

This Fabric mod dumps Java Edition data used by ViaBedrock. It produces these files:

| Dump                        | ViaBedrock asset                      |
|-----------------------------|---------------------------------------|
| `effects.json`              | `data/java/effects.json`              |
| `entity_data_fields.json`   | `data/java/entity_data_fields.json`   |
| `heightmap_blockstates.nbt` | `data/java/heightmap_blockstates.nbt` |
| `registries.nbt`            | `data/java/registries.nbt`            |
| `tags.nbt`                  | `data/java/tags.nbt`                  |

The mod targets Java Edition 26.2, which matches this checkout. The project uses the root [GPLv3 license](../LICENSE). The built jar includes a copy.

## Generate the files

1. From `generator`, run `./gradlew build` (or `gradlew.bat build` on Windows).
2. Install the jar from `build/libs` in a Fabric client for the Minecraft version in `gradle.properties`. Use Java 25.
3. Start the client. The mod writes effects, entity fields, and heightmap states to the game's `dumps` directory.
4. Connect to a vanilla server of the same version. The mod writes `registries.nbt` and `tags.nbt` from configuration packets.
5. Compare the five dumps with `src/main/resources/assets/viabedrock/data/java/` before copying them into ViaBedrock.

Set `-Dviabedrock.generator.output=/path/to/dumps` as a Java argument to change the output directory. Each client launch clears the previous registry and tag dumps. Wait for login to finish before
copying those files. Review entity field names against ViaBedrock's entity enum because the name conversion is version specific.
