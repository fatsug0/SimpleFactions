package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class FactionSubRankManagePermissionsRemove implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePermissionsRemove(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return """
                Removes one or more permissions from a faction rank.
                Every player currently in the rank loses the removed permissions immediately.
                Use this to reduce access without deleting the rank itself.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.permissions.remove";
    }

    @Override
    public String getUsage() {
        return "/faction rank manage permissions remove <rankName> <permission1> <permission2> ...";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 6 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        ArrayList<String> permissions = new ArrayList<>();
        for (int i = 5; i <= args.length; i++) {
            permissions.add(args[i]);
        }

        for (String permission : permissions) {
            plugin.factionManager.factionRankService.RemovePermissionRank(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), args[4], permission);
        }
    }
}
