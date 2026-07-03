package com.gamercorpse.easypronouns.gui;

import com.gamercorpse.easypronouns.EasyPronouns;
import com.gamercorpse.easypronouns.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Set;

public final class PronounsGui {

    private final EasyPronouns plugin;
    private final Player player;

    public PronounsGui(EasyPronouns plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        int size = plugin.getConfig().getInt("gui.size", 54);
        String title = plugin.getConfig().getString("gui.title", "&dEasy Pronouns");

        Inventory inventory = Bukkit.createInventory(new PronounsGuiHolder(), size, ColorUtil.color(title));

        if (plugin.getConfig().getBoolean("gui.filler.enabled", true)) {
            Material fillerMaterial = Material.matchMaterial(plugin.getConfig().getString("gui.filler.material", "BLACK_STAINED_GLASS_PANE"));
            if (fillerMaterial == null) fillerMaterial = Material.BLACK_STAINED_GLASS_PANE;

            for (int i = 0; i < size; i++) {
                inventory.setItem(i, GuiItemUtil.create(
                        fillerMaterial,
                        plugin.getConfig().getString("gui.filler.name", " "),
                        null
                ));
            }
        }

        ConfigurationSection section = plugin.getPronounsConfig().getConfigurationSection("pronouns");
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                String path = "pronouns." + key + ".";
                int slot = plugin.getPronounsConfig().getInt(path + "slot", -1);
                if (slot < 0 || slot >= size) continue;

                String value = plugin.getPronounsConfig().getString(path + "value", key);
                String display = plugin.getPronounsConfig().getString(path + "display", value);
                Material material = Material.matchMaterial(plugin.getPronounsConfig().getString(path + "material", "PAPER"));
                if (material == null) material = Material.PAPER;

                boolean selected = plugin.getPronounManager().hasPronoun(player.getUniqueId(), value);

                inventory.setItem(slot, GuiItemUtil.createPronounItem(plugin, material, display, value, selected));
            }
        }

        int clearSlot = plugin.getConfig().getInt("gui.clear.slot", 49);
        if (clearSlot >= 0 && clearSlot < size) {
            Material clearMaterial = Material.matchMaterial(plugin.getConfig().getString("gui.clear.material", "BARRIER"));
            if (clearMaterial == null) clearMaterial = Material.BARRIER;

            inventory.setItem(clearSlot, GuiItemUtil.create(
                    clearMaterial,
                    plugin.getConfig().getString("gui.clear.name", "&cClear Pronouns"),
                    plugin.getConfig().getStringList("gui.clear.lore")
            ));
        }

        player.openInventory(inventory);
    }
}