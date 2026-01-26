package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
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
        return "This is the rank manage info command";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.info";
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
        if (args.length != 4 || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (!player.hasPermission(getPermission())) return;
        player.sendMessage(plugin.factionManager.playerFactionLink.get(player.getUniqueId()).getRankInfo(args[3]));
    }
}
