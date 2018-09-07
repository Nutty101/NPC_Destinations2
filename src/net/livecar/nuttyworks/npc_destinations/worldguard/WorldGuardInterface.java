package net.livecar.nuttyworks.npc_destinations.worldguard;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import com.sk89q.worldguard.protection.managers.RegionManager;

public interface WorldGuardInterface  {

    public RegionManager getRegionManager(World world);

    public void registerFlags();

    public void unregisterFlags();

    public void registerEvents();
    
    public void checkWorld();
    
    public List<String> getRegionList(World world);
    
    public Location[] getRegionBounds(World world, String regionName);
    
    public boolean isInRegion(Location location, String regionName);
}
