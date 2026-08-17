package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage;

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

public class FactionSubRankManageInfo implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManageInfo(SimpleFactions plugin) {
        this.plugin = plugin;
    }
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return """
                Shows detailed information for one faction rank.
                Displays the rank's members and configured permissions.
                Use this before changing a rank to verify its current setup.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.info";
    }

    @Override
    public String getUsage() {
        return "/faction rank manage info <rankName>";
    }

    @Override
    public String getId() {
        return "rank.manage.info";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Managed Rank Info");
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
        if (args.length != 4 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        player.sendMessage(plugin.factionManager.factionFormatterService.getRankInfo(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), args[3]));
    }
}
