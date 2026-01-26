package com.gus.simpleFactions;

import com.gus.simpleFactions.Commands.Faction.FactionCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public final class SimpleFactions extends JavaPlugin {

    public FactionManager factionManager;
    public TeleportManager teleportManager;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        factionManager = new FactionManager(this);
        teleportManager = new TeleportManager(this);

        Bukkit.getPluginManager().registerEvents(new MainEventListener(this), this);

        InitiateCommands();

        StartUpBanner();

        for (Team team : Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeams()) {
            team.unregister();
        }
    }

    @Override
    public void onDisable() {
        for (Team team : Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeams()) {
            team.unregister();
        }
    }

    private void InitiateCommands(){
        new FactionCommand(this);
    }

    private void StartUpBanner(){
        System.out.println("\n" +
                "\n" +
                "+============================================+\n" +
                "|     ____ ___ __  __ ____  _     _____      |\n" +
                "|    / ___|_ _|  \\/  |  _ \\| |   | ____|     |\n" +
                "|    \\___ \\| || |\\/| | |_) | |   |  _|       |\n" +
                "|     ___) | || |  | |  __/| |___| |___      |\n" +
                "|    |____/___|_|  |_|_|   |_____|_____|     |\n" +
                "| _____ _    ____ _____ ___ ___  _   _ ____  |\n" +
                "||  ___/ \\  / ___|_   _|_ _/ _ \\| \\ | / ___| |\n" +
                "|| |_ / _ \\| |     | |  | | | | |  \\| \\___ \\ |\n" +
                "||  _/ ___ \\ |___  | |  | | |_| | |\\  |___) ||\n" +
                "||_|/_/   \\_\\____| |_| |___\\___/|_| \\_|____/ |\n" +
                "+============================================+\n" +
                "             Developed by fatsug0 \n" +
                "\n");
    }
}
