package net.livecar.nuttyworks.npc_destinations.listeners.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class Command_Manager {
    HashMap<String, Command_Record> registeredCommands  = null;
    private List<String>            commandGroups       = null;
    private DestinationsPlugin      getStorageReference = null;

    public Command_Manager(DestinationsPlugin destRef) {
        this.getStorageReference = destRef;
        registeredCommands = new HashMap<String, Command_Record>();
        commandGroups = new ArrayList<String>();
    }

    public boolean onCommand(CommandSender sender, String[] inargs) {

        int npcid = -1;
        NPCDestinationsTrait npcTrait = null;

        List<String> sList = new ArrayList<String>();
        for (int nCnt = 0; nCnt < inargs.length; nCnt++) {
            if (inargs[nCnt].equalsIgnoreCase("--npc")) {
                // Npc ID should be the next one
                if (inargs.length >= nCnt + 2) {
                    npcid = Integer.parseInt(inargs[nCnt + 1]);
                    nCnt++;
                }
            } else {
                sList.add(inargs[nCnt]);
            }
        }

        inargs = sList.toArray(new String[sList.size()]);
        NPC npc = null;
        if (npcid == -1) {
            // Now lets find the NPC this should run on.
            npc = getStorageReference.getCitizensPlugin.getNPCSelector().getSelected(sender);
            if (npc != null) {
                // Gets NPC Selected for this sender
                npcid = npc.getId();
            }
        } else {
            npc = CitizensAPI.getNPCRegistry().getById(npcid);
        }

        if (npc != null && npc.hasTrait(Owner.class)) {
            npcTrait = npc.getTrait(NPCDestinationsTrait.class);
        }

        boolean isOwner = false;
        if (npc != null && npc.hasTrait(Owner.class)) {
            if (sender instanceof Player) {
                Owner ownerTrait = npc.getTrait(Owner.class);

                if (ownerTrait.isOwnedBy(sender)) {
                    isOwner = true;
                }
            }
        }

        if (inargs.length == 0)
            inargs = new String[] { "help" };

        if (inargs[0].equalsIgnoreCase("help")) {

            for (String groupName : commandGroups) {
                StringBuilder response = new StringBuilder();

                for (Command_Record cmdRecord : registeredCommands.values()) {
                    if (cmdRecord.helpMessage.equals(""))
                        continue;

                    if (cmdRecord.groupName.equals(groupName)) {
                        if (getStorageReference.hasPermissions(sender, cmdRecord.commandPermission) && isPlayer(sender)) {

                            String messageValue = getStorageReference.getMessageManager.buildMessage(cmdRecord.languageFile, sender, "command_jsonhelp." + cmdRecord.helpMessage, null, null, null, null, 0, "")[0];

                            if (messageValue.trim().equals("")) {
                                getStorageReference.getMessageManager.logToConsole(getStorageReference, "Language Message Missing (" + cmdRecord.helpMessage + ")");
                            } else {
                                String permList = "";
                                for (String perm : cmdRecord.commandPermission) {
                                    permList += "  &5" + perm + "\n";
                                }
                                response.append(messageValue.replaceAll("<permission>", permList).replaceAll("<commandname>", cmdRecord.commandName) + ",{\"text\":\" \"},");
                            }
                        } else if (getStorageReference.hasPermissions(sender, cmdRecord.commandPermission) && (!isPlayer(sender) && cmdRecord.allowConsole)) {
                            response.append(cmdRecord.commandName + " ");
                        }
                    }
                }
                if (isPlayer(sender) && !response.toString().trim().equals("")) {
                    String responseString = response.toString();
                    if (responseString.endsWith(",{\"text\":\" \"},")) {
                        responseString = responseString.substring(0, responseString.length() - 13);
                    }
                    String groupHeader = getStorageReference.getMessageManager.buildMessage("destinations", sender, "command_jsonhelp.command_help_group", null, null, null, null, 0, groupName)[0];
                    // groupHeader = groupHeader.replaceAll("<message>",
                    // groupName.toUpperCase());
                    groupHeader = groupHeader.replaceAll("<padding>", String.format("%" + (47 - groupName.length()) + "s", "").replace(' ', '-'));
                    getStorageReference.getMessageManager.sendJsonRaw((Player) sender, groupHeader);
                    getStorageReference.getMessageManager.sendJsonRaw((Player) sender, "[" + responseString + "{\"text\":\"\"}]");
                } else if (!isPlayer(sender) && !response.toString().trim().equals("")) {
                    sender.sendMessage("---[" + groupName + "]--------------------");
                    sender.sendMessage(response.toString());
                }
            }
            return true;
        } else if (registeredCommands.containsKey(inargs[0])) {
            Command_Record cmdRecord = registeredCommands.get(inargs[0].toLowerCase());
            if (!cmdRecord.allowConsole & !isPlayer(sender)) {
                getStorageReference.getMessageManager.sendMessage("destinations", sender, "console_messages.command_noconsole");
                return true;
            }

            if (getStorageReference.hasPermissions(sender, cmdRecord.commandPermission) && (inargs.length - 1 >= cmdRecord.minArguments && inargs.length - 1 <= cmdRecord.maxArguments))
                if (registeredCommands.get(inargs[0].toLowerCase()).invokeCommand(getStorageReference, sender, npc, inargs, isOwner, npcTrait))
                    return true;

            if (isPlayer(sender)) {
                String messageValue = getStorageReference.getMessageManager.buildMessage(cmdRecord.languageFile, sender, "command_jsonhelp." + cmdRecord.helpMessage, null, null, null, null, 0, "")[0];
                if (messageValue.trim().equals("")) {
                    getStorageReference.getMessageManager.logToConsole(getStorageReference, "Language Message Missing (" + cmdRecord.helpMessage + ")");
                } else {
                    String permList = "";
                    for (String perm : cmdRecord.commandPermission) {
                        permList += "  &5" + perm + "\n";
                    }
                    messageValue = messageValue.replaceAll("<permission>", permList).replaceAll("<commandname>", cmdRecord.commandName);
                }
                getStorageReference.getMessageManager.sendMessage("destinations", sender, "messages.commands_invalidarguments", messageValue);
                return true;
            } else
                getStorageReference.getMessageManager.sendMessage(cmdRecord.languageFile, sender, "console_messages." + cmdRecord.helpMessage);
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, String[] arguments) {
        List<String> results = new ArrayList<String>();
        Boolean isPlayer = (sender instanceof Player);

        List<String> sList = new ArrayList<String>();
        for (int nCnt = 0; nCnt < arguments.length; nCnt++) {
            if (arguments[nCnt].equalsIgnoreCase("--npc")) {
                // Npc ID should be the next one
                if (arguments.length >= nCnt + 2) {
                    nCnt++;
                }
            } else {
                sList.add(arguments[nCnt]);
            }
        }
        arguments = sList.toArray(new String[sList.size()]);

        if (arguments.length == 1) {
            for (Command_Record cmdSetting : this.registeredCommands.values()) {
                if ((!isPlayer && cmdSetting.allowConsole) || getStorageReference.hasPermissions(sender, cmdSetting.commandPermission)) {
                    if ((arguments[0].trim().length() > 0 && cmdSetting.commandName.startsWith(arguments[0].trim().toLowerCase())) || arguments[0].trim().equals(""))
                        results.add(cmdSetting.commandName);
                }
            }
        } else {
            for (Command_Record cmdSetting : this.registeredCommands.values()) {
                if ((!isPlayer && cmdSetting.allowConsole) || getStorageReference.hasPermissions(sender, cmdSetting.commandPermission)) {
                    if (arguments[0].trim().equalsIgnoreCase(cmdSetting.commandName)) {
                        if (arguments.length - 1 <= cmdSetting.arguments.length) {
                            String argumentLine = cmdSetting.arguments[arguments.length - 2];
                            String currentArg = arguments[arguments.length - 1].trim();
                            String priorArg = "";
                            if (arguments.length - 2 > -1)
                                priorArg = arguments[arguments.length - 2].trim();

                            if (argumentLine.contains("|")) {
                                if (currentArg.equals("")) {
                                    for (String itemDesc : argumentLine.split("\\|")) {
                                        results.addAll(parseTabItem(itemDesc, priorArg));
                                    }

                                    return results;
                                } else {
                                    for (String argValue : argumentLine.split("\\|")) {
                                        if (argValue.toLowerCase().startsWith(currentArg.toLowerCase())) {
                                            results.addAll(parseTabItem(argValue, priorArg));
                                        }
                                    }
                                    return results;
                                }
                            } else if (argumentLine.equalsIgnoreCase("<PLAYERNAME>")) {
                                return null;
                            } else {
                                results.addAll(parseTabItem(argumentLine, priorArg));
                                return results;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    public void registerCommandClass(Class<?> commandClass) {
        for (Method commandMethod : commandClass.getMethods()) {
            if (commandMethod.isAnnotationPresent(CommandInfo.class)) {
                CommandInfo methodAnnotation = commandMethod.getAnnotation(CommandInfo.class);
                if (!commandGroups.contains(methodAnnotation.group()))
                    commandGroups.add(methodAnnotation.group());
                Command_Record cmdRecord = new Command_Record(methodAnnotation.name(), methodAnnotation.group(), methodAnnotation.languageFile(), methodAnnotation.permission(), methodAnnotation.helpMessage(), methodAnnotation
                        .allowConsole(), methodAnnotation.minArguments(), methodAnnotation.maxArguments(), methodAnnotation.arguments(), commandClass, commandMethod.getName());
                registeredCommands.put(methodAnnotation.name(), cmdRecord);
            }
        }
    }

    private List<String> parseTabItem(String item, String priorArg) {
        List<String> results = new ArrayList<String>();
        if (item.equalsIgnoreCase("<material>") && (!priorArg.equalsIgnoreCase("--region") && !priorArg.equalsIgnoreCase("--npc"))) {
            for (Material materialType : Material.values()) {
                results.add(String.valueOf(materialType.name()));
            }
        } else if (item.equalsIgnoreCase("<plugin>") && (!priorArg.equalsIgnoreCase("--region") && !priorArg.equalsIgnoreCase("--npc"))) {
            for (DestinationsAddon plugin : getStorageReference.getPluginManager.getPlugins()) {
                results.add(String.valueOf(plugin.getActionName()));
            }
        } else if (item.equalsIgnoreCase("<npc>") && (!priorArg.equalsIgnoreCase("--region"))) {
            for (NPC npc : getStorageReference.getCitizensPlugin.getNPCRegistry()) {
                results.add(String.valueOf(npc.getId()));
            }
        } else if (item.equalsIgnoreCase("<region>") && !priorArg.equalsIgnoreCase("--npc")) {
            for (World world : getStorageReference.getServer().getWorlds()) {
                results.addAll(getStorageReference.getWorldGuardPlugin.getRegionList(world));
            }
        } else {
            for (DestinationsAddon plugin : getStorageReference.getPluginManager.getPlugins()) {
                List<String> tabItems = plugin.parseTabItem(item, priorArg);
                if (tabItems.size() > 0)
                    results.addAll(tabItems);
            }

        }
        return results;
    }

    private boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }
}
