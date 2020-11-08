package net.livecar.nuttyworks.npc_destinations.plugins.timemanager;

import org.bukkit.World;

import java.time.LocalDateTime;

public class GameWorldTime {
    public LocalDateTime startDate;
    public int dayCount;
    public int secondsPerDay;
    public long lastGameTime;
    public World gameWorld;
}
