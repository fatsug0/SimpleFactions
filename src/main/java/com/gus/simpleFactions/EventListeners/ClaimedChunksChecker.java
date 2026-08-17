package com.gus.simpleFactions.EventListeners;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.Enums.RaidState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.RaidHandlers.RaidInfoObject;
import com.gus.simpleFactions.SimpleFactions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

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

//        if (plugin.factionManager.factionLandService.getPlayerChunkState().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
//                plugin.factionManager.factionLandService.getPlayerChunkState().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        if (playerCanInteractChunk(e.getPlayer().getUniqueId(), e.getBlock().getChunk())) return;

        //region Raid End Check
        // Must run BEFORE the normal claim protection below: the raid core has to be allowed to
        // actually break during the capture-the-flag phase, otherwise the block-break protection
        // cancels the event and the raid "ends" without the core ever really being destroyed.
        if (plugin.raidManager.getCurrentRaids().containsKey(chunkFaction)) {
            for (RaidInfoObject raidInfoObject : plugin.raidManager.getCurrentRaids().get(chunkFaction)) {
                if (raidInfoObject.getRaidState() != RaidState.CAPTURE_FLAG) {
                    e.setCancelled(true);
                    return;
                }

                if (isRaidCoreBlock(e.getBlock(), raidInfoObject)) {
                    // Core has genuinely been destroyed, end the raid with the attacker as winner.
                    // Let the block break (do not fall through to the normal claim protection below).
                    raidInfoObject.setRaidState(RaidState.END);
                    plugin.raidManager.EndRaid(raidInfoObject, true);
                    return;
                }
            }
        }
        //endregion

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getChunk())) {
            if (!plugin.getConfig().getBoolean("faction.hard-claim.block-break")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                e.setCancelled(true);
            }
        } else {
            if (!plugin.getConfig().getBoolean("faction.weak-claim.block-break")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                e.setCancelled(true);
            }
        }
        //endregion
    }

    private boolean isRaidCoreBlock(Block block, RaidInfoObject raidInfoObject) {
        return block.hasMetadata("CoreRaidBlock") &&
                block.getMetadata("CoreRaidBlock").stream().anyMatch(value -> raidInfoObject.getDefendingFaction().getFactionName().equals(value.asString()));
    }

    private boolean isRaidCoreItem(ItemStack item, RaidInfoObject raidInfoObject) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, "raid_flag");
        String factionName = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return raidInfoObject.getDefendingFaction().getFactionName().equals(factionName);
    }

    public boolean playerCanInteractChunk(UUID playerUUID, Chunk chunk){
        return plugin.factionManager.factionLandService.getLinkedChunks().get(chunk).equals(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(playerUUID));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getBlock().getChunk());
        if (chunkFaction == null) return;

        //region Raid Start Check
        if (plugin.raidManager.getCurrentRaids().containsKey(chunkFaction)) {
            for (RaidInfoObject raidInfoObject : plugin.raidManager.getCurrentRaids().get(chunkFaction)){
                if (raidInfoObject.getRaidState() != RaidState.START) {
                    e.setCancelled(true);
                    return;
                }

                if (isRaidCoreItem(e.getItemInHand(), raidInfoObject)) {
                    e.getBlock().setMetadata("CoreRaidBlock", new FixedMetadataValue(plugin, raidInfoObject.getDefendingFaction().getFactionName()));
                    raidInfoObject.setRaidState(RaidState.GROUNDS);
                    return;
                }
            }
        }
        //endregion

        if (plugin.factionManager.factionLandService.getPlayerChunkState().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.factionLandService.getPlayerChunkState().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getBlock().getChunk())) {
            if (!plugin.getConfig().getBoolean("faction.hard-claim.block-place")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                e.setCancelled(true);
            }
        } else {
            if (!plugin.getConfig().getBoolean("faction.weak-claim.block-place")) {
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                e.setCancelled(true);
            }
        }
        //endregion

    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){

        // First check if it's a hard claim or a weak claim
        FactionObject chunkFaction = plugin.factionManager.factionLandService.getLinkedChunks().get(e.getPlayer().getLocation().getChunk()) == null ? null : plugin.factionManager.factionLandService.getLinkedChunks().get(e.getPlayer().getLocation().getChunk());
        if (chunkFaction == null) return;

        if (plugin.factionManager.factionLandService.getPlayerChunkState().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.HARD) ||
                plugin.factionManager.factionLandService.getPlayerChunkState().get(e.getPlayer().getUniqueId()).equals(PlayerChunkState.WEAK)) return;

        //region Check Claim Chunk
        if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getPlayer().getLocation().getChunk())) {
            if ((e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    && !plugin.getConfig().getBoolean("faction.hard-claim.interact")){
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                e.setCancelled(true);
            }
        } else {
            if ((e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    && !plugin.getConfig().getBoolean("faction.weak-claim.interact")){
                Objects.requireNonNull(Bukkit.getPlayer(e.getPlayer().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
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
                        Objects.requireNonNull(Bukkit.getPlayer(e.getEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                        e.setCancelled(true);
                    }
                } else {
                    if (!plugin.getConfig().getBoolean("faction.weak-claim.pvp")) {
                        Objects.requireNonNull(Bukkit.getPlayer(e.getEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                        e.setCancelled(true);
                    }
                }
            } else {

                if (plugin.factionManager.factionHelperService.isChunkHardClaimed(chunkFaction.getHardClaimedChunks(), e.getDamageSource().getCausingEntity().getLocation().getChunk())) {
                    if (!plugin.getConfig().getBoolean("faction.hard-claim.entity-damage")) {
                        Objects.requireNonNull(Bukkit.getPlayer(e.getDamageSource().getCausingEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                        e.setCancelled(true);
                    }
                } else {
                    if (!plugin.getConfig().getBoolean("faction.weak-claim.entity-damage")) {
                        Objects.requireNonNull(Bukkit.getPlayer(e.getDamageSource().getCausingEntity().getUniqueId())).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "You are in claimed land!"));
                        e.setCancelled(true);
                    }
                }
            }
            //endregion
        }
    }
}
