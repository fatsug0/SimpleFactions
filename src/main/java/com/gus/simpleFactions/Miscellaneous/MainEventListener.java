package com.gus.simpleFactions.Miscellaneous;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.Enums.RaidState;
import com.gus.simpleFactions.FactionHandlers.FactionObject;
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
    public void onBlockBreak(BlockBreakEvent e) {
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.linkedChunks.get(e.getBlock().getChunk()) == null ? null : plugin.factionManager.linkedChunks.get(e.getBlock().getChunk());
        if (chunkFaction == null) return;

        if (plugin.factionManager.playerInProtectedChunks.get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.playerInProtectedChunks.get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (chunkFaction.isChunkHardClaimed(e.getBlock().getChunk())) {
            if (!plugin.getConfig().getBoolean("faction.object.hard-claim.block-break")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                e.setCancelled(true);
            }
        } else {
            if (!plugin.getConfig().getBoolean("faction.object.weak-claim.block-break")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                e.setCancelled(true);
            }
        }
        //endregion

        //region Raid End Check
        for (RaidInfoObject raidInfoObject : plugin.raidManager.currentRaids.get(chunkFaction)){
            if (raidInfoObject.getRaidState() != RaidState.CAPTURE_FLAG) {
                e.setCancelled(true);
                return;
            }

            ItemStack item = new ItemStack(e.getBlock().getType(), 1, e.getBlock().getData());
            if (raidInfoObject.getRaidCore().equals(item)) {
                // Core has been destroyed, end the raid
                raidInfoObject.setRaidState(RaidState.END);
                plugin.raidManager.EndRaid(raidInfoObject, chunkFaction);
            }
        }
        //endregion
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.linkedChunks.get(e.getBlock().getChunk()) == null ? null : plugin.factionManager.linkedChunks.get(e.getBlock().getChunk());
        if (chunkFaction == null) return;

        if (plugin.factionManager.playerInProtectedChunks.get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.playerInProtectedChunks.get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (chunkFaction.isChunkHardClaimed(e.getBlock().getChunk())) {
            if (!plugin.getConfig().getBoolean("faction.object.hard-claim.block-place")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                e.setCancelled(true);
            }
        } else {
            if (!plugin.getConfig().getBoolean("faction.object.weak-claim.block-place")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                e.setCancelled(true);
            }
        }
        //endregion

        //region Raid Start Check
        for (RaidInfoObject raidInfoObject : plugin.raidManager.currentRaids.get(chunkFaction)){
            if (raidInfoObject.getRaidState() != RaidState.START) {
                e.setCancelled(true);
                return;
            }

            ItemStack item = new ItemStack(e.getBlock().getType(), 1, e.getBlock().getData());
            if (raidInfoObject.getRaidCore().equals(item)) {
                // Core has been destroyed, end the raid
                raidInfoObject.setRaidState(RaidState.GROUNDS);
            }
        }
        //endregion
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){

        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.linkedChunks.get(e.getPlayer().getLocation().getChunk()) == null ? null : plugin.factionManager.linkedChunks.get(e.getPlayer().getLocation().getChunk());
        if (chunkFaction == null) return;

        if (plugin.factionManager.playerInProtectedChunks.get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.playerInProtectedChunks.get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (chunkFaction.isChunkHardClaimed(e.getPlayer().getLocation().getChunk())) {
            if ((e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    && !plugin.getConfig().getBoolean("faction.hard-claim.interact")){
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                e.setCancelled(true);
            }
        } else {
            if ((e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    && !plugin.getConfig().getBoolean("faction.weak-claim.interact")){
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                e.setCancelled(true);
            }
        }
        //endregion
    }

    @EventHandler
    public void onPlayerPVP(EntityDamageEvent e){
        if (e.getDamageSource().getCausingEntity() == null) return;

        if (Objects.requireNonNull(e.getDamageSource().getCausingEntity()).getType().equals(EntityType.PLAYER)){

            // First check if it's a hard claim or a weak claim
            FactionObject chunkFaction = plugin.factionManager.linkedChunks.get(e.getEntity().getLocation().getChunk()) == null ? null : plugin.factionManager.linkedChunks.get(e.getEntity().getLocation().getChunk());
            assert chunkFaction != null;

            //region Check Claim Chunk
            if (Objects.requireNonNull(e.getEntity().getType()).equals(EntityType.PLAYER)){ // Other Player is a Player (the one being attacked)

                if (chunkFaction.isChunkHardClaimed(e.getEntity().getLocation().getChunk())) {
                    if (!plugin.getConfig().getBoolean("faction.hard-claim.pvp")) {
                        Objects.requireNonNull(Bukkit.getPlayer(e.getEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                        e.setCancelled(true);
                    }
                } else {
                    if (!plugin.getConfig().getBoolean("faction.weak-claim.pvp")) {
                        Objects.requireNonNull(Bukkit.getPlayer(e.getEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                        e.setCancelled(true);
                    }
                }
            } else {

                if (chunkFaction.isChunkHardClaimed(e.getDamageSource().getCausingEntity().getLocation().getChunk())) {
                    if (!plugin.getConfig().getBoolean("faction.hard-claim.entity-damage")) {
                        Objects.requireNonNull(Bukkit.getPlayer(e.getDamageSource().getCausingEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                        e.setCancelled(true);
                    }
                } else {
                    if (!plugin.getConfig().getBoolean("faction.weak-claim.entity-damage")) {
                        Objects.requireNonNull(Bukkit.getPlayer(e.getDamageSource().getCausingEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou are in claimed land !"));
                        e.setCancelled(true);
                    }
                }
            }
            //endregion
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        // For now, put a default value, will be used for "server owned" regions
        plugin.factionManager.playerInProtectedChunks.put(e.getPlayer().getUniqueId(), PlayerChunkState.PROTECTED);

        // Update player chunk state
        UpdatePlayerChunkState(e.getPlayer().getUniqueId(), e.getPlayer().getLocation().getChunk());

        // Set player rank in tab (if in any)
        if (plugin.factionManager.playerFactionLink.get(e.getPlayer().getUniqueId()) != null){
            var scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
            scoreboard.getTeams().forEach(t -> t.removeEntry(e.getPlayer().getName()));
            Objects.requireNonNull(scoreboard.getTeam("faction" + plugin.factionManager.playerFactionLink.get(e.getPlayer().getUniqueId()).getFactionName())).addEntry(e.getPlayer().getName());
        }

        // Check if the player has all the basic permission that every player should have, make the basic perm persistent
        if (!e.getPlayer().hasPermission("simplefactions")){
            plugin.permissionManager.AddPerm(e.getPlayer(), new ArrayList<>(List.of("simplefactions")));
        }
    }

    @EventHandler
    public void onTntExplode(TNTPrimeEvent e){
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.linkedChunks.get(e.getBlock().getLocation().getChunk()) == null ? null : plugin.factionManager.linkedChunks.get(e.getBlock().getLocation().getChunk());
        if (chunkFaction == null) return;

        //region Check Claim Chunk
        if (chunkFaction.isChunkHardClaimed(e.getBlock().getLocation().getChunk())) {
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
        FactionObject chunkFaction = plugin.factionManager.linkedChunks.get(e.getBlock().getLocation().getChunk()) == null ? null : plugin.factionManager.linkedChunks.get(e.getBlock().getLocation().getChunk());
        if (chunkFaction == null) return;

        //region Check Claim Chunk
        if (e.getBlock().getType().equals(Material.FIRE)){
            if (chunkFaction.isChunkHardClaimed(e.getBlock().getLocation().getChunk())) {
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
                plugin.factionManager.playerInProtectedChunks.put(onlinePlayer.getUniqueId(), PlayerChunkState.PROTECTED);

                // Update player chunk state
                UpdatePlayerChunkState(onlinePlayer.getUniqueId(), onlinePlayer.getLocation().getChunk());
            }
        }
    }

    private boolean PlayerIsInHisFaction(UUID uuid, Chunk chunkToCheck) {
        return (plugin.factionManager.playerFactionLink.containsKey(uuid) && plugin.factionManager.linkedChunks.containsKey(chunkToCheck)) &&
                (plugin.factionManager.playerFactionLink.get(uuid).equals(plugin.factionManager.linkedChunks.get(chunkToCheck)));
    }

    private void UpdatePlayerChunkState(UUID playerUUID, Chunk chunkToCheck){
        if (plugin.factionManager.CanInteractWithChunk(playerUUID, chunkToCheck)){

            // Player can interact with the chunk, either he's in claimed land or he's in the wilderness
            if (PlayerIsInHisFaction(playerUUID, chunkToCheck)){

                //Check if it's a hard or weak claim
                if (plugin.factionManager.linkedChunks.get(chunkToCheck).isChunkHardClaimed(chunkToCheck)){
                    if (plugin.factionManager.playerInProtectedChunks.get(playerUUID) != PlayerChunkState.HARD) {
                        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6§lWelcome home (hard claim) !"));
                        plugin.factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.HARD);
                    }
                } else {
                    if (plugin.factionManager.playerInProtectedChunks.get(playerUUID) != PlayerChunkState.WEAK) {
                        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6§lWelcome home (weak claim) !"));
                        plugin.factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.WEAK);
                    }
                }
            } else {
                if (plugin.factionManager.playerInProtectedChunks.get(playerUUID) != PlayerChunkState.WILDERNESS) {
                    Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§2§lYou have entered wilderness !"));
                    plugin.factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.WILDERNESS);
                }
            }
        } else {

            // Player can only be in a claimed land by another faction
            if (plugin.factionManager.playerInProtectedChunks.get(playerUUID) != PlayerChunkState.ENEMY) {
                Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou have entered the " + plugin.factionManager.linkedChunks.get(chunkToCheck).getFactionName() + " faction !"));
                plugin.factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.ENEMY);
            }
        }
    }
}
