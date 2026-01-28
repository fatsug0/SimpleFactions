package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;

public class FactionSubInvite implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubInvite(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "This is the invite command";
    }

    @Override
    public String getPermission() {
        return "simpleFactions.invite";
    }

    @Override
    public String getUsage() {
        return "/faction invite <playerName>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2 || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (!player.hasPermission(getPermission())) return;
        plugin.factionManager.InvitePlayer(Objects.requireNonNull(Bukkit.getPlayer(args[1])).getUniqueId(), plugin.factionManager.playerFactionLink.get(player.getUniqueId()));
    }
}
