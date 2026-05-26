package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Player;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;

public class FactionSubRankManagePlayerAdd implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePlayerAdd(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return """
                Adds a faction member to a rank.
                The player receives that rank's permissions immediately when online.
                The target player must belong to your faction.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.player.add";
    }

    @Override
    public String getUsage() {
        return "/faction rank manage player add <rankName> <playerName>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 6 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId()))   {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[5]);

        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "That player could not be found!");
            return;
        }

        plugin.factionManager.factionRankService.AddPlayerToRank(
                plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()),
                targetPlayer.getUniqueId(),
                args[4]
        );
    }
}
