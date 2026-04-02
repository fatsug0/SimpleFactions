package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubHelp implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubHelp(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return """
                This is the help command for factions
                If your are lost and doesn't know what to do, this is the command for you!""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction help";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player)){
            sender.sendMessage(sendUsageError());
            return;
        }

        plugin.factionManager.factionFormatterService.SendHelp(player.getUniqueId());
    }
}
