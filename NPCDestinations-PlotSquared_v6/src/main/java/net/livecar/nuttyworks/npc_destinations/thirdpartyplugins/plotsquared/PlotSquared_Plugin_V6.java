package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.plotsquared;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.TimeFlag;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

public class PlotSquared_Plugin_V6 implements PlotSquared {

    private PlotAPI plotAPI = new PlotAPI(); //For some reason you gotta instantiate?

    public boolean playerHasPermissions(Player player) {
        if (player.hasPermission("plots.destinations.bypass"))
            return true;

        PlotPlayer<?> plotPlayer = this.plotAPI.wrapPlayer(player.getUniqueId());

        if (plotPlayer == null) {
            return false;
        }

        if (plotPlayer.getCurrentPlot() != null) {
            Plot curPlot = plotPlayer.getCurrentPlot();
            if (curPlot.isOwner(player.getUniqueId()))
                return true;
            if (curPlot.getTrusted().contains(player.getUniqueId()))
                return true;
            return curPlot.getMembers().contains(player.getUniqueId());
        }

        return false;
    }

    public boolean playerInPlotWithNPC(Player plr, NPC npc) {

        if (plr.hasPermission("plots.destinations.bypass"))
            return true;

        Plot playerPlot = Plot.getPlot(locationToPlot(plr.getLocation()));
        Plot npcPlot = null;

        if (npc.isSpawned())
            npcPlot = Plot.getPlot(locationToPlot(npc.getEntity().getLocation()));
        else
            npcPlot = Plot.getPlot(locationToPlot(npc.getStoredLocation()));

        if (npcPlot == null && playerPlot == null)
            return false;

        return npcPlot.getId().toCommaSeparatedString().equals(playerPlot.getId().toCommaSeparatedString());

    }

    public boolean locationInSamePlotAsNPC(NPC npc, org.bukkit.Location loc) {
        Plot npcPlot = null;
        if (npc.isSpawned())
            npcPlot = Plot.getPlot(locationToPlot(npc.getEntity().getLocation()));
        else
            npcPlot = Plot.getPlot(locationToPlot(npc.getStoredLocation()));

        Plot locationPlot = Plot.getPlot(locationToPlot(loc));

        if (locationPlot == null || npcPlot == null)
            return true;

        return npcPlot.getId().toCommaSeparatedString().equals(locationPlot.getId().toCommaSeparatedString());

    }

    public int getNPCPlotTime(NPC npc) {
        Plot npcPlot;
        if (npc.isSpawned())
            npcPlot = Plot.getPlot(locationToPlot(npc.getEntity().getLocation()));
        else
            npcPlot = Plot.getPlot(locationToPlot(npc.getStoredLocation()));

        if (npcPlot == null) {
            //TODO change for getting local time
            return ((Long) npc.getEntity().getWorld().getTime()).intValue();
        }

        Long timeFlag = npcPlot.getFlag(TimeFlag.class);
        if (!(timeFlag.equals(TimeFlag.TIME_DISABLED.getValue()))) return timeFlag.intValue();
        return ((Long) npc.getEntity().getWorld().getTime()).intValue();
    }

    private Location locationToPlot(org.bukkit.Location loc) {
        return BukkitUtil.adapt(loc);
    }
}
