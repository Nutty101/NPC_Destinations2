package net.livecar.nuttyworks.npc_destinations.metrics;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class BStat_Metrics {

    // Private Junk
    private Metrics            metrics = null;
    private DestinationsPlugin destRef = null;

    public BStat_Metrics(DestinationsPlugin storageRef) {
        destRef = storageRef;
    }

    public void Start() {
        metrics = new Metrics(destRef);

        int npcCount = 0;
        int assignedCount = 0;

        for (Iterator<NPC> npcIter = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().iterator(); npcIter.hasNext();) {
            NPC oTmpNPC = npcIter.next();
            npcCount++;
            if ((oTmpNPC != null) && (oTmpNPC.hasTrait(NPCDestinationsTrait.class))) {
                assignedCount++;
            }
        }

        final int returnNpcCount = npcCount;
        final int returnAssignedCount = assignedCount;

        try {
            metrics.addCustomChart(new Metrics.SingleLineChart("npc_count") {

                @Override
                public int getValue() {
                    return returnNpcCount;
                }
            });
        } catch (Exception e) {
            // Wheee no stats, oh well.
        }

        try {
            metrics.addCustomChart(new Metrics.SingleLineChart("npcs_managed") {

                @Override
                public int getValue() {
                    return returnAssignedCount;
                }
            });
        } catch (Exception e) {
            // Wheee no stats, oh well.
        }

        try {
            metrics.addCustomChart(new Metrics.AdvancedPie("addons_used") {
                public HashMap<String, Integer> getValues(HashMap<String, Integer> map) {
                    for (DestinationsAddon pluginReference : destRef.getPluginManager.getPlugins()) {
                        map.put(pluginReference.getActionName(), Integer.valueOf(1));
                    }
                    return map;
                }
            });
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
        }
    }
}
