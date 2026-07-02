package com.gamercorpse.easypronouns;

import com.gamercorpse.easypronouns.commands.PronounsCommand;
import com.gamercorpse.easypronouns.listeners.ChatListener;
import com.gamercorpse.easypronouns.listeners.GuiListener;
import com.gamercorpse.easypronouns.listeners.PlayerListener;
import com.gamercorpse.easypronouns.storage.DatabaseManager;
import com.gamercorpse.easypronouns.storage.PronounManager;
import com.gamercorpse.easypronouns.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class EasyPronouns extends JavaPlugin {

    private static EasyPronouns instance;

    private DatabaseManager databaseManager;
    private PronounManager pronounManager;
    private EasyPronounsExpansion expansion;

    public static EasyPronouns getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        databaseManager.createTables();

        pronounManager = new PronounManager(this, databaseManager);

        PronounsCommand command = new PronounsCommand(this);
        if (getCommand("pronouns") != null) {
            getCommand("pronouns").setExecutor(command);
            getCommand("pronouns").setTabCompleter(command);
        }

        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);

        expansion = new EasyPronounsExpansion(this);
        expansion.register();

        for (Player player : Bukkit.getOnlinePlayers()) {
            pronounManager.loadPlayer(player.getUniqueId()).thenRun(() -> updatePlayerTab(player));
        }

        getLogger().info("EasyPronouns enabled.");
    }

    @Override
    public void onDisable() {
        if (expansion != null) {
            expansion.unregister();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("EasyPronouns disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();

        databaseManager.close();
        databaseManager.connect();
        databaseManager.createTables();

        for (Player player : Bukkit.getOnlinePlayers()) {
            pronounManager.loadPlayer(player.getUniqueId()).thenRun(() -> updatePlayerTab(player));
        }
    }

    public void updatePlayerTab(Player player) {
        if (!getConfig().getBoolean("display.tablist.enabled", true)) {
            player.getScheduler().run(this, task -> player.playerListName(null), null);
            return;
        }

        String pronouns = pronounManager.getFormattedPronouns(player.getUniqueId());
        if (pronouns.isEmpty()) {
            player.getScheduler().run(this, task -> player.playerListName(null), null);
            return;
        }

        String format = getConfig().getString("display.tablist.format", " &8[%pronouns%]");
        String rendered = format.replace("%pronouns%", pronouns);

        player.getScheduler().run(this, task ->
                player.playerListName(ColorUtil.color(player.getName() + rendered)), null);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PronounManager getPronounManager() {
        return pronounManager;
    }
}