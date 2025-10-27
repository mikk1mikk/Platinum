package com.example.meteoraddon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;

/**
 * ArmorAlert - Meteor Client module
 * Notifies in chat when any armor piece's remaining durability drops below the threshold.
 */
public class ArmorAlert extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("threshold")
        .description("Alert when an armor piece's remaining durability (%) is below this value.")
        .defaultValue(20.0)
        .min(1.0)
        .max(100.0)
        .slider()   // optional in UI
        .build()
    );

    // Keep track of which armor pieces are currently "alerted" so we don't spam
    private boolean[] alerted = new boolean[4]; // 0 = boots, 1 = leggings, 2 = chest, 3 = helmet

    public ArmorAlert() {
        super(Category.Misc, "armor-alert", "Notifies in chat when your armor falls below a percent.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // EquipmentSlot ordering for armor: FEET, LEGS, CHEST, HEAD
        EquipmentSlot[] slots = { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };
        String[] slotNames = { "Boots", "Leggings", "Chestplate", "Helmet" };

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = mc.player.getEquippedStack(slots[i]);

            // If nothing equipped, reset alerted flag for that slot and continue
            if (stack == null || stack.isEmpty()) {
                alerted[i] = false;
                continue;
            }

            // Only check armor items (safety)
            if (!isArmorPiece(stack)) {
                alerted[i] = false;
                continue;
            }

            int max = stack.getMaxDamage();
            int damage = stack.getDamage(); // damage taken
            int remaining = max - damage;
            double percent = (double) remaining * 100.0 / (double) max;

            double thr = threshold.get();

            // If it just dropped below threshold and we haven't alerted for this slot yet -> notify
            if (percent <= thr && !alerted[i]) {
                // info(...) is a helper in Module that prints a client-side message in chat
                info(slotNames[i] + " durability low: " + String.format("%.0f", percent) + "%");
                alerted[i] = true;
            }

            // If durability recovered above threshold, reset alerted so we can alert again later
            if (percent > thr && alerted[i]) {
                alerted[i] = false;
            }
        }
    }

    /** Basic check whether the itemstack is an armor item (helmet/chest/legs/boots) */
    private boolean isArmorPiece(ItemStack stack) {
        // Quick heuristic: check known armor items. You can expand this list if needed.
        return stack.getItem() == Items.DIAMOND_HELMET
            || stack.getItem() == Items.DIAMOND_CHESTPLATE
            || stack.getItem() == Items.DIAMOND_LEGGINGS
            || stack.getItem() == Items.DIAMOND_BOOTS
            || stack.getItem() == Items.NETHERITE_HELMET
            || stack.getItem() == Items.NETHERITE_CHESTPLATE
            || stack.getItem() == Items.NETHERITE_LEGGINGS
            || stack.getItem() == Items.NETHERITE_BOOTS
            || stack.getItem() == Items.IRON_HELMET
            || stack.getItem() == Items.IRON_CHESTPLATE
            || stack.getItem() == Items.IRON_LEGGINGS
            || stack.getItem() == Items.IRON_BOOTS
            || stack.getItem() == Items.GOLDEN_HELMET
            || stack.getItem() == Items.GOLDEN_CHESTPLATE
            || stack.getItem() == Items.GOLDEN_LEGGINGS
            || stack.getItem() == Items.GOLDEN_BOOTS
            || stack.getItem() == Items.CHAINMAIL_HELMET
            || stack.getItem() == Items.CHAINMAIL_CHESTPLATE
            || stack.getItem() == Items.CHAINMAIL_LEGGINGS
            || stack.getItem() == Items.CHAINMAIL_BOOTS
            || stack.getItem() == Items.LEATHER_HELMET
            || stack.getItem() == Items.LEATHER_CHESTPLATE
            || stack.getItem() == Items.LEATHER_LEGGINGS
            || stack.getItem() == Items.LEATHER_BOOTS;
    }
}