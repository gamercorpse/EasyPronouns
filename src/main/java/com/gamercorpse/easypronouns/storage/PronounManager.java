package com.gamercorpse.easypronouns.storage;

import com.gamercorpse.easypronouns.EasyPronouns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class PronounManager {

    private final EasyPronouns plugin;
    private final DatabaseManager databaseManager;
    private final YamlPronounStorage yamlPronounStorage;
    private final ConcurrentHashMap<UUID, List<String>> cache = new ConcurrentHashMap<>();
    private final boolean useMysql;

    public PronounManager(EasyPronouns plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.yamlPronounStorage = new YamlPronounStorage(plugin);

        this.useMysql = databaseManager.isMysqlConfigured()
                && databaseManager.isMysqlAvailable();

        if (!useMysql) {
            yamlPronounStorage.load();
        }
    }

    public CompletableFuture<Void> loadPlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            if (useMysql) {
                loadPlayerFromMysql(uuid);
            } else {
                cache.put(uuid, yamlPronounStorage.getPronouns(uuid));
            }
        });
    }

    public CompletableFuture<Void> setPronouns(UUID uuid, List<String> pronouns) {
        List<String> safeList = sanitize(pronouns);
        cache.put(uuid, safeList);

        return CompletableFuture.runAsync(() -> {
            if (useMysql) {
                savePlayerToMysql(uuid, safeList);
            } else {
                yamlPronounStorage.setPronouns(uuid, safeList);
            }
        });
    }

    public CompletableFuture<Void> addPronoun(UUID uuid, String pronoun) {
        List<String> current = new ArrayList<>(getPronouns(uuid));

        if (!current.contains(pronoun)) {
            current.add(pronoun);
        }

        return setPronouns(uuid, current);
    }

    public CompletableFuture<Void> removePronoun(UUID uuid, String pronoun) {
        List<String> current = new ArrayList<>(getPronouns(uuid));
        current.removeIf(value -> value.equalsIgnoreCase(pronoun));
        return setPronouns(uuid, current);
    }

    public boolean hasPronoun(UUID uuid, String pronoun) {
        return getPronouns(uuid).stream().anyMatch(value -> value.equalsIgnoreCase(pronoun));
    }

    public List<String> getPronouns(UUID uuid) {
        return Collections.unmodifiableList(cache.getOrDefault(uuid, List.of()));
    }

    public String getFormattedPronouns(UUID uuid) {
        String separator = plugin.getConfig().getString("settings.separator", "&7, ");
        return String.join(separator, getPronouns(uuid));
    }

    public String getStorageTypeName() {
        return useMysql ? "MySQL" : "YAML";
    }

    public void shutdown() {
        if (!useMysql) {
            yamlPronounStorage.save();
        }
    }

    private void loadPlayerFromMysql(UUID uuid) {
        List<String> pronouns = new ArrayList<>();

        String sql = "SELECT pronouns FROM `" + databaseManager.getTable() + "` WHERE uuid = ?";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    pronouns.addAll(deserialize(result.getString("pronouns")));
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning("Could not load pronouns from MySQL for " + uuid + ": " + exception.getMessage());

            if (databaseManager.shouldFallbackToYaml()) {
                cache.put(uuid, yamlPronounStorage.getPronouns(uuid));
                return;
            }
        }

        cache.put(uuid, pronouns);
    }

    private void savePlayerToMysql(UUID uuid, List<String> pronouns) {
        String sql = "INSERT INTO `" + databaseManager.getTable() + "` (uuid, pronouns) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE pronouns = VALUES(pronouns)";

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serialize(pronouns));
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().warning("Could not save pronouns to MySQL for " + uuid + ": " + exception.getMessage());

            if (databaseManager.shouldFallbackToYaml()) {
                yamlPronounStorage.setPronouns(uuid, pronouns);
            }
        }
    }

    private List<String> sanitize(List<String> pronouns) {
        List<String> safe = new ArrayList<>();

        for (String pronoun : pronouns) {
            if (pronoun == null || pronoun.isBlank()) {
                continue;
            }

            if (!safe.contains(pronoun)) {
                safe.add(pronoun);
            }
        }

        return safe;
    }

    private String serialize(List<String> pronouns) {
        return String.join("||", pronouns);
    }

    private List<String> deserialize(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }

        List<String> values = new ArrayList<>();
        for (String value : raw.split("\\|\\|")) {
            if (!value.isBlank()) {
                values.add(value);
            }
        }

        return values;
    }
}