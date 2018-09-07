package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.sentinel;

import org.bukkit.command.CommandSender;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.CommandInfo;

public class Sentinel_Commands {
    @CommandInfo(name = "locsentinel", group = "External Plugin Commands", languageFile = "sentinel", helpMessage = "command_locsentinel_help", arguments = { "--npc|#", "<npc>|set|get|clear", "set|get|clear" }, permission = {
            "npcdestinations.editall.locsentinel", "npcdestinations.editown.locsentinel" }, allowConsole = true, minArguments = 2, maxArguments = 2)
    public boolean npcDest_locSentinel(DestinationsPlugin destRef, CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait) {
        if (!sender.hasPermission("npcdestinations.editall.locsentinel") && !sender.isOp() && !(isOwner && sender.hasPermission("npcdestinations.editown.locsentinel"))) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.no_permissions");
            return true;
        } else {
            if (npc == null) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
                return true;
            }

            if (inargs.length != 3) {
                return false;
            }
            if (inargs.length > 2) {
                int nIndex = Integer.parseInt(inargs[1]);
                if (nIndex > destTrait.NPCLocations.size() - 1) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                    return true;
                }

                if (!destTrait.NPCLocations.get(nIndex).managed_Location.equals("")) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait, destTrait.NPCLocations.get(nIndex));
                    return true;
                }

                // Get the location
                Sentinel_LocationSetting locSetting = null;
                Sentinel_Addon addonReference = (Sentinel_Addon) destRef.getPluginManager.getPluginByName("Sentinel");

                if (!addonReference.npcSettings.containsKey(npc.getId()))
                    addonReference.npcSettings.put(npc.getId(), new Sentinel_NPCSetting());

                if (addonReference.npcSettings.get(npc.getId()).locations.containsKey(destTrait.NPCLocations.get(nIndex).LocationIdent))
                    locSetting = addonReference.npcSettings.get(npc.getId()).locations.get(destTrait.NPCLocations.get(nIndex).LocationIdent);

                if (inargs[2].equalsIgnoreCase("clear")) {
                    if (locSetting != null) {
                        addonReference.npcSettings.get(npc.getId()).locations.remove(destTrait.NPCLocations.get(nIndex).LocationIdent);
                    }
                    destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                    return true;
                }

                if (inargs[2].equalsIgnoreCase("set")) {
                    addonReference.npcSettings.get(npc.getId()).locations.remove(destTrait.NPCLocations.get(nIndex).LocationIdent);
                    locSetting = addonReference.pluginReference.getCurrentSettings(npc);
                    locSetting.locationID = destTrait.NPCLocations.get(nIndex).LocationIdent;
                    addonReference.npcSettings.get(npc.getId()).locations.put(destTrait.NPCLocations.get(nIndex).LocationIdent, locSetting);
                    destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                    return true;
                }

                if (locSetting == null) {
                    locSetting = new Sentinel_LocationSetting();
                    locSetting.locationID = destTrait.NPCLocations.get(nIndex).LocationIdent;
                    addonReference.npcSettings.get(npc.getId()).locations.put(destTrait.NPCLocations.get(nIndex).LocationIdent, locSetting);
                }

                if (inargs[2].equalsIgnoreCase("get")) {
                    addonReference.pluginReference.setCurrentSettings(npc, locSetting);
                    destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                    return true;
                }
            }
        }
        return false;
    }
}
