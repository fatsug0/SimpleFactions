package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
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

public class FactionSubRankManagePermissions implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePermissions(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "permissions";
    }

    @Override
    public String getDescription() {
        return """
                Command group for rank permissions.
                Use it to add, remove, or list command permissions assigned to a faction rank.
                Permission changes apply to every player currently assigned to that rank.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank manage permissions <options>";
    }

    @Override
    public String getId() {
        return "rank.manage.permissions";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>(){{
           put("add", new FactionSubRankManagePermissionsAdd(plugin));
           put("list", new FactionSubRankManagePermissionsList(plugin));
           put("remove", new FactionSubRankManagePermissionsRemove(plugin));
        }};
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Rank Permissions");
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
