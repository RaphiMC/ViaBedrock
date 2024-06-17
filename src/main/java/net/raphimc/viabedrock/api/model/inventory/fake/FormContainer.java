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
package net.raphimc.viabedrock.api.model.inventory.fake;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs_bedrock.forms.AForm;
import net.lenni0451.mcstructs_bedrock.forms.elements.*;
import net.lenni0451.mcstructs_bedrock.forms.types.ActionForm;
import net.lenni0451.mcstructs_bedrock.forms.types.CustomForm;
import net.lenni0451.mcstructs_bedrock.forms.types.ModalForm;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTextUtils;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.data.enums.java.ClickType;
import net.raphimc.viabedrock.protocol.provider.FormProvider;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.ArrayList;
import java.util.List;

public class FormContainer extends FakeContainer {

    private static final int SIZE = 27;

    private final int formId;
    private final AForm form;

    private Item[] formItems;
    private int page = 0;

    public FormContainer(UserConnection user, int formId, AForm form) {
        super(user, MenuType.CONTAINER, TextUtil.stringToComponent("Form: " + form.getTitle()));

        this.formId = formId;
        this.form = form;
        this.updateFormItems();
    }

    @Override
    public boolean handleWindowClick(int revision, short slot, byte button, ClickType action) {
        if (action != ClickType.PICKUP) return false;

        if (this.formItems.length > SIZE && slot == SIZE - 1) {
            final int pages = MathUtil.ceil(this.formItems.length / (SIZE - 1F));
            if (button == 0) {
                this.page++;
            } else if (button == 1) {
                this.page--;
            }
            this.page = MathUtil.clamp(this.page, 0, pages - 1);

            return false;
        }
        slot += (short) (this.page * (SIZE - 1));

        if (this.form instanceof ModalForm modalForm) {
            if (button != 0) return false;

            modalForm.setClickedButton(-1);
            if (slot == 1) {
                modalForm.setClickedButton(0);
            } else if (slot == 2) {
                modalForm.setClickedButton(1);
            }

            if (modalForm.getClickedButton() != -1) {
                Via.getManager().getProviders().get(FormProvider.class).sendModalFormResponse(this.user, this.formId, this.form);
                this.close();
                return true;
            }
        } else if (this.form instanceof ActionForm actionForm) {
            if (button != 0) return false;

            actionForm.setClickedButton(-1);
            if (slot > 0 && slot <= actionForm.getButtons().length) {
                actionForm.setClickedButton(slot - 1);
            }

            if (actionForm.getClickedButton() != -1) {
                Via.getManager().getProviders().get(FormProvider.class).sendModalFormResponse(this.user, this.formId, this.form);
                this.close();
                return true;
            }
        } else if (this.form instanceof CustomForm customForm) {
            if (slot == customForm.getElements().length) {
                if (button != 0) return false;

                Via.getManager().getProviders().get(FormProvider.class).sendModalFormResponse(this.user, this.formId, this.form);
                this.close();
                return true;
            }

            if (slot > customForm.getElements().length) return false;
            final AFormElement element = customForm.getElements()[slot];

            if (element instanceof CheckboxFormElement checkbox) {
                if (button != 0) return false;

                checkbox.setChecked(!checkbox.isChecked());
            } else if (element instanceof DropdownFormElement dropdown) {
                if (button != 0 && button != 1) return false;

                final int selected = MathUtil.clamp(dropdown.getSelected(), -1, dropdown.getOptions().length);
                final int newSelected = selected + (button == 0 ? 1 : -1);
                if (newSelected >= dropdown.getOptions().length || selected == -1) {
                    dropdown.setSelected(0);
                } else if (newSelected < 0 || selected == dropdown.getOptions().length) {
                    dropdown.setSelected(dropdown.getOptions().length - 1);
                } else {
                    dropdown.setSelected(newSelected);
                }
            } else if (element instanceof SliderFormElement slider) {
                if (button != 0 && button != 1) return false;

                final float value = slider.getCurrent();
                final float newValue = MathUtil.clamp(value + (button == 0 ? slider.getStep() : -slider.getStep()), slider.getMin(), slider.getMax());
                slider.setCurrent(Math.round(newValue * 1000000F) / 1000000F);
            } else if (element instanceof StepSliderFormElement stepSlider) {
                if (button != 0 && button != 1) return false;

                final int selected = MathUtil.clamp(stepSlider.getSelected(), -1, stepSlider.getSteps().length);
                final int newSelected = selected + (button == 0 ? 1 : -1);
                if (newSelected >= stepSlider.getSteps().length || selected == -1) {
                    stepSlider.setSelected(0);
                } else if (newSelected < 0 || selected == stepSlider.getSteps().length) {
                    stepSlider.setSelected(stepSlider.getSteps().length - 1);
                } else {
                    stepSlider.setSelected(newSelected);
                }
            } else if (element instanceof TextFieldFormElement textField) {
                this.user.get(InventoryTracker.class).openFakeContainer(new AnvilTextInputContainer(this.user, this, TextUtil.stringToComponent("Edit text"), textField::setValue) {
                    @Override
                    public Item[] getJavaItems(UserConnection user) {
                        final List<Item> items = new ArrayList<>();
                        final List<String> description = new ArrayList<>();
                        description.add("§7Description: " + textField.getText());
                        description.add("§7Element: TextField");
                        description.add("§9Close GUI to save");
                        items.add(FormContainer.this.createItem("minecraft:paper", textField.getValue(), description.toArray(new String[0])));
                        return items.toArray(new Item[0]);
                    }

                    @Override
                    public void onClosed() {
                        FormContainer.this.updateFormItems();
                        super.onClosed();
                    }
                });
            }
            this.updateFormItems();
        }

        return false;
    }

