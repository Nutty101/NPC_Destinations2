package net.livecar.nuttyworks.npc_destinations.plugins.timemanager.gameworldtime;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.plugins.timemanager.DestinationsTimeManager;
import net.livecar.nuttyworks.npc_destinations.plugins.timemanager.GameWorldTime;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;

public class GameWorldTimeManager  extends DestinationsTimeManager {
    
    private BukkitTask tickWatcher;
    
    public GameWorldTimeManager()
    {
        super();
        startTimeWatcher();
    }
    
    public String getQuickDescription() {
        return "Default Destinations NPC-World time manager";
    }
    
    public long getNPCTime(NPC npc)
    {
        return (int)npc.getEntity().getWorld().getTime();
    }

    private void startTimeWatcher()
    {
        if (tickWatcher != null)
            return;
        
        tickWatcher = Bukkit.getScheduler().runTaskTimer(DestinationsPlugin.Instance,() -> timeTick(), 0, 20);
    }
    
    private void stopTimeWatcher()
    {
        tickWatcher.cancel();
    }
    
    private void timeTick()
    {
        for (World world : Bukkit.getServer().getWorlds())
        {
            GameWorldTime newWorldTime;
            if (!worlds.containsKey(world.getName()))
            {
                newWorldTime = new GameWorldTime();
                newWorldTime.dayCount = 0;
                //Get the config settings
                try {
                    newWorldTime.startDate = LocalDateTime.parse(DestinationsPlugin.Instance.getConfig().getString("defaultworld.startdate","2020-01-01T00:00:00"));
                } catch (Exception err) {
                    newWorldTime.startDate = LocalDateTime.parse("2020-01-01T00:00:00");
                }
                worlds.put(world.getName(),newWorldTime);
            } else {
                newWorldTime = worlds.get(world.getName());
            }
    
            if (newWorldTime.lastGameTime > world.getTime())
            {
                newWorldTime.lastGameTime = world.getTime();
                newWorldTime.dayCount++;
            } else {
                newWorldTime.lastGameTime = world.getTime();
            }
            worlds.put(world.getName(),newWorldTime);
        }
    }
}
