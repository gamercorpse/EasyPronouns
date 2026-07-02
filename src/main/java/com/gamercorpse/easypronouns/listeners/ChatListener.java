package com.gamercorpse.easypronouns.listeners;

import com.gamercorpse.easypronouns.EasyPronouns;
import com.gamercorpse.easypronouns.utils.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {

    private final EasyPronouns plugin;

    public ChatListener(EasyPronouns plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getConfig().getBoolean("display.chat.enabled", true)) {
            return;
        }

        String pronouns = plugin.getPronounManager().getFormattedPronouns(event.getPlayer().getUniqueId());
        if (pronouns.isEmpty()) {
            return;
        }

        String format = plugin.getConfig().getString("display.chat.format", "&8[%pronouns%] ");
        Component prefix = ColorUtil.color(format.replace("%pronouns%", pronouns));

        event.renderer((source, sourceDisplayName, message, viewer) ->
                prefix.append(sourceDisplayName).append(Component.text(": ")).append(message));
    }
}