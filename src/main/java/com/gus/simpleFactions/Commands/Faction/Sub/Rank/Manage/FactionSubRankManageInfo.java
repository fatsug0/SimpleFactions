package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubRankManageInfo implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManageInfo(SimpleFactions plugin) {
        this.plugin = plugin;
    }
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return """
                Shows detailed information for one faction rank.
                Displays the rank's members and configured permissions.
                Use this before changing a rank to verify its current setup.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank manage info <rankName>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 4 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        player.sendMessage(plugin.factionManager.factionFormatterService.getRankInfo(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), args[3]));
    }
}
