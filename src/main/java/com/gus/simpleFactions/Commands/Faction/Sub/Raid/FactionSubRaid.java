package com.gus.simpleFactions.Commands.Faction.Sub.Raid;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FactionSubRaid implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRaid(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "raid";
    }

    @Override
    public String getDescription() {
        return """
                Starts or schedules a raid against another faction's weak claim.
                Provide the target faction, the raid date, confirmation, and the selected chunk.
                Dates use DD-MM-YYYY:HHMM in UTC.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.raid";
    }

    @Override
    public String getUsage() {
        return "/faction raid <factionName> <raidDate> <confirm>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>() {{
            put("select", new FactionSubRaidSelect(plugin));
        }};
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Raid");
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
        if (args.length != 4 || !args[args.length - 1].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        FactionObject defendingFaction = null;
        for (FactionObject faction : plugin.factionManager.factionMembershipService.getExistingFactions()) {
            if (faction.getFactionName().equals(args[1])) {
                defendingFaction = faction;
                break;
            }
        }

        if (defendingFaction == null) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The defending faction you tried to attack doesn't exists!");
            return;
        }

        plugin.raidManager.SendRaidDeclaration(
                player,
                plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()),
                defendingFaction, args[2]);

    }
}