    @Override
    public void onClosed() {
        Via.getManager().getProviders().get(FormProvider.class).sendModalFormResponse(this.user, this.formId, null);
    }

    @Override
    public Item[] getJavaItems(UserConnection user) {
        if (this.formItems.length > SIZE) {
            final Item[] items = new Item[SIZE];
            final int begin = this.page * (SIZE - 1);
            final int end = Math.min((this.page + 1) * (SIZE - 1), this.formItems.length);
            System.arraycopy(this.formItems, begin, items, 0, end - begin);
            items[SIZE - 1] = this.createItem("minecraft:arrow", "Page navigation", "§9Left click: §6Go to next page", "§9Right click: §6Go to previous page");
            return items;
        } else {
            return this.formItems;
        }
    }

    private void updateFormItems() {
        final List<Item> items = new ArrayList<>();

        if (this.form instanceof ModalForm modalForm) {

            items.add(this.createItem("minecraft:oak_sign", "Text", modalForm.getText()));
            items.add(this.createItem("minecraft:oak_button", modalForm.getButton1()));
            items.add(this.createItem("minecraft:oak_button", modalForm.getButton2()));
        } else if (this.form instanceof ActionForm actionForm) {

            items.add(this.createItem("minecraft:oak_sign", "Text", actionForm.getText()));
            for (final ActionForm.Button button : actionForm.getButtons()) {
                items.add(this.createItem("minecraft:oak_button", button.getText()));
            }
        } else if (this.form instanceof CustomForm customForm) {

            for (AFormElement element : customForm.getElements()) {
                if (element instanceof CheckboxFormElement checkbox) {
                    final List<String> description = new ArrayList<>();
                    description.add("§7Element: Checkbox");
                    description.add("§9Left click: §6Toggle");
                    if (checkbox.isChecked()) {
                        description.add(0, "Checked: §atrue");
                        items.add(this.createItem("minecraft:lime_dye", checkbox.getText(), description.toArray(new String[0])));
                    } else {
                        description.add(0, "Checked: §cfalse");
                        items.add(this.createItem("minecraft:gray_dye", checkbox.getText(), description.toArray(new String[0])));
                    }
                } else if (element instanceof DropdownFormElement dropdown) {
                    final List<String> description = new ArrayList<>();
                    description.add("§7Options:");
                    for (int i = 0; i < dropdown.getOptions().length; i++) {
                        final String option = dropdown.getOptions()[i];
                        if (dropdown.getSelected() == i) {
                            description.add("§a§l" + option);
                        } else {
                            description.add("§c" + option);
                        }
                    }
                    description.add("§7Element: Dropdown");
                    description.add("§9Left click: §6Go to next option");
                    description.add("§9Right click: §6Go to previous option");

                    items.add(this.createItem("minecraft:bookshelf", dropdown.getText(), description.toArray(new String[0])));
                } else if (element instanceof LabelFormElement label) {
                    items.add(this.createItem("minecraft:oak_sign", "Text", label.getText()));
                } else if (element instanceof SliderFormElement slider) {
                    final List<String> description = new ArrayList<>();
                    description.add("§7Current value: §a" + slider.getCurrent());
                    description.add("§7Min value: §a" + slider.getMin());
                    description.add("§7Max value: §a" + slider.getMax());
                    description.add("§7Element: Slider");
                    description.add("§9Left click: §6Increase value by " + slider.getStep());
                    description.add("§9Right click: §6Decrease value by " + slider.getStep());

                    items.add(this.createItem("minecraft:repeater", slider.getText(), description.toArray(new String[0])));
                } else if (element instanceof StepSliderFormElement stepSlider) {
                    final List<String> description = new ArrayList<>();
                    description.add("§7Options:");
                    for (int i = 0; i < stepSlider.getSteps().length; i++) {
                        final String option = stepSlider.getSteps()[i];
                        if (stepSlider.getSelected() == i) {
                            description.add("§a§l" + option);
                        } else {
                            description.add("§c" + option);
                        }
                    }
                    description.add("§7Element: StepSlider");
                    description.add("§9Left click: §6Go to next option");
                    description.add("§9Right click: §6Go to previous option");

                    items.add(this.createItem("minecraft:bookshelf", stepSlider.getText(), description.toArray(new String[0])));
                } else if (element instanceof TextFieldFormElement textField) {
                    final List<String> description = new ArrayList<>();
                    description.add("§7Current value: §a" + textField.getValue());
                    description.add("§7Element: TextField");
                    description.add("§9Left click: §6Edit text");

                    items.add(this.createItem("minecraft:name_tag", textField.getText(), description.toArray(new String[0])));
                } else {
                    throw new IllegalArgumentException("Unknown form element type: " + element.getClass().getSimpleName());
                }
            }

            items.add(this.createItem("minecraft:oak_button", this.user.get(ResourcePacksStorage.class).getTranslations().get("gui.submit")));
        } else {
            throw new IllegalArgumentException("Unknown form type: " + this.form.getClass().getSimpleName());
        }

        this.formItems = items.toArray(new Item[0]);
    }

    private Item createItem(final String identifier, final String name, final String... description) {
        final int id = BedrockProtocol.MAPPINGS.getJavaItems().getOrDefault(identifier, -1);
        if (id == -1) {
            throw new IllegalStateException("Unable to find item with identifier: " + identifier);
        }

        final StructuredDataContainer data = ProtocolConstants.createStructuredDataContainer();
        data.set(StructuredDataKey.ITEM_NAME, this.stringToNbt(name.replace("\n", " | ")));
        if (description.length > 0) {
            final List<Tag> loreTags = new ArrayList<>();
            for (String desc : description) {
                for (final String line : BedrockTextUtils.split(desc, "\n")) {
                    loreTags.add(this.stringToNbt(line));
                }
            }
            data.set(StructuredDataKey.LORE, loreTags.toArray(new Tag[0]));
        }

        return new StructuredItem(id, 1, data);
    }

    private Tag stringToNbt(final String text) {
        final ATextComponent component = TextUtil.stringToComponent(text);
        if (component.getStyle().getColor() == null) {
            component.getStyle().setFormatting(TextFormatting.WHITE);
        }
        component.getStyle().setItalic(false);
        return TextUtil.componentToNbt(component);
    }

}
