package net.livecar.nuttyworks.npc_destinations.metrics;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class BStat_Metrics {

    private Metrics metrics = null;
    private DestinationsPlugin destRef;

    public BStat_Metrics(DestinationsPlugin storageRef) {
        destRef = storageRef;
    }

    public void Start() {
        metrics = new Metrics(destRef, 905);

        int npcCount = 0;
        int assignedCount = 0;

        for (NPC oTmpNPC : net.citizensnpcs.api.CitizensAPI.getNPCRegistry()) {
            npcCount++;
            if ((oTmpNPC != null) && (oTmpNPC.hasTrait(NPCDestinationsTrait.class))) {
                assignedCount++;
            }
        }

        final int returnNpcCount = npcCount;
        final int returnAssignedCount = assignedCount;

        try {
            metrics.addCustomChart(new SingleLineChart("npc_count", () -> returnNpcCount));
        } catch (Exception e) {
            // Wheee no stats, oh well.
        }

        try {
            metrics.addCustomChart(new SingleLineChart("npcs_managed", () -> returnAssignedCount));
        } catch (Exception e) {
            // Wheee no stats, oh well.
        }

        try {
            metrics.addCustomChart(new AdvancedPie("addons_used", () -> {
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
            }));
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
        }
    }
}
