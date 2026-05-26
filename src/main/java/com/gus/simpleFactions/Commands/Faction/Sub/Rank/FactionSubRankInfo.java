package com.gus.simpleFactions.Commands.Faction.Sub.Rank;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubRankInfo implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankInfo(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return """
                Shows an overview of your faction ranks.
                Use it to review existing ranks, assigned members, and configured permissions.
                For one specific rank, use the rank manage info command.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank info";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        player.sendMessage(plugin.factionManager.factionFormatterService.getAllRankInfo(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId())));
    }
}
