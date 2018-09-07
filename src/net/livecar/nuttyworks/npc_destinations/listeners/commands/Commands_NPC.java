package net.livecar.nuttyworks.npc_destinations.listeners.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCTraitCommandAttachEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Navigation_NewDestination;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait.en_RequestedAction;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;
import net.livecar.nuttyworks.npc_destinations.utilities.Utilities;

public class Commands_NPC
{
    @CommandInfo(
            name = "autoset",
            group = "NPC Config Commands",
            helpMessage = "command_autoset_help",
            languageFile = "destinations",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.autoset","npcdestinations.editown.autoset"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_AutoSet(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        // Add the trait to this NPC
        Class<? extends Trait> npcDestClass = CitizensAPI.getTraitFactory().getTraitClass("NPCDestinations");
        if (npcDestClass == null) {
            // Failed to add the trait.. Odd
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        } else if (!npc.hasTrait(npcDestClass)) {
            // Add the trait, and signal other plugins we added the
            // trait incase they care.
            npc.addTrait(npcDestClass);
            Bukkit.getPluginManager().callEvent(new NPCTraitCommandAttachEvent(npc, npcDestClass, sender));
        }
        // Setup the waypoint provider
        Waypoints waypoints = npc.getTrait(Waypoints.class);
        waypoints.setWaypointProvider("NPCDestinations");
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
        
    }

