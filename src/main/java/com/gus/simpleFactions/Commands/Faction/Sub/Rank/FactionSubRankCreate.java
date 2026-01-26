package com.gus.simpleFactions.Commands.Faction.Sub.Rank;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubRankCreate implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankCreate(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "This is the rank create command";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.create";
    }

    @Override
    public String getUsage() {
        return "/faction rank create <rankName>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 3 || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (!player.hasPermission(getPermission())) return;
        plugin.factionManager.playerFactionLink.get(player.getUniqueId()).CreateFactionRank(args[2]);
    }
}
