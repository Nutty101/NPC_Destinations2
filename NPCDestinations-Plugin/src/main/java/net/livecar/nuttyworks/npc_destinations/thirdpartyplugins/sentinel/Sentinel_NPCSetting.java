package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.sentinel;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;

public class Sentinel_NPCSetting {

    public int                                     npcID;
    public HashMap<UUID, Sentinel_LocationSetting> locations;
    public Location                                currentDestination;
    public NPCDestinationsTrait                    destinationsTrait;
    public Date                                    lastAction;

    public Sentinel_NPCSetting() {
        locations = new HashMap<UUID, Sentinel_LocationSetting>();
        lastAction = new Date();
    }

    public void setNPC(Integer npcid) {
        this.npcID = npcid;
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        destinationsTrait = npc.getTrait(NPCDestinationsTrait.class);
        locations = new HashMap<UUID, Sentinel_LocationSetting>();
    }

    public Integer getNPCID() {
        return npcID;
    }
}
