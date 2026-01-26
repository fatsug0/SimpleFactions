package com.gus.simpleFactions.Commands.Faction.Sub.Rank;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubRankDelete implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankDelete(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "this is the rank delete command";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.delete";
    }

    @Override
    public String getUsage() {
        return "/faction rank delete <rankName> confirm";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 4 && !args[3].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (!player.hasPermission(getPermission())) return;
        plugin.factionManager.playerFactionLink.get(player.getUniqueId()).DeleteFactionRank(args[2]);
    }
}
