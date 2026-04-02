package com.gus.simpleFactions.EventListeners;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
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
    public void onPlayerMove(PlayerMoveEvent e){

        // Update player chunk state
        if (e.getTo() != null && !e.getFrom().getChunk().equals(e.getTo().getChunk())) plugin.factionManager.factionHelperService.updatePlayerChunkState(e.getPlayer().getUniqueId(), e.getTo().getChunk());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        // For now, put a default value, will be used for "server owned" regions
        plugin.factionManager.factionLandService.getPlayerChunkState().put(e.getPlayer().getUniqueId(), PlayerChunkState.PROTECTED);

        // Update player chunk state
        plugin.factionManager.factionHelperService.updatePlayerChunkState(e.getPlayer().getUniqueId(), e.getPlayer().getLocation().getChunk());

        // Give back players permissions from their faction rank
        if (plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(e.getPlayer().getUniqueId())) {
            System.out.println("PLAYER HAS A FACTION");
            FactionObject faction = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(e.getPlayer().getUniqueId());
            for (String perm : plugin.factionManager.factionRankService.getRank(faction, faction.getSavedFactionRanks().get(e.getPlayer().getUniqueId())).getPermissions()) {
                System.out.println("GIVING PERM TO PLAYER : " + perm);
                plugin.factionManager.factionRankService.AddPermToPlayer(e.getPlayer().getUniqueId(), perm);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
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
    public void onTntExplode(TNTPrimeEvent e){
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk());
        if (chunkFaction == null) return;

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getLocation().getChunk())) {
            if (!plugin.getConfig().getBoolean("faction.hard-claim.tnt-explosion")){
                e.setCancelled(true);
            }
        } else {
            if (!plugin.getConfig().getBoolean("faction.weak-claim.tnt-explosion")){
                e.setCancelled(true);
            }
        }
        //endregion
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e){
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getLocation().getChunk());
        if (chunkFaction == null) return;

        //region Check Claim Chunk
        if (e.getBlock().getType().equals(Material.FIRE)){
            if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getLocation().getChunk())) {
                if (!plugin.getConfig().getBoolean("faction.hard-claim.fire-spread")){
                    e.setCancelled(true);
                }
            } else {
                if (!plugin.getConfig().getBoolean("faction.weak-claim.fire-spread")){
                    e.setCancelled(true);
                }
            }
        }
        //endregion
    }

    @EventHandler
    public void onServerReload(ServerLoadEvent e){
        // Even if the server is reloading, we need to update the player chunk state (because it's not persistent, for now)
        if (e.getType().equals(ServerLoadEvent.LoadType.RELOAD)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                // For now, put a default value will be used for "server-owned" regions
                plugin.factionManager.factionLandService.addPlayerInProtectedChunks(onlinePlayer.getUniqueId(), PlayerChunkState.PROTECTED);

                // Update player chunk state
                plugin.factionManager.factionHelperService.updatePlayerChunkState(onlinePlayer.getUniqueId(), onlinePlayer.getLocation().getChunk());
            }
        }
    }


}
