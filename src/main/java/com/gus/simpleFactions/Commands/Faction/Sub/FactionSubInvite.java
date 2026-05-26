package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;

public class FactionSubInvite implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubInvite(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return """
                Sends a faction invitation to another player.
                The invited player must accept the invitation with the join command before becoming a member.
                Use this to grow your faction without directly forcing players into it.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.invite";
    }

    @Override
    public String getUsage() {
        return "/faction invite <playerName>";
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

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        plugin.factionManager.factionMembershipService.InvitePlayer(player.getUniqueId(), Objects.requireNonNull(Bukkit.getPlayer(args[1])).getUniqueId(), plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()));
    }
}
