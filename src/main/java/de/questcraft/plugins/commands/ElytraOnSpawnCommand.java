package de.questcraft.plugins.commands;

import de.questcraft.plugins.ElytraOnSpawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ElytraOnSpawnCommand implements CommandExecutor {
    private final ElytraOnSpawn plugin;
    private final FileConfiguration config;
    private final File configFile;

    public ElytraOnSpawnCommand(ElytraOnSpawn plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You don't have the permission to do that.");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /elytraOnSpawn config <key> [value]");
            return false;
        }

        if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("conf")) {
            return !parseFirstArgument(sender, args);
        } else {
            sender.sendMessage("Invalid argument: " + args[0]);
            return false;
        }
    }

    private boolean parseFirstArgument(CommandSender sender, String[] args) {
        String firstArgument = args[1];
        switch (firstArgument.toLowerCase()) {
            case ("reset"):
                plugin.forceSaveDefaultConfig();
                plugin.restartPlugin();
                return true;

            case ("reload"):
                plugin.reloadConfig();
                plugin.restartPlugin();
                return true;

            case ("check"):
                plugin.configCheck();
                sender.sendMessage("Please check your server console");
                return true;

            default:
                if (args.length == 2) {
                    Object value = config.get(firstArgument);
                    if (value != null) {
                        sender.sendMessage("Current value of '" + firstArgument + "': " + value);
                    } else {
                        sender.sendMessage("Configuration key '" + firstArgument + "' not found.");
                    }
                } else if (args.length == 3) {
                    if (parseSecondArgument(sender, firstArgument, args[2])) return true;
                } else {
                    sender.sendMessage("Too many arguments. Usage: /elytraOnSpawn config <key> [value]");
                    return true;
                }
        }
        return false;
    }

    private boolean parseSecondArgument(CommandSender sender, String firstArgument, String secondArgument) {
        switch (firstArgument.toLowerCase()) {
            case "verbose", "boostsound", "switchgamemodecancelsound", "particle":
                boolean boolValue = Boolean.parseBoolean(secondArgument);
                config.set(firstArgument, boolValue);
                break;
            case "spawnradius", "boostsoundvolume", "boostsoundpitch",
                 "switchgamemodecancelsoundvolume", "switchgamemodecancelsoundpitch":
                int intValue = Integer.parseInt(secondArgument);
                config.set(firstArgument, intValue);
                break;
            case "flyboostmultiplier", "startsoundboost":
                float floatValue = Float.parseFloat(secondArgument);
                if (floatValue < 1) {
                    sender.sendMessage("Value must be 1 or higher.");
                    break;
                }
                config.set(firstArgument, floatValue);
                break;
            case "world":
                config.set(firstArgument, secondArgument);
            default:
                sender.sendMessage("Unknown configuration key: " + firstArgument);
                return true;
        }

        try {
            config.save(configFile);
            sender.sendMessage("Configuration updated: " + firstArgument + " = " + secondArgument);
            plugin.reloadConfig();
        } catch (IOException e) {
            sender.sendMessage("Error saving configuration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}