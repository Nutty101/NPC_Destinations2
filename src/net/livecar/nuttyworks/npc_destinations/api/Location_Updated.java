package net.livecar.nuttyworks.npc_destinations.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.citizensnpcs.api.npc.NPC;

public class Location_Updated extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Destination_Setting      destinationChanged;
    private NPC                      owningNPC;

    public Location_Updated(NPC changedNPC, Destination_Setting changedDestination) {
        destinationChanged = changedDestination;
        owningNPC = changedNPC;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public NPC getNPC() {
        return owningNPC;
    }

    public Destination_Setting getDestination() {
        return destinationChanged;
    }
}
