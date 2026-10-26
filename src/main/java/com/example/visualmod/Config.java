package com.example.visualmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static Config I = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualmod.json");

    public boolean triggerBot = false, triggerCrit = true;
    public double triggerMinDelay = 0, triggerMaxDelay = 3; // ticks
    public boolean aimAssist = false, aimClickOnly = true;
    public double aimRange = 5, aimFov = 90, aimSpeed = 2;
    public boolean fullbright = false, espPlayers = false, espMobs = false;

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) I = c;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(I)); } catch (Exception e) { e.printStackTrace(); }
    }
}
