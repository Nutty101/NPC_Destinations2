package net.livecar.nuttyworks.npc_destinations.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.citizensnpcs.api.npc.NPC;

public class Navigation_NewDestination extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Destination_Setting      targetDestination;
    private NPC                      owningNPC;
    private boolean                  cancelEvent;
    private boolean                  forcedEvent;

    public Navigation_NewDestination(NPC npc, Destination_Setting newDestination, boolean forced) {
        targetDestination = newDestination;
        owningNPC = npc;
        forcedEvent = forced;
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

    public boolean isForced() {
        return forcedEvent;
    }

    public Destination_Setting getDestination() {
        return targetDestination;
    }
}
