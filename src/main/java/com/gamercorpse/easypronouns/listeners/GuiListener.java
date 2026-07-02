package com.gamercorpse.easypronouns.listeners;

import com.gamercorpse.easypronouns.EasyPronouns;
import com.gamercorpse.easypronouns.gui.PronounKeys;
import com.gamercorpse.easypronouns.gui.PronounsGui;
import com.gamercorpse.easypronouns.gui.PronounsGuiHolder;
import com.gamercorpse.easypronouns.utils.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

public final class GuiListener implements Listener {

    private final EasyPronouns plugin;

    public GuiListener(EasyPronouns plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PronounsGuiHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        int clearSlot = plugin.getConfig().getInt("gui.clear.slot", 49);
        if (event.getRawSlot() == clearSlot) {
            plugin.getPronounManager().setPronouns(player.getUniqueId(), java.util.List.of()).thenRun(() -> {
                plugin.updatePlayerTab(player);
                player.getScheduler().run(plugin, task -> {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("gui.saved-message", "&aUpdated.")
                            .replace("%pronouns%", "")));
                    new PronounsGui(plugin, player).open();
                }, null);
            });
            return;
        }

        String value = event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                .get(PronounKeys.PRONOUN_VALUE, PersistentDataType.STRING);

        if (value == null || value.isBlank()) {
            return;
        }

        if (plugin.getPronounManager().hasPronoun(player.getUniqueId(), value)) {
            plugin.getPronounManager().removePronoun(player.getUniqueId(), value).thenRun(() -> finish(player));
            return;
        }

        int max = plugin.getConfig().getInt("settings.max-pronouns.amount", 3);
        boolean maxEnabled = plugin.getConfig().getBoolean("settings.max-pronouns.enabled", true);

        if (maxEnabled && plugin.getPronounManager().getPronouns(player.getUniqueId()).size() >= max) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("gui.max-reached-message", "&cMax reached.")
                    .replace("%max%", String.valueOf(max))));
            return;
        }

        plugin.getPronounManager().addPronoun(player.getUniqueId(), value).thenRun(() -> finish(player));
    }

    private void finish(Player player) {
        plugin.updatePlayerTab(player);

        player.getScheduler().run(plugin, task -> {
            String pronouns = plugin.getPronounManager().getFormattedPronouns(player.getUniqueId());
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("gui.saved-message", "&aUpdated.")
                    .replace("%pronouns%", pronouns)));
            new PronounsGui(plugin, player).open();
        }, null);
    }
}