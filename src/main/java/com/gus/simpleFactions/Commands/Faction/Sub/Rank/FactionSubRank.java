package com.gus.simpleFactions.Commands.Faction.Sub.Rank;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.FactionSubRankManage;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class FactionSubRank implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRank(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "rank";
    }

    @Override
    public String getDescription() {
        return "This is the rank command";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank";
    }

    @Override
    public String getUsage() {
        return "/faction rank <options>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>() {{
            put("create", new FactionSubRankCreate(plugin));
            put("delete", new FactionSubRankDelete(plugin));
            put("info",  new FactionSubRankInfo(plugin));
            put("manage", new FactionSubRankManage(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(sendUsageError());
    }

}
