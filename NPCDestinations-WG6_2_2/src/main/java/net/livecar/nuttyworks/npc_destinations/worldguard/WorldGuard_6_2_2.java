package net.livecar.nuttyworks.npc_destinations.worldguard;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class WorldGuard_6_2_2 implements WorldGuardInterface, Listener {
    private WorldGuardPlugin getWorldGuardPlugin = null;
    private Plugin destRef = null;


    public static boolean isValidVersion() {
        try {
            Class.forName("com.sk89q.worldguard.bukkit.WGBukkit");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public WorldGuard_6_2_2(Plugin storageRef) {
        getWorldGuardPlugin = WGBukkit.getPlugin();
        destRef = storageRef;
    }

    public RegionManager getRegionManager(World world) {
        return getWorldGuardPlugin.getRegionManager(world);
    }

    public void registerFlags() {
    }

    public void unregisterFlags() {

    }

    public Location[] getRegionBounds(World world, String regionName) {
        ProtectedRegion boundRegion = getWorldGuardPlugin.getRegionManager(world).getRegion(regionName);
        if (boundRegion == null)
            return new Location[0];

        Location[] boundLocs = new Location[2];
        boundLocs[0] = new Location(world, boundRegion.getMinimumPoint().getBlockX(), boundRegion.getMinimumPoint().getBlockY(), boundRegion.getMinimumPoint().getBlockZ());
        boundLocs[1] = new Location(world, boundRegion.getMaximumPoint().getBlockX(), boundRegion.getMaximumPoint().getBlockY(), boundRegion.getMaximumPoint().getBlockZ());
        boundRegion = null;

        return boundLocs;
    }

    public boolean isInRegion(Location location, String regionName) {
        ProtectedRegion boundRegion = getWorldGuardPlugin.getRegionManager(location.getWorld()).getRegion(regionName);
        if (boundRegion == null)
            return false;

        return boundRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, destRef);
    }

    @Override
    public List<String> getRegionList(World world) {
        List<String> regionList = new ArrayList<String>();
        regionList.addAll(getWorldGuardPlugin.getRegionManager(world).getRegions().keySet());
        return regionList;
    }

    @Override
    public RegionShape getRegionShape(Location location, String regionName) {
        ProtectedRegion boundRegion = getWorldGuardPlugin.getRegionManager(location.getWorld()).getRegion(regionName);
        if (boundRegion == null)
            return null;

        switch (boundRegion.getType()) {
            case CUBOID:
                return RegionShape.CUBOID;
            case GLOBAL:
                return RegionShape.GLOBAL;
            case POLYGON:
                return RegionShape.POLYGON;
        }
        return null;
    }

}
