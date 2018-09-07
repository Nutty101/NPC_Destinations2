package net.livecar.nuttyworks.npc_destinations.pathing;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PathFindingQueue {
    public String                requestedBy;
    public NPC                   npc;
    public NPCDestinationsTrait  npcTrait;
    public List<Material>        allowedPathBlocks;
    public Boolean               opensGates;
    public Boolean               opensWoodDoors;
    public Boolean               opensMetalDoors;

    public int                   range;
    public int                   start_X, start_Y, start_Z;
    public int                   end_X, end_Y, end_Z;
    public World                 world;

    public HashMap<String, Tile> open;
    public HashMap<String, Tile> closed;

    private int                  blocksBelow;

    public Long                  blocksProcessed;
    public Long                  timeSpent;
    public Date                  processingStarted = null;

    public PathingResult         pathFindingResult;

    public void setBlocksBelow(int depth) {
        blocksBelow = depth;
    }

    public Location getPathLocation(Location source) {
        if (blocksBelow == -1) // Means we want the absolute 0 block to find a
                               // path
            return new Location(source.getWorld(), source.getX(), 0, source.getZ());

        return new Location(source.getWorld(), source.getX(), source.getY() - blocksBelow, source.getZ());
    }

    public int getBlocksBelow() {
        return blocksBelow;
    }

}
