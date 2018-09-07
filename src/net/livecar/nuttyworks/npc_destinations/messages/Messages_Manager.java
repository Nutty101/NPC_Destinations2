package net.livecar.nuttyworks.npc_destinations.messages;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;
import net.livecar.nuttyworks.npc_destinations.DebugTarget;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

public class Messages_Manager {
    private List<LogDetail>    logHistory;
    private jsonChat           jsonManager;
    private DestinationsPlugin destRef = null;

    public Messages_Manager(DestinationsPlugin storageRef) {
        destRef = storageRef;
        jsonManager = new jsonChat(destRef);
    }

    public void consoleMessage(Plugin callingPlugin, String langFile, String msgKey) {
        consoleMessage(callingPlugin, langFile, msgKey, "");
    }

    public void consoleMessage(Plugin callingPlugin, String langFile, String msgKey, String extendedMessage) {
        if (msgKey.equals("console_messages.plugin_debug")) {
            if (destRef.debugTargets == null)
                return;
            for (DebugTarget debugOutput : destRef.debugTargets) {
                if (!(debugOutput.targetSender instanceof Player)) {
                    for (String message : buildMessage(langFile, msgKey.toLowerCase(), extendedMessage))
                        logToConsole(callingPlugin, message);
                }
            }
        } else {
            for (String message : buildMessage(langFile, msgKey.toLowerCase(), extendedMessage))
                logToConsole(callingPlugin, message);
        }
    }

