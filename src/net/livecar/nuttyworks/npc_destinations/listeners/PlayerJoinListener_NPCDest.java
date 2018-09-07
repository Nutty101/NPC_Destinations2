package net.livecar.nuttyworks.npc_destinations.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class PlayerJoinListener_NPCDest implements org.bukkit.event.Listener {

    private DestinationsPlugin destRef = null;

    public PlayerJoinListener_NPCDest(DestinationsPlugin storageRef) {
        destRef = storageRef;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        // Remove this player from the debug if they are in it
        destRef.debugTargets.remove(event.getPlayer());
    }
}
