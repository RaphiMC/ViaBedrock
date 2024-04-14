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
package net.raphimc.viabedrock.protocol.providers.impl;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.lenni0451.mcstructs_bedrock.forms.AForm;
import net.raphimc.viabedrock.api.model.inventory.fake.FormContainer;
import net.raphimc.viabedrock.protocol.providers.FormProvider;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

public class InventoryFormProvider extends FormProvider {

    @Override
    public void openModalForm(final UserConnection user, final int id, final AForm form) throws Exception {
        user.get(InventoryTracker.class).openFakeContainer(new FormContainer(user, id, form));
    }

}
