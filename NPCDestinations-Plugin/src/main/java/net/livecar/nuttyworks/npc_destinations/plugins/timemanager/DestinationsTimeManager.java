package net.livecar.nuttyworks.npc_destinations.plugins.timemanager;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DestinationsTimeManager {
    
    public Map<String, GameWorldTime> worlds;
    
    public void DestinationsTimeManager()
    {
        worlds = new HashMap<>();
        loadWorldClocks();
    }
    
    public String getQuickDescription() {
        return "This is not a valid time manager";
    }
    
    public void saveConfig() {
        saveWorldClocks();
    }
    
    public long getNPCTime(NPC npc)
    {
        return npc.getEntity().getWorld().getTime();
    }
    
    private void loadWorldClocks()
    {
        File worldConfigFile = new File(DestinationsPlugin.Instance.getDataFolder(), "worldclock_settings.yml");
        
        YamlConfiguration worldSettings = DestinationsPlugin.Instance.getUtilitiesClass.loadConfiguration(worldConfigFile);
    
        if (worldSettings != null) {
            for (World world : Bukkit.getServer().getWorlds()) {
                if (worldSettings.contains("worlds." + world.getName())) {
                    GameWorldTime worldTime = new GameWorldTime();
                    worldTime.dayCount = worldSettings.getInt("worlds." + world.getName() + ".daycount", 0);
                    worldTime.dayCount = worldSettings.getInt("worlds." + world.getName() + ".secondsperday", 1200);
                    worldTime.gameWorld = world;
                    worldTime.lastGameTime = worldSettings.getLong("worlds." + world.getName() + ".lastGameTime",0);
                    try {
                        worldTime.startDate = LocalDateTime.parse(worldSettings.getString("worlds." + world.getName() + ".startDate","2020-01-01T00:00:00"));
                    } catch (Exception err) {
                        worldTime.startDate = LocalDateTime.parse("2020-01-01T00:00:00");
                    }
                    worlds.put(world.getName(),worldTime);
                }
            }
        }
    }
    
    private void saveWorldClocks()
    {
        if (!DestinationsPlugin.Instance.getDataFolder().exists())
            DestinationsPlugin.Instance.getDataFolder().mkdirs();
        File worldConfigFile = new File(DestinationsPlugin.Instance.getDataFolder(), "worldclock_settings.yml");
        YamlConfiguration worldSettings = new YamlConfiguration();
    
        for (GameWorldTime gameWorldTime : worlds.values()) {
            worldSettings.set("worlds." + gameWorldTime.gameWorld.getName() + ".daycount", gameWorldTime.dayCount);
            if ( gameWorldTime.secondsPerDay != 1200)
                worldSettings.set("worlds." + gameWorldTime.gameWorld.getName() + ".secondsperday", gameWorldTime.secondsPerDay);
            worldSettings.set("worlds." + gameWorldTime.gameWorld.getName() + ".lastGameTime", gameWorldTime.lastGameTime);
            worldSettings.set("worlds." + gameWorldTime.gameWorld.getName() + ".startDate", gameWorldTime.startDate);
        }
    
        try {
            worldSettings.save(worldConfigFile);
        } catch (IOException e) {
            // Problem return and don't save (Not right)
        }
        
    }
    
}
