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

public class FactionSubHelp implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubHelp(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return """
                Shows faction help and available commands.
                Use this when you need a quick overview of what SimpleFactions can do.
                For detailed syntax, run a command incorrectly or check its usage line.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean requiresFaction() {
        return false;
    }

    @Override
    public boolean requiresInput() {
        return false;
    }

    @Override
    public String getUsage() {
        return "/faction help";
    }

    @Override
    public String getId() {
        return "help";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Help");
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
        if (args.length != 1 || !(sender instanceof Player player)){
            sender.sendMessage(sendUsageError());
            return;
        }

        plugin.factionManager.factionFormatterService.SendHelp(player.getUniqueId());
    }
}
