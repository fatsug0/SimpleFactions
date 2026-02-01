package com.gus.simpleFactions.Commands.Builders;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface CommandInterface {
    String getName();
    String getDescription();
    String getPermission();
    String getUsage();
    HashMap<String, CommandInterface> getSubCommands();

    void execute(CommandSender sender, String[] args);

    default String sendUsageError(){
        return ChatColor.RED + ChatColor.BOLD.toString() +  "You used the command " + ChatColor.ITALIC + getName() + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + " incorrectly!\n" + ChatColor.RESET +
                ChatColor.BOLD + " ===== \n" + ChatColor.RESET +
                getDescription() + "\n" + ChatColor.RESET +
                ChatColor.BOLD + " ===== \n" + ChatColor.RESET +
                "Permission: " + ChatColor.DARK_PURPLE + getPermission() + "\n" + ChatColor.RESET +
                ChatColor.BOLD + " ===== \n" + ChatColor.RESET +
                ChatColor.GRAY + ChatColor.ITALIC + "Usage: " + getUsage();
    }
}
