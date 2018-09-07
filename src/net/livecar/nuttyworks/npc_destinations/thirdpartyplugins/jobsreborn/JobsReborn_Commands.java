package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.jobsreborn;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Location_Updated;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.CommandInfo;
import net.livecar.nuttyworks.npc_destinations.utilities.Utilities;

public class JobsReborn_Commands {
    @CommandInfo(
            name = "locjobs",
            group = "External Plugin Commands",
            languageFile = "jobsreborn",
            helpMessage = "command_locjobs_help",
            arguments = { "--npc|#","<npc>|>|<|clear",">|<|clear" },
            permission = {"npcdestinations.editall.locjobs","npcdestinations.editown.locjobs"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 20
            )
    public boolean npcDest_locjobs(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (!sender.hasPermission("npcdestinations.editall.locjobs") && !sender.isOp() && !(isOwner && sender.hasPermission("npcdestinations.editown.locjobs"))) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.no_permissions");
            return true;
        } else {
            if (npc == null) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
                return true;
            }

            
            if (inargs.length > 2) {
                int nIndex = Integer.parseInt(inargs[1]);
                if (nIndex > destTrait.NPCLocations.size() - 1) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                    return true;
                }

                if (!destTrait.NPCLocations.get(nIndex).managed_Location.equals("")) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",destTrait, destTrait.NPCLocations.get(nIndex));
                    return true;
                }

                //Get the location
                JobsReborn_LocationSetting locSetting = null;

                JobsReborn_Addon addonReference = (JobsReborn_Addon)destRef.getPluginManager.getPluginByName("JOBSREBORN");
                
                if (!addonReference.pluginReference.npcSettings.containsKey(npc.getId()))
                    addonReference.pluginReference.npcSettings.put(npc.getId(), new JobsReborn_NPCSetting());
                
                if (addonReference.pluginReference.npcSettings.get(npc.getId()).locations.containsKey(destTrait.NPCLocations.get(nIndex).LocationIdent)) 
                    locSetting = addonReference.pluginReference.npcSettings.get(npc.getId()).locations.get(destTrait.NPCLocations.get(nIndex).LocationIdent);
                
                
                if (inargs[2].equalsIgnoreCase("clear")) {
                    if (locSetting != null)
                    {
                        addonReference.pluginReference.npcSettings.get(npc.getId()).locations.remove(destTrait.NPCLocations.get(nIndex).LocationIdent);
                        
                        // V1.39 -- Event
                        Location_Updated changeEvent = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
                        Bukkit.getServer().getPluginManager().callEvent(changeEvent);
                    }
                    
                    destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                    return true;
                }
                
                if (locSetting == null)
                {
                    locSetting = new JobsReborn_LocationSetting();
                    locSetting.locationID = destTrait.NPCLocations.get(nIndex).LocationIdent;
                    addonReference.pluginReference.npcSettings.get(npc.getId()).locations.put(destTrait.NPCLocations.get(nIndex).LocationIdent, locSetting);
                }
                
                if (inargs[2].equalsIgnoreCase(">")) {
                    locSetting.jobs_Greater = true;
                } else if (inargs[2].equalsIgnoreCase("<")) {
                    locSetting.jobs_Greater = false;
                } else {
                    return false;
                }
                
                if (Utilities.tryParseInt(inargs[3])) {
                    locSetting.jobs_Max = Integer.parseInt(inargs[3]);
                } else {
                    return false;
                }

                String sJobName = "";
                for (int i = 4; i < inargs.length; i++) {
                    sJobName += inargs[i] + " ";
                }
                if (destRef.getJobsRebornPlugin != null && destRef.getJobsRebornPlugin.JobExists(sJobName.trim())) {
                    locSetting.jobs_Name = sJobName.trim();
                } else {
                    return false;
                }

                // V1.39 -- Event
                Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
                Bukkit.getServer().getPluginManager().callEvent(changedLocation);

                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                return true;
            } 
        }
        return false;
    }

    
}
