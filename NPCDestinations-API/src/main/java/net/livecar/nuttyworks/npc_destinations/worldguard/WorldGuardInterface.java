package net.livecar.nuttyworks.npc_destinations.worldguard;

import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public interface WorldGuardInterface  {

    enum RegionShape {
        CUBOID,
        POLYGON,
        GLOBAL
    }

    RegionManager getRegionManager(World world);

    void registerFlags();

    void unregisterFlags();

    void registerEvents();

    List<String> getRegionList(World world);

    Location[] getRegionBounds(World world, String regionName);

    boolean isInRegion(Location location, String regionName);

    RegionShape getRegionShape(Location location, String regionName);

}
