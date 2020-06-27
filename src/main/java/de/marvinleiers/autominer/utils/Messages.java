package de.marvinleiers.autominer.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Messages
{
    private static FileConfiguration config;
    private static JavaPlugin plugin;

    public static String get(String path, boolean prefix)
    {
        return prefix ? get(path) : ChatColor.translateAlternateColorCodes('&', config.getString(path));
    }

    public static String get(String path)
    {
        return ChatColor.translateAlternateColorCodes('&', path.equalsIgnoreCase("prefix") ? config.getString("prefix") : config.getString("prefix") + " " + config.getString(path));
    }

    private static void addDefaults()
    {
        config.options().copyDefaults(true);

        config.addDefault("prefix", "&b[AutoMiner]&f");
        config.addDefault("no-player", "&cThis command is for players only!");
        config.addDefault("item-name", "&b&lAuto&f&lMiner");
        config.addDefault("mining-interval-in-seconds", 10);
        config.addDefault("place-chest", "&7Please place a chest on top of the miner for it to start mining.");
        config.addDefault("blocks-per-coal", 5);

        plugin.saveConfig();
    }

    public static void setUp(JavaPlugin plugin)
    {
        Messages.plugin = plugin;

        config = plugin.getConfig();

        addDefaults();
    }
}
