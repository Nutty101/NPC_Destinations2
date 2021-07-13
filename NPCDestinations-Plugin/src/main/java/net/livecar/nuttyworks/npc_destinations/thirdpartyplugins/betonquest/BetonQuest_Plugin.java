package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.event.Listener;


public class BetonQuest_Plugin implements Listener, BetonQuest_Interface {
    static DestinationsPlugin destRef = null;

    @SuppressWarnings("deprecation")
    public BetonQuest_Plugin(DestinationsPlugin storageRef) {
        destRef = storageRef;

        BetonQuest_Plugin.this.onStart();
    }

    private void onStart() {
        BetonQuest.getInstance().registerEvents("npcdest_goloc", Event_goloc.class);
        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.betonquest_events", "npcdest_goloc");

        BetonQuest.getInstance().registerConditions("npcdest_currentlocation", Condition_CurrentLocation.class);
        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.betonquest_conditions", "npcdest_currentlocation");
        BetonQuest.getInstance().registerConditions("npcdest_distancetolocation", Condition_DistanceToLocation.class);
        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.betonquest_conditions", "npcdest_distancetolocation");
    }
}
