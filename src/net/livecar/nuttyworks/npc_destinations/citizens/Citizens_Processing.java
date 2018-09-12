package net.livecar.nuttyworks.npc_destinations.citizens;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.livecar.nuttyworks.npc_destinations.DebugTarget;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.api.Navigation_NewDestination;
import net.livecar.nuttyworks.npc_destinations.api.Navigation_Reached;
import net.livecar.nuttyworks.npc_destinations.api.enumerations.TriBoolean;
import net.livecar.nuttyworks.npc_destinations.bridges.MCUtilsBridge.inHandLightSource;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait.en_CurrentAction;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait.en_RequestedAction;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class Citizens_Processing {

    DestinationsPlugin         destRef            = null;
    List<Material>             doorMaterials      = null;

    static Citizens_Processing processingInstance = null;

    public Citizens_Processing(DestinationsPlugin storageRef) {
        destRef = storageRef;
        processingInstance = this;
    }

    public static boolean goalAdapter_ShouldExecute(NPC npc, Citizens_Goal citGoal) {
        return processingInstance.shouldExecute(npc, citGoal);
    }

    public static void trait_loadSettings(NPCDestinationsTrait trait, DataKey key) {
        processingInstance.load(trait, key);
    }

    public static void trait_saveSettings(NPCDestinationsTrait trait, DataKey key) {
        processingInstance.save(trait, key);
    }

    public static void trait_clearPendingDestinations(NPCDestinationsTrait trait) {
        processingInstance.clearPendingDestinations(trait);
    }

    public static void trait_removePendingDestination(NPCDestinationsTrait trait, int index) {
        processingInstance.removePendingDestination(trait, index);
    }

    public static void debugMessage(Level debugLevel, String extendedMessage) {
        processingInstance.destRef.getMessageManager.debugMessage(debugLevel, extendedMessage);
    }

    public static void sendDebugMessage(String langFile, String msgKey, NPC npc, NPCDestinationsTrait npcTrait) {
        processingInstance.destRef.getMessageManager.sendDebugMessage(langFile, msgKey, npc, npcTrait);
    }

    public static void trait_locationReached(NPCDestinationsTrait trait) {
        processingInstance.locationReached(trait);
    }

    public static Destination_Setting trait_getCurLocation(NPCDestinationsTrait trait, boolean noNull) {
        return processingInstance.getCurLocation(trait, noNull);
    }

    // Private functions, ain't nobody got eyes for this
    private Destination_Setting getCurLocation(NPCDestinationsTrait trait, boolean noNull) {

        // 1.16 Locked locations
        if (trait.requestedAction == en_RequestedAction.SET_LOCATION && trait.locationLockUntil != null && new Date().before(trait.locationLockUntil)) {
            destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|setLocation:" + trait.currentLocation.destination.toString());
            if (trait.last_Loc_Reached == null)
                trait.last_Loc_Reached = trait.setLocation.LocationIdent;
            return trait.setLocation;
        } else if (trait.requestedAction == en_RequestedAction.SET_LOCATION && trait.locationLockUntil != null && new Date().after(trait.locationLockUntil)) {
            trait.requestedAction = en_RequestedAction.NORMAL_PROCESSING;
            trait.locationLockUntil = null;
            trait.setLocation = null;
        } else if (trait.requestedAction == en_RequestedAction.SET_LOCATION && trait.locationLockUntil == null) {
            trait.requestedAction = en_RequestedAction.NORMAL_PROCESSING;
        }

        if (trait.requestedAction == en_RequestedAction.NO_PROCESSING) {
            destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|NO_PROCESSING:" + trait.currentLocation.destination.toString());
            return null;
        }

        // 1.6: Random location support
        if (trait.locationLockUntil != null && new Date().before(trait.locationLockUntil) && !noNull) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            if (trait.currentLocation.destination == null) {
                destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Locked Location > Null|Lock: " + dateFormat.format(trait.locationLockUntil) + ">" + dateFormat.format(new Date()));
            } else {
                destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Locked Location > Null|Lock: " + dateFormat.format(trait.locationLockUntil) + ">" + dateFormat.format(new Date())
                        + trait.currentLocation.destination.toString());
            }
            return null;
        }

        // 1.6: Check to see if Locations has data, if so convert them over and
        // clean it out.
        if (trait.Locations.size() > 0) {
            for (String sLoc : trait.Locations) {
                String[] sLocation = sLoc.split(":");

                Destination_Setting oLoc = new Destination_Setting();
                oLoc.destination = new Location(trait.getNPC().getEntity().getWorld(), Long.parseLong(sLocation[0]), Long.parseLong(sLocation[1]), Long.parseLong(sLocation[2]));
                oLoc.TimeOfDay = Integer.parseInt(sLocation[3]);
                oLoc.Probability = 0;
                oLoc.Wait_Maximum = 0;
                oLoc.Wait_Minimum = 0;
                oLoc.setMaxDistance(trait.MaxDistFromDestination);
                oLoc.setWanderingDistance(0);
                if (trait.NPCLocations == null) {
                    trait.NPCLocations = new ArrayList<Destination_Setting>();
                }
                trait.NPCLocations.add(oLoc);
            }
            trait.Locations.clear();
        }

        Destination_Setting oCurrentLoc = null;
        int nMinDiff = Integer.MAX_VALUE;
        int nTimeOfDay = ((Long) trait.getNPC().getEntity().getLocation().getWorld().getTime()).intValue();

        // Plotsquared, time of plot!
        if (destRef.getPlotSquared != null) {
            nTimeOfDay = destRef.getPlotSquared.getNPCPlotTime(trait.getNPC());
        }

        for (Destination_Setting oLoc : trait.NPCLocations) {
            if (oLoc.TimeOfDay == -1)
                continue;

            int nDiff = Math.abs(oLoc.TimeOfDay - nTimeOfDay);
            boolean pluginBlocked = false;
            // 1.29 - Check weather flags

            for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
                if (trait.enabledPlugins.contains(plugin.getActionName())) {
                    try {
                        if (!plugin.isDestinationEnabled(trait.getNPC(), trait, oLoc)) {
                            pluginBlocked = true;
                            break;
                        }
                    } catch (Exception err) {
                        StringWriter sw = new StringWriter();
                        err.printStackTrace(new PrintWriter(sw));

                        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_debug", err.getMessage() + "\n" + sw.toString());
                    }
                }
            }

            if (pluginBlocked)
                continue;

            if (oLoc.WeatherFlag > 0) {
                if (!oLoc.destination.getWorld().hasStorm() && oLoc.WeatherFlag == 1) {
                    if (nMinDiff > nDiff && oLoc.TimeOfDay <= nTimeOfDay) {
                        destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Weather: Only Clear|" + oLoc.destination.toString() + "|" + oLoc.destination.getWorld().hasStorm());
                        nMinDiff = nDiff;
                        oCurrentLoc = oLoc;
                    }
                } else if (oLoc.destination.getWorld().hasStorm() && oLoc.WeatherFlag == 2) {
                    if (nMinDiff > nDiff && oLoc.TimeOfDay <= nTimeOfDay) {
                        destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Weather: Storming|" + oLoc.destination.toString() + "|" + oLoc.destination.getWorld().hasStorm());
                        nMinDiff = nDiff;
                        oCurrentLoc = oLoc;
                    }
                }
            } else if (nMinDiff > nDiff && oLoc.TimeOfDay <= nTimeOfDay) {
                nMinDiff = nDiff;
                oCurrentLoc = oLoc;
            }
        }

        if (oCurrentLoc == null) {
            nMinDiff = 0;
            for (Destination_Setting oLoc : trait.NPCLocations) {
                boolean pluginBlocked = false;
                // 1.29 - Check weather flags

                for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
                    if (trait.enabledPlugins.contains(plugin.getActionName().toUpperCase())) {
                        try {
                            if (!plugin.isDestinationEnabled(trait.getNPC(), trait, oLoc)) {
                                pluginBlocked = true;
                                break;
                            }
                        } catch (Exception err) {
                            StringWriter sw = new StringWriter();
                            err.printStackTrace(new PrintWriter(sw));

                        }
                    }
                }
                if (pluginBlocked)
                    continue;

                if (oLoc.WeatherFlag > 0) {
                    if (!oLoc.destination.getWorld().hasStorm() && oLoc.WeatherFlag == 1) {
                        if (oLoc.TimeOfDay > nMinDiff) {
                            destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Weather: Clear Only|" + oLoc.destination.toString() + "|" + oLoc.destination.getWorld().hasStorm());
                            nMinDiff = oLoc.TimeOfDay;
                            oCurrentLoc = oLoc;
                        }
                    } else if (oLoc.destination.getWorld().hasStorm() && oLoc.WeatherFlag == 2) {
                        if (oLoc.TimeOfDay > nMinDiff) {
                            destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Weather: Storms Only|" + oLoc.destination.toString() + "|" + oLoc.destination.getWorld().hasStorm());
                            nMinDiff = oLoc.TimeOfDay;
                            oCurrentLoc = oLoc;
                        }
                    }

                } else if (oLoc.TimeOfDay > nMinDiff) {
                    nMinDiff = oLoc.TimeOfDay;
                    oCurrentLoc = oLoc;
                }
            }
        }

        if (oCurrentLoc == null) {
            destRef.getMessageManager.debugMessage(Level.FINEST, "NPC:" + trait.getNPC().getId() + "|curLocation: return null");
            return null;
        } else {
            if (trait.currentLocation != null && trait.currentLocation.TimeOfDay == oCurrentLoc.TimeOfDay && trait.pendingDestinations != null && trait.pendingDestinations.size() > 0) {
                destRef.getMessageManager.debugMessage(Level.FINEST, "NPC:" + trait.getNPC().getId() + "|curLocation: CurLoc Time matches, return current");
                return trait.currentLocation;
            }

            // 1.6: Final check to see if we have a random situation
            // 1.29: updated to allow weather changes
            if (trait.currentLocation != null && trait.currentLocation.TimeOfDay == oCurrentLoc.TimeOfDay && trait.locationLockUntil == null) {
                if (trait.currentLocation.destination != null && oCurrentLoc.destination.toString() == trait.currentLocation.destination.toString()) {
                    destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Weather: return current|" + trait.currentLocation.destination.toString());
                    return trait.currentLocation;
                }
            }

            if (trait.locationLockUntil != null && new Date().after(trait.locationLockUntil))
                trait.locationLockUntil = null;

            ArrayList<Destination_Setting> oTmpDests = new ArrayList<Destination_Setting>();
            boolean bHasPercent = false;
            for (Destination_Setting oLoc : trait.NPCLocations) {
                if (oLoc.TimeOfDay == oCurrentLoc.TimeOfDay) {
                    if (oLoc.Probability > 0 && oLoc.Probability != 100) {
                        bHasPercent = true;
                        destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|PercLocation:" + oLoc.destination.toString());
                        oTmpDests.add(oLoc);
                    }
                }
            }

            if (oTmpDests.size() == 1) {
                Navigation_Reached navReached = new Navigation_Reached(trait.getNPC(), trait.currentLocation);
                Bukkit.getServer().getPluginManager().callEvent(navReached);

                if (fireLocationChangedEvent(trait, oTmpDests.get(0)))
                    return trait.currentLocation;

                trait.setCurrentLocation(oTmpDests.get(0));
                destRef.getMessageManager.debugMessage(Level.FINEST, "NPC:" + trait.getNPC().getId() + "|curLocation: single loc, returned");
                return oTmpDests.get(0);
            } else if (oTmpDests.size() > 1) {
                Random random = new Random();
                if (!bHasPercent) {
                    while (true) {
                        int nRnd = random.nextInt(oTmpDests.size());

                        oCurrentLoc = oTmpDests.get(nRnd);
                        // Does this destination have a max/min time to spend
                        // there?

                        if (trait.currentLocation != null && oCurrentLoc == trait.currentLocation) {
                            // Try again
                        } else {
                            if (fireLocationChangedEvent(trait, oCurrentLoc)) {
                                return trait.currentLocation;
                            }
                            trait.setCurrentLocation(oCurrentLoc);
                            break;
                        }
                    }
                } else {

                    int nCnt = 0;
                    int nPercent = random.nextInt(100);

                    for (Destination_Setting oLoc : oTmpDests) {
                        if (oLoc.Probability > 0 && oLoc.Probability != 100) {
                            nCnt += oLoc.Probability;
                            if (nPercent <= nCnt) {
                                oCurrentLoc = oLoc;
                                if (fireLocationChangedEvent(trait, oCurrentLoc)) {
                                    return trait.currentLocation;
                                }
                                trait.setCurrentLocation(oCurrentLoc);
                                break;
                            }
                        }
                    }
                    // Get the first 0% one (really 100%)
                    for (Destination_Setting oLoc : oTmpDests) {
                        if (oLoc.Probability == 0) {
                            oCurrentLoc = oLoc;
                            if (fireLocationChangedEvent(trait, oCurrentLoc)) {
                                return trait.currentLocation;
                            }
                            trait.setCurrentLocation(oCurrentLoc);
                            break;
                        }
                    }
                }
            }
        }

        if (trait.monitoredLocation != null && trait.monitoredLocation == oCurrentLoc) {
            destRef.getMessageManager.debugMessage(Level.FINEST, "NPC:" + trait.getNPC().getId() + "|curLocation: Monitored Location, no change");
            return null;
        }

        destRef.getMessageManager.debugMessage(Level.FINEST, "NPC:" + trait.getNPC().getId() + "|curLocation: default return");
        if (oCurrentLoc != trait.currentLocation) {
            if (fireLocationChangedEvent(trait, oCurrentLoc)) {
                return trait.currentLocation;
            }
        }
        trait.setCurrentLocation(oCurrentLoc);
        trait.monitoredLocation = null;
        return oCurrentLoc;
    }

    private void locationReached(NPCDestinationsTrait trait) {
        if (trait.currentLocation.destination == null) {
            return;
        }

        if (trait.last_Loc_Reached != null && trait.last_Loc_Reached == trait.currentLocation.LocationIdent) {
            destRef.getMessageManager.debugMessage(Level.FINEST, "NPC:" + trait.getNPC().getId() + "|locationReached: last: " + trait.last_Loc_Reached.toString() + " CurLoc:" + trait.currentLocation.LocationIdent.toString());
            return;
        }

        processingInstance.destRef.getMessageManager.sendDebugMessage("destinations", "debug_messages.goal_reacheddestination", trait.getNPC(), trait);

        // Notify all plugins that the location has been reached.
        boolean cancelProcessing = false;

        for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
            if (trait.enabledPlugins.contains(plugin.getActionName().toUpperCase())) {
                try {
                    if (plugin.onNavigationReached(trait.getNPC(), trait, trait.currentLocation))
                        cancelProcessing = true;
                } catch (Exception err) {
                    StringWriter sw = new StringWriter();
                    err.printStackTrace(new PrintWriter(sw));
                    destRef.getMessageManager.consoleMessage(destRef, "destinations", "console_Messages.plugin_error", err.getMessage() + "\n" + sw.toString());
                }
            }
        }

        // Fire the navigation event
        Navigation_Reached navReached = new Navigation_Reached(trait.getNPC(), trait.currentLocation);
        Bukkit.getServer().getPluginManager().callEvent(navReached);
        if (navReached.isCancelled() || cancelProcessing)
            return;

        trait.last_Loc_Reached = trait.currentLocation.LocationIdent;
        destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Destinations_Trait.LocationReached:" + trait.currentLocation.destination.toString());

        // 1.44 -- Process the commands in the command subset for this NPC
        for (String commandString : trait.currentLocation.arrival_Commands)
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commandString);

        if (!(trait.getNPC().getEntity() instanceof Player)) {
            // Stuff below really does not work on other NPC's
            return;
        }

        Equipment locEquip = trait.getNPC().getTrait(Equipment.class);

        // Are we to clear the inventory?
        if (trait.currentLocation.items_Clear) {
            locEquip.set(EquipmentSlot.HELMET, null);
            locEquip.set(EquipmentSlot.CHESTPLATE, null);
            locEquip.set(EquipmentSlot.LEGGINGS, null);
            locEquip.set(EquipmentSlot.BOOTS, null);
            locEquip.set(EquipmentSlot.HAND, null);
            locEquip.set(EquipmentSlot.HELMET, null);

            // V1.9 or greater
            if (destRef.Version >= 10900) {
                locEquip.set(EquipmentSlot.OFF_HAND, null);
            }
        }

        if (trait.currentLocation.items_Head != null) {

            locEquip.set(EquipmentSlot.HELMET, trait.currentLocation.items_Head);
        }
        if (trait.currentLocation.items_Chest != null)
            locEquip.set(EquipmentSlot.CHESTPLATE, trait.currentLocation.items_Chest);
        if (trait.currentLocation.items_Legs != null)
            locEquip.set(EquipmentSlot.LEGGINGS, trait.currentLocation.items_Legs);
        if (trait.currentLocation.items_Boots != null)
            locEquip.set(EquipmentSlot.BOOTS, trait.currentLocation.items_Boots);

        if (trait.currentLocation.items_Hand != null)
            locEquip.set(EquipmentSlot.HAND, trait.currentLocation.items_Hand);

        if (destRef.Version >= 10900) {
            if (trait.currentLocation.items_Offhand != null)
                locEquip.set(EquipmentSlot.OFF_HAND, trait.currentLocation.items_Offhand);
        }

        // Lighting V1.19 - Check for torches in either hand
        if (destRef.getLightPlugin != null) {
            boolean startLightTask = false;
            if (locEquip.get(EquipmentSlot.HAND) != null && (destRef.getMCUtils.isHoldingTorch(locEquip.get(EquipmentSlot.HAND).getType()) != inHandLightSource.NOLIGHT )) {
                startLightTask = true;
                destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Lighting");

            }
            if (destRef.Version >= 10900) {
                if (locEquip.get(EquipmentSlot.OFF_HAND) != null && (destRef.getMCUtils.isHoldingTorch(locEquip.get(EquipmentSlot.OFF_HAND).getType()) != inHandLightSource.NOLIGHT )) {
                    destRef.getMessageManager.debugMessage(Level.FINE, "NPC:" + trait.getNPC().getId() + "|Lighting-1_10");
                    startLightTask = true;
                }
            }

            if (startLightTask) {
                if (trait.lightTask < 1) {
                    final int npcID = trait.getNPC().getId();
                    trait.lightTask = Bukkit.getScheduler().scheduleSyncDelayedTask(destRef, new Runnable() {
                        public void run() {
                            updateLighting(npcID);
                        }
                    }, 10);
                }
            }
        }

        // V1.33 - Skins
        if (!trait.currentLocation.player_Skin_Name.isEmpty() && trait.getNPC().getEntity() instanceof Player) {
            destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + trait.getNPC().getId() + "|SKINCOMPARE|\r\n" + trait.getNPC().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA).toString()
                    + "\r\n" + trait.currentLocation.player_Skin_Texture_Metadata);

            if (!trait.getNPC().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA).equals(trait.currentLocation.player_Skin_Texture_Metadata) && !trait.getNPC().data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA).equals(
                    trait.currentLocation.player_Skin_Texture_Signature)) {
                trait.getNPC().data().remove(NPC.PLAYER_SKIN_UUID_METADATA);
                trait.getNPC().data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
                trait.getNPC().data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
                trait.getNPC().data().remove("cached-skin-uuid-name");
                trait.getNPC().data().remove("cached-skin-uuid");
                trait.getNPC().data().remove(NPC.PLAYER_SKIN_UUID_METADATA);

                // Set the skin
                trait.getNPC().data().set(NPC.PLAYER_SKIN_USE_LATEST, false);
                trait.getNPC().data().set("cached-skin-uuid-name", trait.currentLocation.player_Skin_Name);
                trait.getNPC().data().set("cached-skin-uuid", trait.currentLocation.player_Skin_UUID);
                trait.getNPC().data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, trait.currentLocation.player_Skin_Name);
                trait.getNPC().data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, trait.currentLocation.player_Skin_Texture_Metadata);
                trait.getNPC().data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, trait.currentLocation.player_Skin_Texture_Signature);

                if (trait.getNPC().isSpawned()) {

                    SkinnableEntity skinnable = trait.getNPC().getEntity() instanceof SkinnableEntity ? (SkinnableEntity) trait.getNPC().getEntity() : null;
                    if (skinnable != null) {
                        Skin.get(skinnable).applyAndRespawn(skinnable);

                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void clearPendingDestinations(NPCDestinationsTrait trait) {
        for (Location pendDestination : trait.getPendingDestinations()) {
            for (DebugTarget debugOutput : destRef.debugTargets) {
                if (debugOutput.getTargets().size() == 0 || debugOutput.getTargets().contains(trait.getNPC().getId())) {
                    if (((Player) debugOutput.targetSender).isOnline()) {
                        Player player = ((Player) debugOutput.targetSender);
                        if (player.getWorld().equals(pendDestination.getWorld())) {
                            try {
                                // 1.13+
                                player.sendBlockChange(pendDestination, pendDestination.getBlock().getBlockData());
                            } catch (NoSuchMethodError ex) {
                                // Legacy
                                player.sendBlockChange(pendDestination, pendDestination.getBlock().getType(), pendDestination.getBlock().getData());
                            }


                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void removePendingDestination(NPCDestinationsTrait trait, int index) {
        for (DebugTarget debugOutput : destRef.debugTargets) {
            if (debugOutput.getTargets().size() == 0 || debugOutput.getTargets().contains(trait.getNPC().getId())) {
                if (((Player) debugOutput.targetSender).isOnline()) {
                    Player player = ((Player) debugOutput.targetSender);
                    if (player.getWorld().equals(trait.getPendingDestinations().get(index).getWorld())) {
                        try {
                            // 1.13+
                            player.sendBlockChange(trait.getPendingDestinations().get(index), trait.getPendingDestinations().get(index).getBlock().getBlockData());
                        } catch (NoSuchMethodError ex) {
                            // Legacy
                            player.sendBlockChange(trait.getPendingDestinations().get(index), trait.getPendingDestinations().get(index).getBlock().getType(), trait.getPendingDestinations().get(index).getBlock().getData());
                        }
                    }
                }
            }
        }
    }

    private boolean shouldExecute(NPC npc, Citizens_Goal citGoal) {
        if (destRef.getPathClass == null)
            return false;
        if (!npc.isSpawned())
            return false;

        if (!npc.hasTrait(NPCDestinationsTrait.class)) {
            Bukkit.getLogger().log(Level.INFO, "NPC [" + npc.getId() + "/" + npc.getName() + "] has not been setup to use the NPCDestinations path provider");
            npc.despawn(DespawnReason.PLUGIN);
            return false;
        }

        // NPC is not currently spawned. Exit out.
        if (!npc.isSpawned()) {
            return false;
        }

        NPCDestinationsTrait trait = npc.getTrait(NPCDestinationsTrait.class);

        // Timeout the path finding it taking to long or stalled
        if ((trait.getCurrentAction() == en_CurrentAction.PATH_HUNTING) && (trait.getPendingDestinations().size() == 0)) {
            if (trait.lastPathCalc != null) {
                long nSeconds = (new Date().getTime() - trait.lastPathCalc.getTime()) / 1000L % 60L;
                if (nSeconds > destRef.getConfig().getInt("seek-time", 15)) {
                    destRef.getMessageManager.debugMessage(Level.FINE, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|Timeout>Path Failed");
                    trait.setCurrentAction(en_CurrentAction.IDLE_FAILED);
                    trait.lastPathCalc = new Date();
                }
            } else {
                destRef.getMessageManager.debugMessage(Level.FINE, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|Path Failed/IDLE");
                trait.setCurrentAction(en_CurrentAction.IDLE_FAILED);
            }
            return false;
        }

        if ((trait.getCurrentAction() == en_CurrentAction.IDLE || trait.getCurrentAction() == en_CurrentAction.IDLE_FAILED) && (trait.getPendingDestinations().size() == 0)) {
            if (trait.lastPathCalc != null) {
                long nSeconds = (new Date().getTime() - trait.lastPathCalc.getTime()) / 1000L % 60L;
                if (nSeconds < 3) {
                    return false;
                }
            }
        }
                
        if (trait.getCurrentAction() != en_CurrentAction.IDLE) {
            // Check the area near the NPC for players. Pause if so
            int traitDistance = 0;
            int traitPause = 0;
            if (trait.currentLocation != null && trait.currentLocation.Pause_Distance > -1) {
                if (trait.currentLocation.Pause_Distance > -1 && trait.currentLocation.Pause_Distance > trait.PauseForPlayers) {
                    traitDistance = trait.currentLocation.Pause_Distance * trait.currentLocation.Pause_Distance;
                    traitPause = trait.currentLocation.Pause_Timeout;

                    switch (trait.getCurrentAction()) {
                    case RANDOM_MOVEMENT:
                        if (!trait.currentLocation.Pause_Type.equalsIgnoreCase("ALL") && !trait.currentLocation.Pause_Type.equalsIgnoreCase("WANDERING")) {
                            traitDistance = 0;
                            traitPause = 0;
                        }
                        break;
                    case PATH_FOUND:
                    case TRAVELING:
                        if (!trait.currentLocation.Pause_Type.equalsIgnoreCase("ALL") && !trait.currentLocation.Pause_Type.equalsIgnoreCase("TRAVELING")) {
                            traitDistance = 0;
                            traitPause = 0;
                        }
                        break;
                    default:
                        break;
                    }
                } else {
                    traitDistance = trait.PauseForPlayers * trait.PauseForPlayers;
                    traitPause = trait.PauseTimeout;
                }
            } else {
                traitDistance = trait.PauseForPlayers * trait.PauseForPlayers;
                traitPause = trait.PauseTimeout;
            }
            if (traitPause == -1)
                traitPause = Integer.MAX_VALUE;

            if (trait.lastPauseLocation == null || trait.lastPauseLocation.distanceSquared(npc.getEntity().getLocation()) > traitDistance) {
                for (Player plrEntity : Bukkit.getOnlinePlayers()) {

                    if ((plrEntity.getWorld() == npc.getEntity().getWorld()) && (plrEntity.getLocation().distanceSquared(npc.getEntity().getLocation()) < traitDistance)) {
                        if (trait.lastPlayerPause == null)
                            trait.lastPlayerPause = new Date();

                        if (trait.lastPlayerPause.getTime() < (new Date().getTime() - (traitPause * 1000))) {
                            trait.lastPauseLocation = npc.getEntity().getLocation().clone();
                            trait.lastPlayerPause = null;
                            break;
                        }

                        trait.lastResult = ("Paused for player " + plrEntity.getDisplayName());
                        trait.lastPauseLocation = null;
                        return false;
                    }
                }
            }
        }

        // Validate if the item on the list to open is something we can, and
        // open it
        if (npc.getNavigator().getTargetAsLocation() != null && npc.getNavigator().getTargetType() == TargetType.LOCATION) {
            trait.processOpenableObjects();

            // check if the NPC is stuck and sitting in one place. if so, recalc
            // the path to the end.
            if ((npc.getNavigator().isNavigating() & npc.getNavigator().getTargetAsLocation().distanceSquared(npc.getEntity().getLocation()) > 3.0D)) {
                long nSeconds = (new Date().getTime() - trait.lastPositionChange.getTime()) / 1000L % 60L;
                if (nSeconds > 10L) {
                    npc.getNavigator().cancelNavigation();
                    trait.clearPendingDestinations();
                    trait.lastResult = "Stalled on path, recalc";
                    trait.lastPositionChange = new Date();
                    trait.setCurrentAction(en_CurrentAction.IDLE_FAILED);
                    destRef.getMessageManager.debugMessage(Level.FINE, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|NPC_Stuck>Path Failed");
                    return false;
                }
                return false;
            }
        }

        Double speed = npc.getEntity().getVelocity().lengthSquared();

        if (trait.getPendingDestinations().size() > 0 && trait.lastNavigationPoint != null) {

            Double distTarget = npc.getEntity().getLocation().distanceSquared(trait.lastNavigationPoint);
            if (distTarget > 1 && speed > 0.00615)
                return false;
        }

        if (npc.getNavigator().isNavigating()) {
            if (npc.getEntity().getLocation().distanceSquared(npc.getNavigator().getTargetAsLocation()) > 1)
                return false;
        }

        // Do we have any pending destinations to walk to??
        if (trait.getPendingDestinations().size() > 0) {
            Location oLastDest;
            Destination_Setting oCurDest = trait.GetCurrentLocation();
            if (!trait.getCurrentAction().equals(en_CurrentAction.RANDOM_MOVEMENT) && oCurDest != null) {
                oLastDest = trait.getPendingDestinations().get(trait.getPendingDestinations().size() - 1);

                if (trait.getMonitoringPlugin() == null && ((oLastDest.getBlockX() != oCurDest.destination.getBlockX()) || (oLastDest.getBlockZ() != oCurDest.destination.getBlockZ()))) {
                    npc.getNavigator().cancelNavigation();
                    trait.clearPendingDestinations();
                    trait.setCurrentAction(en_CurrentAction.IDLE);
                    trait.lastResult = "Destination changed, recalc";
                    trait.lastPositionChange = new Date();
                    if (destRef.debugTargets != null)
                        destRef.getMessageManager.sendDebugMessage("destinations", "Debug_Messages.goal_newdestination", npc, trait);
                    destRef.getMessageManager.debugMessage(Level.FINE, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|NewDestination>IDLE");
                    return false;
                }
            }
            trait.processOpenableObjects();
            destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|NewTarget: " + trait.getPendingDestinations().get(0).toString());

            if (oCurDest != null) {
                // V 2.1.1 - Allow for citizen settings per destination to help
                // with pathing issues.
                switch (oCurDest.citizens_AvoidWater) {
                case False:
                    npc.getNavigator().getLocalParameters().avoidWater(false);
                    break;
                case NotSet:
                    npc.getNavigator().getLocalParameters().avoidWater(trait.citizens_AvoidWater);
                    break;
                case True:
                    npc.getNavigator().getLocalParameters().avoidWater(true);
                    break;

                }

                switch (oCurDest.citizens_Swim) {
                case False:
                    npc.data().set("swim", false);
                    break;
                case NotSet:
                    npc.data().set("swim", trait.citizens_Swim);
                    break;
                case True:
                    npc.data().set("swim", true);
                    break;
                }

                /*
                 * 1.25 - Removed. Stick with the old pathfinder.
                 * 
                 * 
                 * switch (oCurDest.citizens_NewPathFinder) { case False:
                 * npc.getNavigator().getLocalParameters().useNewPathfinder(
                 * false); break; case NotSet:
                 * npc.getNavigator().getLocalParameters().useNewPathfinder(
                 * trait.citizens_NewPathFinder); break; case True:
                 * npc.getNavigator().getLocalParameters().useNewPathfinder(true
                 * ); break; }
                 */

                switch (oCurDest.citizens_DefaultStuck) {
                case False:
                case NotSet:
                    npc.getNavigator().getLocalParameters().stuckAction(trait.citizens_DefaultStuck ? TeleportStuckAction.INSTANCE : null);
                    break;
                case True:
                    npc.getNavigator().getLocalParameters().stuckAction(TeleportStuckAction.INSTANCE);
                    break;
                }

                npc.getNavigator().getLocalParameters().distanceMargin(oCurDest.citizens_DistanceMargin < 0D ? trait.citizens_DistanceMargin : oCurDest.citizens_DistanceMargin);
                npc.getNavigator().getLocalParameters().pathDistanceMargin(oCurDest.citizens_PathDistanceMargin < 0D ? trait.citizens_PathDistanceMargin : oCurDest.citizens_PathDistanceMargin);
            } else {
                npc.data().set("swim", trait.citizens_Swim);

                npc.getNavigator().getLocalParameters().avoidWater(trait.citizens_AvoidWater);
                npc.getNavigator().getLocalParameters().useNewPathfinder(trait.citizens_NewPathFinder);
                npc.getNavigator().getLocalParameters().stuckAction(trait.citizens_DefaultStuck ? TeleportStuckAction.INSTANCE : null);

                npc.getNavigator().getLocalParameters().distanceMargin(trait.citizens_DistanceMargin);
                npc.getNavigator().getLocalParameters().pathDistanceMargin(trait.citizens_PathDistanceMargin);
            }

            for (DebugTarget debugOutput : destRef.debugTargets) {
                if (debugOutput.getTargets().size() == 0 || debugOutput.getTargets().contains(npc.getId())) {
                    Player debugTarget = (Player) debugOutput.targetSender;
                    if (debugTarget.isOnline()) {
                        debugOutput.removeDebugBlockSent(trait.getPendingDestinations().get(0));
                    }
                }
            }

            /*
             * Removed 1.25 - Sticking with the old path finder
             * 
             * Path overrides to fix npc spinning
             * 
             * boolean needsOldPathing = false; boolean hasDoors = false; for
             * (int ncnt = 0; ncnt < 3; ncnt++) { if
             * ((trait.pendingDestinations.size() - 1) >= ncnt) { Location
             * locHist = trait.pendingDestinations.get(ncnt); switch
             * (locHist.getBlock().getType().toString()) { case "GRASS_PATH":
             * case "SOIL": case "SOUL_SAND": needsOldPathing = true; default:
             * break; } if (locHist.clone().add(0, 1,
             * 0).getBlock().getType().toString().toLowerCase().contains("door")
             * ) { needsOldPathing = false; hasDoors = true; }
             * 
             * }
             * 
             * if ((trait.processedDestinations.size() - 1) >= ncnt) { Location
             * locHist =
             * trait.processedDestinations.get((trait.processedDestinations.size
             * () - 1) - ncnt); switch (locHist.getBlock().getType().toString())
             * { case "GRASS_PATH": case "SOIL": case "SOUL_SAND":
             * needsOldPathing = true; default: break; } if
             * (locHist.clone().add(0, 1,
             * 0).getBlock().getType().toString().toLowerCase().contains("door")
             * ) { needsOldPathing = false; hasDoors = true; }
             * 
             * }
             * 
             * }
             */

            if (destRef.getPathClass.requiresOpening(trait.getPendingDestinations().get(0).clone())) {
                Location destLoc = trait.getPendingDestinations().get(0).clone().add(0, 1, 0);

                // We need to look at the door
                net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), trait.getPendingDestinations().get(0).clone().add(0, 1, 0));
                Location newLoc = trait.getPendingDestinations().get(0).clone().add(0.5, 1, 0.5);
                newLoc.setYaw(npc.getEntity().getLocation().getYaw());
                newLoc.setPitch(npc.getEntity().getLocation().getPitch());
                npc.teleport(newLoc, TeleportCause.PLUGIN);

                if (npc.getEntity().getLocation().distanceSquared(trait.getPendingDestinations().get(0).clone().add(0, 1, 0)) > 1) {
                    Vector v = destLoc.subtract(npc.getEntity().getLocation().clone()).toVector().normalize();
                    npc.getEntity().setVelocity(v.multiply(0.5));
                    return false;
                } else {
                    net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                    Vector v = destLoc.subtract(npc.getEntity().getLocation().clone()).toVector().normalize();
                    npc.getEntity().setVelocity(v.multiply(0.5));
                    trait.removePendingDestination(0);
                    if (trait.pendingDestinations.size() < 1)
                        return false;
                }
            }

            // if (needsOldPathing || (trait.pendingDestinations.size() < 4 &&
            // !hasDoors)) {
            npc.getNavigator().getLocalParameters().avoidWater(true);
            npc.getNavigator().getLocalParameters().useNewPathfinder(false);
            npc.getNavigator().getLocalParameters().stuckAction(TeleportStuckAction.INSTANCE);
            npc.getNavigator().getLocalParameters().distanceMargin(0.5D);
            npc.getNavigator().getLocalParameters().pathDistanceMargin(0.5D);
            /*
             * } else {
             * npc.getNavigator().getLocalParameters().avoidWater(true);
             * npc.getNavigator().getLocalParameters().useNewPathfinder(false);
             * npc.getNavigator().getLocalParameters().stuckAction(
             * TeleportStuckAction.INSTANCE);
             * npc.getNavigator().getLocalParameters().distanceMargin(1D);
             * npc.getNavigator().getLocalParameters().pathDistanceMargin(1D); }
             */

            // Loop and validate the angle from the NPC to see how far we can go
            // on the next target, improve the goofy look all over issue
            Double pathAngle = Double.MAX_VALUE;
            int maxDist = 0;
            Location lastLocation = new Location(npc.getEntity().getWorld(), npc.getEntity().getLocation().getBlockX(), npc.getEntity().getLocation().getBlockY(), npc.getEntity().getLocation().getBlockZ());

            do {
                if (maxDist > trait.getPendingDestinations().size())
                    break;
                if (maxDist > 10)
                    break;

                Vector locVect = lastLocation.toVector().subtract(trait.getPendingDestinations().get(0).toVector()).normalize();
                Vector npcVect = npc.getEntity().getLocation().getDirection();
                double angle = Math.acos(locVect.dot(npcVect));

                if (destRef.getPathClass.requiresOpening(trait.getPendingDestinations().get(0).clone()))
                    break;

                if (maxDist < 2) {
                    pathAngle = Math.toDegrees(angle);
                    lastLocation = trait.getPendingDestinations().get(0).clone();
                    trait.processedDestinations.add(trait.getPendingDestinations().get(0));
                    trait.removePendingDestination(0);
                    maxDist++;
                } else {
                    if (Math.toDegrees(angle) != Math.abs(pathAngle)) {
                        break;
                    }
                    pathAngle = Math.toDegrees(angle);
                    lastLocation = trait.getPendingDestinations().get(0).clone();
                    trait.processedDestinations.add(trait.getPendingDestinations().get(0));
                    trait.removePendingDestination(0);
                    maxDist++;
                }
            } while (true);

            if (lastLocation == null) {
                lastLocation = trait.getPendingDestinations().get(0).clone();
                trait.processedDestinations.add(trait.getPendingDestinations().get(0));
                trait.removePendingDestination(0);
            }

            npc.getNavigator().setTarget(lastLocation.add(0, 1, 0));
            trait.lastNavigationPoint = lastLocation.clone().add(0, 1, 0);

            trait.lastPositionChange = new Date();
            return false;
        }

        // Check if we have any locations to move to.
        Destination_Setting oLoc = trait.GetCurrentLocation();
        if (oLoc == null) {
            return false;
        }

        // is there a timeout lock?
        if (trait.locationLockUntil == null) {
            Random random = new Random();
            if (trait.currentLocation.Time_Minimum > 0) {
                // need to set the time out.
                int nWaitTime = random.nextInt((trait.currentLocation.Time_Maximum - trait.currentLocation.Time_Minimum) + 1) + trait.currentLocation.Time_Minimum;
                trait.locationLockUntil = new java.util.Date(System.currentTimeMillis() + (nWaitTime * 1000));
                trait.setCurrentAction(en_CurrentAction.IDLE);
                if (destRef.debugTargets != null) {
                    destRef.getMessageManager.sendDebugMessage("destinations", "Debug_Messages.goal_timeddestination", npc, trait);
                }
            }
        }

        // Is this NPC being monitored by a plugin?
        if (trait.monitoredLocation != null && trait.monitoredLocation.LocationIdent == oLoc.LocationIdent) {
            // return and do nothing. Let the plugin manage this location
            return false;
        }

        // Main movement thread
        boolean processWander = false;
        if (oLoc.getWanderingDistance() > 0 && !npc.getNavigator().isNavigating() && npc.getEntity().getLocation().distanceSquared(oLoc.destination) <= oLoc.getWanderingDistanceSquared())
            processWander = true;
        if (!oLoc.Wandering_Region.equals("") && !npc.getNavigator().isNavigating() && destRef.getWorldGuardPlugin != null)
            processWander = true;

        if (processWander) {
            Random random = new Random();
            if (trait.locationLockUntil == null && trait.getCurrentAction().equals(en_CurrentAction.RANDOM_MOVEMENT)) {
                int nWaitTime = 1;
                if (trait.currentLocation.Wait_Maximum == 0 || trait.currentLocation.Wait_Minimum == 0) {
                    nWaitTime = 1;
                } else {
                    int nextInt = (trait.currentLocation.Wait_Maximum - trait.currentLocation.Wait_Minimum) + 1;
                    if (nextInt < 2)
                        nWaitTime = 1;
                    else
                        nWaitTime = random.nextInt(nextInt) + trait.currentLocation.Wait_Minimum;
                }

                trait.locationLockUntil = new java.util.Date(System.currentTimeMillis() + (nWaitTime * 1000));
                trait.setCurrentAction(en_CurrentAction.PATH_FOUND);
                destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|SetLockedTime:" + trait.locationLockUntil);
                return true;
            } else if (!oLoc.Wandering_Region.equals("") && destRef.getWorldGuardPlugin != null) {

                // Get the region based on the name.
                Location[] regionPoints = destRef.getWorldGuardPlugin.getRegionBounds(npc.getEntity().getWorld(), oLoc.Wandering_Region);
                if (regionPoints.length == 0) {
                    // bad region, do nothing.
                    return true;
                }
                trait.lastLocation = trait.currentLocation;
                int nTrys = 0;
                while (nTrys < 50) {
                    Location oNewDest = new Location(npc.getEntity().getLocation().getWorld(), regionPoints[0].getBlockX(), npc.getEntity().getLocation().getBlockY(), regionPoints[0].getBlockZ());
                    destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|oNewDest.add(random.nextInt((int)" + (oLoc.getWanderingDistance() * 2) + "-" + oLoc
                            .getWanderingDistance() + ", 0, random.nextInt((int)" + (oLoc.getWanderingDistance() * 2) + "-" + oLoc.getWanderingDistance() + ")");
                    oNewDest.add(random.nextInt(regionPoints[1].getBlockX() - regionPoints[0].getBlockX()), 0, random.nextInt(regionPoints[1].getBlockZ() - regionPoints[0].getBlockZ()));
                    for (byte y = -3; y <= 2; y++) {

                        if (destRef.getPlotSquared != null) {
                            if (!destRef.getPlotSquared.locationInSamePlotAsNPC(npc, oNewDest))
                                continue;
                        }

                        if (destRef.getWorldGuardPlugin.isInRegion(oNewDest, oLoc.Wandering_Region)) {
                            if (destRef.getPathClass.isLocationWalkable(oNewDest.getBlock().getRelative(0, y, 0).getLocation())) {
                                if (oLoc.Wandering_UseBlocks && trait.AllowedPathBlocks != null && trait.AllowedPathBlocks.size() > 0) {
                                    if (trait.AllowedPathBlocks.contains(oNewDest.getBlock().getRelative(0, y, 0).getLocation().getBlock().getType())) {
                                        trait.lastPositionChange = new Date();
                                        trait.setCurrentAction(en_CurrentAction.RANDOM_MOVEMENT);
                                        trait.locationLockUntil = null;

                                        trait.setCurrentLocation(oLoc);
                                        destRef.getPathClass.addToQueue(npc, trait, npc.getEntity().getLocation().add(0.D, -1.0D, 0.0D), oNewDest.getBlock().getRelative(0, y, 0).getLocation(), destRef.maxDistance, trait.AllowedPathBlocks,
                                                trait.blocksUnderSurface, trait.OpensGates, trait.OpensWoodDoors, trait.OpensMetalDoors, "Destinations.Goal.Random");
                                        return true;
                                    }
                                } else {
                                    trait.lastPositionChange = new Date();
                                    trait.setCurrentAction(en_CurrentAction.RANDOM_MOVEMENT);
                                    trait.locationLockUntil = null;

                                    trait.setCurrentLocation(oLoc);

                                    destRef.getPathClass.addToQueue(npc, trait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), oNewDest.getBlock().getRelative(0, y, 0).getLocation(), destRef.maxDistance, new ArrayList<Material>(), 0,
                                            true, true, true, "Destinations.Goal.Random");
                                }
                                return true;
                            }
                        }
                    }
                    nTrys++;
                }
            } else {
                // Continue the random movement
                trait.lastLocation = trait.currentLocation;
                int nTrys = 0;
                while (nTrys < 50) {
                    Location oNewDest = new Location(npc.getEntity().getLocation().getWorld(), npc.getEntity().getLocation().getBlockX(), npc.getEntity().getLocation().getBlockY(), npc.getEntity().getLocation().getBlockZ());
                    destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|oNewDest.add(random.nextInt((int)" + (oLoc.getWanderingDistance() * 2) + "-" + oLoc
                            .getWanderingDistance() + ", 0, random.nextInt((int)" + (oLoc.getWanderingDistance() * 2) + "-" + oLoc.getWanderingDistance() + ")");
                    oNewDest.add(random.nextInt((int) oLoc.getWanderingDistance() * 2) - oLoc.getWanderingDistance(), 0, random.nextInt((int) oLoc.getWanderingDistance() * 2) - oLoc.getWanderingDistance());
                    if (oLoc.destination.distanceSquared(oNewDest) <= oLoc.getWanderingDistanceSquared()) {
                        for (byte y = -3; y <= 2; y++) {

                            if (destRef.getPlotSquared != null) {
                                if (!destRef.getPlotSquared.locationInSamePlotAsNPC(npc, oNewDest))
                                    continue;
                            }

                            if (destRef.getPathClass.isLocationWalkable(oNewDest.getBlock().getRelative(0, y, 0).getLocation(), false, false, false)) {
                                if (oLoc.Wandering_UseBlocks && trait.AllowedPathBlocks != null && trait.AllowedPathBlocks.size() > 0) {
                                    if (trait.AllowedPathBlocks.contains(oNewDest.getBlock().getRelative(0, y, 0).getLocation().getBlock().getType())) {
                                        trait.lastPositionChange = new Date();
                                        trait.setCurrentAction(en_CurrentAction.RANDOM_MOVEMENT);
                                        trait.locationLockUntil = null;

                                        trait.setCurrentLocation(oLoc);
                                        destRef.getPathClass.addToQueue(npc, trait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), oNewDest.getBlock().getRelative(0, y, 0).getLocation(), destRef.maxDistance, trait.AllowedPathBlocks,
                                                trait.blocksUnderSurface, trait.OpensGates, trait.OpensWoodDoors, trait.OpensMetalDoors, "Destinations.Goal.Random");
                                        return true;
                                    }
                                } else {
                                    trait.lastPositionChange = new Date();
                                    trait.setCurrentAction(en_CurrentAction.RANDOM_MOVEMENT);
                                    trait.locationLockUntil = null;

                                    trait.setCurrentLocation(oLoc);

                                    destRef.getPathClass.addToQueue(npc, trait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), oNewDest.getBlock().getRelative(0, y, 0).getLocation(), destRef.maxDistance, new ArrayList<Material>(), 0,
                                            true, true, true, "Destinations.Goal.Random");
                                }
                                return true;
                            }
                        }
                    }
                    nTrys++;
                }
            }
        } else if (trait.getCurrentAction().equals(en_CurrentAction.RANDOM_MOVEMENT) && !npc.getNavigator().isNavigating()) {
            if (npc.getEntity().getLocation() != null && trait.currentLocation.destination != null && trait.lastLocation.destination != null) {
                if (npc.getEntity().getLocation().distanceSquared(trait.lastLocation.destination) > 3 && trait.currentLocation.destination != trait.lastLocation.destination) {
                    destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|RandomWander");
                    trait.lastPositionChange = new Date();
                    destRef.getPathClass.addToQueue(npc, trait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), (trait.lastLocation != null ? trait.lastLocation.destination : trait.currentLocation.destination), destRef.maxDistance,
                            new ArrayList<Material>(), 0, true, true, true, "Destinations.Goal.Random");
                } else {
                    trait.setCurrentAction(en_CurrentAction.IDLE);
                }
            } else {
                trait.setCurrentAction(en_CurrentAction.IDLE);
            }
            return false;
        } else if (trait.getCurrentAction().equals(en_CurrentAction.RANDOM_MOVEMENT) && npc.getNavigator().isNavigating()) {
            return false;
        }

        if (!npc.isSpawned() || oLoc.destination == null)
            return false;

        Double nDist = Double.valueOf(npc.getEntity().getLocation().distanceSquared(oLoc.destination));
        if (nDist.doubleValue() > oLoc.getMaxDistanceSquared()) {
            trait.setCurrentAction(en_CurrentAction.PATH_HUNTING);
            if ((trait.TeleportOnNoPath.booleanValue()) && (trait.lastResult.startsWith("unable to find a path"))) {
                citGoal.nFailedPathCount += 1;
                if (citGoal.nFailedPathCount > 2) {
                    trait.lastResult = "Failed to locate a valid path to destination";
                    citGoal.nFailedPathCount = 0;
                    teleportSurface(npc.getEntity(), oLoc.destination.clone().add(0, 1, 0));
                    trait.locationReached();
                    if (destRef.debugTargets != null)
                        destRef.getMessageManager.sendDebugMessage("destinations", "Debug_Messages.path_novalidpath", npc, trait);
                    destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|NoValidPath");
                    return false;
                }
                trait.lastResult = "Failed to locate a valid path to destination";
            }
            int nCnt;
            if (npc.getEntity().getLocation().getBlock().isLiquid()) {
                nCnt = 1;
                for (;;) {
                    if ((npc.getEntity().getLocation().add(0.0D, nCnt, 0.0D).getBlock().isEmpty()) && (npc.getEntity().getLocation().add(0.0D, nCnt + 1, 0.0D).getBlock().isEmpty())) {
                        teleportSurface(npc.getEntity(), npc.getEntity().getLocation().add(0.5D, nCnt, 0.5D));
                        break;
                    }
                    nCnt++;
                }
            }
            trait.lastLocation = trait.currentLocation;

            if (trait.currentLocation == null)
                fireLocationChangedEvent(trait, oLoc);

            trait.setCurrentLocation(oLoc);

            // V1.33 - Skins (Change the skin if the new one has a skin set and
            // is set to false
            if (!oLoc.player_Skin_Name.isEmpty() && !oLoc.player_Skin_ApplyOnArrival && (npc.getEntity() instanceof Player) && !npc.getNavigator().isNavigating()) {

                destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|SKINCOMPARE|\r\n" + npc.data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA).toString() + "\r\n"
                        + oLoc.player_Skin_Texture_Metadata);
                if (!npc.data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA).toString().equals(oLoc.player_Skin_Texture_Metadata) && !npc.data().get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA).toString().equals(
                        oLoc.player_Skin_Texture_Signature)) {
                    npc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA);
                    npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA);
                    npc.data().remove(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA);
                    npc.data().remove("cached-skin-uuid-name");
                    npc.data().remove("cached-skin-uuid");
                    npc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA);

                    // Set the skin
                    npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, false);
                    npc.data().set("cached-skin-uuid-name", oLoc.player_Skin_Name);
                    npc.data().set("cached-skin-uuid", oLoc.player_Skin_UUID);
                    npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, oLoc.player_Skin_Name);
                    npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, oLoc.player_Skin_Texture_Metadata);
                    npc.data().setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, oLoc.player_Skin_Texture_Signature);

                    if (npc.isSpawned()) {

                        SkinnableEntity skinnable = npc.getEntity() instanceof SkinnableEntity ? (SkinnableEntity) npc.getEntity() : null;
                        if (skinnable != null) {
                            Skin.get(skinnable).applyAndRespawn(skinnable);

                        }
                    }
                }
            }

            destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|NewPathFinding: " + oLoc.destination.toString());

            destRef.getPathClass.addToQueue(npc, trait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), new Location(oLoc.destination.getWorld(), oLoc.destination.getBlockX(), oLoc.destination.getBlockY(), oLoc.destination
                    .getBlockZ()).add(0.5D, 0, 0.5D), destRef.maxDistance, trait.AllowedPathBlocks, trait.blocksUnderSurface, trait.OpensGates, trait.OpensWoodDoors, trait.OpensMetalDoors, "Destinations.Goal.Destination");

            trait.setCurrentAction(en_CurrentAction.TRAVELING);
        } else if (trait.getCurrentAction() == en_CurrentAction.PATH_FOUND && trait.getPendingDestinations().size() == 0) {
            // path ended
            destRef.getMessageManager.debugMessage(Level.FINEST, "NPCDestinations_Goal.shouldExecute()|NPC:" + npc.getId() + "|PathEnded-found");
            teleportSurface(npc.getEntity(), trait.currentLocation.destination.clone().add(0, 0, 0), TeleportCause.PLUGIN);
            trait.setCurrentAction(en_CurrentAction.IDLE);
            this.locationReached(trait);
            trait.lastLocation = trait.currentLocation;
            trait.setCurrentLocation(oLoc);

        } else if (trait.getCurrentAction() == en_CurrentAction.TRAVELING && trait.getPendingDestinations().size() == 0) {
            Vector vel = trait.getNPC().getEntity().getVelocity();
            if (Math.abs(vel.getX()) > 0.05 || Math.abs(vel.getY()) > 0.1 || Math.abs(vel.getZ()) > 0.05) {
                // Still moving
                return false;
            }
            // path ended
            trait.setCurrentAction(en_CurrentAction.IDLE);
            this.locationReached(trait);
            trait.lastLocation = trait.currentLocation;
            trait.setCurrentLocation(oLoc);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void load(NPCDestinationsTrait trait, DataKey key) {
        int nCnt = 0;

        if (trait.AllowedPathBlocks == null)
            trait.AllowedPathBlocks = new ArrayList<Material>();

        while (true) {
            if (key.keyExists("AllowedBlocks." + nCnt)) {
                String materialType = key.getString("AllowedBlocks." + nCnt);

                try {
                    trait.AllowedPathBlocks.add(Material.getMaterial(materialType));
                } catch (Exception err) {
                    // Just don't add this material
                }
            } else {
                break;
            }
            nCnt++;
        }
        nCnt = 0;

        trait.citizens_Swim = key.getBoolean("Destinations.citizens_Swim", true);
        trait.citizens_NewPathFinder = key.getBoolean("Destinations.citizens_NewPathFinder", true);
        trait.citizens_AvoidWater = key.getBoolean("Destinations.citizens_AvoidWater", true);
        trait.citizens_DistanceMargin = key.getDouble("Destinations.citizens_DistanceMargin", 1D);
        trait.citizens_PathDistanceMargin = key.getDouble("Destinations.citizens_PathDistanceMargin ", 1D);
        trait.maxProcessingTime = key.getInt("Destinations.maxprocessingtime", -1);

        // To convert the blocks under to the new format.
        if (key.keyExists("LookOneBlockDown")) {
            if (key.getBoolean("LookOneBlockDown")) {
                trait.blocksUnderSurface = 1;
            } else {
                trait.blocksUnderSurface = 0;
            }
            key.removeKey("LookOneBlockDown");
        }

        while (true) {
            if (key.getString("Destinations." + nCnt) != "") {
                Destination_Setting oLoc = null;
                try {
                    oLoc = new Destination_Setting();
                    if (key.getString("Destinations." + nCnt + ".locationid", "").trim().isEmpty()) {
                        oLoc.LocationIdent = UUID.randomUUID();
                    } else {
                        oLoc.LocationIdent = UUID.fromString(key.getString("Destinations." + nCnt + ".locationid"));
                    }
                    oLoc.destination = new Location(destRef.getServer().getWorld(key.getString("Destinations." + nCnt + ".Location.world")), key.getInt("Destinations." + nCnt + ".Location.x"), key.getInt("Destinations." + nCnt
                            + ".Location.y"), key.getInt("Destinations." + nCnt + ".Location.z"), Float.parseFloat(key.getString("Destinations." + nCnt + ".Location.yaw", "0.0")), Float.parseFloat(key.getString("Destinations." + nCnt
                                    + ".Location.pitch", "0.0")));
                    oLoc.destination.add(0.5, 0, 0.5);
                    oLoc.setMaxDistance(key.getDouble("Destinations." + nCnt + ".MaxDistance"));
                    oLoc.Probability = key.getInt("Destinations." + nCnt + ".Probability.ChancePercent");
                    oLoc.Time_Minimum = key.getInt("Destinations." + nCnt + ".Probability.Min_Time");
                    oLoc.Time_Maximum = key.getInt("Destinations." + nCnt + ".Probability.Max_Time");
                    oLoc.TimeOfDay = key.getInt("Destinations." + nCnt + ".TimeOfDay") == 0 ? 1 : key.getInt("Destinations." + nCnt + ".TimeOfDay");
                    oLoc.Wait_Maximum = key.getInt("Destinations." + nCnt + ".WanderSettings.Wait_Maximum");
                    oLoc.Wait_Minimum = key.getInt("Destinations." + nCnt + ".WanderSettings.Wait_Minimum");
                    oLoc.setWanderingDistance(key.getDouble("Destinations." + nCnt + ".WanderSettings.Wandering_Distance"));
                    oLoc.Alias_Name = key.getString("Destinations." + nCnt + ".AliasName", "");

                    oLoc.Wandering_UseBlocks = key.getBoolean("Destinations." + nCnt + ".UseBlockSetting", false);

                    // 1.29 Weather
                    oLoc.WeatherFlag = key.getInt("Destinations." + nCnt + ".WeatherFlag", 0);

                    oLoc.Wandering_Region = "";

                    if (key.keyExists("Destinations." + nCnt + ".WanderSettings.Wandering_Region"))
                        oLoc.Wandering_Region = key.getString("Destinations." + nCnt + ".WanderSettings.Wandering_Region", "");

                    if (key.keyExists("Destinations." + nCnt + ".WanderSettings.Use_Blocks"))
                        oLoc.Wandering_UseBlocks = key.getBoolean("Destinations." + nCnt + ".Use_Blocks");

                    if (key.keyExists("Destinations." + nCnt + ".Items.Head"))
                        oLoc.items_Head = (ItemStack) key.getRaw("Destinations." + nCnt + ".Items.Head");
                    if (key.keyExists("Destinations." + nCnt + ".Items.Chest"))
                        oLoc.items_Chest = (ItemStack) key.getRaw("Destinations." + nCnt + ".Items.Chest");
                    if (key.keyExists("Destinations." + nCnt + ".Items.Legs"))
                        oLoc.items_Legs = (ItemStack) key.getRaw("Destinations." + nCnt + ".Items.Legs");
                    if (key.keyExists("Destinations." + nCnt + ".Items.Boots"))
                        oLoc.items_Boots = (ItemStack) key.getRaw("Destinations." + nCnt + ".Items.Boots");
                    if (key.keyExists("Destinations." + nCnt + ".Items.Hand"))
                        oLoc.items_Hand = (ItemStack) key.getRaw("Destinations." + nCnt + ".Items.Hand");
                    if (key.keyExists("Destinations." + nCnt + ".Items.OffHand"))
                        oLoc.items_Offhand = (ItemStack) key.getRaw("Destinations." + nCnt + ".Items.OffHand");
                    if (key.keyExists("Destinations." + nCnt + ".Items.Clear"))
                        oLoc.items_Clear = key.getBoolean("Destinations." + nCnt + ".Items.Clear", false);

                    // V1.33 - Skins
                    oLoc.player_Skin_Name = key.getString("Destinations." + nCnt + ".Skin.Name", "");
                    oLoc.player_Skin_UUID = key.getString("Destinations." + nCnt + ".Skin.UUID", "");
                    oLoc.player_Skin_ApplyOnArrival = key.getBoolean("Destinations." + nCnt + ".Skin.ApplyOnArrival", false);
                    oLoc.player_Skin_Texture_Metadata = key.getString("Destinations." + nCnt + ".Skin.MetaData", "");
                    oLoc.player_Skin_Texture_Signature = key.getString("Destinations." + nCnt + ".Skin.Signature", "");

                    // 2.1.1 -- Citizens pathfinding changes.
                    if (key.keyExists("Destinations." + nCnt + ".Citizens.Swim"))
                        oLoc.citizens_Swim = key.getString("Destinations." + nCnt + ".Citizens.Swim").equalsIgnoreCase("true") ? TriBoolean.True : TriBoolean.False;
                    if (key.keyExists("Destinations." + nCnt + ".Citizens.NewPathfinder"))
                        oLoc.citizens_NewPathFinder = key.getString("Destinations." + nCnt + ".Citizens.NewPathfinder").equalsIgnoreCase("true") ? TriBoolean.True : TriBoolean.False;
                    if (key.keyExists("Destinations." + nCnt + ".Citizens.AvoidWater"))
                        oLoc.citizens_AvoidWater = key.getString("Destinations." + nCnt + ".Citizens.AvoidWater").equalsIgnoreCase("true") ? TriBoolean.True : TriBoolean.False;
                    if (key.keyExists("Destinations." + nCnt + ".Citizens.DefaultStuck"))
                        oLoc.citizens_DefaultStuck = key.getString("Destinations." + nCnt + ".Citizens.DefaultStuck").equalsIgnoreCase("true") ? TriBoolean.True : TriBoolean.False;
                    if (key.keyExists("Destinations." + nCnt + ".Citizens.DistanceMargin"))
                        oLoc.citizens_DistanceMargin = key.getDouble("Destinations." + nCnt + ".Citizens.DistanceMargin");
                    if (key.keyExists("Destinations." + nCnt + ".Citizens.PathDistance"))
                        oLoc.citizens_PathDistanceMargin = key.getDouble("Destinations." + nCnt + ".Citizens.PathDistance");

                    // V1.44 - Commands
                    oLoc.arrival_Commands = new ArrayList<String>();
                    if (key.keyExists("Destinations." + nCnt + ".Commands.arrival"))
                        oLoc.arrival_Commands = (ArrayList<String>) key.getRaw("Destinations." + nCnt + ".Commands.arrival");

                    // V1.50 - Location pausing
                    if (key.keyExists("Destinations." + nCnt + ".Pause.Distance"))
                        oLoc.Pause_Distance = key.getInt("Destinations." + nCnt + ".Pause.Distance", -1);
                    if (key.keyExists("Destinations." + nCnt + ".Pause.TimeOut"))
                        oLoc.Pause_Timeout = key.getInt("Destinations." + nCnt + ".Pause.TimeOut", -1);
                    if (key.keyExists("Destinations." + nCnt + ".Pause.Type"))
                        oLoc.Pause_Type = key.getString("Destinations." + nCnt + ".Pause.Type", "ALL");

                    if (!key.keyExists("Destinations." + nCnt + ".PluginSettings")) {
                        key.setString("Destinations." + nCnt + ".PluginSettings", "");
                    }

                    for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
                        if (plugin.getActionName().equalsIgnoreCase("sentinel")) {
                            // V2.1.X - Convert to the internal plugin based
                            // addon
                            if (key.keyExists("Destinations." + nCnt + ".Sentinel") && !key.getString("Destinations." + nCnt + ".Sentinel.lastSet", "0").equals("0")) {
                                destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_conversion", plugin.getActionName());
                                try {
                                    plugin.onLocationLoading(trait.getNPC(), trait, oLoc, key.getRelative("Destinations." + nCnt));
                                } catch (Exception err) {
                                    StringWriter sw = new StringWriter();
                                    err.printStackTrace(new PrintWriter(sw));
                                    destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_debug", err.getMessage() + "\n" + sw.toString());
                                }

                                for (DataKey storageKey : key.getRelative("Destinations." + nCnt + ".Sentinel").getSubKeys()) {
                                    key.removeKey("Destinations." + nCnt + ".Sentinel." + storageKey.name());
                                }
                                key.removeKey("Destinations." + nCnt + ".Sentinel");
                            } else {
                                try {
                                    plugin.onLocationLoading(trait.getNPC(), trait, oLoc, key.getRelative("Destinations." + nCnt + ".PluginSettings"));
                                } catch (Exception err) {
                                    StringWriter sw = new StringWriter();
                                    err.printStackTrace(new PrintWriter(sw));
                                    destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_debug", err.getMessage() + "\n" + sw.toString());
                                }
                            }
                        } else if (plugin.getActionName().equalsIgnoreCase("jobsreborn")) {
                            // V2.1.X - Convert to the internal plugin based
                            // jobs from the old location
                            if (key.keyExists("Destinations." + nCnt + ".JobsReborn") && !key.getString("Destinations." + nCnt + ".JobsReborn.JobName", "").equals("")) {
                                destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_conversion", plugin.getActionName());
                                try {
                                    plugin.onLocationLoading(trait.getNPC(), trait, oLoc, key.getRelative("Destinations." + nCnt));
                                } catch (Exception err) {
                                    StringWriter sw = new StringWriter();
                                    err.printStackTrace(new PrintWriter(sw));
                                    destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_debug", err.getMessage() + "\n" + sw.toString());
                                }

                                for (DataKey storageKey : key.getRelative("Destinations." + nCnt + ".JobsReborn").getSubKeys()) {
                                    key.removeKey("Destinations." + nCnt + ".JobsReborn." + storageKey.name());
                                }
                                key.removeKey("Destinations." + nCnt + ".JobsReborn");
                            } else {

                                try {
                                    plugin.onLocationLoading(trait.getNPC(), trait, oLoc, key.getRelative("Destinations." + nCnt + ".PluginSettings"));
                                } catch (Exception err) {
                                    StringWriter sw = new StringWriter();
                                    err.printStackTrace(new PrintWriter(sw));
                                    destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_debug", err.getMessage() + "\n" + sw.toString());
                                }
                            }
                        } else {
                            try {
                                plugin.onLocationLoading(trait.getNPC(), trait, oLoc, key.getRelative("Destinations." + nCnt + ".PluginSettings"));
                            } catch (Exception err) {
                                StringWriter sw = new StringWriter();
                                err.printStackTrace(new PrintWriter(sw));
                                destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_debug", err.getMessage() + "\n" + sw.toString());
                            }
                        }
                    }
                } catch (Exception err) {
                    destRef.getMessageManager.logToConsole(destRef, "Failure Loading NPC. " + trait.getNPC().getId());
                    StringWriter sw = new StringWriter();
                    err.printStackTrace(new PrintWriter(sw));
                    destRef.getMessageManager.logToConsole(destRef, sw.toString());
                    break;
                }

                if (oLoc.destination != null) {
                    trait.NPCLocations.add(oLoc);
                }
            } else {
                break;
            }
            nCnt++;
        }

        if (key.keyExists("enabledplugins")) {
            trait.enabledPlugins = (ArrayList<String>) key.getRaw("enabledplugins");
        }
    }

    private void save(NPCDestinationsTrait trait, DataKey key) {
        key.removeKey("Destinations");
        key.removeKey("AllowedBlocks");

        for (int n = 0; n < trait.AllowedPathBlocks.size(); n++) {
            key.setString("AllowedBlocks." + n, trait.AllowedPathBlocks.get(n).name());
        }
        key.setRaw("enabledplugins", trait.enabledPlugins);

        key.setBoolean("Destinations.citizens_Swim", trait.citizens_Swim);
        key.setBoolean("Destinations.citizens_NewPathFinder", trait.citizens_NewPathFinder);
        key.setBoolean("Destinations.citizens_AvoidWater", trait.citizens_AvoidWater);
        key.setDouble("Destinations.citizens_DistanceMargin", trait.citizens_DistanceMargin);
        key.setDouble("Destinations.citizens_PathDistanceMargin", trait.citizens_PathDistanceMargin);
        key.setInt("Destinations.maxprocessingtime", trait.maxProcessingTime);

        int i = 0;
        for (int nCnt = 0; nCnt < trait.NPCLocations.size(); nCnt++) {
            // Do not save any managed locations!
            if (trait.NPCLocations.get(i).managed_Location.length() == 0) {
                key.setString("Destinations." + i + ".locationid", trait.NPCLocations.get(i).LocationIdent.toString());
                key.setString("Destinations." + i + ".Location.world", trait.NPCLocations.get(i).destination.getWorld().getName());
                key.setInt("Destinations." + i + ".Location.x", trait.NPCLocations.get(i).destination.getBlockX());
                key.setInt("Destinations." + i + ".Location.y", trait.NPCLocations.get(i).destination.getBlockY());
                key.setInt("Destinations." + i + ".Location.z", trait.NPCLocations.get(i).destination.getBlockZ());
                key.setString("Destinations." + i + ".Location.pitch", trait.NPCLocations.get(i).destination.getPitch() + "");
                key.setString("Destinations." + i + ".Location.yaw", trait.NPCLocations.get(i).destination.getYaw() + "");
                key.setDouble("Destinations." + i + ".MaxDistance", trait.NPCLocations.get(i).getMaxDistance());
                key.setInt("Destinations." + i + ".Probability.ChancePercent", trait.NPCLocations.get(i).Probability);
                key.setInt("Destinations." + i + ".Probability.Min_Time", trait.NPCLocations.get(i).Time_Minimum);
                key.setInt("Destinations." + i + ".Probability.Max_Time", trait.NPCLocations.get(i).Time_Maximum);
                key.setInt("Destinations." + i + ".TimeOfDay", trait.NPCLocations.get(i).TimeOfDay);
                key.setInt("Destinations." + i + ".WanderSettings.Wait_Maximum", trait.NPCLocations.get(i).Wait_Maximum);
                key.setInt("Destinations." + i + ".WanderSettings.Wait_Minimum", trait.NPCLocations.get(i).Wait_Minimum);
                key.setString("Destinations." + i + ".WanderSettings.Wandering_Region", trait.NPCLocations.get(i).Wandering_Region);
                key.setDouble("Destinations." + i + ".WanderSettings.Wandering_Distance", trait.NPCLocations.get(i).getWanderingDistance());
                key.setBoolean("Destinations." + i + ".WanderSettings.Wandering_UseBlocks", trait.NPCLocations.get(i).Wandering_UseBlocks);
                key.setString("Destinations." + i + ".AliasName", trait.NPCLocations.get(i).Alias_Name);
                key.setBoolean("Destinations." + i + ".UseBlockSetting", trait.NPCLocations.get(i).Wandering_UseBlocks);
                key.setInt("Destinations." + i + ".WeatherFlag", trait.NPCLocations.get(i).WeatherFlag);

                key.setRaw("Destinations." + i + ".Items.Head", trait.NPCLocations.get(i).items_Head);
                key.setRaw("Destinations." + i + ".Items.Chest", trait.NPCLocations.get(i).items_Chest);
                key.setRaw("Destinations." + i + ".Items.Boots", trait.NPCLocations.get(i).items_Boots);
                key.setRaw("Destinations." + i + ".Items.Legs", trait.NPCLocations.get(i).items_Legs);
                key.setRaw("Destinations." + i + ".Items.OffHand", trait.NPCLocations.get(i).items_Offhand);
                key.setRaw("Destinations." + i + ".Items.Hand", trait.NPCLocations.get(i).items_Hand);
                key.setRaw("Destinations." + i + ".Items.Clear", trait.NPCLocations.get(i).items_Clear);

                // V1.33 - Skins
                key.setString("Destinations." + i + ".Skin.Name", trait.NPCLocations.get(i).player_Skin_Name);
                key.setString("Destinations." + i + ".Skin.UUID", trait.NPCLocations.get(i).player_Skin_UUID);
                key.setBoolean("Destinations." + i + ".Skin.ApplyOnArrival", trait.NPCLocations.get(i).player_Skin_ApplyOnArrival);
                key.setString("Destinations." + i + ".Skin.MetaData", trait.NPCLocations.get(i).player_Skin_Texture_Metadata);
                key.setString("Destinations." + i + ".Skin.Signature", trait.NPCLocations.get(i).player_Skin_Texture_Signature);

                // V2.1.1 - Citizens
                if (trait.NPCLocations.get(i).citizens_Swim != TriBoolean.NotSet)
                    key.setString("Destinations." + i + ".Citizens.Swim", trait.NPCLocations.get(i).citizens_Swim == TriBoolean.True ? "True" : "False");
                if (trait.NPCLocations.get(i).citizens_NewPathFinder != TriBoolean.NotSet)
                    key.setString("Destinations." + i + ".Citizens.NewPathfinder", trait.NPCLocations.get(i).citizens_NewPathFinder == TriBoolean.True ? "True" : "False");
                if (trait.NPCLocations.get(i).citizens_AvoidWater != TriBoolean.NotSet)
                    key.setString("Destinations." + i + ".Citizens.AvoidWater", trait.NPCLocations.get(i).citizens_AvoidWater == TriBoolean.True ? "True" : "False");
                if (trait.NPCLocations.get(i).citizens_DefaultStuck != TriBoolean.NotSet)
                    key.setString("Destinations." + i + ".Citizens.DefaultStuck", trait.NPCLocations.get(i).citizens_DefaultStuck == TriBoolean.True ? "True" : "False");
                if (trait.NPCLocations.get(i).citizens_DistanceMargin > -1D)
                    key.setDouble("Destinations." + i + ".Citizens.DistanceMargin", trait.NPCLocations.get(i).citizens_DistanceMargin);
                if (trait.NPCLocations.get(i).citizens_PathDistanceMargin > -1D)
                    key.setDouble("Destinations." + i + ".Citizens.PathDistance", trait.NPCLocations.get(i).citizens_PathDistanceMargin);

                // V1.44 - Commands
                key.setRaw("Destinations." + i + ".Commands.arrival", trait.NPCLocations.get(i).arrival_Commands);

                // V1.50 - Location pausing
                key.setInt("Destinations." + i + ".Pause.Distance", trait.NPCLocations.get(i).Pause_Distance);
                key.setInt("Destinations." + i + ".Pause.TimeOut", trait.NPCLocations.get(i).Pause_Timeout);
                key.setString("Destinations." + i + ".Pause.Type", trait.NPCLocations.get(i).Pause_Type);

                if (!key.keyExists("Destinations." + i + ".PluginSettings")) {
                    key.setString("Destinations." + i + ".PluginSettings", "");
                }

                for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
                    try {
                        plugin.onLocationSaving(trait.getNPC(), trait, trait.NPCLocations.get(i), key.getRelative("Destinations." + i + ".PluginSettings"));
                    } catch (Exception err) {
                        StringWriter sw = new StringWriter();
                        err.printStackTrace(new PrintWriter(sw));
                        destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_error", err.getMessage() + "\n" + sw.toString());
                    }
                }
                i++;
            }
        }
    }

    private void updateLighting(final int npcID) {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
        if (npc == null)
            return;

        NPCDestinationsTrait trait = npc.getTrait(NPCDestinationsTrait.class);
        if (trait == null)
            return;

        trait.lightTask = 0;
        if (!trait.getNPC().isSpawned()) {
            destRef.getLightPlugin.DeleteLight(trait.lastLighting_Loc);
            trait.lastLighting_Loc = null;
            trait.lastLighting_Time = null;
            return;
        }

        boolean startLightTask = false;
        int lightLevel = 0;
        Equipment locEquip = trait.getNPC().getTrait(Equipment.class);

        if (locEquip.get(EquipmentSlot.HAND) != null) {
            if (destRef.getMCUtils.isHoldingTorch(locEquip.get(EquipmentSlot.HAND).getType()) == inHandLightSource.REDSTONE_TORCH) {
                lightLevel = 7;
                startLightTask = true;
            } else if (destRef.getMCUtils.isHoldingTorch(locEquip.get(EquipmentSlot.HAND).getType()) == inHandLightSource.WOODEN_TORCH) {
                lightLevel = 14;
                startLightTask = true;
            }
        }
        if (destRef.Version >= 10900) {
            if (locEquip.get(EquipmentSlot.OFF_HAND) != null) {
                if (destRef.getMCUtils.isHoldingTorch(locEquip.get(EquipmentSlot.OFF_HAND).getType()) == inHandLightSource.REDSTONE_TORCH) {
                    lightLevel = 7;
                    startLightTask = true;
                } else if (destRef.getMCUtils.isHoldingTorch(locEquip.get(EquipmentSlot.OFF_HAND).getType()) == inHandLightSource.WOODEN_TORCH) {
                    lightLevel = 14;
                    startLightTask = true;
                }
            }
        }

        if (startLightTask) {
            if (trait.lastLighting_Loc != null && trait.lastLighting_Loc.distanceSquared(trait.getNPC().getEntity().getLocation()) > 5) {
                if (trait.lastLighting_Loc != null)
                    destRef.getLightPlugin.DeleteLight(trait.lastLighting_Loc);

                trait.lastLighting_Loc = trait.getNPC().getEntity().getLocation();
                trait.lastLighting_Time = new Date();
                destRef.getLightPlugin.CreateLight(trait.lastLighting_Loc, lightLevel);
            } else if (trait.lastLighting_Loc != null && (new Date().getTime() - trait.lastLighting_Time.getTime()) > 5000) {
                if (trait.lastLighting_Loc != null)
                    destRef.getLightPlugin.DeleteLight(trait.lastLighting_Loc);

                trait.lastLighting_Loc = trait.getNPC().getEntity().getLocation();
                trait.lastLighting_Time = new Date();
                destRef.getLightPlugin.CreateLight(trait.lastLighting_Loc, lightLevel);
            } else if (trait.lastLighting_Loc == null) {
                trait.lastLighting_Loc = trait.getNPC().getEntity().getLocation();
                trait.lastLighting_Time = new Date();
                destRef.getLightPlugin.CreateLight(trait.lastLighting_Loc, lightLevel);
            }

            trait.lightTask = Bukkit.getScheduler().scheduleSyncDelayedTask(destRef, new Runnable() {
                public void run() {
                    updateLighting(npcID);
                }
            }, 5);
        } else if (trait.lastLighting_Loc != null) {
            destRef.getLightPlugin.DeleteLight(trait.lastLighting_Loc);
            trait.lastLighting_Loc = null;
            trait.lastLighting_Time = null;
        }
    }

    public boolean fireLocationChangedEvent(NPCDestinationsTrait trait, Destination_Setting newDestination) {

        if (trait.currentLocation.LocationIdent == null || newDestination.LocationIdent == null) {
            return false;
        } else if (trait.currentLocation.LocationIdent.equals(newDestination.LocationIdent)) {
            return true;
        }

        // Notify all plugins that the location has been reached.
        boolean cancelProcessing = false;

        for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
            if (trait.enabledPlugins.contains(plugin.getActionName().toUpperCase())) {
                try {
                    if (plugin.onNewDestination(trait.getNPC(), trait, newDestination))
                        cancelProcessing = true;
                } catch (Exception err) {
                    StringWriter sw = new StringWriter();
                    err.printStackTrace(new PrintWriter(sw));
                    destRef.getMessageManager.consoleMessage(destRef, "destinations", "Console_Messages.plugin_error", err.getMessage() + "\n" + sw.toString());
                }
            }
        }

        // Fire the navigation event
        Navigation_NewDestination changeEvent = new Navigation_NewDestination(trait.getNPC(), newDestination, false);
        Bukkit.getServer().getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled() || cancelProcessing) {
            trait.lastPauseLocation = null;
            trait.lastPlayerPause = null;
            return true;
        }
        return false;
    }

    private void teleportSurface(Entity npc, Location loc, TeleportCause reason) {
        if (!destRef.getPathClass.isLocationWalkable(loc, false, false, false)) {
            if (!destRef.getPathClass.isLocationWalkable(loc.clone().add(0, 1, 0), false, false, false))
                npc.teleport(loc, reason);
            else
                npc.teleport(loc.clone().add(0, 1, 0), reason);
        } else {
            npc.teleport(loc, reason);
        }

    }

    private void teleportSurface(Entity npc, Location loc) {
        teleportSurface(npc, loc, TeleportCause.PLUGIN);
    }
}
