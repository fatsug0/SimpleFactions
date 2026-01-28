package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubDisband implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubDisband(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "This is the disband faction command";
    }

    @Override
    public String getPermission() {
        return "simplefactions.disband";
    }

    @Override
    public String getUsage() {
        return "/faction disband confirm";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2 || !args[1].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (!player.hasPermission(getPermission())) return;
        plugin.factionManager.playerFactionLink.get(player.getUniqueId()).DisbandFaction(player.getUniqueId());
    }
}
