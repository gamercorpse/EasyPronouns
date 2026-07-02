package com.gamercorpse.easypronouns.commands;

import com.gamercorpse.easypronouns.EasyPronouns;
import com.gamercorpse.easypronouns.gui.PronounsGui;
import com.gamercorpse.easypronouns.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PronounsCommand implements CommandExecutor, TabCompleter {

    private final EasyPronouns plugin;

    public PronounsCommand(EasyPronouns plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("easypronouns.reload")) {
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
                return true;
            }

            plugin.reloadPlugin();
            sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.reloaded", "&aReloaded.")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color("&cOnly players can open the pronouns GUI."));
            return true;
        }

        if (!player.hasPermission("easypronouns.use")) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
            return true;
        }

        new PronounsGui(plugin, player).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabs = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("easypronouns.reload")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                tabs.add("reload");
            }
        }

        return tabs;
    }
}