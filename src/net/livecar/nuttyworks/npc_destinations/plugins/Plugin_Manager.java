package net.livecar.nuttyworks.npc_destinations.plugins;

import java.util.ArrayList;
import java.util.HashMap;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class Plugin_Manager {
    private HashMap<String, DestinationsAddon> pluginRegistration = new HashMap<>();
    private DestinationsPlugin                 destRef            = null;

    public Plugin_Manager(DestinationsPlugin storageRef) {
        pluginRegistration = new HashMap<>();
        destRef = storageRef;
    }

    public void registerPlugin(DestinationsAddon pluginClass) {
        if (pluginRegistration.containsKey(pluginClass.getActionName().toUpperCase())) {
            destRef.getMessageManager.consoleMessage(destRef, "destinations", "console_messages.plugin_registration_exists", pluginClass.getActionName());
            return;
        }
        pluginRegistration.put(pluginClass.getActionName().toUpperCase(), pluginClass);
    }

    public ArrayList<DestinationsAddon> getPlugins() {
        return new ArrayList<DestinationsAddon>(pluginRegistration.values());
    }

    public DestinationsAddon getPluginByName(String actionName) {
        return pluginRegistration.get(actionName.toUpperCase());
    }

}
