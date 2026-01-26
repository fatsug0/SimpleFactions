package com.gus.simpleFactions.Commands.Faction;

import com.gus.simpleFactions.Commands.Builders.CommandHandler;
import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.Commands.Faction.Sub.*;
import com.gus.simpleFactions.Commands.Faction.Sub.Home.FactionSubHome;
import com.gus.simpleFactions.Commands.Faction.Sub.Rank.FactionSubRank;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;

import java.util.*;

public class FactionCommand extends CommandHandler implements CommandInterface {

    private final SimpleFactions plugin;

    public FactionCommand(SimpleFactions plugin) {
        super(
                "faction",
                new String[]{"f", "fac"},
                "This is the faction command",
                "simplefactions.faction",
                "/faction <options>"
        );
        this.plugin = plugin;
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>(){{
            put("home", new FactionSubHome(plugin));
            put("rank", new FactionSubRank(plugin));
            put("claim", new FactionSubClaim(plugin));
            put("create", new FactionSubCreate(plugin));
            put("disband", new FactionSubDisband(plugin));
            put("help", new FactionSubHelp(plugin));
            put("info", new FactionSubInfo(plugin));
            put("invite", new FactionSubInvite(plugin));
            put("join", new FactionSubJoin(plugin));
            put("kick", new FactionSubKick(plugin));
            put("leave", new FactionSubLeave(plugin));
            put("unclaim", new FactionSubUnclaim(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Get the last argument and execute the command linked to it
        // Need to get the last argument subcommand and return them
        // Last argument -> args[args.length - 1]

        if (args.length == 0) {
            sender.sendMessage(sendUsageError());
            return;
        }

        CommandInterface command = this;

        // Check if the first argument is a valid subcommand
        for (int i = 0; i < args.length - 1; i++) {
            if (getSubCommands().containsKey(args[i])) {
                command = command.getSubCommands().get(args[i]);
            }
        }

        command.execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Need to get the last argument subcommand and return them
        // Last argument -> args[args.length - 1]

        // Need to transform the last argument (String) into a CommandInterface to get the proper subcommands
        HashMap<String, CommandInterface> currentSubCommands = getSubCommands();
        for (int i = 0; i < args.length - 1; i++) {
            if (!currentSubCommands.containsKey(args[i])) return new ArrayList<>();
            currentSubCommands = currentSubCommands.get(args[i]).getSubCommands();
        }

        return new ArrayList<>(currentSubCommands.keySet());
    }
}
