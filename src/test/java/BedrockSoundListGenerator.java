/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class BedrockSoundListGenerator {

    public static void main(String[] args) throws Throwable {
        final File bedrockClientDataDir = new File("data_dir_here");
        final File resourcePacksDir = new File(bedrockClientDataDir, "resource_packs");

        final Set<String> soundList = new HashSet<>();
        for (File packDir : resourcePacksDir.listFiles()) {
            if (packDir.getName().equals("vanilla_vr")) continue;
            if (packDir.getName().startsWith("chemistry")) continue;
            if (packDir.getName().startsWith("education")) continue;

            final File soundsDefinitionsFile = new File(packDir, "sounds/sound_definitions.json");
            if (soundsDefinitionsFile.exists()) {
                System.out.println(soundsDefinitionsFile.getAbsolutePath());
                JsonObject obj = JsonParser.parseString(Files.readString(soundsDefinitionsFile.toPath())).getAsJsonObject();
                if (obj.has("sound_definitions")) {
                    obj = obj.getAsJsonObject("sound_definitions");
                }

                for (Map.Entry<String, JsonElement> soundDefinitions : obj.entrySet()) {
                    soundList.add(soundDefinitions.getKey());
                }
            }
        }

        final List<String> sortedSoundList = new ArrayList<>(soundList);
        Collections.sort(sortedSoundList);
        System.out.println(new Gson().newBuilder().setPrettyPrinting().create().toJson(sortedSoundList));
    }

}
