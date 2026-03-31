package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class FactionSubStorage implements CommandInterface {

    private SimpleFactions plugin;
    public FactionSubStorage(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "storage";
    }

    @Override
    public String getDescription() {
        return """
                This is the faction storage,
                every faction has the equivalent of 2 double chest of storage accessible via this command
                Every member of every rank has access to this storage.
                """;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction storage";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) return;

        player.openInventory(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()).getFactionInv().getInventory());
    }
}
