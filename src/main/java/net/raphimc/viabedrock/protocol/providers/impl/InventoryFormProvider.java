/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.providers.impl;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.lenni0451.mcstructs_bedrock.forms.AForm;
import net.lenni0451.mcstructs_bedrock.forms.types.ActionForm;
import net.lenni0451.mcstructs_bedrock.forms.types.CustomForm;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.inventory.fake.FormContainer;
import net.raphimc.viabedrock.protocol.providers.FormProvider;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.logging.Level;

public class InventoryFormProvider extends FormProvider {

    @Override
    public void openModalForm(final UserConnection user, final int id, final AForm form) throws Exception {
        final InventoryTracker inventoryTracker = user.get(InventoryTracker.class);

        if (form instanceof ActionForm && ((ActionForm) form).getButtons().length > 26) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to open a form with more than 26 buttons");
            this.sendModalFormResponse(user, id, null);
            return;
        } else if (form instanceof CustomForm && ((CustomForm) form).getElements().length > 26) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to open a form with more than 26 buttons");
            this.sendModalFormResponse(user, id, null);
            return;
        }

        inventoryTracker.openFakeContainer(new FormContainer(user, inventoryTracker.getNextFakeWindowId(), id, form));
    }

}
