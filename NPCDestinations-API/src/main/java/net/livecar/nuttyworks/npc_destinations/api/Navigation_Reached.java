package net.livecar.nuttyworks.npc_destinations.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.citizensnpcs.api.npc.NPC;

public class Navigation_Reached extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Destination_Setting      targetDestination;
    private NPC                      owningNPC;
    private boolean                  cancelEvent;

    public Navigation_Reached(NPC npc, Destination_Setting newDestination) {
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

    @Override
    public void setCancelled(boolean cancel) {
        cancelEvent = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelEvent;
    }

    public NPC getNPC() {
        return owningNPC;
    }

    public Destination_Setting getDestination() {
        return targetDestination;
    }
}
