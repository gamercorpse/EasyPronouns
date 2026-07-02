package com.gamercorpse.easypronouns.listeners;

import com.gamercorpse.easypronouns.EasyPronouns;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    private final EasyPronouns plugin;

    public PlayerListener(EasyPronouns plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPronounManager().loadPlayer(event.getPlayer().getUniqueId()).thenRun(() ->
                plugin.updatePlayerTab(event.getPlayer()));
    }
}