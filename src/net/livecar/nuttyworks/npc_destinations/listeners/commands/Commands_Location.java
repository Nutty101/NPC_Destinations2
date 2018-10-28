package net.livecar.nuttyworks.npc_destinations.listeners.commands;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.api.Location_Added;
import net.livecar.nuttyworks.npc_destinations.api.Location_Deleted;
import net.livecar.nuttyworks.npc_destinations.api.Location_Updated;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.utilities.Utilities;

public class Commands_Location
{

    @CommandInfo(
            name = "loccommands",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_loccommands_help",
            arguments = { "--npc|#","<npc>|#","#" },
            permission = {"npcdestinations.editall.loccommands","npcdestinations.editown.loccommands"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 1
            )
    public boolean npcDest_LocCommands(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (!npc.isSpawned()) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_notspawned");
        } else if (destTrait.NPCLocations.size() > 0) {
            if (inargs.length == 2) {
                int nIndex = Integer.parseInt(inargs[1]);
                if (nIndex > destTrait.NPCLocations.size() - 1) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                    return true;
                }
                Destination_Setting oCurLoc = destTrait.NPCLocations.get(nIndex);
                if (oCurLoc == null) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_nolocations");
                } else {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_header", destTrait, oCurLoc, 0);
                    for (int nCnt = 0; nCnt < oCurLoc.arrival_Commands.size(); nCnt++) {
                        destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_detail", destTrait, oCurLoc, nCnt);
                    }
                }
            }
        }
        return true;
    }
  
    @CommandInfo(
            name = "locaddcmd",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_locaddcmd_help",
            arguments = { "--npc|#","<npc>|#","#","#" },
            permission = {"npcdestinations.editall.locaddcmd","npcdestinations.editown.locaddcmd"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 999
            )
    public boolean npcDest_LocAddCmd(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        if (!npc.isSpawned()) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_notspawned");
        } else if (destTrait.NPCLocations.size() > 0) {
            if (inargs.length == 2) {
                int nIndex = Integer.parseInt(inargs[1]);
                if (nIndex > destTrait.NPCLocations.size() - 1) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                    return true;
                }
                Destination_Setting oCurLoc = destTrait.NPCLocations.get(nIndex);
                if (!oCurLoc.managed_Location.equals("")) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait, oCurLoc);
                    return true;
                }
                
                if (!(sender instanceof Player))
                    return false;
                                    
                Player plr = (Player) sender;  
                
                if (!destRef.getMCUtils.isHoldingBook(plr))  
                    return false;

                BookMeta meta = (BookMeta) destRef.getMCUtils.getMainHand(plr).getItemMeta();
                String commandString = "";
                for (int pageNum = 1; pageNum <= meta.getPageCount(); pageNum++)
                    commandString += meta.getPage(1).trim();
                oCurLoc.arrival_Commands.add(commandString);
                Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
                Bukkit.getServer().getPluginManager().callEvent(changedLocation);
                
                destRef.getCommandManager.onCommand(sender, new String[] { "loccommands", "--npc", Integer.toString(npc.getId()), inargs[1] });
                return true;
            } else if (inargs.length > 2) {
                int nIndex = Integer.parseInt(inargs[1]);
                if (nIndex > destTrait.NPCLocations.size() - 1) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_badargs");
                    return true;
                }
                Destination_Setting oCurLoc = destTrait.NPCLocations.get(nIndex);
                if (oCurLoc == null) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_nolocations");
                } else {

                    if (!oCurLoc.managed_Location.equals("")) {
                        destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait, oCurLoc);
                        return true;
                    }

                    String commandString = "";

                    for (int nCnt = 2; nCnt < inargs.length; nCnt++) {
                        commandString += inargs[nCnt] + " ";
                    }
                    oCurLoc.arrival_Commands.add(commandString.trim());
                    Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
                    Bukkit.getServer().getPluginManager().callEvent(changedLocation);
                    destRef.getCommandManager.onCommand(sender, new String[] { "loccommands", "--npc", Integer.toString(npc.getId()), inargs[1] });
                    return true;
                }
            }
        }
        return false;
        
    }

    
    @CommandInfo(
            name = "locdelcmd",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_locdelcmd_help",
            arguments = { "--npc|#","<npc>|#","#","#" },
            permission = {"npcdestinations.editall.locdelcmd","npcdestinations.editown.locdelcmd"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 2
            )
    public boolean npcDest_LocDelCommand(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        if (!npc.isSpawned()) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_notspawned");
        } else if (destTrait.NPCLocations.size() > 0) {
            if (inargs.length == 3) {
                int nIndex = Integer.parseInt(inargs[1]);
                if (nIndex > destTrait.NPCLocations.size() - 1) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                    return true;
                }
                Destination_Setting oCurLoc = destTrait.NPCLocations.get(nIndex);
                if (oCurLoc == null) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_info_nolocations");
                } else {

                    if (!oCurLoc.managed_Location.equals("")) {
                        destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait, oCurLoc);
                        return true;
                    }

                    int listIndex = Integer.parseInt(inargs[2]);
                    if (listIndex > oCurLoc.arrival_Commands.size() - 1) {
                        destRef.getMessageManager.sendMessage("destinations", sender,
                                "messages.commands_commands_badargs");
                        return true;
                    }
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_commands_delete", destTrait, oCurLoc, 0);
                    oCurLoc.arrival_Commands.remove(listIndex);
                    Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(
                            nIndex));
                    Bukkit.getServer().getPluginManager().callEvent(changedLocation);
                    destRef.getCommandManager.onCommand(sender, new String[] { "loccommands", "--npc", Integer.toString(npc.getId()), inargs[1] });
                    return true;
                }
            }
        }
        return false;

    }
    
    @CommandInfo(
            name = "locweather",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_locweather_help",
            arguments = { "--npc|#|clear|storm|any","<npc>|#|clear|storm|any","#|clear|storm|any","clear|storm|any" },
            permission = {"npcdestinations.editall.locweather","npcdestinations.editown.locweather"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 2
            )
    public boolean npcDest_LocWeather(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
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
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",
                        destTrait, destTrait.NPCLocations.get(nIndex));
                return true;
            }

            if (inargs[2].equalsIgnoreCase("clear")) {
                destTrait.NPCLocations.get(nIndex).WeatherFlag = 1;
            } else if (inargs[2].equalsIgnoreCase("storm")) {
                destTrait.NPCLocations.get(nIndex).WeatherFlag = 2;
            } else if (inargs[2].equalsIgnoreCase("any")) {
                destTrait.NPCLocations.get(nIndex).WeatherFlag = 0;
            } else {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_locweather_badargs");
                return true;
            }
            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        } else {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_locweather_badargs");
            return true;
        }
        
    }

    @CommandInfo(
            name = "localias",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_localias_help",
            arguments = { "--npc|#","<npc>|#"},
            permission = {"npcdestinations.editall.localias","npcdestinations.editown.localias"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 2
            )
    public boolean npcDest_LocAlias(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {

        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        if (inargs.length > 2) {
            // Loop the NPC and see if we have an alias
            for (Destination_Setting oDestination : destTrait.NPCLocations) {
                if (oDestination.Alias_Name.equalsIgnoreCase(inargs[2])) {
                    // Exists
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_localias_duplicate");
                    return true;
                }
            }

            if (!StringUtils.isNumeric(inargs[1])) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_localias_badargs");
                return true;
            }

            int nIndex = Integer.parseInt(inargs[1]);
            if (nIndex > destTrait.NPCLocations.size() - 1) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                return true;
            }

            if (!destTrait.NPCLocations.get(nIndex).managed_Location.equals("")) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait, destTrait.NPCLocations.get(nIndex));
                return true;
            }

            destTrait.NPCLocations.get(nIndex).Alias_Name = inargs[2];

            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        } 
        return false;
    }
    
    @CommandInfo(
            name = "addlocation",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_addlocation_help",
            arguments = { "--npc|sunrise|sunset|never|#","sunrise|sunset|never|#|<npc>"},
            permission = {"npcdestinations.editall.localias","npcdestinations.editown.localias"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 1
            )
    public boolean npcDest_AddLocation(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait);
            return true;
        }

        Player player = (Player) sender;
        if (inargs.length > 1) {
            int nTimeOfDay = 0;
            if (inargs[1].equalsIgnoreCase("sunrise")) {
                nTimeOfDay = 22500;
            } else if (inargs[1].equalsIgnoreCase("sunset")) {
                nTimeOfDay = 13000;
            } else if (inargs[1].equalsIgnoreCase("never")) {
                nTimeOfDay = -1;
            } else {
                if (inargs[1].matches("\\d+")) {
                    nTimeOfDay = Integer.parseInt(inargs[1]);
                    if (nTimeOfDay < 1 || nTimeOfDay > 24000) {
                        destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                        return true;
                    }
                } else {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_addlocation_badargs", destTrait);
                    return true;
                }
            }
            Destination_Setting oLoc = new Destination_Setting();
            oLoc.destination = new Location(player.getLocation().getWorld(), player.getLocation().getBlockX()
                    + 0.5, player.getLocation().getBlockY(), player.getLocation().getBlockZ() + 0.5, 0.0F, 0.0F);
            
            if (player.getLocation().getYaw() < 0)
                oLoc.destination.setYaw(player.getLocation().getYaw()+360);
            
            oLoc.destination.setPitch(player.getLocation().getPitch());
            
            oLoc.TimeOfDay = nTimeOfDay;
            oLoc.Probability = 100;
            oLoc.Wait_Maximum = 0;
            oLoc.Wait_Minimum = 0;
            oLoc.setWanderingDistance(0);
            oLoc.Time_Minimum = 0;
            oLoc.Time_Maximum = 0;
            oLoc.Alias_Name = "";
            oLoc.setMaxDistance(destTrait.MaxDistFromDestination);
            oLoc.LocationIdent = UUID.randomUUID();
            oLoc.arrival_Commands = new ArrayList<String>();

            oLoc.player_Skin_Name = "";
            oLoc.player_Skin_UUID = "";
            oLoc.player_Skin_ApplyOnArrival = false;
            oLoc.player_Skin_Texture_Metadata = "";
            oLoc.player_Skin_Texture_Signature = "";
            oLoc.Pause_Distance = -1;
            oLoc.Pause_Timeout = -1;
            oLoc.Pause_Type = "ALL";

            
            if (destRef.getPlotSquared != null)
            {
                //Validate that the user is in the same plot as the NPC to ensure they cannot have the NPC run into other plots
                if (!destRef.getPlotSquared.playerHasPermissions(player))
                {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_plotquared_denied", destTrait);
                    return true;
                }
                
                if (!destRef.getPlotSquared.playerInPlotWithNPC(player, destTrait.getNPC()))
                {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_plotquared_npcdifferent", destTrait);
                    return true;
                }
            }
            
            if (destTrait.NPCLocations == null) {
                destTrait.NPCLocations = new ArrayList<Destination_Setting>();
            }

            // V1.39 -- Event
            final Location_Added newLocation = new Location_Added(npc, oLoc);
            Bukkit.getServer().getPluginManager().callEvent(newLocation);
            if (newLocation.isCancelled()) {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_addlocation_blocked", destTrait);
                return true;
            }
            destTrait.NPCLocations.add(oLoc);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        } else {
            destRef.getMessageManager.sendMessage("destinations", sender,
                    "messages.commands_addlocation_badargs", destTrait);
            return true;
        }
    }

    
    @CommandInfo(
            name = "removelocation",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_removelocation_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.removelocation","npcdestinations.editown.removelocation"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 1
            )
    public boolean npcDest_RemoveLocation(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {

        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait);
            return true;
        }
        if (inargs.length > 1) {
            int nIndex = Integer.parseInt(inargs[1]);
            if (nIndex > -1 && nIndex <= destTrait.NPCLocations.size()) {
                if (!destTrait.NPCLocations.get(nIndex).managed_Location.equals("")) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",
                            destTrait, destTrait.NPCLocations.get(nIndex));
                    return true;
                }

                // V1.39 -- Event
                final Destination_Setting removedDest = destTrait.NPCLocations.get(Integer.parseInt(inargs[1]));
                Location_Deleted removeEvent = new Location_Deleted(npc, removedDest);
                Bukkit.getServer().getPluginManager().callEvent(removeEvent);
                if (removeEvent.isCancelled()) {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_removelocation_blocked", destTrait);
                    return true;
                }
                destTrait.NPCLocations.remove(Integer.parseInt(inargs[1]));
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                return true;
            } else {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_info_nolocations", destTrait);
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Configured Locations: ");
            Location oCurLoc = destTrait.GetCurrentLocation().destination;
            if (oCurLoc == null) {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_info_nolocations", destTrait);
            } else {
                for (Destination_Setting oLoc : destTrait.NPCLocations) {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_info_location", destTrait, oLoc);
                }
            }
            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        }
    }
    
    @CommandInfo(
            name = "locwand",
            group = "NPC Location Commands",
            languageFile = "destinations",        
            helpMessage = "command_locwand_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.locwand","npcdestinations.editown.locwand"},
            allowConsole = true,
            minArguments = 4,
            maxArguments = 5
            )
    public boolean npcDest_LocWand(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        try {
            int nLoc = Integer.parseInt(inargs[1]);
            if (nLoc > destTrait.NPCLocations.size() - 1) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                return true;
            }
            
            if (!destTrait.NPCLocations.get(nLoc).managed_Location.equals("")) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",destTrait, destTrait.NPCLocations.get(nLoc));
                return true;
            }
            if (Utilities.isNumeric(inargs[2]))
            {
                int nDist = Integer.parseInt(inargs[2]);
                destTrait.NPCLocations.get(nLoc).setWanderingDistance(nDist);
                destTrait.NPCLocations.get(nLoc).Wandering_Region = "";
            }
            else
            {
                destTrait.NPCLocations.get(nLoc).setWanderingDistance(0.0);
                destTrait.NPCLocations.get(nLoc).Wandering_Region = inargs[2];
            }
            
            int nMin = Integer.parseInt(inargs[3]);
            int nMax = Integer.parseInt(inargs[4]);
            
            destTrait.NPCLocations.get(nLoc).Wait_Minimum = nMin;
            destTrait.NPCLocations.get(nLoc).Wait_Maximum = nMax;
            destTrait.NPCLocations.get(nLoc).Wandering_UseBlocks = false;

            for (String sArg : inargs) {
                if (sArg.equalsIgnoreCase("--blocks")) {
                    destTrait.NPCLocations.get(nLoc).Wandering_UseBlocks = true;
                }
            }
            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        } catch (Exception e) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_locwand_badargs",destTrait);
        }
        return true;
        
    }
    
    @CommandInfo(
            name = "locskin",
            group = "NPC Location Commands",
            languageFile = "destinations",            
            helpMessage = "command_locskin_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.locskin","npcdestinations.editown.locskin"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 2
            )
    public boolean npcDest_LocSkin(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        if (inargs.length > 1) {
            int nIndex = Integer.parseInt(inargs[1]);
            if (nIndex > destTrait.NPCLocations.size() - 1) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                return true;
            }

            if (!destTrait.NPCLocations.get(nIndex).managed_Location.equals("")) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",
                        destTrait, destTrait.NPCLocations.get(nIndex));
                return true;
            }

            if (inargs[2].equalsIgnoreCase("show")) {
                if (destTrait.NPCLocations.get(nIndex).player_Skin_UUID.trim().isEmpty() || !(npc
                        .getEntity() instanceof Player)) {
                    destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_locskin_notset");
                    return true;
                } else {
                    npc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA);
                    npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
                    npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
                    npc.data().remove("cached-skin-uuid-name");
                    npc.data().remove("cached-skin-uuid");
                    npc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA);

                    // Set the skin
                    npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, false);
                    npc.data().set("cached-skin-uuid-name", destTrait.NPCLocations.get(nIndex).player_Skin_Name);
                    npc.data().set("cached-skin-uuid", destTrait.NPCLocations.get(nIndex).player_Skin_UUID);
                    npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, destTrait.NPCLocations.get(
                            nIndex).player_Skin_Name);
                    npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, destTrait.NPCLocations
                            .get(nIndex).player_Skin_Texture_Metadata);
                    npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA,
                            destTrait.NPCLocations.get(nIndex).player_Skin_Texture_Signature);

                    if (npc.isSpawned()) {

                        SkinnableEntity skinnable = npc.getEntity() instanceof SkinnableEntity
                                ? (SkinnableEntity) npc.getEntity() : null;
                        if (skinnable != null) {
                            Skin.get(skinnable).applyAndRespawn(skinnable);

                        }
                    }
                    destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                    return true;
                }
            } else if (inargs[2].equalsIgnoreCase("clear")) {
                destTrait.NPCLocations.get(nIndex).player_Skin_Name = "";
                destTrait.NPCLocations.get(nIndex).player_Skin_UUID = "";
                destTrait.NPCLocations.get(nIndex).player_Skin_Texture_Metadata = "";
                destTrait.NPCLocations.get(nIndex).player_Skin_Texture_Signature = "";
                // V1.39 -- Event
                Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
                Bukkit.getServer().getPluginManager().callEvent(changedLocation);
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                return true;
            } else if (inargs[2].equalsIgnoreCase("before")) {
                destTrait.NPCLocations.get(nIndex).player_Skin_ApplyOnArrival = false;
            } else if (inargs[2].equalsIgnoreCase("after")) {
                destTrait.NPCLocations.get(nIndex).player_Skin_ApplyOnArrival = true;
            } else {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_locskin_badargs");
                return true;
            }

            destTrait.NPCLocations.get(nIndex).player_Skin_Name = npc.data().get("cached-skin-uuid-name")
                    .toString();
            destTrait.NPCLocations.get(nIndex).player_Skin_UUID = npc.data().get("cached-skin-uuid").toString();
            destTrait.NPCLocations.get(nIndex).player_Skin_Texture_Metadata = npc.data().get(
                    NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA).toString();
            destTrait.NPCLocations.get(nIndex).player_Skin_Texture_Signature = npc.data().get(
                    NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA).toString();

            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nIndex));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);
            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        }
        
        return false;
    }
 
    @CommandInfo(
            name = "locinv",
            group = "NPC Location Commands",
            languageFile = "destinations",            
            helpMessage = "command_locinv_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.locinv","npcdestinations.editown.locinv"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 2
            )
    public boolean npcDest_LocInv(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc");
            return true;
        }

        if (inargs.length > 1) {

            if (inargs.length < 2 || !StringUtils.isNumeric(inargs[1])) {
                return false;
            }

            int nLoc = Integer.parseInt(inargs[1]);
            if (nLoc > destTrait.NPCLocations.size() - 1) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                return true;
            }
            
            if (!destTrait.NPCLocations.get(nLoc).managed_Location.equals("")) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",
                        destTrait, destTrait.NPCLocations.get(nLoc));
                return true;
            }

            Equipment npcINV = npc.getTrait(Equipment.class);
            destTrait.NPCLocations.get(nLoc).items_Head = npcINV.get(EquipmentSlot.HELMET);
            destTrait.NPCLocations.get(nLoc).items_Boots = npcINV.get(EquipmentSlot.BOOTS);
            destTrait.NPCLocations.get(nLoc).items_Chest = npcINV.get(EquipmentSlot.CHESTPLATE);
            destTrait.NPCLocations.get(nLoc).items_Legs = npcINV.get(EquipmentSlot.LEGGINGS);
            destTrait.NPCLocations.get(nLoc).items_Hand = npcINV.get(EquipmentSlot.HAND);

            destTrait.NPCLocations.get(nLoc).items_Clear = inargs.length > 2 && inargs[2].contains("--clear");

            if (Bukkit.getServer().getClass().getPackage().getName().startsWith("v1_9"))
                destTrait.NPCLocations.get(nLoc).items_Offhand = npcINV.get(EquipmentSlot.OFF_HAND);

            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        }
        
        return false;
    }

    
    @CommandInfo(
            name = "locmax",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_locmax_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.locmax","npcdestinations.editown.locmax"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 2
            )
    public boolean npcDest_locmax(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }
        if (inargs.length != 3) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_locmax_badargs",
                    destTrait);
            return true;
        }
        try {
            int nLoc = Integer.parseInt(inargs[1]);
            if (nLoc > destTrait.NPCLocations.size() - 1) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                return true;
            }
            
            int nDist = Integer.parseInt(inargs[2]);

            if (!destTrait.NPCLocations.get(nLoc).managed_Location.equals("")) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed",
                        destTrait, destTrait.NPCLocations.get(nLoc));
                return true;
            }

            destTrait.NPCLocations.get(nLoc).setMaxDistance(nDist);

            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);
            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        } catch (Exception e) {
        }
        return false;
        
    }

    @CommandInfo(
            name = "locpause",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_locpause_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.locpause","npcdestinations.editown.locpause"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 4
            )
    public boolean npcDest_locpause(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }
        if (inargs.length == 1) {
            return false;
        }

        int nLoc = Integer.parseInt(inargs[1]);
        if (nLoc > destTrait.NPCLocations.size() - 1) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
            return true;
        }
        
        if (!destTrait.NPCLocations.get(nLoc).managed_Location.equals("")) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait,
                    destTrait.NPCLocations.get(nLoc));
            return true;
        }

        if (inargs.length == 2) {
            destTrait.NPCLocations.get(nLoc).Pause_Distance = -1;
            destTrait.NPCLocations.get(nLoc).Pause_Timeout = -1;
            destTrait.NPCLocations.get(nLoc).Pause_Type = "ALL";

            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);
            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        } else if (inargs.length == 3) {
            try {
                int nDist = Integer.parseInt(inargs[2]);
                destTrait.NPCLocations.get(nLoc).setMaxDistance(nDist);
                destTrait.NPCLocations.get(nLoc).Pause_Timeout = -1;
                destTrait.NPCLocations.get(nLoc).Pause_Type = "ALL";

                // V1.39 -- Event
                Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
                Bukkit.getServer().getPluginManager().callEvent(changedLocation);
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                return true;
            } catch (Exception e) {
            }
        } else if (inargs.length == 4) {
            try {
                destTrait.NPCLocations.get(nLoc).Pause_Distance = Integer.parseInt(inargs[2]);
                destTrait.NPCLocations.get(nLoc).Pause_Timeout = Integer.parseInt(inargs[3]);
                destTrait.NPCLocations.get(nLoc).Pause_Type = "ALL";

                // V1.39 -- Event
                Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
                Bukkit.getServer().getPluginManager().callEvent(changedLocation);
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                return true;
            } catch (Exception e) {
            }
        } else if (inargs.length == 5) {
            try {
                destTrait.NPCLocations.get(nLoc).Pause_Distance = Integer.parseInt(inargs[2]);
                destTrait.NPCLocations.get(nLoc).Pause_Timeout = Integer.parseInt(inargs[3]);

                if (inargs[4].equalsIgnoreCase("TRAVELING")) {
                    destTrait.NPCLocations.get(nLoc).Pause_Type = "TRAVELING";
                } else if (inargs[4].equalsIgnoreCase("WANDERING")) {
                    destTrait.NPCLocations.get(nLoc).Pause_Type = "WANDERING";
                } else {
                    destTrait.NPCLocations.get(nLoc).Pause_Type = "ALL";
                }

                // V1.39 -- Event
                Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
                Bukkit.getServer().getPluginManager().callEvent(changedLocation);
                destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
                return true;
            } catch (Exception e) {
            }
            
        }
        return false;
    }

    @CommandInfo(
            name = "locprob",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_locprob_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.locprob","npcdestinations.editown.locprob"},
            allowConsole = true,
            minArguments = 4,
            maxArguments = 4
            )
    public boolean npcDest_locprob(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        if (inargs.length != 5) {
            return false;
        }
        
        try {
            int nLoc = Integer.parseInt(inargs[1]);
            if (nLoc > destTrait.NPCLocations.size() - 1) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
                return true;
            }

            if (!destTrait.NPCLocations.get(nLoc).managed_Location.equals("")) {
                destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait, destTrait.NPCLocations.get(nLoc));
                return true;
            }

            int nChance = Integer.parseInt(inargs[2]);
            int nMin = Integer.parseInt(inargs[3]);
            int nMax = Integer.parseInt(inargs[4]);
            destTrait.NPCLocations.get(nLoc).Probability = nChance;
            destTrait.NPCLocations.get(nLoc).Time_Minimum = nMin;
            destTrait.NPCLocations.get(nLoc).Time_Maximum = nMax;

            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        } catch (Exception e) {
        }
        return false;
        
    }

    @CommandInfo(
            name = "loctime",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_loctime_help",
            arguments = { "#","sunrise|sunset|never"},
            permission = {"npcdestinations.editall.loctime","npcdestinations.editown.loctime"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 2
            )
    public boolean npcDest_loctime(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }

        if (inargs.length != 3) {
            return false;
        }
        int nLoc = Integer.parseInt(inargs[1]);
        if (nLoc > destTrait.NPCLocations.size() - 1) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_commands_invalidloc");
            return true;
        }
        
        if (!destTrait.NPCLocations.get(nLoc).managed_Location.equals("")) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait, destTrait.NPCLocations.get(nLoc));
            return true;
        }
        
        int nTimeOfDay = 0;
        if (inargs[2].equalsIgnoreCase("sunrise")) {
            nTimeOfDay = 22500;
        } else if (inargs[2].equalsIgnoreCase("sunset")) {
            nTimeOfDay = 13000;
        } else if (inargs[2].equalsIgnoreCase("never")) {
            nTimeOfDay = -1;
        } else {
            try {
                nTimeOfDay = Integer.parseInt(inargs[2]);
                if (nTimeOfDay < 0 || nTimeOfDay > 24000) {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_loctime_badargs", destTrait);
                    return true;
                }
            } catch (Exception err) {
                destRef.getMessageManager.sendMessage("destinations", sender,
                        "messages.commands_loctime_badargs", destTrait);
                return true;
            }
        }
        destTrait.NPCLocations.get(nLoc).TimeOfDay = nTimeOfDay;
        // V1.39 -- Event
        Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
        Bukkit.getServer().getPluginManager().callEvent(changedLocation);

        destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
        return true;
    
    }

    @CommandInfo(
            name = "locloc",
            group = "NPC Location Commands",
            languageFile = "destinations",
            helpMessage = "command_locloc_help",
            arguments = { "--npc|#","<npc>","#"},
            permission = {"npcdestinations.editall.locloc","npcdestinations.editown.locloc"},
            allowConsole = true,
            minArguments = 1,
            maxArguments = 4
            )
    public boolean npcDest_locloc(DestinationsPlugin destRef,CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait)
    {
        if (npc == null) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.invalid_npc", destTrait, null);
            return true;
        }
        int nLoc = Integer.parseInt(inargs[1]);
        if (!destTrait.NPCLocations.get(nLoc).managed_Location.equals("")) {
            destRef.getMessageManager.sendMessage("destinations", sender, "messages.commands_managed", destTrait,
                    destTrait.NPCLocations.get(nLoc));
            return true;
        }

        if (inargs.length == 2 && (sender instanceof Player)) {
            // Set to the users location
            destTrait.NPCLocations.get(nLoc).destination = ((Player) sender).getLocation().clone();
            destTrait.NPCLocations.get(nLoc).destination.setYaw(Math.abs(destTrait.NPCLocations.get(nLoc).destination
                    .getYaw()));

            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        }

        if (inargs.length < 5) {
            return false;
        }
        
        try {
            int nX = Integer.parseInt(inargs[2]);
            int nY = Integer.parseInt(inargs[3]);
            int nZ = Integer.parseInt(inargs[4]);

            float nYaw = 0.0F;
            float nPitch = 0.0F;
            if (inargs.length > 5) {
                if (Float.parseFloat(inargs[5]) < 0)
                    nYaw = Float.parseFloat(inargs[5])+360;
                else 
                    nYaw = Float.parseFloat(inargs[5]);
            }
            if (inargs.length > 6) {
                nPitch = Float.parseFloat(inargs[6]);
            }
            
            Location newLocation = new Location(destTrait.NPCLocations.get(nLoc).destination
                    .getWorld(), nX + 0.5, nY, nZ + 0.5, nYaw, nPitch);

            if (destRef.getPlotSquared != null)
            {
                //Validate that the user is in the same plot as the NPC to ensure they cannot have the NPC run into other plots
                if (!destRef.getPlotSquared.playerHasPermissions((Player)sender))
                {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_plotquared_denied", destTrait);
                    return true;
                }
                
                if (!destRef.getPlotSquared.playerInPlotWithNPC((Player)sender, destTrait.getNPC()))
                {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_plotquared_npcdifferent", destTrait);
                    return true;
                }

                if (!destRef.getPlotSquared.locationInSamePlotAsNPC(destTrait.getNPC(),newLocation))
                {
                    destRef.getMessageManager.sendMessage("destinations", sender,
                            "messages.commands_plotquared_npcdifferent", destTrait);
                    return true;
                }
            }

            destTrait.NPCLocations.get(nLoc).destination = newLocation;
            
            // V1.39 -- Event
            Location_Updated changedLocation = new Location_Updated(npc, destTrait.NPCLocations.get(nLoc));
            Bukkit.getServer().getPluginManager().callEvent(changedLocation);

            destRef.getCommandManager.onCommand(sender, new String[] { "info", "--npc", Integer.toString(npc.getId()) });
            return true;
        } catch (Exception e) {
            
        }
        return false;
       
    }
}