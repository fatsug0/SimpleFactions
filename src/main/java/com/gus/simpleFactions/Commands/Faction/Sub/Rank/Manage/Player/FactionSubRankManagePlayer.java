package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Player;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class FactionSubRankManagePlayer implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePlayer(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getDescription() {
        return """
                This is the rank manage player command
                You can add, remove a or more players for a rank
                You can also list all the players for a rank""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank manage player <option>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>(){{
           put("add", new FactionSubRankManagePlayerAdd(plugin));
           put("list", new FactionSubRankManagePlayerList(plugin));
           put("remove", new FactionSubRankManagePlayerRemove(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(sendUsageError());
    }
}
