package com.gus.simpleFactions.Commands.Faction.Sub.Raid;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FactionSubRaidSelect implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRaidSelect(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "select";
    }

    @Override
    public String getDescription() {
        return """
                Selects your current chunk as a raid target.
                Use this while standing on the weak claim you want to raid.
                The selected chunk can then be used by the raid command.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.raid.select";
    }

    @Override
    public String getUsage() {
        return "/faction raid select here";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return null;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Select Raid Chunk");
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
        if (args.length != 3 || !args[args.length - 1].equalsIgnoreCase("here") || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        FactionObject playerFaction = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId());

        // Check if the chunk is claimed
        FactionObject chunkOwner = plugin.factionManager.factionLandService.getLinkedChunks().get(player.getLocation().getChunk());

        if (chunkOwner == null) {
            player.sendMessage("This chunk is not claimed.");
            return;
        }

        // Check if it's not his own faction
        if (playerFaction.equals(chunkOwner)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You cannot raid your own faction!");
            return;
        }

        // Check chunk selection limit
        int currentSelectionSize = plugin.raidManager.getCurrentFactionSelection().getOrDefault(playerFaction, new java.util.ArrayList<>()).size();
        int maxSelectionSize = Math.max(1, (int) Math.round(plugin.raidManager.powerToChunkSelectionCoefficient * chunkOwner.getPower()));
        if (currentSelectionSize >= maxSelectionSize) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have reached the maximum of chunk selections for raiding !");
            return;
        }

        // Check if it's a weak chunk
        if (!isWeakChunk(player.getLocation().getChunk(), chunkOwner)) {
           player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You cannot raid a hard chunk!");
           return;
        }

        if (plugin.raidManager.getCurrentFactionSelection().getOrDefault(playerFaction, new java.util.ArrayList<>()).contains(player.getLocation().getChunk())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You already selected this chunk !");
            return;
        }

        plugin.raidManager.addChunkToSelection(playerFaction, player.getLocation().getChunk());
        player.sendMessage(ChatColor.GREEN + "Raid chunk selected.");
    }

    private boolean isWeakChunk(Chunk chunk, FactionObject chunkOwner){
        return plugin.factionManager.factionLandService.getLinkedChunks().containsKey(chunk) &&
                chunkOwner.getWeakClaimedChunks().contains(chunk);
    }
}
