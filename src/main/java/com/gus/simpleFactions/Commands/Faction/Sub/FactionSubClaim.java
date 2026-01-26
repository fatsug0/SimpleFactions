package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubClaim implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubClaim(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public String getDescription() {
        return "This is the claim land command";
    }

    @Override
    public String getPermission() {
        return "simplefactions.claim";
    }

    @Override
    public String getUsage() {
        return "/faction claim";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (!player.hasPermission(getPermission())) return;
        plugin.factionManager.playerFactionLink.get(player.getUniqueId()).ClaimLand(player.getUniqueId());
    }
}
