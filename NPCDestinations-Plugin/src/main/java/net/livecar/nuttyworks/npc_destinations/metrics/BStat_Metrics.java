package net.livecar.nuttyworks.npc_destinations.metrics;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

public class BStat_Metrics {

    // Private Junk
    private Metrics            metrics = null;
    private DestinationsPlugin destRef = null;

    public BStat_Metrics(DestinationsPlugin storageRef) {
        destRef = storageRef;
    }

    public void Start() {
        metrics = new Metrics(destRef,905);

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
            metrics.addCustomChart(new Metrics.SingleLineChart("npc_count", new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return returnNpcCount;
                }
            }));
        } catch (Exception e) {
            // Wheee no stats, oh well.
        }

        try {
            metrics.addCustomChart(new Metrics.SingleLineChart("npcs_managed", new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return returnAssignedCount;
                }
            }));
        } catch (Exception e) {
            // Wheee no stats, oh well.
        }

        try {
            metrics.addCustomChart(new Metrics.AdvancedPie("addons_used", new Callable<Map<String, Integer>>() {
                @Override
                public Map<String, Integer> call() throws Exception {
                    Map<String, Integer> valueMap = new HashMap<>();
                    for (DestinationsAddon pluginReference : destRef.getPluginManager.getPlugins()) {
                        valueMap.put(pluginReference.getActionName(), Integer.valueOf(1));
                    }
                    
                    if (destRef.getPlotSquared != null)
                        valueMap.put("PlotSquared", Integer.valueOf(1));
    
                    if (destRef.getJobsRebornPlugin != null)
                        valueMap.put("JobsReborn", Integer.valueOf(1));
    
                    if (destRef.getLightPlugin != null)
                        valueMap.put("LightAPI", Integer.valueOf(1));
    
                    if (destRef.getBetonQuestPlugin != null)
                        valueMap.put("BentonQuest", Integer.valueOf(1));
    
                    return valueMap;
                }
            }));
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
        }
    }
}
