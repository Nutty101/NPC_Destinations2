package net.livecar.nuttyworks.npc_destinations;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugTarget {

    public CommandSender  targetSender;
    private List<Integer> targetIDS;
    public List<Location> debugBlocksSent;

    public DebugTarget(CommandSender sender, Integer npcID) {
        targetSender = sender;
        targetIDS = new ArrayList<Integer>();
        if (npcID > -1) {
            targetIDS.add(npcID);
        }
        if (debugBlocksSent == null)
            debugBlocksSent = new ArrayList<Location>();
    }

    public void addNPCTarget(Integer npcID) {
        if (!targetIDS.contains(npcID))
            targetIDS.add(npcID);
    }

    public void removeNPCTarget(Integer npcID) {
        targetIDS.remove(npcID);
    }

    public List<Integer> getTargets() {
        return targetIDS;
    }

    public void addDebugBlockSent(Location blockLocation, Material material) {
        if (debugBlocksSent.contains(blockLocation))
            return;
        debugBlocksSent.add(blockLocation);

        if (((Player) targetSender).isOnline()) {
            DestinationsPlugin.Instance.getMCUtils.sendClientBlock((Player) targetSender, blockLocation, material);
        }
    }

    public void removeDebugBlockSent(Location blockLocation) {
        if (!debugBlocksSent.contains(blockLocation))
            return;
        debugBlocksSent.remove(blockLocation);
        if (((Player) targetSender).isOnline()) {
            DestinationsPlugin.Instance.getMCUtils.sendClientBlock((Player) targetSender, blockLocation, null);
        }
    }

    public void clearDebugBlocks() {
        for (Location blockLocation : debugBlocksSent) {
            if (((Player) targetSender).isOnline()) {
                DestinationsPlugin.Instance.getMCUtils.sendClientBlock((Player) targetSender, blockLocation, null);
            }
        }
        debugBlocksSent.clear();
    }
}
