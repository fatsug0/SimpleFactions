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

public class FactionSubDisband implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubDisband(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return """
                Permanently deletes your faction.
                Only the faction owner can disband the faction, and the command requires confirmation.
                All members are removed, all claims are released, and the faction team is deleted.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.disband";
    }

    @Override
    public String getUsage() {
        return "/faction disband confirm";
    }

    @Override
    public String getId() {
        return "disband";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.TNT);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Disband Faction");
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
        if (args.length != 2 || !args[1].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        plugin.factionManager.factionMembershipService.DisbandFaction(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), player.getUniqueId());
    }
}
