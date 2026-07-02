package com.gamercorpse.easypronouns.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class PronounsGuiHolder implements InventoryHolder {

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("PronounsGuiHolder does not store an inventory instance.");
    }
}