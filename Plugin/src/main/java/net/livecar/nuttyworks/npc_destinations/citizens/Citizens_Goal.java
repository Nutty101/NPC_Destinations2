package net.livecar.nuttyworks.npc_destinations.citizens;

import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.EventHandler;

public class Citizens_Goal extends BehaviorGoalAdapter {
    public boolean    forceFinish;
    public int        nFailedPathCount = 0;
    private final NPC npc;

    private Citizens_Goal(NPC npc) {
        this.npc = npc;
    }

    @EventHandler
    public void onFinish(NavigationCompleteEvent event) {
        if (event.getNPC() == this.npc) {
            this.forceFinish = true;
            // NPCDestinations_Trait localNPCDestinations_Trait =
            // (NPCDestinations_Trait)
            // event.getNPC().getTrait(NPCDestinations_Trait.class);
        }
    }

    public void reset() {
        this.forceFinish = false;
    }

    public BehaviorStatus run() {
        if ((!this.npc.getNavigator().isNavigating()) || (this.forceFinish)) {
            return BehaviorStatus.SUCCESS;
        }
        return BehaviorStatus.RUNNING;
    }

    public boolean shouldExecute() {
        return Citizens_Processing.goalAdapter_ShouldExecute(this.npc, this);
    }

    public static Citizens_Goal createWithNPC(NPC npc) {
        return new Citizens_Goal(npc);
    }
}
