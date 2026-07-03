package com.gamercorpse.easypronouns;

import com.gamercorpse.easypronouns.commands.PronounsCommand;
import com.gamercorpse.easypronouns.listeners.ChatListener;
import com.gamercorpse.easypronouns.listeners.GuiListener;
import com.gamercorpse.easypronouns.listeners.PlayerListener;
import com.gamercorpse.easypronouns.storage.DatabaseManager;
import com.gamercorpse.easypronouns.storage.PronounManager;
import com.gamercorpse.easypronouns.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class EasyPronouns extends JavaPlugin {

    private static EasyPronouns instance;

    private DatabaseManager databaseManager;
    private PronounManager pronounManager;
    private EasyPronounsExpansion expansion;

    private File pronounsFile;
    private FileConfiguration pronounsConfig;

    public static EasyPronouns getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        setupPronounsFile();

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

        getLogger().info("EasyPronouns enabled using " + pronounManager.getStorageTypeName() + " storage.");
    }

    @Override
    public void onDisable() {
        if (expansion != null) {
            expansion.unregister();
        }

        if (pronounManager != null) {
            pronounManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("EasyPronouns disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        setupPronounsFile();

        if (pronounManager != null) {
            pronounManager.shutdown();
        }

        databaseManager.close();
        databaseManager.connect();
        databaseManager.createTables();

        pronounManager = new PronounManager(this, databaseManager);

        for (Player player : Bukkit.getOnlinePlayers()) {
            pronounManager.loadPlayer(player.getUniqueId()).thenRun(() -> updatePlayerTab(player));
        }
    }

    private void setupPronounsFile() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create EasyPronouns data folder.");
        }

        migrateOldPronounsStorageFile();

        pronounsFile = new File(getDataFolder(), "pronouns.yml");

        if (!pronounsFile.exists()) {
            saveResource("pronouns.yml", false);
        }

        pronounsConfig = YamlConfiguration.loadConfiguration(pronounsFile);
    }

    private void migrateOldPronounsStorageFile() {
        File oldFile = new File(getDataFolder(), "pronouns.yml");
        File newPlayerDataFile = new File(getDataFolder(), "playerdata.yml");

        if (!oldFile.exists()) {
            return;
        }

        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);

        boolean looksLikeOldPlayerStorage = oldConfig.isConfigurationSection("players")
                && !oldConfig.isConfigurationSection("pronouns");

        if (!looksLikeOldPlayerStorage) {
            return;
        }

        if (!newPlayerDataFile.exists()) {
            boolean renamed = oldFile.renameTo(newPlayerDataFile);

            if (renamed) {
                getLogger().info("Migrated old pronouns.yml player storage to playerdata.yml.");
            } else {
                getLogger().warning("Could not rename old pronouns.yml to playerdata.yml. Please move it manually.");
            }
        } else {
            File backup = new File(getDataFolder(), "pronouns-old-playerdata.yml");
            boolean renamed = oldFile.renameTo(backup);

            if (renamed) {
                getLogger().warning("Existing playerdata.yml found. Old pronouns.yml was moved to pronouns-old-playerdata.yml.");
            } else {
                getLogger().warning("Could not move old pronouns.yml. Please back it up manually before using the new pronouns.yml format.");
            }
        }
    }

    public FileConfiguration getPronounsConfig() {
        if (pronounsConfig == null) {
            setupPronounsFile();
        }

        return pronounsConfig;
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