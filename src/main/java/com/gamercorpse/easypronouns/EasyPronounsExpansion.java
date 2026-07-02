package com.gamercorpse.easypronouns;

import org.bukkit.OfflinePlayer;

public final class EasyPronounsExpansion {

    private final EasyPronouns plugin;
    private boolean registered;

    public EasyPronounsExpansion(EasyPronouns plugin) {
        this.plugin = plugin;
    }

    public String getIdentifier() {
        return "easypronouns";
    }

    public String getAuthor() {
        return "Gamercorpse";
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public boolean persist() {
        return true;
    }

    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || player.getUniqueId() == null) {
            return "";
        }

        if (params.equalsIgnoreCase("pronouns")) {
            return plugin.getPronounManager().getFormattedPronouns(player.getUniqueId());
        }

        if (params.equalsIgnoreCase("pronouns_raw")) {
            return String.join(", ", plugin.getPronounManager().getPronouns(player.getUniqueId()));
        }

        return null;
    }

    public boolean register() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            registered = false;
            return false;
        }

        plugin.getLogger().warning("PlaceholderAPI was detected, but EasyPronouns was built without the PlaceholderAPI compile API.");
        plugin.getLogger().warning("Native chat and tablist pronouns still work.");
        plugin.getLogger().warning("To enable %easypronouns_pronouns%, add PlaceholderAPI as a working Maven dependency in your build environment.");

        registered = false;
        return false;
    }

    public void unregister() {
        registered = false;
    }

    public boolean isRegistered() {
        return registered;
    }
}