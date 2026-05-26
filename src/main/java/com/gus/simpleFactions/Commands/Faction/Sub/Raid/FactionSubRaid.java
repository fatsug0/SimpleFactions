package com.gus.simpleFactions.Commands.Faction.Sub.Raid;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubRaid implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRaid(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "raid";
    }

    @Override
    public String getDescription() {
        return """
                Starts or schedules a raid against another faction's weak claim.
                Provide the target faction, the raid date, confirmation, and the selected chunk.
                Dates use DD-MM-YYYY:HHMM in UTC.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.raid";
    }

    @Override
    public String getUsage() {
        return "/faction raid <factionName> <raidDate> <confirm>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>() {{
            put("select", new FactionSubRaidSelect(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 4 || !args[args.length - 1].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        FactionObject defendingFaction = null;
        for (FactionObject faction : plugin.factionManager.factionMembershipService.getExistingFactions()) {
            if (faction.getFactionName().equals(args[1])) {
                defendingFaction = faction;
                break;
            }
        }

        if (defendingFaction == null) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The defending faction you tried to attack doesn't exists!");
            return;
        }

        plugin.raidManager.SendRaidDeclaration(
                player,
                plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()),
                defendingFaction, args[2]);

    }
}
