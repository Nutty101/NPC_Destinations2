package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.jobsreborn;

import org.bukkit.Material;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class JobsReborn_Addon extends DestinationsAddon {
    public JobsReborn_Plugin pluginReference = null;

    public JobsReborn_Addon(JobsReborn_Plugin instanceRef) {
        pluginReference = instanceRef;
    }

    @Override
    public String getPluginIcon() {
        return "☎";
    }

    @Override
    public String getActionName() {
        return "JobsReborn";
    }

    @Override
    public String getQuickDescription() {
        String[] response = pluginReference.destRef.getMessageManager.buildMessage("jobsreborn", "jobs_reborn.plugin_description", "");
        return response[0];
    }

    @Override
    public String getDestinationHelp(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        String[] response = pluginReference.destRef.getMessageManager.buildMessage("jobsreborn", null, "jobs_reborn.plugin_destination", npcTrait, location, npc, null, 0, "");
        return response[0];
    }

    public String parseLanguageLine(String message, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material blockMaterial, NPC npc, int ident) {
        if (locationSetting != null) {
            if (!pluginReference.npcSettings.containsKey(npc.getId())) {
                message = message.replaceAll("<location\\.jobname>", "");
                message = message.replaceAll("<location\\.jobmax>", "");
                return message;
            }

            if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(locationSetting.LocationIdent)) {
                JobsReborn_LocationSetting locSetting = pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent);

                if (message.toLowerCase().contains("<location.jobname>"))
                    message = message.replaceAll("<location\\.jobname>", locSetting.jobs_Name);
                if (message.toLowerCase().contains("<location.jobmax>")) {
                    if (locSetting.jobs_Name.trim().equalsIgnoreCase("")) {
                        message = message.replaceAll("<location\\.jobmax>", "Inactive, no job name");
                    } else if (locSetting.jobs_Greater) {
                        if (locSetting.jobs_Max == -1) {
                            message = message.replaceAll("<location\\.jobmax>", "Job at max");
                        } else {
                            message = message.replaceAll("<location\\.jobmax>", "More than " + Integer.toString(locSetting.jobs_Max) + " players.");
                        }
                    } else {
                        if (locSetting.jobs_Max == -1) {
                            message = message.replaceAll("<location\\.jobmax>", "Less the max users");
                        } else {
                            message = message.replaceAll("<location\\.jobmax>", "Less than " + Integer.toString(locSetting.jobs_Max) + " players.");
                        }
                    }
                }
            } else {
                message = message.replaceAll("<location\\.jobname>", "");
                message = message.replaceAll("<location\\.jobmax>", "");
            }
        }
        return message;
    }

    public boolean isDestinationEnabled(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        if (location != null) {
            if (!pluginReference.npcSettings.containsKey(npc.getId()))
                return true;

            if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent)) {
                JobsReborn_LocationSetting locSetting = pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent);

                if (!locSetting.jobs_Name.trim().equalsIgnoreCase("")) {
                    // If at max slots and set to deny, remove location from
                    // list
                    if (locSetting.jobs_Max == -1) {
                        return !pluginReference.JobAtMax(locSetting.jobs_Name) || locSetting.jobs_Greater;
                    } else {
                        if (pluginReference.JobCount(locSetting.jobs_Name) >= locSetting.jobs_Max && !locSetting.jobs_Greater) {
                            return false;
                        }
                        return pluginReference.JobCount(locSetting.jobs_Name) > locSetting.jobs_Max || !locSetting.jobs_Greater;
                    }
                }
            }
        }
        return true;
    }

    public void onLocationLoading(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!storageKey.keyExists("JobsReborn"))
            return;

        JobsReborn_NPCSetting npcSetting;
        if (!pluginReference.npcSettings.containsKey(npc.getId())) {
            npcSetting = new JobsReborn_NPCSetting();
            npcSetting.setNPC(npc.getId());
            pluginReference.npcSettings.put(npc.getId(), npcSetting);
        } else {
            npcSetting = pluginReference.npcSettings.get(npc.getId());
        }

        JobsReborn_LocationSetting locationConfig = new JobsReborn_LocationSetting();
        locationConfig.jobs_Name = storageKey.getString("JobsReborn.JobName", "");
        locationConfig.jobs_Max = storageKey.getInt("JobsReborn.JobMax", 0);
        if (locationConfig.jobs_Max < 0) {
            locationConfig.jobs_Greater = false;
            locationConfig.jobs_Max = Math.abs(locationConfig.jobs_Max);
        } else {
            locationConfig.jobs_Greater = true;
            locationConfig.jobs_Max = Math.abs(locationConfig.jobs_Max);
        }
        locationConfig.locationID = location.LocationIdent;
        npcSetting.locations.put(location.LocationIdent, locationConfig);
        return;
    }

    public void onLocationSaving(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!pluginReference.npcSettings.containsKey(npc.getId()))
            return;
        if (!pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent))
            return;

        JobsReborn_LocationSetting locationConfig = pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent);

        if (locationConfig.locationID != null) {
            storageKey.setString("JobsReborn.JobName", locationConfig.jobs_Name);
            storageKey.setInt("JobsReborn.JobMax", locationConfig.jobs_Greater ? locationConfig.jobs_Max : 0 - locationConfig.jobs_Max);
        }
        return;
    }

    public boolean onNavigationReached(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        return false;
    }

    public boolean onNewDestination(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        return false;
    }

    public void onEnableChanged(NPC npc, NPCDestinationsTrait npcTrait, boolean enabled) {
        return;
    }
}
