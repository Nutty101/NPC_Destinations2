package net.livecar.nuttyworks.npc_destinations.plugins.timemanager.realworldtime;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.plugins.timemanager.DestinationsTimeManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;

public class DestinationsRealWorldTimeManager extends DestinationsTimeManager {
    
    private float daySec;
    private float mcSeconds = 1200/86400F;
    private LocalDateTime startDate;
    
    public DestinationsRealWorldTimeManager()
    {
        super();
        //Get the config settings
        try {
            startDate = LocalDateTime.parse(DestinationsPlugin.Instance.getConfig().getString("realtime.startdate","2020-01-01T00:00:00"));
        } catch (Exception err) {
            startDate = LocalDateTime.parse("2020-01-01T00:00:00");
        }
        this.daySec = 86400.00F/DestinationsPlugin.Instance.getConfig().getInt("realworld.secondsperday",1200);
    }
    
    @Override
    public String getQuickDescription() {
        return "Realworld Time to Minecraft time";
    }
    
    public long getNPCTime(NPC npc) {
       
        Duration difference = Duration.between(startDate,LocalDateTime.now());
        LocalDateTime newTime = startDate.plusSeconds((long)Math.abs(difference.getSeconds()*daySec));
    
        int mcTime = 0;
        if (newTime.getHour()<6)
            mcTime = (24+(newTime.getHour()-6))*1000;
        else
            mcTime = (newTime.getHour()-6)*1000;
    
        mcTime = mcTime + (int)(newTime.getMinute()*(mcSeconds*60));
        mcTime = mcTime + newTime.getSecond();
        
        return mcTime;
    }

    public LocalDateTime getLocalDateTime(World world)
    {
        LocalDateTime endDate = LocalDateTime.now();
        Duration difference = Duration.between(startDate,endDate);
        return startDate.plusSeconds((long)Math.abs(difference.getSeconds()*daySec));
    }

    //TODO for future ability to track dates
    public DayOfWeek getNPCDayOfWeek(NPC npc)
    {
        return null;
    }
    
}
