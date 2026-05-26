package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions.FactionSubRankManagePermissions;
import com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Player.FactionSubRankManagePlayer;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class FactionSubRankManage implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManage(SimpleFactions plugin) {
        this.plugin = plugin;
    }
    @Override
    public String getName() {
        return "manage";
    }

    @Override
    public String getDescription() {
        return """
                Command group for editing a specific faction rank.
                Use it to manage players, manage permissions, or inspect one rank in detail.
                Changes affect how members in that rank interact with faction commands.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank manage <option>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>() {{
           put("info", new FactionSubRankManageInfo(plugin));
           put("permissions", new FactionSubRankManagePermissions(plugin));
           put("player", new FactionSubRankManagePlayer(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(sendUsageError());
    }
}
