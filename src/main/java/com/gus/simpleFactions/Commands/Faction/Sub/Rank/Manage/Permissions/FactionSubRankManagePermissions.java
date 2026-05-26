package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class FactionSubRankManagePermissions implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePermissions(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "permissions";
    }

    @Override
    public String getDescription() {
        return """
                Command group for rank permissions.
                Use it to add, remove, or list command permissions assigned to a faction rank.
                Permission changes apply to every player currently assigned to that rank.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank manage permissions <options>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>(){{
           put("add", new FactionSubRankManagePermissionsAdd(plugin));
           put("list", new FactionSubRankManagePermissionsList(plugin));
           put("remove", new FactionSubRankManagePermissionsRemove(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(sendUsageError());
    }

}
