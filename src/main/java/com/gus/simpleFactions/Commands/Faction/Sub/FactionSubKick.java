package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;

public class FactionSubKick implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubKick(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getDescription() {
        return """
                Removes a player from your faction.
                This command requires confirmation because it changes faction membership.
                The target player must currently belong to your faction.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.kick";
    }

    @Override
    public String getUsage() {
        return "/faction kick <playerName> confirm";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 3 && !args[2].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        plugin.factionManager.factionMembershipService.KickPlayer(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), Objects.requireNonNull(Bukkit.getPlayer(args[1])).getUniqueId(), true);
    }
}
