package net.livecar.nuttyworks.npc_destinations.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
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

public class WorldGuard_7_0_3 implements WorldGuardInterface, Listener {

    public static final StateFlag CHUNK_FLAG = new StateFlag("ndest-forcechunk", false);
    private Plugin                destRef    = null;

    public static boolean isValidVersion() {
        try {
            //Validate that BlockVector3 class exists
            Class.forName("com.sk89q.worldedit.math.BlockVector3");
        } catch (Exception e) {
            return false;
        }

        try {
            //Validate that getWorldByName method exists (New beta's do not have this function anymore)
            Class.forName("com.sk89q.worldguard.internal.platform.WorldGuardPlatform").getMethod("getWorldByName",(Class<?>[]) null);
            return false;
        } catch (Exception e) {
            return true;
        }

    }

    public WorldGuard_7_0_3(Plugin storageRef) {
        destRef = storageRef;
        
    }

    public RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    public void registerFlags() {
        WorldGuard.getInstance().getFlagRegistry().register(CHUNK_FLAG);
    }

    public void unregisterFlags() {
        // WorldGuard.getInstance().getFlagRegistry().unregisterHandler(WG_ChunkFlag.FACTORY);
    }

    @SuppressWarnings("deprecation")
    public void checkWorld() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getServer().getWorlds()) {
                    RegionManager rm = getRegionManager(world);
                    if (rm != null) {
                        for (ProtectedRegion region : rm.getRegions().values()) {
                            if (region.getFlag(CHUNK_FLAG) == StateFlag.State.ALLOW) {
                                BlockVector3 min = region.getMinimumPoint();
                                BlockVector3 max = region.getMaximumPoint();
                                Location minLoc = new Location(world, min.getX(), min.getY(), min.getZ());
                                Location maxLoc = new Location(world, max.getX(), max.getY(), max.getZ());
                                for (int x = minLoc.getChunk().getX(); x <= maxLoc.getChunk().getX(); x++) {
                                    for (int z = minLoc.getChunk().getZ(); z <= maxLoc.getChunk().getZ(); z++) {
                                        world.getChunkAt(x, z).load();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTask(destRef);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        RegionManager rm = null;
        try {
            rm = getRegionManager(e.getWorld());
        } catch (Exception err) {
            // Possible fix for worldguard not having the world in it's memory
            // yet.
        }

        if (rm != null) {
            ProtectedCuboidRegion chunkRegion = new ProtectedCuboidRegion("destwg_region", BlockVector3.at(e.getChunk().getX() * 16, 0, e.getChunk().getZ() * 16), BlockVector3.at(e.getChunk().getX() * 16 + 15, 255, e.getChunk().getZ() * 16
                    + 15));
            for (ProtectedRegion region : rm.getApplicableRegions(chunkRegion)) {
                if (region.getFlag(CHUNK_FLAG) == StateFlag.State.ALLOW) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        RegionManager rm = getRegionManager(e.getWorld());
        if (rm != null) {
            for (ProtectedRegion region : rm.getRegions().values()) {
                if (region.getFlag(CHUNK_FLAG) == StateFlag.State.ALLOW) {
                    BlockVector3 min = region.getMinimumPoint();
                    BlockVector3 max = region.getMaximumPoint();
                    Location minLoc = new Location(e.getWorld(), min.getX(), min.getY(), min.getZ());
                    Location maxLoc = new Location(e.getWorld(), max.getX(), max.getY(), max.getZ());
                    for (int x = minLoc.getChunk().getX(); x <= maxLoc.getChunk().getX(); x++) {
                        for (int z = minLoc.getChunk().getZ(); z <= maxLoc.getChunk().getZ(); z++) {
                            e.getWorld().getChunkAt(x, z).load();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, destRef);
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
