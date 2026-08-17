package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions.FactionSubRankManagePermissions;
import com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Player.FactionSubRankManagePlayer;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FactionSubRankManage implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManage(SimpleFactions plugin) {
        this.plugin = plugin;
    }
    @Override
    public String getName() {
        return "manage";
    }

    @Override
    public String getDescription() {
        return """
                Command group for editing a specific faction rank.
                Use it to manage players, manage permissions, or inspect one rank in detail.
                Changes affect how members in that rank interact with faction commands.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank manage <option>";
    }

    @Override
    public String getId() {
        return "rank.manage";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>() {{
           put("info", new FactionSubRankManageInfo(plugin));
           put("permissions", new FactionSubRankManagePermissions(plugin));
           put("player", new FactionSubRankManagePlayer(plugin));
        }};
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SMITHING_TABLE);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Manage Rank");
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
        sender.sendMessage(sendUsageError());
    }
}
