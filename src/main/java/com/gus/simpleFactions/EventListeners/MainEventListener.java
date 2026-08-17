package com.gus.simpleFactions.EventListeners;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.*;

public class MainEventListener implements Listener {

    private final SimpleFactions plugin;

    public MainEventListener(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        // Update player chunk state
        if (e.getTo() != null && !e.getFrom().getChunk().equals(e.getTo().getChunk()))
            plugin.factionManager.factionHelperService.updatePlayerChunkState(e.getPlayer().getUniqueId(), e.getTo().getChunk());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        // For now, put a default value, will be used for "server owned" regions
        plugin.factionManager.factionLandService.getPlayerChunkState().put(e.getPlayer().getUniqueId(), PlayerChunkState.PROTECTED);

        // Update player chunk state
        plugin.factionManager.factionHelperService.updatePlayerChunkState(e.getPlayer().getUniqueId(), e.getPlayer().getLocation().getChunk());

        // Restore the player's faction rank state and reapply permissions
        if (plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(e.getPlayer().getUniqueId())) {
            FactionObject faction = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(e.getPlayer().getUniqueId());

            plugin.factionManager.factionRankService.ensureMembersHaveValidRanks(faction);
            plugin.factionManager.factionRankService.reapplyRankPermissions(faction, e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Save player tab team to faction
        if (plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(e.getPlayer().getUniqueId())) {
            FactionObject faction = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(e.getPlayer().getUniqueId());
            for (FactionRankObject factionRank : faction.getFactionRanks()) {
                if (factionRank.getRankMembers().contains(e.getPlayer().getUniqueId())) {
                    faction.addSavedFactionRank(e.getPlayer().getUniqueId(), factionRank.getRankName());
                    plugin.factionManager.factionRankService.removeAttachment(e.getPlayer().getUniqueId());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onTntExplode(TNTPrimeEvent e) {
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk());
        if (chunkFaction == null) return;

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getLocation().getChunk())) {
            if (!plugin.getConfig().getBoolean("faction.hard-claim.tnt-explosion")) {
                e.setCancelled(true);
            }
        } else {
            if (!plugin.getConfig().getBoolean("faction.weak-claim.tnt-explosion")) {
                e.setCancelled(true);
            }
        }
        //endregion
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk());
        if (chunkFaction == null) return;

        //region Check Claim Chunk
        if (e.getBlock().getType().equals(Material.FIRE)) {
            if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getLocation().getChunk())) {
                if (!plugin.getConfig().getBoolean("faction.hard-claim.fire-spread")) {
                    e.setCancelled(true);
                }
            } else {
                if (!plugin.getConfig().getBoolean("faction.weak-claim.fire-spread")) {
                    e.setCancelled(true);
                }
            }
        }
        //endregion
    }

    @EventHandler
    public void onServerReload(ServerLoadEvent e) {
        // Even if the server is reloading, we need to update the player chunk state (because it's not persistent, for now)
        if (e.getType().equals(ServerLoadEvent.LoadType.RELOAD)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                // For now, put a default value will be used for "server-owned" regions
                plugin.factionManager.factionLandService.addPlayerInProtectedChunks(onlinePlayer.getUniqueId(), PlayerChunkState.PROTECTED);

                // Update player chunk state
                plugin.factionManager.factionHelperService.updatePlayerChunkState(onlinePlayer.getUniqueId(), onlinePlayer.getLocation().getChunk());

                if (plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(onlinePlayer.getUniqueId())) {
                    FactionObject faction = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(onlinePlayer.getUniqueId());

                    plugin.factionManager.factionRankService.ensureMembersHaveValidRanks(faction);
                    plugin.factionManager.factionRankService.reapplyRankPermissions(faction, onlinePlayer.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!plugin.factionCommandUi.isFactionGuiInventory(e.getView().getTopInventory())) return;

        // Lock the whole menu down - no moving/taking items in or out, regardless of click type.
        e.setCancelled(true);

        // Only react to clicks on the GUI itself, not the player's own inventory below it.
        if (!e.getView().getTopInventory().equals(e.getClickedInventory())) return;

        plugin.factionCommandUi.handleClick(player, e.getSlot());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (plugin.factionCommandUi.isFactionGuiInventory(e.getView().getTopInventory())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        if (plugin.factionCommandUi.isFactionGuiInventory(e.getInventory())) {
            plugin.factionCommandUi.handleClose(player);
        }
    }
}
