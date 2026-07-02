package com.gamercorpse.easypronouns.gui;

import com.gamercorpse.easypronouns.EasyPronouns;
import com.gamercorpse.easypronouns.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class GuiItemUtil {

    private GuiItemUtil() {
    }

    public static ItemStack create(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(ColorUtil.color(name));

            if (lore != null && !lore.isEmpty()) {
                meta.lore(ColorUtil.colorList(lore));
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createPronounItem(EasyPronouns plugin, Material material, String name, String value, boolean selected) {
        List<String> lore = selected
                ? plugin.getConfig().getStringList("gui.selected-lore")
                : plugin.getConfig().getStringList("gui.unselected-lore");

        ItemStack item = create(material, name, lore);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(null);
            item.setItemMeta(meta);
        }

        item.editPersistentDataContainer(container ->
                container.set(PronounKeys.PRONOUN_VALUE, org.bukkit.persistence.PersistentDataType.STRING, value));

        return item;
    }
}