package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FactionSubPrefix implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubPrefix(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "prefix";
    }

    @Override
    public String getDescription() {
        return "This is the faction prefix command\n " +
                "First write the prefix, then colors" +
                "if there is more than one color, a gradient will be created (max 5 colors)";
    }

    @Override
    public String getPermission() {
        return "simplefactions.prefix";
    }

    @Override
    public String getUsage() {
        return "/faction prefix <prefixName> <color1> [color2] [color3] [color4] [color5]";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 7 || args.length < 3 || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (!player.hasPermission(getPermission())) return;

        FactionObject faction = plugin.factionManager.playerFactionLink.get(player.getUniqueId());
        faction.setTeamPrefix(args[1], new ArrayList<>(Arrays.asList(args).subList(2, args.length)));
    }
}
