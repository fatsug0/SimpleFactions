package com.gus.simpleFactions.Commands.Faction.Sub.Rank.Manage.Player;

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

public class FactionSubRankManagePlayer implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRankManagePlayer(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getDescription() {
        return """
                Command group for rank membership.
                Use it to add players to a rank, remove players from a rank, or list rank members.
                A player's rank controls which faction permissions they receive.""";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/faction rank manage player <option>";
    }

    @Override
    public String getId() {
        return "rank.manage.player";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>(){{
           put("add", new FactionSubRankManagePlayerAdd(plugin));
           put("list", new FactionSubRankManagePlayerList(plugin));
           put("remove", new FactionSubRankManagePlayerRemove(plugin));
        }};
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Rank Players");
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
