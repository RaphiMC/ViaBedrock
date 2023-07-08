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
package net.raphimc.viabedrock.api.model.inventory.fake;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import net.lenni0451.mcstructs.core.TextFormatting;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs_bedrock.forms.AForm;
import net.lenni0451.mcstructs_bedrock.forms.elements.*;
import net.lenni0451.mcstructs_bedrock.forms.types.ActionForm;
import net.lenni0451.mcstructs_bedrock.forms.types.CustomForm;
import net.lenni0451.mcstructs_bedrock.forms.types.ModalForm;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.MenuType;
import net.raphimc.viabedrock.protocol.data.enums.java.WindowClickActions;
import net.raphimc.viabedrock.protocol.providers.FormProvider;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;

import java.util.ArrayList;
import java.util.List;

public class FormContainer extends FakeContainer {

    private final int formId;
    private final AForm form;

    public FormContainer(UserConnection user, byte windowId, int formId, AForm form) {
        super(user, windowId, MenuType.CONTAINER, TextUtil.stringToComponent("Form: " + form.getTitle()));

        this.formId = formId;
        this.form = form;
    }

    @Override
    public boolean handleWindowClick(int revision, short slot, byte button, int action) throws Exception {
        if (action != WindowClickActions.PICKUP) return false;

        if (this.form instanceof ModalForm) {
            final ModalForm modalForm = (ModalForm) this.form;
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
        } else if (this.form instanceof ActionForm) {
            final ActionForm actionForm = (ActionForm) this.form;
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
        } else if (this.form instanceof CustomForm) {
            final CustomForm customForm = (CustomForm) this.form;
            if (slot == customForm.getElements().length) {
                if (button != 0) return false;

                Via.getManager().getProviders().get(FormProvider.class).sendModalFormResponse(this.user, this.formId, this.form);
                this.close();
                return true;
            }

            if (slot > customForm.getElements().length) return false;
            final AFormElement element = customForm.getElements()[slot];

            if (element instanceof CheckboxFormElement) {
                final CheckboxFormElement checkbox = (CheckboxFormElement) element;
                if (button != 0) return false;

                checkbox.setChecked(!checkbox.isChecked());
            } else if (element instanceof DropdownFormElement) {
                final DropdownFormElement dropdown = (DropdownFormElement) element;
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
            } else if (element instanceof SliderFormElement) {
                final SliderFormElement slider = (SliderFormElement) element;
                if (button != 0 && button != 1) return false;

                final float value = slider.getCurrent();
                final float newValue = MathUtil.clamp(value + (button == 0 ? slider.getStep() : -slider.getStep()), slider.getMin(), slider.getMax());
                slider.setCurrent(Math.round(newValue * 1000000F) / 1000000F);
            } else if (element instanceof StepSliderFormElement) {
                final StepSliderFormElement stepSlider = (StepSliderFormElement) element;
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
            } else if (element instanceof TextFieldFormElement) {
                final TextFieldFormElement textField = (TextFieldFormElement) element;
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
                });
            }
        }

        return false;
    }

    @Override
    public void onClosed() throws Exception {
        Via.getManager().getProviders().get(FormProvider.class).sendModalFormResponse(this.user, this.formId, null);
    }

    @Override
    public Item[] getJavaItems(UserConnection user) {
        final List<Item> items = new ArrayList<>();

        if (this.form instanceof ModalForm) {
            final ModalForm modalForm = (ModalForm) this.form;

            items.add(this.createItem("minecraft:oak_sign", "Text", modalForm.getText().split("\n")));
            items.add(this.createItem("minecraft:oak_button", modalForm.getButton1()));
            items.add(this.createItem("minecraft:oak_button", modalForm.getButton2()));
        } else if (this.form instanceof ActionForm) {
            final ActionForm actionForm = (ActionForm) this.form;

            items.add(this.createItem("minecraft:oak_sign", "Text", actionForm.getText().split("\n")));
            for (final ActionForm.Button button : actionForm.getButtons()) {
                items.add(this.createItem("minecraft:oak_button", button.getText()));
            }
        } else if (this.form instanceof CustomForm) {
            final CustomForm customForm = (CustomForm) this.form;

            for (AFormElement element : customForm.getElements()) {
                if (element instanceof CheckboxFormElement) {
                    final CheckboxFormElement checkbox = (CheckboxFormElement) element;
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
                } else if (element instanceof DropdownFormElement) {
                    final DropdownFormElement dropdown = (DropdownFormElement) element;
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
                } else if (element instanceof LabelFormElement) {
                    final LabelFormElement label = (LabelFormElement) element;
                    items.add(this.createItem("minecraft:oak_sign", "Text", label.getText()));
                } else if (element instanceof SliderFormElement) {
                    final SliderFormElement slider = (SliderFormElement) element;
                    final List<String> description = new ArrayList<>();
                    description.add("§7Current value: §a" + slider.getCurrent());
                    description.add("§7Min value: §a" + slider.getMin());
                    description.add("§7Max value: §a" + slider.getMax());
                    description.add("§7Element: Slider");
                    description.add("§9Left click: §6Increase value by " + slider.getStep());
                    description.add("§9Right click: §6Decrease value by " + slider.getStep());

                    items.add(this.createItem("minecraft:repeater", slider.getText(), description.toArray(new String[0])));
                } else if (element instanceof StepSliderFormElement) {
                    final StepSliderFormElement stepSlider = (StepSliderFormElement) element;
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
                } else if (element instanceof TextFieldFormElement) {
                    final TextFieldFormElement textField = (TextFieldFormElement) element;
                    final List<String> description = new ArrayList<>();
                    description.add("§7Current value: §a" + textField.getValue());
                    description.add("§7Element: TextField");
                    description.add("§9Left click: §6Edit text");

                    items.add(this.createItem("minecraft:name_tag", textField.getText(), description.toArray(new String[0])));
                } else {
                    throw new IllegalArgumentException("Unknown form element type: " + element.getClass().getSimpleName());
                }
            }

            items.add(this.createItem("minecraft:oak_button", "Submit"));
        } else {
            throw new IllegalArgumentException("Unknown form type: " + this.form.getClass().getSimpleName());
        }

        return items.toArray(new Item[0]);
    }

    private Item createItem(final String identifier, final String name, final String... description) {
        final int id = BedrockProtocol.MAPPINGS.getJavaItems().getOrDefault(identifier, -1);
        if (id == -1) {
            throw new IllegalStateException("Unable to find item with identifier: " + identifier);
        }

        final CompoundTag tag = new CompoundTag();
        final CompoundTag displayTag = new CompoundTag();
        displayTag.put("Name", new StringTag(this.stringToJson(name)));
        if (description.length > 0) {
            final ListTag loreTags = new ListTag(StringTag.class);
            for (final String line : description) {
                loreTags.add(new StringTag(this.stringToJson(line)));
            }
            displayTag.put("Lore", loreTags);
        }
        tag.put("display", displayTag);

        return new DataItem(id, (byte) 1, (short) 0, tag);
    }

    private String stringToJson(final String text) {
        final ATextComponent component = TextUtil.stringToComponent(text);
        if (component.getStyle().getColor() == null) {
            component.getStyle().setFormatting(TextFormatting.WHITE);
        }
        component.getStyle().setItalic(false);
        return TextUtil.componentToJson(component);
    }

}
