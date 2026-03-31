package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubRankManagePermissionsList implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePermissionsList(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return """
                This is the rank manage permissions list command
                You can list all the permissions for a rank""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.permissions.list";
    }

    @Override
    public String getUsage() {
        return "/faction rank manage permissions list <rankName>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 5 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) return;
//        player.sendMessage(plugin.factionManager.factionFormatterService.SendRankPermissionsInfo(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), args[4]));
    }
}
