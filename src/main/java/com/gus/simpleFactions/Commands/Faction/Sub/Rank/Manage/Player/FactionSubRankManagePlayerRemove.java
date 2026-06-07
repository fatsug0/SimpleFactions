package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Player;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FactionSubRankManagePlayerRemove implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePlayerRemove(SimpleFactions plugin) {
        this.plugin = plugin;
    }
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return """
                Removes a faction member from a rank.
                This command requires confirmation because it can remove active permissions.
                The target player remains in the faction unless removed with the kick command.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.rank.manage.player.remove";
    }

    @Override
    public String getUsage() {
        return "/faction rank manage player remove <rankName> <playerName> confirm";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Remove Rank Player");
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
        if (args.length != 7 && !args[6].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        plugin.factionManager.factionRankService.RemovePlayerFromRank(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), Objects.requireNonNull(Bukkit.getPlayer(args[4])), args[1]);
    }
}
