package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.Json.FactionObjectWrapper;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubSave implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubSave(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getDescription() {
        return """
                This is the save command
                This is a helper command to save the faction data to the database
                """;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction save";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        plugin.jsonHandler.WriteJson("faction-dara", player.getName(), new FactionObjectWrapper(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId())));
    }
}
