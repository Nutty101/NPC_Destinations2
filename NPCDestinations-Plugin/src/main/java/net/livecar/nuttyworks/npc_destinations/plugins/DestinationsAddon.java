package net.livecar.nuttyworks.npc_destinations.plugins;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;

public class DestinationsAddon {
    public String getPluginIcon() {
        return "?";
    }

    public String getActionName() {
        return "INVALID";
    }

    public String getQuickDescription() {
        return "The developer forgot to add a description";
    }

    public String getDestinationHelp(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        return ",{\"text\":\"&a?&e\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"The developer of this plugin didn't design it right.\"}}";
    }

    public List<String> parseTabItem(String item, String priorArg) {
        return new ArrayList<String>();
    }
    
    public String parseLanguageLine(String message, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material blockMaterial, NPC npc, int ident) {
        return message;
    }

    public boolean isDestinationEnabled(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        return true;
    }

    public void onLocationLoading(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        return;
    }

    public void onLocationSaving(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
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
