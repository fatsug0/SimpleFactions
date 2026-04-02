package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Player;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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
                This is the rank manage player add command
                You can add a player to a rank""";
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
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return;
        }

        plugin.factionManager.factionRankService.AddPlayerToRank(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), Objects.requireNonNull(Bukkit.getPlayer(args[5])), args[4]);
    }
}
