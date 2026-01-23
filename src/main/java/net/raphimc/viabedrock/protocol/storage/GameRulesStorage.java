/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.State;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.java.EntityEvent;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEventType;
import net.raphimc.viabedrock.protocol.model.GameRule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class GameRulesStorage extends StoredObject {

    // Maybe important: recipesUnlock, showRecipeMessages, showBorderEffect

    private final Map<String, Object> gameRules = new HashMap<>(BedrockProtocol.MAPPINGS.getBedrockGameRules());

    public GameRulesStorage(final UserConnection user, final GameRule[] gameRules) {
        super(user);

        this.updateGameRules(gameRules);
    }

    public void updateGameRules(final GameRule[] gameRules) {
        final State state = this.user().getProtocolInfo().getServerState();
        for (GameRule gameRule : gameRules) {
            if (gameRule.value() != null) {
                final Object previousValue = this.gameRules.put(gameRule.name().toLowerCase(Locale.ROOT), gameRule.value());
                if (previousValue == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unknown game rule: " + gameRule.name());
                    continue;
                }

                if (previousValue != gameRule.value() && state == State.PLAY) {
                    if (gameRule.name().equalsIgnoreCase("showCoordinates") && ViaBedrock.getConfig().shouldTranslateShowCoordinatesGameRule()) {
                        final ClientPlayerEntity clientPlayer = this.user().get(EntityTracker.class).getClientPlayer();
                        PacketFactory.sendJavaEntityEvent(this.user(), clientPlayer, (boolean) gameRule.value() ? EntityEvent.FULL_DEBUG_INFO : EntityEvent.REDUCED_DEBUG_INFO);
                    } else if (gameRule.name().equalsIgnoreCase("doImmediateRespawn")) {
                        PacketFactory.sendJavaGameEvent(this.user(), GameEventType.IMMEDIATE_RESPAWN, (boolean) gameRule.value() ? 1F : 0F);
                    } else if (gameRule.name().equalsIgnoreCase("doLimitedCrafting")) {
                        PacketFactory.sendJavaGameEvent(this.user(), GameEventType.LIMITED_CRAFTING, (boolean) gameRule.value() ? 1F : 0F);
                    }
                }
            }
        }
    }

    public <T> T getGameRule(final String name) {
        return (T) this.gameRules.get(name.toLowerCase(Locale.ROOT));
    }

}
