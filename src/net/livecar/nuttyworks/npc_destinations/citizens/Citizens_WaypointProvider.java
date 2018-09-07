package net.livecar.nuttyworks.npc_destinations.citizens;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Citizens_WaypointProvider implements WaypointProvider {
    private Goal             currentGoal;
    private NPC              npc;
    private volatile boolean paused;

    public WaypointEditor createEditor(CommandSender sender, CommandContext args) {
        return new WaypointEditor() {
            public void begin() {
            }

            public void end() {
            }
        };
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void load(DataKey key) {
    }

    public void onRemove() {
        this.npc.getDefaultGoalController().removeGoal(this.currentGoal);
    }

    public void onSpawn(NPC npc) {
        this.npc = npc;
        NPCDestinationsTrait trait = null;
        if (!npc.hasTrait(NPCDestinationsTrait.class)) {
            // Npc has not been assigned a location
            Bukkit.getLogger().log(java.util.logging.Level.INFO, "NPC [" + npc.getId() + "/" + npc.getName() + "] auto adding the NPCDestinations trait as the waypoint provider was added");
            npc.addTrait(NPCDestinationsTrait.class);
            trait = npc.getTrait(NPCDestinationsTrait.class);
        } else {
            trait = npc.getTrait(NPCDestinationsTrait.class);
        }

        if (trait.TeleportOnFailedStartLoc == null)
            trait.TeleportOnFailedStartLoc = true;
        if (trait.TeleportOnNoPath == null)
            trait.TeleportOnNoPath = true;

        if (this.currentGoal == null) {
            this.currentGoal = Citizens_Goal.createWithNPC(npc);
        }
        npc.getDefaultGoalController().addGoal(this.currentGoal, 1);
    }

    public void save(DataKey key) {
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}