    @CommandInfo(
            name = "info",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_info_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.info","npcdestinations.editown.info"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_Info(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {

        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "----- " + destRef.getDescription().getName() + " ----- V "
                + destRef.getDescription().getVersion());
        destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_settings", destTrait,
                null);

        if (!npc.isSpawned()) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_notspawned");
        } else if (destTrait.NPCLocations.size() > 0) {
            sender.sendMessage(ChatColor.GREEN + "Configured Locations: ");
            for (int nCnt = 0; nCnt < destTrait.NPCLocations.size(); nCnt++) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_location",
                        destTrait, destTrait.NPCLocations.get(nCnt));
            }
        } else {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_nolocations");
        }
        return true;
    }
    
    @CommandInfo(
            name = "process",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_process_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.process","npcdestinations.editown.process"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_Process(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }
        if (destTrait.getRequestedAction() == en_RequestedAction.NO_PROCESSING) {
            destTrait.setRequestedAction(en_RequestedAction.NORMAL_PROCESSING);
        } else {
            destTrait.setRequestedAction(en_RequestedAction.NO_PROCESSING);
        }
        return true;
    } 
    
    @CommandInfo(
            name = "citizens",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_citizens_help",
            arguments = { "--npc|#","<npc>|#","#|Y|N","#|Y|N","Y|N","Y|N","Y|N","Y|N" },
            permission = {"npcdestinations.editall.citizens","npcdestinations.editown.citizens"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 6
            )
    public boolean npcDest_Citizens(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        // parse the commands
        if (inargs.length < 2) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_citizens_settings",
                    destTrait);
            return true;
        }
        // citizens {distancemargin} {pathdistancemargin}
        // {newpathfinder} {swim} {avoidwater}

        try {
            destTrait.citizens_DistanceMargin = Double.parseDouble(inargs[1]);
        } catch (Exception err) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_citizens_invalid");
            return true;
        }

        if (inargs.length > 2)
            try {
                destTrait.citizens_PathDistanceMargin = Double.parseDouble(inargs[2]);
            } catch (Exception err) {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_citizens_invalid");
                return true;
            }

        if (inargs.length > 3) {
            destTrait.citizens_NewPathFinder = inargs[3].equalsIgnoreCase("y");
        }

        if (inargs.length > 4) {
            destTrait.citizens_Swim = inargs[4].equalsIgnoreCase("y");
        }

        if (inargs.length > 5) {
            destTrait.citizens_AvoidWater = inargs[5].equalsIgnoreCase("y");
        }

        if (inargs.length > 6) {
            destTrait.citizens_DefaultStuck = inargs[6].equalsIgnoreCase("y");
        }

        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }    
    
    @CommandInfo(
            name = "goloc",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_goloc_help",
            arguments = { "--npc|#","<npc>","#" },
            permission = {"npcdestinations.editall.goloc","npcdestinations.editown.goloc"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 2
            )
    public boolean npcDest_Goloc(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        if (inargs.length > 1) {
            int nLocNum = -1;

            if (inargs[1].matches("\\d+")) {
                // Location #
                nLocNum = Integer.parseInt(inargs[1]);
                if (nLocNum > destTrait.NPCLocations.size() - 1) {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_goloc_invalid");
                    return true;
                }
            } else {
                // Alias
                for (int nCnt = 0; nCnt < destTrait.NPCLocations.size(); nCnt++) {
                    // V 1.45 -- Added location ID support for this
                    // command.
                    if (destTrait.NPCLocations.get(nCnt).Alias_Name.equalsIgnoreCase(inargs[1])
                            || destTrait.NPCLocations.get(nCnt).LocationIdent.toString().equalsIgnoreCase(
                                    inargs[1])) {
                        // Exists
                        nLocNum = nCnt;
                        break;
                    }
                }
            }
            if (nLocNum > -1) {
                long nLength = 0;
                if (inargs.length == 3) {
                    if (StringUtils.isNumeric(inargs[2])) {
                        nLength = Long.parseLong(inargs[2]) * 1000;
                    } else {
                        destRef.getMessageManager.sendMessage("destinations", sender,
                                "messages.commands_goloc_badargs");
                        return true;
                    }
                } else {
                    nLength = 86400 * 1000; // 1 day
                }

                if (!destTrait.NPCLocations.get(nLocNum).managed_Location.equals("")) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",
                            destTrait, destTrait.NPCLocations.get(nLocNum));
                    return true;
                }

                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_goloc_set",
                        destTrait, destTrait.NPCLocations.get(nLocNum));

                // Notify all plugins that the location has been
                // reached.
                for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
                    if (destTrait.enabledPlugins.contains(plugin.getActionName())) {
                        try {
                            plugin.onNewDestination(npc, destTrait, destTrait.NPCLocations.get(nLocNum));
                        } catch (Exception err) {
                            StringWriter sw = new StringWriter();
                            err.printStackTrace(new PrintWriter(sw));
                            destRef.getMessageManager.consoleMessage(destRef, "destinations",
                                    "Console_Messages.plugin_error", err.getMessage() + "\n" + sw
                                            .toString());
                        }
                    }
                }

                // Fire the navigation event
                Navigation_NewDestination newLocation = new Navigation_NewDestination(npc, destTrait.NPCLocations
                        .get(nLocNum), true);
                Bukkit.getServer().getPluginManager().callEvent(newLocation);
                
                npc.getNavigator().cancelNavigation();
                destTrait.clearPendingDestinations();
                destTrait.lastResult = "Forced location";
                destTrait.setLocation = destTrait.NPCLocations.get(nLocNum);
                destRef.getCitizensProc.fireLocationChangedEvent(destTrait, destTrait.NPCLocations.get(nLocNum));

                destTrait.currentLocation = destTrait.NPCLocations.get(nLocNum);
                destTrait.locationLockUntil = new java.util.Date(System.currentTimeMillis() + nLength);
                destTrait.lastPositionChange = new Date();
                destTrait.setRequestedAction(en_RequestedAction.SET_LOCATION);
                
                return true;
            }
        }
        return false;
    }  
 
    @CommandInfo(
            name = "enableplugin",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_enableplugin_help",
            arguments = { "--npc|<plugin>","<npc>","<plugin>" },
            permission = {"npcdestinations.editall.enableplugin","npcdestinations.editown.enableplugin"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 1
            )
    public boolean npcDest_EnablePlugin(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        if (inargs.length > 1) {
            if (destTrait.enabledPlugins.size() == 0) {
                destTrait.enabledPlugins.add(inargs[1].toUpperCase());
                if (destRef.getPluginManager.getPluginByName(inargs[1].toUpperCase()) != null) {
                    destRef.getPluginManager.getPluginByName(inargs[1].toUpperCase()).onEnableChanged(npc,
                            destTrait, true);
                }
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                return true;
            }
            if (destTrait.enabledPlugins.contains(inargs[1].toUpperCase())) {
                destTrait.enabledPlugins.remove(inargs[1].toUpperCase());
                if (destRef.getPluginManager.getPluginByName(inargs[1].toUpperCase()) != null) {
                    destRef.getPluginManager.getPluginByName(inargs[1].toUpperCase()).onEnableChanged(npc,
                            destTrait, false);
                }
            } else {
                if (destRef.getPluginManager.getPluginByName(inargs[1].toUpperCase()) != null) {
                    destTrait.enabledPlugins.add(inargs[1].toUpperCase());
                    if (destRef.getPluginManager.getPluginByName(inargs[1].toUpperCase()) != null) {
                        destRef.getPluginManager.getPluginByName(inargs[1].toUpperCase()).onEnableChanged(npc,
                                destTrait, true);
                    }
                } else {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_enableplugin_badargs");
                    return true;
                }
            }
            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        }
    
        return false;
    }

    @CommandInfo(
            name = "pauseplayer",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_pauseplayer_help",
            arguments = { "--npc|#","<npc>|#","#"},
            permission = {"npcdestinations.editall.pauseplayer","npcdestinations.editown.pauseplayer"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 3
            )
    public boolean npcDest_PausePlayer(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }
        if (inargs.length == 1) {
            destRef.getMessageManager.sendMessage("destinations", sender,
                    "messages.commands_pauseplayer_badargs", destTrait);
            return true;
        }

        if (inargs.length == 1) {
            destTrait.PauseForPlayers = -1;
            destTrait.PauseTimeout = -1;
            // V1.39 -- Event
            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        } else if (inargs.length == 2) {
            try {
                destTrait.PauseForPlayers = Integer.parseInt(inargs[1]);
                destTrait.PauseTimeout = -1;
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            } catch (Exception e) {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_pauseplayer_badargs", destTrait);
            }
            return true;
        } else if (inargs.length == 3) {
            try {
                destTrait.PauseForPlayers = Integer.parseInt(inargs[1]);
                destTrait.PauseTimeout = Integer.parseInt(inargs[2]);
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            } catch (Exception e) {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_pauseplayer_badargs", destTrait);
            }
            return true;
        }
        return false;
    }
    
    @CommandInfo(
            name = "addblock",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_addblock_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.addblock","npcdestinations.editown.addblock"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_AddBlock(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait);
            return true;
        }

        Player player = (Player) sender;
        try {
            if (destTrait.AllowedPathBlocks.contains(destRef.getMCUtils.getMainHand(player).getType())) {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_addblock_exists", destTrait, null, destRef.getMCUtils.getMainHand(player).getType());
            } else {
                destTrait.AllowedPathBlocks.add(destRef.getMCUtils.getMainHand(player).getType());
            }
        } catch (Exception err) {
            return false;
        }
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }
    
    @SuppressWarnings("deprecation")
    @CommandInfo(
            name = "removeblock",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_removeblock_help",
            arguments = { "--npc|<material>","<npc>","<material>" },
            permission = {"npcdestinations.editall.removeblock","npcdestinations.editown.removeblock"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 1
            )
    public boolean npcDest_RemoveBlock(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        if (inargs.length > 1) {
            if (StringUtils.isNumeric(inargs[1])) {
                destRef.getMessageManager.sendMessage("destinations", sender,"messages.commands_removeblock_badargs", destTrait);
            } else {
                Material material = null;
                try {
                    material = Material.getMaterial(inargs[1]);
                } catch (Exception err) {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_removeblock_badargs", destTrait);
                }
                if (!destTrait.AllowedPathBlocks.contains(material)) {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_removeblock_notinlist", destTrait);
                } else {
                    destTrait.AllowedPathBlocks.remove(material);
                    destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                }
            }
        } else {
            Player player = (Player) sender;
            try {
                if (!destTrait.AllowedPathBlocks.contains(player.getItemInHand().getType())) {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_removeblock_notinlist", destTrait);
                } else {
                    destTrait.AllowedPathBlocks.remove(player.getItemInHand().getType());
                    destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                }
            } catch (Exception err) {
                return false;
            }
        }
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }

    @CommandInfo(
            name = "removeallblocks",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_removeallblocks_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.removeallblocks","npcdestinations.editown.removeallblocks"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_RemoveAllBlocks(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        destTrait.AllowedPathBlocks.clear();
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }

    @CommandInfo(
            name = "opengates",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_opengates_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.opengates","npcdestinations.editown.opengates"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_OpenGates(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        destTrait.OpensGates = !destTrait.OpensGates;
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }

    @CommandInfo(
            name = "openwooddoors",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_openwooddoors_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.openwooddoors","npcdestinations.editown.openwooddoors"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_OpenWoodDoors(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        destTrait.OpensWoodDoors = !destTrait.OpensWoodDoors;
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }
    
    @CommandInfo(
            name = "openmetaldoors",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_openmetaldoors_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.openmetaldoors","npcdestinations.editown.openmetaldoors"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 0
            )
    public boolean npcDest_OpenMetalDoors(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        destTrait.OpensMetalDoors = !destTrait.OpensMetalDoors;
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }
    
    @CommandInfo(
            name = "blocksunder",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_blocksunder_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.blocksunder","npcdestinations.editown.blocksunder"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 1
            )
    public boolean npcDest_BlocksUnder(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        if (inargs.length == 1) {
            destTrait.blocksUnderSurface = 0;
        } else {
            if (Utilities.isNumeric(inargs[1]))
                destTrait.blocksUnderSurface = Integer.parseInt(inargs[1]);
            else
                return false;
        }
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }
    
    @CommandInfo(
            name = "maxprocessing",
            group = "NPC Config Commands",
            languageFile = "destinations",
            helpMessage = "command_maxprocessing_help",
            arguments = { "--npc","<npc>" },
            permission = {"npcdestinations.editall.maxprocessing","npcdestinations.editown.maxprocessing"},
            allowConsole = true,
            minArguments = 0,
            maxArguments = 1
            )
    public boolean npcDest_MaxProcessing(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        if (inargs.length == 1) {
            destTrait.maxProcessingTime = -1;
        } else {
            if (Utilities.isNumeric(inargs[1]))
                destTrait.maxProcessingTime = Integer.parseInt(inargs[1]);
            else
                return false;
        }
        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    }
}