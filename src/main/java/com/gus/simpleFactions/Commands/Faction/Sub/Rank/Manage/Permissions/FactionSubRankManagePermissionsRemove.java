package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
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
        return "This is the rank manage permissions remove command";
    }

    @Override
    public String getPermission() {
        return "simpleFactions.rank.manage.permissions.remove";
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
        if (args.length < 6 || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        ArrayList<String> permissions = new ArrayList<>();
        for (int i = 5; i <= args.length; i++) {
            permissions.add(args[i]);
        }

        if (!player.hasPermission(getPermission())) return;
        plugin.factionManager.playerFactionLink.get(player.getUniqueId()).RemovePermissionRank(args[4], permissions);
    }
}
