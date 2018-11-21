package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;

public class BetonQuest_Plugin_V1_9 implements Listener, BetonQuest_Interface {
    static DestinationsPlugin destRef = null;

    @SuppressWarnings("deprecation")
    public BetonQuest_Plugin_V1_9(DestinationsPlugin storageRef) {
        destRef = storageRef;

        String[] versionParts = destRef.getServer().getPluginManager().getPlugin("BetonQuest").getDescription().getVersion().split("\\.");

        if (versionParts.length == 3) {
            int verID = Integer.parseInt(versionParts[2]);
            if (verID < 4) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(storageRef, new BukkitRunnable() {
                    public void run() {
                        BetonQuest_Plugin_V1_9.this.onStart();
                    }
                });

            } else {
                BetonQuest_Plugin_V1_9.this.onStart();
            }
        } else if (versionParts.length > 1) {
            if (versionParts[0].equals("1") && versionParts[1].startsWith("10")) {
                BetonQuest_Plugin_V1_9.this.onStart();
            } else if (versionParts[0].equals("1") && versionParts[1].startsWith("9")) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(storageRef, new BukkitRunnable() {
                    public void run() {
                        BetonQuest_Plugin_V1_9.this.onStart();
                    }
                });
            }
        }
    }

    private void onStart() {
        BetonQuest.getInstance().registerEvents("npcdest_goloc", Event_goloc_V1_9.class);
        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.betonquest_events", "npcdest_goloc");

        BetonQuest.getInstance().registerConditions("npcdest_currentlocation", Condition_CurrentLocation_V1_9.class);
        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.betonquest_conditions", "npcdest_currentlocation");
        BetonQuest.getInstance().registerConditions("npcdest_distancetolocation", Condition_DistanceToLocation_V1_9.class);
        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.betonquest_conditions", "npcdest_distancetolocation");
    }
}
