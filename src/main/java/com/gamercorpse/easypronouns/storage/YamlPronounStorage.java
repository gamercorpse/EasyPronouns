package com.gamercorpse.easypronouns.storage;

import com.gamercorpse.easypronouns.EasyPronouns;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class YamlPronounStorage {

    private final EasyPronouns plugin;
    private final File file;
    private FileConfiguration data;

    public YamlPronounStorage(EasyPronouns plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public synchronized void load() {
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create EasyPronouns data folder.");
            }

            if (!file.exists() && !file.createNewFile()) {
                plugin.getLogger().warning("Could not create playerdata.yml.");
            }
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not prepare playerdata.yml: " + exception.getMessage());
        }

        data = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized List<String> getPronouns(UUID uuid) {
        if (data == null) {
            load();
        }

        return new ArrayList<>(data.getStringList("players." + uuid + ".pronouns"));
    }

    public synchronized void setPronouns(UUID uuid, List<String> pronouns) {
        if (data == null) {
            load();
        }

        data.set("players." + uuid + ".pronouns", pronouns);
        data.set("players." + uuid + ".updated-at", System.currentTimeMillis());
        save();
    }

    public synchronized void save() {
        if (data == null) {
            return;
        }

        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save playerdata.yml: " + exception.getMessage());
        }
    }
}