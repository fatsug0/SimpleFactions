package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Permissions;

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

public class FactionSubRankManagePermissionsAdd implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePermissionsAdd(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return """
                Adds one or more permissions to a faction rank.
                Every player currently in the rank receives the new permissions immediately.
                Use full permission nodes, such as simplefactions.claim or simplefactions.invite.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.permissions.add";
    }

    @Override
    public String getUsage() {
        return "/faction rank manage permissions add <rankName> <permission1> <permission2> ...";
    }

    @Override
    public String getId() {
        return "rank.manage.permissions.add";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Add Permission");
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
        if (args.length < 6 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        ArrayList<String> permissions = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            permissions.add(args[i]);
        }

        for (String permission : permissions) {
            plugin.factionManager.factionRankService.AddPermissionRank(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), args[4], permission);

        }
    }
}
