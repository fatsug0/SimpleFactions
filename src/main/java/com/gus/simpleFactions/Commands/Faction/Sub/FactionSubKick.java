package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
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
import java.util.Objects;

public class FactionSubKick implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubKick(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getDescription() {
        return """
                Removes a player from your faction.
                This command requires confirmation because it changes faction membership.
                The target player must currently belong to your faction.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.kick";
    }

    @Override
    public String getUsage() {
        return "/faction kick <playerName> confirm";
    }

    @Override
    public String getId() {
        return "kick";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.IRON_BOOTS);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Kick Player");
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
        if (args.length != 3 || !args[2].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())){
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player not found or not online!");
            return;
        }
        plugin.factionManager.factionMembershipService.KickPlayer(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()), targetPlayer.getUniqueId(), true);
    }
}
