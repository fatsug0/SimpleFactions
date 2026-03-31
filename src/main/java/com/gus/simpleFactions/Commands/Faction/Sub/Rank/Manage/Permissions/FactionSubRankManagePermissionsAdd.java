package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class FactionSubRankManagePermissionsAdd implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePermissionsAdd(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return """
                This is the rank manage permissions add command
                You can add a or more permissions for a rank
                The added permissions will be added to all players in the faction from that rank""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.permissions.add";
    }

    @Override
    public String getUsage() {
        return "/faction rank manage permissions add <rankName> <permission1> <permission2> ...";
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

        ArrayList<String> permissions = new ArrayList<>();
        for (int i = 5; i <= args.length; i++) {
            permissions.add(args[i]);
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) return;
        for (String permission : permissions) {
            plugin.factionManager.factionRankService.AddPermissionRank(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), args[4], permission);

        }
    }
}
