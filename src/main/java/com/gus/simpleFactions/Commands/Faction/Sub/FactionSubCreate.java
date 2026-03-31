package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;

public class FactionSubCreate implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubCreate(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return """
                This is the create faction command
                You can create a faction for yourself
                You will automatically be the leader of the faction with full power
                You can now claim land, invite other players and more!""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction create <factionName>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        System.out.println(Arrays.toString(args));
        if (args.length != 2 || !(sender instanceof Player player)){
            sender.sendMessage(sendUsageError());
            return;
        }

        plugin.factionManager.CreateFaction(player.getUniqueId(), args[1]);
    }
}
