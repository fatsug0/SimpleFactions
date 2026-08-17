package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FactionSubInfo implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubInfo(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return """
                Displays important information about your current faction.
                Shows faction details such as members, power, claims, ranks, and other status data.
                You must be a member of a faction to use this command.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean requiresInput() {
        return false;
    }

    @Override
    public String getUsage() {
        return "/faction info";
    }

    @Override
    public String getId() {
        return "info";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Faction Info");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "────────────────────");
        for (String line : getDescription().strip().split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.isBlank()) lore.add(ChatColor.GRAY + trimmed);
        }
        lore.add(ChatColor.DARK_GRAY + "────────────────────");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        plugin.factionManager.factionFormatterService.SendFactionInfo(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), player.getUniqueId());
    }
}
