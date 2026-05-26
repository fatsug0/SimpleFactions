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
                Command group for rank membership.
                Use it to add players to a rank, remove players from a rank, or list rank members.
                A player's rank controls which faction permissions they receive.""";
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
