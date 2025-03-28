/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.model.resourcepack.BlockDefinitions;
import net.raphimc.viabedrock.api.model.resourcepack.SoundDefinitions;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.Puv_Legacy_LevelSoundEvent;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import util.Util;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class BedrockSoundListGenerator {

    private static final Map<String, Puv_Legacy_LevelSoundEvent> SOUND_EVENT_MAP = Map.ofEntries(
            Map.entry("camera.take_picture", Puv_Legacy_LevelSoundEvent.TakePicture),
            Map.entry("leashknot.break", Puv_Legacy_LevelSoundEvent.BreakLeashKnot),
            Map.entry("leashknot.place", Puv_Legacy_LevelSoundEvent.PlaceLeashKnot),
            Map.entry("drip.lava.pointed_dripstone", Puv_Legacy_LevelSoundEvent.PointedDripstoneDripLava),
            Map.entry("cauldron_drip.water.pointed_dripstone", Puv_Legacy_LevelSoundEvent.PointedDripstoneCauldronDripWater),
            Map.entry("drip.water.pointed_dripstone", Puv_Legacy_LevelSoundEvent.PointedDripstoneDripWater),
            Map.entry("cauldron_drip.lava.pointed_dripstone", Puv_Legacy_LevelSoundEvent.PointedDripstoneCauldronDripLava),
            Map.entry("armor.repair_wolf", Puv_Legacy_LevelSoundEvent.WolfArmorRepair),
            Map.entry("armor.crack_wolf", Puv_Legacy_LevelSoundEvent.WolfArmorCrack),
            Map.entry("armor.break_wolf", Puv_Legacy_LevelSoundEvent.WolfArmorBreak),
            Map.entry("block.end_portal_frame.fill", Puv_Legacy_LevelSoundEvent.EnderEyePlaced),
            Map.entry("mob.husk.convert_to_zombie", Puv_Legacy_LevelSoundEvent.ConvertHuskToZombie),
            Map.entry("tilt_down.big_dripleaf", Puv_Legacy_LevelSoundEvent.BigDripleafTiltDown),
            Map.entry("tilt_up.big_dripleaf", Puv_Legacy_LevelSoundEvent.BigDripleafTiltUp),
            Map.entry("item.spyglass.use", Puv_Legacy_LevelSoundEvent.UseSpyglass),
            Map.entry("item.spyglass.stop_using", Puv_Legacy_LevelSoundEvent.StopUsingSpyglass),
            Map.entry("block.furnace.lit", Puv_Legacy_LevelSoundEvent.FurnaceUse),
            Map.entry("random.anvil_use", Puv_Legacy_LevelSoundEvent.AnvilUse),
            Map.entry("block.click", Puv_Legacy_LevelSoundEvent.BlockClick),
            Map.entry("block.click.fail", Puv_Legacy_LevelSoundEvent.BlockClickFail),
            Map.entry("block.sign.waxed_interact_fail", Puv_Legacy_LevelSoundEvent.WaxedSignInteractFail),
            Map.entry("power.on.sculk_sensor", Puv_Legacy_LevelSoundEvent.SculkSensorPowerOn),
            Map.entry("power.off.sculk_sensor", Puv_Legacy_LevelSoundEvent.SculkSensorPowerOff),
            Map.entry("block.composter.fill_success", Puv_Legacy_LevelSoundEvent.ComposterFillLayer),
            Map.entry("bubble.up", Puv_Legacy_LevelSoundEvent.BubbleColumnUpwards),
            Map.entry("bubble.down", Puv_Legacy_LevelSoundEvent.BubbleColumnDownwards),
            Map.entry("elderguardian.curse", Puv_Legacy_LevelSoundEvent.GuardianCurse),
            Map.entry("pick_berries.cave_vines", Puv_Legacy_LevelSoundEvent.CaveVinesPickBerries),
            Map.entry("lodestone_compass.link_compass_to_lodestone", Puv_Legacy_LevelSoundEvent.LinkCompassToLodestone),
            Map.entry("chime.amethyst_block", Puv_Legacy_LevelSoundEvent.AmethystBlockChime),
            Map.entry("block.smoker.smoke", Puv_Legacy_LevelSoundEvent.SmokerUse),
            Map.entry("block.blastfurnace.fire_crackle", Puv_Legacy_LevelSoundEvent.BlastFurnaceUse),
            Map.entry("block.bell.hit", Puv_Legacy_LevelSoundEvent.Bell),
            Map.entry("mob.armor_stand.place", Puv_Legacy_LevelSoundEvent.ArmorPlace),
            Map.entry("block.turtle_egg.attack", Puv_Legacy_LevelSoundEvent.TurtleEggAttacked),
            Map.entry("block.end_portal.spawn", Puv_Legacy_LevelSoundEvent.EndPortalCreated),
            Map.entry("mob.hoglin.converted_to_zombified", Puv_Legacy_LevelSoundEvent.HoglinConvertToZombified),
            Map.entry("smithing_table.use", Puv_Legacy_LevelSoundEvent.UseSmithingTable),
            Map.entry("item.book.put", Puv_Legacy_LevelSoundEvent.LecternBookPlace),
            Map.entry("mob.warning", Puv_Legacy_LevelSoundEvent.MobWarning),
            Map.entry("irongolem.crack", Puv_Legacy_LevelSoundEvent.CrackIronGolem),
            Map.entry("irongolem.repair", Puv_Legacy_LevelSoundEvent.RepairIronGolem),
            Map.entry("convert_mooshroom", Puv_Legacy_LevelSoundEvent.MooshroomConvert),
            Map.entry("converted_to_zombified", Puv_Legacy_LevelSoundEvent.ConvertToZombified),
            Map.entry("prepare.attack", Puv_Legacy_LevelSoundEvent.PrepareAttackSpell),
            Map.entry("drink.honey", Puv_Legacy_LevelSoundEvent.HoneybottleDrink),
            Map.entry("item.use.on", Puv_Legacy_LevelSoundEvent.ItemUseOn),
            Map.entry("break_pot", Puv_Legacy_LevelSoundEvent.BreakDecoratedPot),
            Map.entry("shatter_pot", Puv_Legacy_LevelSoundEvent.ShatterDecoratedPot),
            Map.entry("charge.sculk", Puv_Legacy_LevelSoundEvent.SculkCharge)
    );

    private static final Set<String> CANCELLED_SOUNDS = Set.of(
            "imitate.panda",
            "guardian.flop",
            "block.sculk_sensor.clicking",
            "block.sculk_sensor.clicking_stop",
            "block.sculk.place",
            "item.bone_meal.use",
            "mob.armor_stand.break",
            "mob.armor_stand.land",
            "mob.armor_stand.hit",
            "sniff",
            "emerge",
            "spit",
            "dig",
            "scream",
            "sound",
            "jealous",
            "idle",
            "slightly_angry",
            "note.bass"
    );

    private static final Set<Puv_Legacy_LevelSoundEvent> SPECIAL_SOUNDS = Set.of(
            // Mapped in code
            Puv_Legacy_LevelSoundEvent.RecordNull,
            Puv_Legacy_LevelSoundEvent.Note,
            // Those seem to do nothing
            Puv_Legacy_LevelSoundEvent.RecordPlaying,
            Puv_Legacy_LevelSoundEvent.ItemFizz,
            Puv_Legacy_LevelSoundEvent.MobWarningBaby,
            Puv_Legacy_LevelSoundEvent.HaggleIdle,
            Puv_Legacy_LevelSoundEvent.ImitateIllusionIllager,
            Puv_Legacy_LevelSoundEvent.Default,
            Puv_Legacy_LevelSoundEvent.SpawnBaby,
            Puv_Legacy_LevelSoundEvent.Scared,
            Puv_Legacy_LevelSoundEvent.JumpPrevent,
            Puv_Legacy_LevelSoundEvent.Bump,
            // Edu sounds
            Puv_Legacy_LevelSoundEvent.ElemConstructOpen,
            Puv_Legacy_LevelSoundEvent.IceBombHit,
            Puv_Legacy_LevelSoundEvent.BalloonPop,
            Puv_Legacy_LevelSoundEvent.LTReactionIceBomb,
            Puv_Legacy_LevelSoundEvent.LTReactionBleach,
            Puv_Legacy_LevelSoundEvent.LTReactionElephantToothpaste,
            Puv_Legacy_LevelSoundEvent.LTReactionElephantToothpaste2,
            Puv_Legacy_LevelSoundEvent.LTReactionGlowStick,
            Puv_Legacy_LevelSoundEvent.LTReactionGlowStick2,
            Puv_Legacy_LevelSoundEvent.LTReactionLuminol,
            Puv_Legacy_LevelSoundEvent.LTReactionSalt,
            Puv_Legacy_LevelSoundEvent.LTReactionFertilizer,
            Puv_Legacy_LevelSoundEvent.LTReactionFireball,
            Puv_Legacy_LevelSoundEvent.LTReactionMagnesiumSalt,
            Puv_Legacy_LevelSoundEvent.LTReactionMiscFire,
            Puv_Legacy_LevelSoundEvent.LTReactionFire,
            Puv_Legacy_LevelSoundEvent.LTReactionMiscExplosion,
            Puv_Legacy_LevelSoundEvent.LTReactionMiscMystical,
            Puv_Legacy_LevelSoundEvent.LTReactionMiscMystical2,
            Puv_Legacy_LevelSoundEvent.LTReactionProduct,
            Puv_Legacy_LevelSoundEvent.SparklerUse,
            Puv_Legacy_LevelSoundEvent.GlowStickUse,
            Puv_Legacy_LevelSoundEvent.SparklerActive
    );

    /* Last output:
        Unknown block sound: normal
        Unknown sound: jump.ladder
        Unknown sound: jump.metal
        Unknown sound: mob.irongolem.say
        Unknown sound: land.ladder
        Unknown sound: mob.attack
        Unknown sound: mob.attack
        Unknown sound: mob.piglin.attack
        Unknown sound: use.powder_snow
        Unknown sound: use.azalea
        Unknown sound: use.azalea_leaves
        Unknown sound: use.anvil
        Unknown sound: place.sculk_vein
        Unknown sound: place.froglight
        Unknown sound: use.big_dripleaf
        Unknown sound: nearby_closest.warden
        Unknown sound: mob.zombie.converted_to_drowned
        Unknown sound: nearby_close.warden
        Unknown sound: mob.blaze.ambient
        Unknown sound: nearby_closer.warden
    */

    public static void main(String[] args) throws Throwable {
        final ResourcePacksStorage resourcePacksStorage = Util.getClientResourcePacks(new File("C:\\Users\\User\\Desktop\\data"));

        final JsonObject soundList = new JsonObject();
        final Set<String> soundsWithoutCategory = new HashSet<>();
        for (Map.Entry<String, SoundDefinitions.SoundDefinition> entry : resourcePacksStorage.getSounds().soundDefinitions().entrySet()) {
            if (entry.getValue().category() == null) {
                soundsWithoutCategory.add(entry.getKey());
            } else {
                soundList.addProperty(entry.getKey(), entry.getValue().category());
            }
        }

        // Seems like mojang forgot to set the category for these sounds
        if (soundsWithoutCategory.contains("sign.ink_sac.use")) {
            soundsWithoutCategory.remove("sign.ink_sac.use");
            soundList.addProperty("sign.ink_sac.use", "block");
        }
        if (soundsWithoutCategory.contains("sign.dye.use")) {
            soundsWithoutCategory.remove("sign.dye.use");
            soundList.addProperty("sign.dye.use", "block");
        }
        // Check if there are any sounds without a category
        for (String s : soundsWithoutCategory) {
            throw new IllegalStateException("Sound without category: " + s);
        }

        final String json = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(GsonUtil.sort(soundList));
        Files.writeString(new File("sounds.json").toPath(), json);

        final Map<String, String> blockSounds = new TreeMap<>();
        for (Map.Entry<String, BlockDefinitions.BlockDefinition> blockEntry : resourcePacksStorage.getBlocks().blocks().entrySet()) {
            if (blockEntry.getValue().sound() != null && !blockEntry.getValue().sound().isEmpty()) {
                blockSounds.put(blockEntry.getKey(), blockEntry.getValue().sound());
            }
        }

        final String json3 = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(blockSounds);
        Files.writeString(new File("block_sounds.json").toPath(), json3);

        final JsonObject levelSoundMappings = new JsonObject();
        Map<String, Map<String, SoundDefinitions.ConfiguredSound>> mapping = new HashMap<>();
        for (Map.Entry<String, SoundDefinitions.EventSound> entry : resourcePacksStorage.getSounds().eventSounds().entrySet()) {
            mapping.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).put("", entry.getValue().sound());
        }

        for (Map.Entry<String, SoundDefinitions.EventSounds> entry : resourcePacksStorage.getSounds().entitySounds().entrySet()) {
            for (Map.Entry<String, SoundDefinitions.ConfiguredSound> soundEntry : entry.getValue().eventSounds().entrySet()) {
                mapping.computeIfAbsent(soundEntry.getKey(), k -> new HashMap<>()).put("entity:" + entry.getKey(), soundEntry.getValue());
            }
        }

        for (Map.Entry<String, SoundDefinitions.EventSounds> entry : resourcePacksStorage.getSounds().blockSounds().entrySet()) {
            if (!blockSounds.containsValue(entry.getKey())) {
                System.out.println("Unknown block sound: " + entry.getKey());
                continue;
            }
            for (Map.Entry<String, SoundDefinitions.ConfiguredSound> soundEntry : entry.getValue().eventSounds().entrySet()) {
                mapping.computeIfAbsent(soundEntry.getKey(), k -> new HashMap<>()).put("block:" + entry.getKey(), soundEntry.getValue());
            }
        }

        for (Map.Entry<String, Map<String, SoundDefinitions.ConfiguredSound>> entry : mapping.entrySet()) {
            Puv_Legacy_LevelSoundEvent soundEvent = string2SoundEvent(entry.getKey());
            if (soundEvent != null) {
                if (levelSoundMappings.has(soundEvent.name())) {
                    System.out.println("Duplicate sound event: " + soundEvent.name() + " (" + entry.getKey() + ")");
                    continue;
                }
                JsonObject sounds = new JsonObject();
                for (Map.Entry<String, SoundDefinitions.ConfiguredSound> soundEntry : entry.getValue().entrySet()) {
                    if (!soundList.has(soundEntry.getValue().sound())) {
                        System.out.println("Unknown sound: " + soundEntry.getValue().sound());
                        continue;
                    }
                    sounds.add(soundEntry.getKey(), soundEntry.getValue().toJson());
                }
                levelSoundMappings.add(soundEvent.name(), sounds);
            }
        }
        for (Puv_Legacy_LevelSoundEvent soundEvent : SPECIAL_SOUNDS) {
            if (levelSoundMappings.has(soundEvent.name())) {
                System.out.println("Duplicate sound event: " + soundEvent.name());
                continue;
            }
            levelSoundMappings.add(soundEvent.name(), null);
        }

        final Map<String, JsonElement> sortedJson = new TreeMap<>();
        levelSoundMappings.entrySet().forEach(entry -> sortedJson.put(entry.getKey(), entry.getValue()));
        levelSoundMappings.entrySet().clear();
        sortedJson.forEach(levelSoundMappings::add);
        final String json2 = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create().toJson(levelSoundMappings);
        Files.writeString(new File("level_sound_event_mappings.json").toPath(), json2);
    }

    private static Puv_Legacy_LevelSoundEvent string2SoundEvent(String s) {
        if (SOUND_EVENT_MAP.containsKey(s)) {
            return SOUND_EVENT_MAP.get(s);
        } else if (CANCELLED_SOUNDS.contains(s)) {
            return null;
        }
        final String original = s;

        if (s.startsWith("ambient.") && (s.endsWith(".additions") || s.endsWith(".mood") || s.endsWith(".loop"))) {
            final String[] split = s.split("\\.");
            if (split[2].equals("additions")) {
                split[2] = "addition";
            }
            s = split[0] + split[2] + split[1];
        }

        if (s.startsWith("block.")) {
            s = s.replace("block.", "");
        } else if (s.startsWith("item.")) {
            s = s.replace("item.", "");
        } else if (s.startsWith("armor.")) {
            s = s.replace("armor.", "");
        } else if (s.startsWith("mob.")) {
            s = s.replace("mob.", "");
        } else if (s.startsWith("particle.")) {
            s = s.replace("particle.", "");
        }
        if (s.endsWith(".take_result")) {
            s = s.replace(".take_result", ".use");
        } else if (s.endsWith(".hatch")) {
            s += "ed";
        }
        s = s.replace(".", "");
        s = s.replace("horn_call", "goatcall");

        for (Puv_Legacy_LevelSoundEvent soundEvent : Puv_Legacy_LevelSoundEvent.values()) {
            if (s.equalsIgnoreCase(soundEvent.name())) {
                return soundEvent;
            } else if (s.replace("_", "").equalsIgnoreCase(soundEvent.name())) {
                return soundEvent;
            } else if (("ambient" + s).equalsIgnoreCase(soundEvent.name())) {
                return soundEvent;
            }
        }

        System.err.println("Unknown sound event: " + original);
        return null;
    }

}
