package com.gus.simpleFactions.EventListeners;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.Enums.RaidState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.RaidHandlers.RaidInfoObject;
import com.gus.simpleFactions.SimpleFactions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ClaimedChunksChecker implements Listener {

    private final SimpleFactions plugin;
    public ClaimedChunksChecker(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getChunk());
        if (chunkFaction == null) return;

        if (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getChunk())) {
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
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getChunk());
        if (chunkFaction == null) return;

        if (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getChunk())) {
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
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getPlayer().getLocation().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getPlayer().getLocation().getChunk());
        if (chunkFaction == null) return;

        if (plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.factionLandService.getPlayerInProtectedChunks().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getPlayer().getLocation().getChunk())) {
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
            FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getEntity().getLocation().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getEntity().getLocation().getChunk());
            assert chunkFaction != null;

            //region Check Claim Chunk
            if (Objects.requireNonNull(e.getEntity().getType()).equals(EntityType.PLAYER)){ // Other Player is a Player (the one being attacked)


                if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getEntity().getLocation().getChunk())) {
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

                if (                plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getDamageSource().getCausingEntity().getLocation().getChunk())) {
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
}
