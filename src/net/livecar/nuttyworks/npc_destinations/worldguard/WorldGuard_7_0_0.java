package net.livecar.nuttyworks.npc_destinations.worldguard;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuard_7_0_0 implements WorldGuardInterface, Listener {
    
    public static final StateFlag CHUNK_FLAG          = new StateFlag("ndest-forcechunk", false);
    private Plugin    destRef             = null;

    public WorldGuard_7_0_0(Plugin storageRef) {
        destRef = storageRef;
    }

    public RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(WorldGuard.getInstance().getPlatform().getWorldByName(world.getName()));
    }

    public void registerFlags() {
        WorldGuard.getInstance().getFlagRegistry().register(CHUNK_FLAG);
    }

    public void unregisterFlags() {
        //WorldGuard.getInstance().getFlagRegistry().unregisterHandler(WG_ChunkFlag.FACTORY);
    }

    @SuppressWarnings("deprecation")
    public void checkWorld() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(destRef, new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getServer().getWorlds()) {
                    RegionManager rm = getRegionManager(world);
                    if (rm != null) {
                        for (ProtectedRegion region : rm.getRegions().values()) {
                            if (region.getFlag(CHUNK_FLAG) == StateFlag.State.ALLOW) {
                                BlockVector min = region.getMinimumPoint();
                                BlockVector max = region.getMaximumPoint();
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
        });

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
            ProtectedCuboidRegion chunkRegion = new ProtectedCuboidRegion("destwg_region", new BlockVector(e.getChunk().getX() * 16, 0, e.getChunk().getZ() * 16), new BlockVector(e.getChunk().getX() * 16 + 15, 255, e.getChunk().getZ() * 16
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
                    BlockVector min = region.getMinimumPoint();
                    BlockVector max = region.getMaximumPoint();
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
        Bukkit.getPluginManager().registerEvents(this,destRef);
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

}