    public void logToConsole(Plugin callingPlugin, String logLine) {
        Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + callingPlugin.getDescription().getName() + "] " + logLine);
    }

    public void debugMessage(Level debugLevel, String extendedMessage) {
        if (logHistory == null)
            logHistory = new ArrayList<LogDetail>();

        if (destRef.debugLogLevel.intValue() <= debugLevel.intValue()) {
            logHistory.add(new LogDetail("[" + debugLevel.toString() + "] " + extendedMessage));

            if (destRef.isEnabled()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(destRef, new Runnable() {
                    public void run() {
                        saveDebugMessages();
                    }
                }, 500);
            } else {
                saveDebugMessages();
            }
        }
    }

    private void saveDebugMessages() {
        if (logHistory != null && logHistory.size() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'.log'");
            try (FileWriter fileOut = new FileWriter(new File(destRef.loggingPath, dateFormat.format(logHistory.get(0).logDateTime)), true)) {
                for (LogDetail logLine : logHistory) {
                    SimpleDateFormat lnDateFormat = new SimpleDateFormat("hh:mm:ss:SSSSS");

                    fileOut.write(lnDateFormat.format(logLine.logDateTime) + "|" + logLine.logContent + "\r\n");
                }
                logHistory.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendJsonRaw(Player sender, String message) {
        this.jsonManager.sendJsonMessage(sender, message);
    }

    public String[] buildMessage(String langFile, String msgKey, String extendedMessage) {
        String[] messages = this.getResultMessage(langFile, msgKey.toLowerCase());

        for (int nCnt = 0; nCnt < messages.length; nCnt++) {
            messages[nCnt] = messages[nCnt].replaceAll("<message>", extendedMessage);
        }
        return messages;
    }

    public String[] buildMessage(String langFile, CommandSender sender, String msgKey, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, NPC npc, Material material, int ident) {
        return buildMessage(langFile, sender, msgKey, npcTrait, locationSetting, npc, material, ident, "");
    }

    public String[] buildMessage(String langFile, CommandSender sender, String msgKey, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, NPC npc, Material material, int ident, String rawMessage) {
        List<String> processedMessages = new ArrayList<String>();

        String[] messages = this.getResultMessage(langFile, msgKey.toLowerCase());

        for (int nCnt = 0; nCnt < messages.length; nCnt++) {
            String messageLine = parseMessage(sender, langFile, messages[nCnt], npcTrait, locationSetting, material, npc, ident);
            messageLine = messageLine.replaceAll("<message>", rawMessage);
            processedMessages.add(messageLine);
        }
        return processedMessages.toArray(new String[processedMessages.size()]);
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, null, null, null, null, 0, ""));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, npcTrait, locationSetting, npcTrait.getNPC(), null, 0, ""));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, String message) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, npcTrait, locationSetting, npcTrait.getNPC(), null, 0, message));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, int ident) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, npcTrait, locationSetting, npcTrait.getNPC(), null, ident, ""));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, NPCDestinationsTrait npcTrait) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, npcTrait, null, npcTrait.getNPC(), null, 0, ""));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, String message) {
        sendMessage(sender, buildMessage(langFile, msgKey, message));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, NPC npc) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, null, null, npc, null, 0, ""));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, Material material) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, null, null, null, material, 0, ""));
    }

    public void sendMessage(String langFile, CommandSender sender, String msgKey, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material material) {
        sendMessage(sender, buildMessage(langFile, sender, msgKey, npcTrait, locationSetting, npcTrait.getNPC(), material, 0, ""));
    }

    private void sendMessage(CommandSender sender, String[] messages) {
        if (sender instanceof Player) {
            String sjsonMessage = "";
            for (String sMsg : messages) {
                if (sMsg.startsWith("[") && sjsonMessage.length() > 0) {
                    jsonManager.sendJsonMessage((Player) sender, ChatColor.translateAlternateColorCodes('&', sjsonMessage));
                    sjsonMessage = "";
                }
                sjsonMessage += sMsg;

                if (sjsonMessage.endsWith("]")) {
                    if (sjsonMessage.endsWith(",]"))
                        sjsonMessage = sjsonMessage.substring(0, sjsonMessage.length() - 2) + "]";
                    if (!sjsonMessage.equalsIgnoreCase("[]")) {
                        jsonManager.sendJsonMessage((Player) sender, ChatColor.translateAlternateColorCodes('&', sjsonMessage));
                        sjsonMessage = "";
                    } else {
                        sjsonMessage = "";
                    }
                }
            }
        } else {
            for (String sMsg : messages) {
                sender.sendMessage(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', sMsg)));
            }
        }
    }

    public void sendDebugMessage(String langFile, String msgKey, NPC npc, NPCDestinationsTrait npcTrait, String message) {
        if (destRef.debugTargets != null) {
            for (DebugTarget debugOutput : destRef.debugTargets) {
                if (debugOutput.getTargets().size() == 0 || debugOutput.getTargets().contains(npc.getId())) {
                    if (((Player) debugOutput.targetSender).isOnline()) {
                        String[] messages = buildMessage(langFile, debugOutput.targetSender, msgKey, npcTrait, npcTrait.currentLocation, npcTrait.getNPC(), null, 0, message);
                        sendMessage(debugOutput.targetSender, messages);
                    }
                }
            }
        }
    }

    public void sendDebugMessage(String langFile, String msgKey, NPC npc, NPCDestinationsTrait npcTrait) {
        sendDebugMessage(langFile, msgKey, npc, npcTrait, "");
    }

    public void sendDebugMessage(String langFile, String msgKey, NPC npc, String message) {
        NPCDestinationsTrait trait = null;
        if (npc.hasTrait(NPCDestinationsTrait.class))
            trait = npc.getTrait(NPCDestinationsTrait.class);
        sendDebugMessage(langFile, msgKey, npc, trait, message);
    }

    @SuppressWarnings("deprecation")
    private String parseMessage(CommandSender sender, String langFile, String message, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material blockMaterial, NPC npc, int ident) {

        for (DestinationsAddon pluginReference : destRef.getPluginManager.getPlugins()) {
            message = pluginReference.parseLanguageLine(message, npcTrait, locationSetting, blockMaterial, npc, ident);
        }

        if (message.toLowerCase().contains("<plugin.seek-time>")) {
            if (npcTrait != null && npcTrait.maxProcessingTime > 0)
                message = replaceAll(message, "<plugin.seek-time>", Integer.toString(npcTrait.maxProcessingTime));
            else
                message = replaceAll(message, "<plugin.seek-time>", Integer.toString(destRef.getConfig().getInt("seek-time", 10)));
        }
        if (message.toLowerCase().contains("<plugin.distance>"))
            message = replaceAll(message, "<plugin.distance>", Integer.toString(destRef.getConfig().getInt("max-distance", 10)));
        if (message.toLowerCase().contains("<plugin.language>"))
            message = replaceAll(message, "<plugin.language>", destRef.getConfig().getString("language", "en-dafault"));

        if (message.toLowerCase().contains("<debug.targetlist>")) {
            for (DebugTarget debugOutput : destRef.debugTargets) {
                if (sender == null)
                    message = replaceAll(message, "<debug.targetlist>", "Invalid player");
                else {
                    String targetList = "";
                    if ((debugOutput.targetSender instanceof Player) && debugOutput.targetSender.equals(sender)) {
                        List<Integer> npcIDS = debugOutput.getTargets();

                        for (int nCnt = 0; nCnt < npcIDS.size(); nCnt++) {
                            NPC npcTarget = CitizensAPI.getNPCRegistry().getById(npcIDS.get(nCnt));
                            String debugTargetLine = ",{\"text\":\"[\",\"color\":\"yellow\"}," + "{\"text\":\"X\",\"color\":\"red\"," + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/npcdest debug " + npcTarget.getId()
                                    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + npcTarget.getFullName() + "\"}}," + "{\"text\":\"] \",\"color\":\"yellow\"}," + "{\"text\":\"" + npcTarget.getFullName()
                                    + "  \",\"color\":\"white\"}";
                            targetList += debugTargetLine;
                        }
                    }
                    message = replaceAll(message, "<debug.targetlist>", targetList);
                }
            }
        }

        if (npcTrait != null) {
            // Replace variables
            if (message.toLowerCase().contains("<setting.pauseforplayers>"))
                message = replaceAll(message, "<setting.pauseforplayers>", Integer.toString(npcTrait.PauseForPlayers));
            if (message.toLowerCase().contains("<setting.pausefortimeout>")) {
                if (npcTrait.PauseTimeout < 0)
                    message = replaceAll(message, "<setting.pausefortimeout>", "Unlimited pause");
                if (npcTrait.PauseTimeout > -1)
                    message = replaceAll(message, "<setting.pausefortimeout>", Integer.toString(npcTrait.PauseTimeout));
            }
            if (message.toLowerCase().contains("<setting.enabledplugins>")) {
                if (destRef.getPluginManager.getPlugins().size() == 0) {
                    message = replaceAll(message, "<setting.enabledplugins>", "{\"text\":\"There are no plugins available\",\"color\":\"yellow\"}");
                } else {
                    String pluginList = ""; // "{\"text\":\" \"},";
                    for (DestinationsAddon pluginReference : destRef.getPluginManager.getPlugins()) {
                        String pluginLine = "{\"text\":\"[\",\"color\":\"yellow\"},";
                        if (npcTrait.enabledPlugins.contains(pluginReference.getActionName().toUpperCase())) {
                            pluginLine += "{\"text\":\"" + pluginReference.getPluginIcon() + "\",\"color\":\"green\",";
                        } else {
                            pluginLine += "{\"text\":\"" + pluginReference.getPluginIcon() + "\",\"color\":\"red\",";
                        }
                        pluginLine += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/npcdest enableplugin --npc <npc.id> " + pluginReference.getActionName() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\""
                                + pluginReference.getQuickDescription() + "\"}},";
                        pluginLine += "{\"text\":\"]\",\"color\":\"yellow\"},";
                        pluginLine += "{\"text\":\"" + pluginReference.getActionName() + "\",\"color\":\"white\"},{\"text\":\" \"},";
                        pluginList += pluginLine;
                    }
                    message = replaceAll(message, "<setting.enabledplugins>", pluginList + "{\"text\":\" \"}");
                }
            }
            if (message.toLowerCase().contains("<setting.blocksundersurface>"))
                message = replaceAll(message, "<setting.blocksundersurface>", npcTrait.blocksUnderSurface == 0 ? this.getResultMessage(langFile, "result_Messages.false_text")[0]
                        : npcTrait.blocksUnderSurface == -1 ? this.getResultMessage(langFile, "result_messages.lowest_block")[0] : Integer.toString(npcTrait.blocksUnderSurface));
            if (message.toLowerCase().contains("<setting.locationscount>"))
                message = replaceAll(message, "<setting.locationscount>", npcTrait.Locations == null ? "0" : Integer.toString(npcTrait.Locations.size()));
            if (message.toLowerCase().contains("<setting.opensgates>"))
                message = replaceAll(message, "<setting.opensgates>", !npcTrait.OpensGates ? "X\",\"color\":\"red" : "✔\",\"color\":\"white");
            if (message.toLowerCase().contains("<setting.openswooddoors>"))
                message = replaceAll(message, "<setting.openswooddoors>", !npcTrait.OpensWoodDoors ? "X\",\"color\":\"red" : "✔\",\"color\":\"white");
            if (message.toLowerCase().contains("<setting.opensmetaldoors>"))
                message = replaceAll(message, "<setting.opensmetaldoors>", !npcTrait.OpensMetalDoors ? "X\",\"color\":\"red" : "✔\",\"color\":\"white");
            if (message.toLowerCase().contains("<setting.teleportonfailedstartloc>"))
                message = replaceAll(message, "<setting.teleportonfailedstartloc>", npcTrait.TeleportOnFailedStartLoc == null ? this.getResultMessage(langFile, "result_messages.false_text")[0]
                        : (npcTrait.TeleportOnFailedStartLoc ? this.getResultMessage(langFile, "result_Messages.true_text")[0] : this.getResultMessage(langFile, "result_messages.false_text")[0]));
            if (message.toLowerCase().contains("<setting.teleportonnopath>"))
                message = replaceAll(message, "<setting.teleportonnopath>", npcTrait.TeleportOnNoPath == null ? this.getResultMessage(langFile, "result_Messages.false_text")[0]
                        : (npcTrait.TeleportOnNoPath ? this.getResultMessage(langFile, "result_Messages.true_text")[0] : this.getResultMessage(langFile, "result_Messages.false_text")[0]));
            if (message.toLowerCase().contains("<setting.maxdistfromdestination>"))
                message = replaceAll(message, "<setting.maxdistfromdestination>", Integer.toString(npcTrait.MaxDistFromDestination));
            if (message.toLowerCase().contains("<setting.currentaction>"))
                message = replaceAll(message, "<setting.currentaction>", npcTrait.getCurrentAction() == null ? this.getResultMessage(langFile, "result_Messages.action_idle")[0] : "action_" + npcTrait.getCurrentAction().toString());
            if (message.toLowerCase().contains("<setting.lastresult>"))
                message = replaceAll(message, "<setting.lastresult>", npcTrait.lastResult == null ? this.getResultMessage(langFile, "result_Messages.action_idle")[0] : npcTrait.lastResult);
            if (message.toLowerCase().contains("<setting.maxprocessing>"))
                message = replaceAll(message, "<setting.maxprocessing>", String.valueOf(npcTrait.maxProcessingTime));

            if (message.toLowerCase().contains("<setting.pendingdestinationscount>")) {
                if (npcTrait.getPendingDestinations() == null) {
                    message = replaceAll(message, "<setting.pendingdestinationscount>", "0");
                } else {
                    message = replaceAll(message, "<setting.pendingdestinationscount>", Integer.toString(npcTrait.getPendingDestinations().size()));
                }
            }
            if (message.toLowerCase().contains("<setting.allowedpathblockscount>"))
                message = replaceAll(message, "<setting.allowedpathblockscount>", npcTrait.AllowedPathBlocks == null ? "0" : Integer.toString(npcTrait.AllowedPathBlocks.size()));
            if (message.toLowerCase().contains("<setting.pathtime>")) {
                if (npcTrait.lastPathCalc == null) {
                    message = replaceAll(message, "<setting.pathtime>", "??");
                } else {
                    long findTime = (new Date().getTime()) - (npcTrait.lastPathCalc.getTime() / 1000 % 60);
                    message = replaceAll(message, "<setting.pathtime>", Long.toString(findTime));
                }
            }
            if (message.toLowerCase().contains("<setting.lastresult>"))
                message = replaceAll(message, "<setting.lastresult>", npcTrait.lastResult);
            if (message.toLowerCase().contains("<setting.allowedpathblocks>")) {
                if (npcTrait == null || npcTrait.AllowedPathBlocks == null || npcTrait.AllowedPathBlocks.size() == 0) {
                    message = replaceAll(message, "<setting.allowedpathblocks>", "{\"text\":\"ANY\",\"color\":\"yellow\"}");
                } else {
                    String blockString = "";
                    for (Material allowedMaterial : npcTrait.AllowedPathBlocks) {
                        blockString += parseMessage(sender, langFile, this.getResultMessage(langFile, "result_Messages.blocktext")[0], npcTrait, locationSetting, allowedMaterial, npc, 0);
                    }
                    message = replaceAll(message, "<setting.allowedpathblocks>", blockString);
                }
            }
            if (message.toLowerCase().contains("<setting.statusmessage>")) {
                if (npcTrait.monitoredLocation != null && npcTrait.monitoredLocation.LocationIdent.toString().equals(npcTrait.currentLocation.LocationIdent.toString())) {
                    if (npcTrait.getMonitoringPlugin() != null)
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_monitored")[0].replaceAll("<message>", npcTrait.getMonitoringPlugin().getDescription().getName()));
                    else
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_monitored")[0].replaceAll("<message>", destRef.getDescription().getName()));
                }

                switch (npcTrait.getRequestedAction()) {
                case NORMAL_PROCESSING:
                    switch (npcTrait.getCurrentAction()) {
                    case IDLE:
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_idle")[0]);
                        break;
                    case IDLE_FAILED:
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_idle_failure")[0]);
                        break;
                    case PATH_HUNTING:
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_path_hunting")[0] + " (" + Long.toString((new Date().getTime() - npcTrait.lastPathCalc.getTime())
                                / 1000 % 60) + ")");
                        break;
                    case RANDOM_MOVEMENT:
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_random_movement")[0]);
                        break;
                    case TRAVELING:
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_traveling")[0]);
                        break;
                    case PATH_FOUND:
                        String newMessage = this.getResultMessage(langFile, "result_Messages.action_path_found")[0];
                        if (npcTrait.getPendingDestinations() == null) {
                            newMessage += " (0)";
                        } else {
                            newMessage += " (" + Integer.toString(npcTrait.getPendingDestinations().size()) + ")";
                        }
                        message = replaceAll(message, "<setting.statusmessage>", newMessage);
                        break;
                    default:
                        break;
                    }
                    break;
                case NO_PROCESSING:
                    message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_no_processing")[0]);
                    break;
                case SET_LOCATION:
                    if (npcTrait.locationLockUntil != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        // message =
                        // replaceAll(message,"<setting.statusmessage>",this.getResultMessage(Result_Messages.action_set_location)
                        // + "(" + DateUtils.
                        // Long.toString(((npcTrait.locationLockUntil.getTime()
                        // - new Date().getTime()) / 1000)) + ")");
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_set_location")[0] + "(" + sdf.format((npcTrait.locationLockUntil.getTime() - new Date().getTime()))
                                + ")");
                    } else {
                        message = replaceAll(message, "<setting.statusmessage>", this.getResultMessage(langFile, "result_Messages.action_set_location")[0]);
                    }
                    break;
                default:
                    break;
                }
            }

            // Citizens settings
            if (message.toLowerCase().contains("<citizens.pathdistance>"))
                message = replaceAll(message, "<citizens.pathdistance>", Double.toString(npcTrait.citizens_DistanceMargin));
            if (message.toLowerCase().contains("<citizens.pathmargin>"))
                message = replaceAll(message, "<citizens.pathmargin>", Double.toString(npcTrait.citizens_PathDistanceMargin));
            if (message.toLowerCase().contains("<citizens.pathfinder>"))
                message = replaceAll(message, "<citizens.pathfinder>", npcTrait.citizens_NewPathFinder ? this.getResultMessage(langFile, "result_Messages.true_text")[0] : this.getResultMessage(langFile, "result_Messages.false_text")[0]);
            if (message.toLowerCase().contains("<citizens.swims>"))
                message = replaceAll(message, "<citizens.swims>", npcTrait.citizens_Swim ? this.getResultMessage(langFile, "result_Messages.true_text")[0] : this.getResultMessage(langFile, "result_Messages.false_text")[0]);
            if (message.toLowerCase().contains("<citizens.avoidwater>"))
                message = replaceAll(message, "<citizens.avoidwater>", npcTrait.citizens_AvoidWater ? this.getResultMessage(langFile, "result_Messages.true_text")[0] : this.getResultMessage(langFile, "result_Messages.false_text")[0]);
            if (message.toLowerCase().contains("<citizens.stuckaction>"))
                message = replaceAll(message, "<citizens.stuckaction>", npcTrait.citizens_DefaultStuck ? this.getResultMessage(langFile, "result_Messages.true_text")[0] : this.getResultMessage(langFile, "result_Messages.false_text")[0]);

            if (message.toLowerCase().contains("<patheng.lastproctime>"))
                message = replaceAll(message, "<patheng.lastproctime>", String.valueOf(npcTrait.processingTime.intValue()));
            if (message.toLowerCase().contains("<patheng.lastbps>"))
                message = replaceAll(message, "<patheng.lastbps>", String.valueOf(npcTrait.blocksPerSec.intValue()));

        }
        if (npc != null) {
            if (message.toLowerCase().contains("<npc.id>"))
                message = replaceAll(message, "<npc.id>", Integer.toString(npc.getId()));
            if (message.toLowerCase().contains("<npc.name>"))
                message = replaceAll(message, "<npc.name>", npc.getName().replace("[", "").replace("]", "]"));
            if (message.toLowerCase().contains("<npc.spawned>"))
                message = replaceAll(message, "<npc.spawned>", npc.isSpawned() ? this.getResultMessage(langFile, "result_Messages.true_text")[0] : this.getResultMessage(langFile, "result_Messages.false_text")[0]);
            if (message.toLowerCase().contains("<npc.location>"))
                message = replaceAll(message, "<npc.location>", "(" + npc.getEntity().getLocation().getBlockX() + "," + npc.getEntity().getLocation().getBlockY() + "," + npc.getEntity().getLocation().getBlockZ() + ")");
            if (message.toLowerCase().contains("<npc.type>"))
                message = replaceAll(message, "<npc.type>", npc.getEntity().getType().name());
        }

        if (blockMaterial != null) {
            if (message.toLowerCase().contains("<material.id>"))
                message = replaceAll(message, "<material.id>", Integer.toString(blockMaterial.getId()));
            if (message.toLowerCase().contains("<material.name>"))
                message = replaceAll(message, "<material.name>", blockMaterial.name());
        }

        if (locationSetting != null) {
            if (message.toLowerCase().contains("<location.current>")) {
                boolean bFound = false;
                for (int nCnt = 0; nCnt < npcTrait.NPCLocations.size(); nCnt++) {
                    if (npcTrait.NPCLocations.get(nCnt).equals(locationSetting) && locationSetting.equals(npcTrait.currentLocation)) {
                        message = replaceAll(message, "<location.current>", "{\"text\":\"" + Integer.toString(nCnt) + "\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + locationSetting.LocationIdent
                                .toString() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + this.getResultMessage(langFile, "result_Messages.current_location")[0] + "\n&eID: " + locationSetting.LocationIdent.toString()
                                + "\"}}");
                        bFound = true;
                        break;
                    }
                }

                if (!bFound) {
                    message = replaceAll(message, "<location.current>", "{\"text\":\"<location.id>\",\"color\":\"white\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + locationSetting.LocationIdent.toString()
                            + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"&eID: " + locationSetting.LocationIdent.toString() + "\"}}");
                }
            }
            if (message.toLowerCase().contains("<location.id>")) {
                for (int nCnt = 0; nCnt < npcTrait.NPCLocations.size(); nCnt++) {
                    if (npcTrait.NPCLocations.get(nCnt).equals(locationSetting)) {
                        message = replaceAll(message, "<location.id>", Integer.toString(nCnt));
                    }
                }
            }
            if (message.toLowerCase().contains("<location.plugins>")) {
                String pluginResponses = "";
                for (DestinationsAddon plugin : destRef.getPluginManager.getPlugins()) {
                    if (npcTrait.enabledPlugins.contains(plugin.getActionName().toUpperCase())) {
                        String response = plugin.getDestinationHelp(npc, npcTrait, locationSetting);
                        if (!response.equals("")) {
                            pluginResponses += response;
                        }
                    }
                }
                message = replaceAll(message, "<location.plugins>", pluginResponses);
            }

            if (message.toLowerCase().contains("<location.unique>"))
                message = replaceAll(message, "<location.unique>", locationSetting.LocationIdent.toString());
            if (message.toLowerCase().contains("<location.timeofday>")) {
                if (locationSetting.TimeOfDay == -1)
                    message = replaceAll(message, "<location.timeofday>", this.getResultMessage(langFile, "result_Messages.disabled_text")[0]);
                else
                    message = replaceAll(message, "<location.timeofday>", Integer.toString(locationSetting.TimeOfDay));
            }
            if (message.toLowerCase().contains("<location.probability>"))
                message = replaceAll(message, "<location.probability>", Integer.toString(locationSetting.Probability));
            if (message.toLowerCase().contains("<location.time_min>"))
                message = replaceAll(message, "<location.time_min>", Integer.toString(locationSetting.Time_Minimum));
            if (message.toLowerCase().contains("<location.time_max>"))
                message = replaceAll(message, "<location.time_max>", Integer.toString(locationSetting.Time_Maximum));
            if (message.toLowerCase().contains("<location.destinationyaw>"))
                message = replaceAll(message, "<location.destinationyaw>", "(" + locationSetting.destination.getBlockX() + "," + locationSetting.destination.getBlockY() + "," + locationSetting.destination.getBlockZ() + ") ["
                        + locationSetting.destination.getYaw() + "]");
            if (message.toLowerCase().contains("<location.destination>"))
                message = replaceAll(message, "<location.destination>", "(" + locationSetting.destination.getBlockX() + "," + locationSetting.destination.getBlockY() + "," + locationSetting.destination.getBlockZ() + ")");
            if (message.toLowerCase().contains("<location.destinationraw>"))
                message = replaceAll(message, "<location.destinationraw>", "" + locationSetting.destination.getBlockX() + " " + locationSetting.destination.getBlockY() + " " + locationSetting.destination.getBlockZ() + "");

            if (message.toLowerCase().contains("<location.wandering_settingtype>")) {
                if (!locationSetting.Wandering_Region.trim().equals("")) {
                    message = replaceAll(message, "<location.wandering_settingtype>", this.getResultMessage(langFile, "result_Messages.wander_setting_region")[0]);
                } else {
                    message = replaceAll(message, "<location.wandering_settingtype>", this.getResultMessage(langFile, "result_Messages.wander_setting_distance")[0]);
                }
            }

            if (message.toLowerCase().contains("<location.wandering_setting>")) {
                if (!locationSetting.Wandering_Region.trim().equals("")) {
                    message = replaceAll(message, "<location.wandering_setting>", locationSetting.Wandering_Region);
                } else {
                    message = replaceAll(message, "<location.wandering_setting>", Double.toString(locationSetting.getWanderingDistance()));
                }
            }

            if (message.toLowerCase().contains("<location.wandering_distance>")) {
                message = replaceAll(message, "<location.wandering_distance>", Double.toString(locationSetting.getWanderingDistance()));
            }
            if (message.toLowerCase().contains("<location.wait_minimum>"))
                message = replaceAll(message, "<location.wait_minimum>", Integer.toString(locationSetting.Wait_Minimum));
            if (message.toLowerCase().contains("<location.wait_maximum>"))
                message = replaceAll(message, "<location.wait_maximum>", Integer.toString(locationSetting.Wait_Maximum));
            if (message.toLowerCase().contains("<location.max_distance>"))
                message = replaceAll(message, "<location.max_distance>", Double.toString(locationSetting.getMaxDistance()));
            if (message.toLowerCase().contains("<location.pause_distance>"))
                message = replaceAll(message, "<location.pause_distance>", Integer.toString(locationSetting.Pause_Distance));
            if (message.toLowerCase().contains("<location.pause_timeout>"))
                message = replaceAll(message, "<location.pause_timeout>", Integer.toString(locationSetting.Pause_Timeout));
            if (message.toLowerCase().contains("<location.pause_style>"))
                message = replaceAll(message, "<location.pause_style>", locationSetting.Pause_Type);
            if (message.toLowerCase().contains("<location.alias_name>"))
                message = replaceAll(message, "<location.alias_name>", locationSetting.Alias_Name == null ? "" : locationSetting.Alias_Name);
            if (message.toLowerCase().contains("<location.distanceto>"))
                message = replaceAll(message, "<location.distanceto>", Long.toString((long) npc.getEntity().getLocation().distance(locationSetting.destination)));
            if (message.toLowerCase().contains("<location.useblocks>"))
                message = replaceAll(message, "<location.useblocks>", locationSetting.Wandering_UseBlocks.toString());
            if (message.toLowerCase().contains("<location.skin>"))
                message = replaceAll(message, "<location.skin>", locationSetting.player_Skin_Name.isEmpty() ? "Not Set" : locationSetting.player_Skin_Name);
            if (message.toLowerCase().contains("<location.skin_action>"))
                message = replaceAll(message, "<location.skin_action>", locationSetting.player_Skin_ApplyOnArrival ? "End location" : "Start Location");
            if (message.toLowerCase().contains("<location.clearinv>"))
                message = replaceAll(message, "<location.clearinv>", !locationSetting.items_Clear ? this.getResultMessage(langFile, "result_messages.false_text")[0] : this.getResultMessage(langFile, "result_messages.true_text")[0]);
            if (message.toLowerCase().contains("<location.helmet>"))
                message = replaceAll(message, "<location.helmet>", locationSetting.items_Head == null ? "" : getItemName(locationSetting.items_Head));
            if (message.toLowerCase().contains("<location.chest>"))
                message = replaceAll(message, "<location.chest>", locationSetting.items_Chest == null ? "" : getItemName(locationSetting.items_Chest));
            if (message.toLowerCase().contains("<location.legs>"))
                message = replaceAll(message, "<location.legs>", locationSetting.items_Legs == null ? "" : getItemName(locationSetting.items_Legs));
            if (message.toLowerCase().contains("<location.boots>"))
                message = replaceAll(message, "<location.boots>", locationSetting.items_Boots == null ? "" : getItemName(locationSetting.items_Boots));
            if (message.toLowerCase().contains("<location.hand>"))
                message = replaceAll(message, "<location.hand>", locationSetting.items_Hand == null ? "" : getItemName(locationSetting.items_Hand));
            if (message.toLowerCase().contains("<location.offhand>"))
                message = replaceAll(message, "<location.offhand>", locationSetting.items_Offhand == null ? "" : getItemName(locationSetting.items_Offhand));

            // 1.29 - Weather
            if (message.toLowerCase().contains("<location.weather>")) {
                if (locationSetting.WeatherFlag == 1) {
                    message = replaceAll(message, "<location.weather>", "Clear Weather");
                } else if (locationSetting.WeatherFlag == 2) {
                    message = replaceAll(message, "<location.weather>", "Stormy Weather");
                } else {
                    message = replaceAll(message, "<location.weather>", "Any Weather");
                }
            }

            // V1.44 -- Commands
            if (message.toLowerCase().contains("<command.id>")) {
                message = replaceAll(message, "<command.id>", String.valueOf(ident));
            }
            if (message.toLowerCase().contains("<command.textlong>")) {
                String start = message.substring(0, message.indexOf("<command.textlong>"));
                String end = message.substring(message.indexOf("<command.textlong>") + ("<command.textlong>".length()));
                String replacement = locationSetting.arrival_Commands.get(ident).replace("\\", "\\\\").replace("\"", "\\\"").replace("{", "\\{").replace("}", "\\}");
                String metaWindow = "";
                while (true) {
                    if (replacement.length() > 51) {
                        metaWindow += replacement.substring(0, 50) + "\n";
                        replacement = replacement.substring(50);
                    } else {
                        metaWindow += replacement;
                        replacement = "";
                        break;
                    }
                }
                message = start + metaWindow + end;
            }
            if (message.toLowerCase().contains("<command.textshort>")) {
                String start = message.substring(0, message.indexOf("<command.textshort>"));
                String end = message.substring(message.indexOf("<command.textshort>") + ("<command.textshort>".length()));
                String replacement = "";
                if (locationSetting.arrival_Commands.get(ident).length() > 51)
                    replacement = locationSetting.arrival_Commands.get(ident).substring(0, 50).replace("\\", "\\\\").replace("\"", "\\\"").replace("{", "\\{").replace("}", "\\}") + "...";
                else
                    replacement = locationSetting.arrival_Commands.get(ident).replace("\\", "\\\\").replace("\"", "\\\"").replace("{", "\\{").replace("}", "\\}");
                message = start + replacement + end;
            }
        }

        if (message.toLowerCase().contains("<pathengine.queue.count>"))
            if (destRef.getPathClass != null) {
                message = replaceAll(message, "<pathengine.queue.count>", Integer.toString(destRef.getPathClass.path_Queue.size()));
            } else {
                message = replaceAll(message, "<pathengine.queue.count>", "0");
            }

        if (message.toLowerCase().contains("<pathengine.currentnpc>"))
            if (destRef.getPathClass != null && destRef.getPathClass.currentTask != null) {
                message = replaceAll(message, "<pathengine.currentnpc>", destRef.getPathClass.currentTask.npc.getFullName());
            } else {
                message = replaceAll(message, "<pathengine.queue.count>", "0");
            }

        return message;

    }

    private String[] getResultMessage(String langFile, String msgKey) {
        String language = destRef.currentLanguage;
        msgKey = msgKey.toLowerCase();
        List<String> response = new ArrayList<String>();

        if (!destRef.getLanguageManager.languageStorage.containsKey(language + "-" + langFile)) {
            logToConsole(destRef, "Missing language [" + destRef.currentLanguage + "-" + langFile + "." + msgKey + "] check your language files.");
            language = "en_def";
        }

        if (!destRef.getLanguageManager.languageStorage.containsKey(language + "-" + langFile)) {
            logToConsole(destRef, "Missing language [" + destRef.currentLanguage + "-" + langFile + "." + msgKey + "] check your language files.");
            response.add("Language file failure. Contact the servers admin");
            return response.toArray(new String[response.size()]);
        }

        if (!destRef.getLanguageManager.languageStorage.get(language + "-" + langFile).contains(msgKey)) {
            if (!destRef.getLanguageManager.languageStorage.get(language + "-destinations").contains(msgKey)) {
                logToConsole(destRef, "Missing language item [" + destRef.currentLanguage + "-" + langFile + "." + msgKey + "] check your language files.");
                response.add("Language file failure. Contact the servers admin");
                return response.toArray(new String[response.size()]);
            } else {
                langFile = "destinations";
            }
        }

        if (destRef.getLanguageManager.languageStorage.get(language + "-" + langFile).isList(msgKey)) {
            response.addAll(destRef.getLanguageManager.languageStorage.get(language + "-" + langFile).getStringList(msgKey));
        } else {
            response.add(destRef.getLanguageManager.languageStorage.get(language + "-" + langFile).getString(msgKey));
        }
        return response.toArray(new String[response.size()]);
    }

    private String getItemName(ItemStack item) {
        if (item == null)
            return "";
        if (item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
            return item.getItemMeta().getDisplayName();
        }
        if (item.getType() != null)
            return item.getType().name();
        return "";
    }

    private String replaceAll(String source, String oldText, String newText) {
        do {
            if (source.indexOf(oldText) > 0)
                source = replace(source, oldText, newText);
            else
                break;
        } while (true);
        return source;
    }

    private String replace(String source, String oldText, String newText) {
        if (source == null) {
            return null;
        }
        int i = 0;
        if ((i = source.indexOf(oldText, i)) >= 0) {
            char[] sourceArray = source.toCharArray();
            char[] nsArray = newText.toCharArray();
            int oLength = oldText.length();
            StringBuilder buf = new StringBuilder(sourceArray.length);
            buf.append(sourceArray, 0, i).append(nsArray);
            i += oLength;
            int j = i;
            // Replace all remaining instances of oldString with newString.
            while ((i = source.indexOf(oldText, i)) > 0) {
                buf.append(sourceArray, j, i - j).append(nsArray);
                i += oLength;
                j = i;
            }
            buf.append(sourceArray, j, sourceArray.length - j);
            source = buf.toString();
            buf.setLength(0);
        }
        return source;
    }
}

class LogDetail {
    public Date   logDateTime;
    public String logContent;

    public LogDetail(String logContent) {
        logDateTime = new Date();
        this.logContent = logContent;
    }
}
