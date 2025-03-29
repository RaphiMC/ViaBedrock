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
package net.raphimc.viabedrock.api.model.container.fake;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import net.lenni0451.mcstructs_bedrock.forms.Form;
import net.lenni0451.mcstructs_bedrock.forms.elements.*;
import net.lenni0451.mcstructs_bedrock.forms.types.ActionForm;
import net.lenni0451.mcstructs_bedrock.forms.types.CustomForm;
import net.lenni0451.mcstructs_bedrock.forms.types.ModalForm;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTextUtils;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ModalFormCancelReason;
import net.raphimc.viabedrock.protocol.data.enums.java.ClickType;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.ArrayList;
import java.util.List;

public class FormContainer extends FakeContainer {

    private static final int SIZE = 27;

    private final int formId;
    private final Form form;

    private Item[] formItems;
    private int page = 0;
    private boolean sentResponse = false;

    public FormContainer(final UserConnection user, final int formId, final Form form) {
        super(user, ContainerType.CONTAINER, TextUtil.stringToTextComponent("Form: " + form.getTitle()));

        this.formId = formId;
        this.form = form;
        this.updateFormItems();
    }

    @Override
    public boolean handleClick(final int revision, short slot, final byte button, final ClickType action) {
        if (action != ClickType.PICKUP) return false;
        if (slot >= SIZE) return false;

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
                this.close();
                return true;
            }
        } else if (this.form instanceof ActionForm actionForm) {
            if (button != 0 || slot == 0) return false;

            actionForm.setClickedButton(-1);
            final FormElement[] elements = actionForm.getElements();
            slot -= 1;
            if (slot >= 0 && slot < elements.length) {
                final FormElement clickedElement = elements[slot];
                if (clickedElement instanceof ButtonFormElement) {
                    int buttonIndex = 0;
                    for (FormElement element : elements) {
                        if (element == clickedElement) {
                            break;
                        }
                        if (element instanceof ButtonFormElement) {
                            buttonIndex++;
                        }
                    }
                    actionForm.setClickedButton(buttonIndex);
                }
            }

            if (actionForm.getClickedButton() != -1) {
                this.close();
                return true;
            }
        } else if (this.form instanceof CustomForm customForm) {
            if (slot == customForm.getElements().length) {
                if (button != 0) return false;

                this.close();
                return true;
            }

            if (slot > customForm.getElements().length) return false;
            final FormElement element = customForm.getElements()[slot];

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
                this.user.get(InventoryTracker.class).openContainer(new AnvilTextInputContainer(this.user, TextUtil.stringToTextComponent("Edit text"), textField::setValue) {
                    @Override
                    public Item[] getJavaItems() {
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
        this.sendModalFormResponse(true);
    }

    @Override
    public void close() {
        this.sendModalFormResponse(false);
        super.close();
    }

    @Override
    public Item[] getJavaItems() {
        final Item[] items;
        if (this.formItems.length > SIZE) {
            items = StructuredItem.emptyArray(SIZE);
            final int begin = this.page * (SIZE - 1);
            final int end = Math.min((this.page + 1) * (SIZE - 1), this.formItems.length);
            for (int i = 0; i < end - begin; i++) {
                items[i] = this.formItems[begin + i].copy();
            }
            items[SIZE - 1] = this.createItem("minecraft:arrow", "Page navigation", "§9Left click: §6Go to next page", "§9Right click: §6Go to previous page");
        } else {
            items = new Item[this.formItems.length];
            for (int i = 0; i < this.formItems.length; i++) {
                items[i] = this.formItems[i].copy();
            }
        }
        return items;
    }

    private void updateFormItems() {
        final List<Item> items = new ArrayList<>();

        if (this.form instanceof ModalForm modalForm) {
            items.add(this.createItem("minecraft:oak_sign", "Text", modalForm.getText()));
            items.add(this.createItem("minecraft:oak_button", modalForm.getButton1()));
            items.add(this.createItem("minecraft:oak_button", modalForm.getButton2()));
        } else if (this.form instanceof ActionForm actionForm) {
            items.add(this.createItem("minecraft:oak_sign", "Text", actionForm.getText()));
            for (FormElement element : actionForm.getElements()) {
                items.add(this.createItemForFormElement(element));
            }
        } else if (this.form instanceof CustomForm customForm) {
            for (FormElement element : customForm.getElements()) {
                items.add(this.createItemForFormElement(element));
            }
            items.add(this.createItem("minecraft:oak_button", this.user.get(ResourcePacksStorage.class).getTexts().get("gui.submit")));
        } else {
            throw new IllegalArgumentException("Unhandled form type: " + this.form.getClass().getSimpleName());
        }

        this.formItems = items.toArray(new Item[0]);
    }

    private Item createItemForFormElement(final FormElement element) {
        if (element instanceof ButtonFormElement button) {
            return this.createItem("minecraft:oak_button", button.getText());
        } else if (element instanceof CheckboxFormElement checkbox) {
            final List<String> description = new ArrayList<>();
            description.add("§7Element: Checkbox");
            description.add("§9Left click: §6Toggle");
            if (checkbox.isChecked()) {
                description.add(0, "Checked: §atrue");
                return this.createItem("minecraft:lime_dye", checkbox.getText(), description.toArray(new String[0]));
            } else {
                description.add(0, "Checked: §cfalse");
                return this.createItem("minecraft:gray_dye", checkbox.getText(), description.toArray(new String[0]));
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

            return this.createItem("minecraft:bookshelf", dropdown.getText(), description.toArray(new String[0]));
        } else if (element instanceof SliderFormElement slider) {
            final List<String> description = new ArrayList<>();
            description.add("§7Current value: §a" + slider.getCurrent());
            description.add("§7Min value: §a" + slider.getMin());
            description.add("§7Max value: §a" + slider.getMax());
            description.add("§7Element: Slider");
            description.add("§9Left click: §6Increase value by " + slider.getStep());
            description.add("§9Right click: §6Decrease value by " + slider.getStep());

            return this.createItem("minecraft:repeater", slider.getText(), description.toArray(new String[0]));
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

            return this.createItem("minecraft:bookshelf", stepSlider.getText(), description.toArray(new String[0]));
        } else if (element instanceof TextFieldFormElement textField) {
            final List<String> description = new ArrayList<>();
            description.add("§7Current value: §a" + textField.getValue());
            description.add("§7Element: TextField");
            description.add("§9Left click: §6Edit text");

            return this.createItem("minecraft:name_tag", textField.getText(), description.toArray(new String[0]));
        } else if (element instanceof HeaderFormElement header) {
            return this.createItem("minecraft:oak_sign", "Header", header.getText());
        } else if (element instanceof LabelFormElement label) {
            return this.createItem("minecraft:oak_sign", "Text", label.getText());
        } else if (element instanceof DividerFormElement) {
            return this.createItem("minecraft:iron_bars", "Divider");
        } else {
            throw new IllegalArgumentException("Unhandled form element type: " + element.getClass().getSimpleName());
        }
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
        final TextComponent component = TextUtil.stringToTextComponent(text);
        if (component.getStyle().getColor() == null) {
            component.getStyle().setFormatting(TextFormatting.WHITE);
        }
        component.getStyle().setItalic(false);
        return TextUtil.textComponentToNbt(component);
    }

    private void sendModalFormResponse(final boolean userClosed) {
        if (this.sentResponse) return;
        this.sentResponse = true;

        final PacketWrapper modalFormResponse = PacketWrapper.create(ServerboundBedrockPackets.MODAL_FORM_RESPONSE, this.user);
        modalFormResponse.write(BedrockTypes.UNSIGNED_VAR_INT, this.formId); // id
        modalFormResponse.write(Types.BOOLEAN, !userClosed); // has response
        if (!userClosed) {
            modalFormResponse.write(BedrockTypes.STRING, this.form.serializeResponse() + "\n"); // response
            modalFormResponse.write(Types.BOOLEAN, false); // has cancel reason
        } else {
            modalFormResponse.write(Types.BOOLEAN, true); // has cancel reason
            modalFormResponse.write(Types.BYTE, (byte) ModalFormCancelReason.UserClosed.getValue()); // cancel reason
        }
        modalFormResponse.sendToServer(BedrockProtocol.class);
    }

}
