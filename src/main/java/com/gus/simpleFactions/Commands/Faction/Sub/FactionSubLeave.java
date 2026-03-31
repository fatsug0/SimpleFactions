package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubLeave implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubLeave(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return """
                This is the leave command
                You can leave your current faction""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction leave confirm";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2 || !args[1].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        plugin.factionManager.factionMembershipService.LeaveFaction(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), player.getUniqueId());
    }

}
