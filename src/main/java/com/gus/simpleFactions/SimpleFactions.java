package com.gus.simpleFactions;

import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFactions extends JavaPlugin {

    public FactionManager factionManager = new FactionManager(this);

    @Override
    public void onEnable() {
        System.out.println("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
