package com.gus.simpleFactions.Commands.Faction.Sub.Home;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactionSubHome implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubHome(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getDescription() {
        return "This is the home command";
    }

    @Override
    public String getPermission() {
        return "simplefactions";
    }

    @Override
    public String getUsage() {
        return "/faction home <option>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>(){{
           put("set", new FactionSubHomeSet(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sendUsageError();
            return;
        }

        if (!player.hasPermission(getPermission())) return;
        plugin.factionManager.factionMembershipService.TeleportHome(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), player.getUniqueId());
    }
}
