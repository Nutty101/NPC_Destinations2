package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.plotsquared;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

public class PlotSquared_Plugin_V3 implements PlotSquared{

    public boolean playerHasPermissions(Player plr) {
        if (plr.hasPermission("plots.destinations.bypass"))
            return true;

        PlotPlayer pplr = PlotPlayer.wrap(plr);

        if (pplr.getCurrentPlot() != null) {
            Plot curPlot = pplr.getCurrentPlot();
            if (curPlot.isOwner(plr.getUniqueId()))
                return true;
            if (curPlot.getTrusted().contains(plr.getUniqueId()))
                return true;
            return curPlot.getMembers().contains(plr.getUniqueId());
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
        Plot npcPlot = null;
        if (npc.isSpawned())
            npcPlot = Plot.getPlot(locationToPlot(npc.getEntity().getLocation()));
        else
            npcPlot = Plot.getPlot(locationToPlot(npc.getStoredLocation()));

        if (npcPlot == null)
            return ((Long) npc.getEntity().getWorld().getTime()).intValue();

        if (npcPlot.hasFlag(Flags.TIME)) {
            Optional<Long> timeFlag = npcPlot.getFlag(Flags.TIME);
            return timeFlag.get().intValue();
        }
        return ((Long) npc.getEntity().getWorld().getTime()).intValue();

    }

    private com.intellectualcrafters.plot.object.Location locationToPlot(org.bukkit.Location loc) {
        return com.plotsquared.bukkit.util.BukkitUtil.getLocation(loc);
    }

}
