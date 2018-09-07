package net.livecar.nuttyworks.npc_destinations.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.citizensnpcs.api.npc.NPC;

public class Location_Added extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Destination_Setting      destinationChanged;
    private NPC                      owningNPC;
    private boolean                  cancelEvent;

    public Location_Added(NPC changedNPC, Destination_Setting changedDestination) {
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

    @Override
    public void setCancelled(boolean cancel) {
        cancelEvent = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelEvent;
    }

    public Destination_Setting getDestination() {
        return destinationChanged;
    }
}
