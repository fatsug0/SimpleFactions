package com.gus.simpleFactions.Commands.Faction.Sub.Rank;

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

public class FactionSubRankDelete implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankDelete(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return """
                Deletes a rank from your faction.
                This command requires confirmation because members assigned to that rank will lose it.
                Use it only after moving affected players to another rank when needed.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.delete";
    }

    @Override
    public String getUsage() {
        return "/faction rank delete <rankName> confirm";
    }

    @Override
    public String getId() {
        return "rank.delete";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Delete Rank");
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
        if (args.length != 4 || !args[3].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        plugin.factionManager.factionRankService.DeleteFactionRank(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), args[2], player);
    }
}
