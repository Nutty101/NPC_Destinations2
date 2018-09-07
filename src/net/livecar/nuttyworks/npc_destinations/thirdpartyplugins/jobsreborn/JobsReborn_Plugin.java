package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.jobsreborn;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class JobsReborn_Plugin {
    public DestinationsPlugin                  destRef       = null;
    public JobsReborn_Addon                    getJobsPlugin = null;
    public Map<Integer, JobsReborn_NPCSetting> npcSettings   = new HashMap<Integer, JobsReborn_NPCSetting>();

    @SuppressWarnings("deprecation")
    public JobsReborn_Plugin(DestinationsPlugin storageRef) {
        destRef = storageRef;
        this.getJobsPlugin = new JobsReborn_Addon(this);
        DestinationsPlugin.Instance.getPluginManager.registerPlugin(getJobsPlugin);
        destRef.getCommandManager.registerCommandClass(JobsReborn_Commands.class);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(destRef, new BukkitRunnable() {
            @Override
            public void run() {
                onStart();
            }
        });
    }

    private void onStart() {
        // Not listening at this moment for the events. Leaving this empty.
    }

    public int JobCount(String jobName) {
        if (JobExists(jobName)) {
            return Jobs.getJob(jobName).getTotalPlayers();
        } else {
            return -1;
        }
    }

    public boolean JobAtMax(String jobName) {
        if (JobExists(jobName)) {
            return (Jobs.getJob(jobName).getTotalPlayers() >= Jobs.getJob(jobName).getMaxSlots());
        } else {
            return true;
        }
    }

    public boolean JobExists(String jobName) {
        for (Job oJob : Jobs.getJobs()) {
            if (oJob.getName().equalsIgnoreCase(jobName))
                return true;
        }
        return false;
    }
}
