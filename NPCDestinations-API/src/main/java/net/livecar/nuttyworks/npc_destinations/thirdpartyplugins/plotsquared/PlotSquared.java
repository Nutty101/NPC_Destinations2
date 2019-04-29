package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.plotsquared;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

public interface PlotSquared {

    boolean playerHasPermissions(Player plr);

    boolean playerInPlotWithNPC(Player plr, NPC npc);

    boolean locationInSamePlotAsNPC(NPC npc, org.bukkit.Location loc) ;

    int getNPCPlotTime(NPC npc);
}
