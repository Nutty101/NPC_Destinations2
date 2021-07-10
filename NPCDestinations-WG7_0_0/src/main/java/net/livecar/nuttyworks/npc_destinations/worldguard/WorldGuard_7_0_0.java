package net.livecar.nuttyworks.npc_destinations.worldguard;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class WorldGuard_7_0_0 implements WorldGuardInterface, Listener {

    private Plugin                destRef    = null;

    public static boolean isValidVersion() {
        try {
            Class.forName("com.sk89q.worldedit.BlockVector");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public WorldGuard_7_0_0(Plugin storageRef) {
        destRef = storageRef;
    }

    public RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(WorldGuard.getInstance().getPlatform().getWorldByName(world.getName()));
    }

    public void registerFlags() {
    }

    public void unregisterFlags() {
    }

    @Override
    public void registerEvents() {
    
    }

    public Location[] getRegionBounds(World world, String regionName) {
        ProtectedRegion boundRegion = getRegionManager(world).getRegion(regionName);
        if (boundRegion == null)
            return new Location[0];

        Location[] boundLocs = new Location[2];
        boundLocs[0] = new Location(world, boundRegion.getMinimumPoint().getBlockX(), boundRegion.getMinimumPoint().getBlockY(), boundRegion.getMinimumPoint().getBlockZ());
        boundLocs[1] = new Location(world, boundRegion.getMaximumPoint().getBlockX(), boundRegion.getMaximumPoint().getBlockY(), boundRegion.getMaximumPoint().getBlockZ());
        boundRegion = null;

        return boundLocs;
    }

    public boolean isInRegion(Location location, String regionName) {
        ProtectedRegion boundRegion = getRegionManager(location.getWorld()).getRegion(regionName);
        if (boundRegion == null)
            return false;

        return boundRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public List<String> getRegionList(World world) {
        List<String> regionList = new ArrayList<String>();
        regionList.addAll(getRegionManager(world).getRegions().keySet());
        return regionList;
    }

    @Override
    public RegionShape getRegionShape(Location location, String regionName) {
        ProtectedRegion boundRegion = getRegionManager(location.getWorld()).getRegion(regionName);
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
