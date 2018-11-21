package net.livecar.nuttyworks.npc_destinations.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.citizensnpcs.api.npc.NPC;

public class Navigation_Failed extends Event {
    private static final HandlerList handlers = new HandlerList();

    private Destination_Setting      targetDestination;
    private NPC                      owningNPC;

    public Navigation_Failed(NPC npc, Destination_Setting newDestination) {
        targetDestination = newDestination;
        owningNPC = npc;
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
        return targetDestination;
    }
}
