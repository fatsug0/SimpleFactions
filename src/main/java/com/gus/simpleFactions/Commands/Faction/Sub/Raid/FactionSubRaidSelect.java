package com.gus.simpleFactions.Commands.Faction.Sub.Raid;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubRaidSelect implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRaidSelect(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "select";
    }

    @Override
    public String getDescription() {
        return """
                This is the raid select command
                It is used to select the chunks to raid
                """;
    }

    @Override
    public String getPermission() {
        return "simplefactions.raid.select";
    }

    @Override
    public String getUsage() {
        return "/faction raid select here";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 3 || !args[args.length - 1].equalsIgnoreCase("here") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return;
        }

        // Check if it's not his own faction
        if (plugin.factionManager.factionLandService.getLinkedChunks().get(player.getLocation().getChunk()).equals(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()))) {
            player.sendMessage(ChatColor.RED + "You cannot raid your own faction!");
            return;
        }

        // Check if it's a weak chunk
        if (!plugin.factionManager.factionLandService.getLinkedChunks().get(player.getLocation().getChunk()).getWeakClaimedChunks().contains(player.getLocation().getChunk())) {
           player.sendMessage(ChatColor.RED + "You cannot raid a hard chunk!");
           return;
        }

        plugin.raidManager.addCurrentFactionSelection(
                plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()),
                plugin.factionManager.factionLandService.getLinkedChunks().get(player.getLocation().getChunk()),
                player.getLocation().getChunk(),
                player);
    }
}
