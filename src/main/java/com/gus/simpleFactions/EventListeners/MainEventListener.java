package com.gus.simpleFactions.EventListeners;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.Enums.RaidState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.RaidHandlers.RaidInfoObject;
import com.gus.simpleFactions.SimpleFactions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MainEventListener implements Listener {

    private final SimpleFactions plugin;
    public MainEventListener(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){

        // Update player chunk state
        if (e.getTo() != null && !e.getFrom().getChunk().equals(e.getTo().getChunk())) UpdatePlayerChunkState(e.getPlayer().getUniqueId(), e.getTo().getChunk());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        // For now, put a default value, will be used for "server owned" regions
        plugin.factionManager.factionLandService.getPlayerInProtectedChunks().put(e.getPlayer().getUniqueId(), PlayerChunkState.PROTECTED);

        // Update player chunk state
        UpdatePlayerChunkState(e.getPlayer().getUniqueId(), e.getPlayer().getLocation().getChunk());

        // Set player rank in tab (if in any)
        if (plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(e.getPlayer().getUniqueId()) != null){
            var scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
            scoreboard.getTeams().forEach(t -> t.removeEntry(e.getPlayer().getName()));
            Objects.requireNonNull(scoreboard.getTeam("faction" + plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(e.getPlayer().getUniqueId()).getFactionName())).addEntry(e.getPlayer().getName());
        }

        // Check if the player has all the basic permission that every player should have, make the basic perm persistent
        if (!e.getPlayer().hasPermission("simplefactions")){
            plugin.permissionManager.AddPerm(e.getPlayer(), new ArrayList<>(List.of("simplefactions")));
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
        if (e.getType().equals(ServerLoadEvent.LoadType.STARTUP)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                // For now, put a default value will be used for "server-owned" regions
                plugin.factionManager.factionLandService.addPlayerInProtectedChunks(onlinePlayer.getUniqueId(), PlayerChunkState.PROTECTED);

                // Update player chunk state
                UpdatePlayerChunkState(onlinePlayer.getUniqueId(), onlinePlayer.getLocation().getChunk());
            }
        }
    }

    private boolean PlayerIsInHisFaction(UUID uuid, Chunk chunkToCheck) {
        return (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().containsKey(uuid) && plugin.factionManager.factionLandService.getPlayerInProtectedChunks().containsKey(chunkToCheck)) &&
                (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(uuid).equals(plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(chunkToCheck)));
    }

    private void UpdatePlayerChunkState(UUID playerUUID, Chunk chunkToCheck){
        if (plugin.factionManager.factionHelperService.CanInteractWithChunk(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(playerUUID), playerUUID, chunkToCheck)){

            // Player can interact with the chunk, either he's in claimed land or he's in the wilderness
            if (PlayerIsInHisFaction(playerUUID, chunkToCheck)){

                //Check if it's a hard or weak claim
                if (                plugin.factionManager.factionHelperService.isChunkHardClaimed(plugin.factionManager.factionLandService.getLinkedChunks().get(chunkToCheck).getHardClaimedChunks(), chunkToCheck)){
                    if (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(playerUUID) != PlayerChunkState.HARD) {
                        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6§lWelcome home (hard claim) !"));
                        plugin.factionManager.factionLandService.getPlayerInProtectedChunks().put(playerUUID, PlayerChunkState.HARD);
                    }
                } else {
                    if (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(playerUUID) != PlayerChunkState.WEAK) {
                        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6§lWelcome home (weak claim) !"));
                        plugin.factionManager.factionLandService.getPlayerInProtectedChunks().put(playerUUID, PlayerChunkState.WEAK);
                    }
                }
            } else {
                if (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(playerUUID) != PlayerChunkState.WILDERNESS) {
                    Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§2§lYou have entered wilderness !"));
                    plugin.factionManager.factionLandService.getPlayerInProtectedChunks().put(playerUUID, PlayerChunkState.WILDERNESS);
                }
            }
        } else {

            // Player can only be in a claimed land by another faction
            if (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(playerUUID) != PlayerChunkState.ENEMY) {
                Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou have entered the " + plugin.factionManager.factionLandService.getLinkedChunks().get(chunkToCheck).getFactionName() + " faction !"));
                plugin.factionManager.factionLandService.getPlayerInProtectedChunks().put(playerUUID, PlayerChunkState.ENEMY);
            }
        }
    }
}
