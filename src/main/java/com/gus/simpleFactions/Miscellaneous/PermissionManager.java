package com.gus.simpleFactions.Miscellaneous;

import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PermissionManager {

    private HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

    private final SimpleFactions plugin;
    public PermissionManager(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public void AddPerm(Player player, ArrayList<String> permsToAdd){
        PermissionAttachment attachment;
        if (!perms.containsKey(player.getUniqueId())){
            attachment = player.addAttachment(plugin);
            perms.put(player.getUniqueId(), attachment);
        } else {
            attachment = perms.get(player.getUniqueId());
        }

        for (String perm : permsToAdd){
            if (!player.hasPermission(perm)){
                attachment.setPermission(perm, true);
            }
        }

        player.recalculatePermissions();
    }

    public void RemovePerm(Player player, ArrayList<String> permsToRemove){
        if (!perms.containsKey(player.getUniqueId())) return;
        PermissionAttachment attachment = perms.get(player.getUniqueId());

        for (String perm : permsToRemove){
            if (player.hasPermission(perm)){
                attachment.unsetPermission(perm);
            }
        }

        player.recalculatePermissions();
    }
}
