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
        return ChatColor.RED + ChatColor.BOLD.toString() +  "You used the command incorrectly!\n" +
                ChatColor.GRAY + ChatColor.ITALIC + "Usage: " + getUsage();
    }

    default List<String> sendSubCommands(HashMap<String, CommandInterface> subCommands){
        if (subCommands == null){return null;}
        List<String> subCommandNames = new ArrayList<>();
        for (CommandInterface subCommand : subCommands.values()){
            subCommandNames.add(subCommand.getName());
        }
        return subCommandNames;
    }
}
