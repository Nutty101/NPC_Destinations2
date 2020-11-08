package net.livecar.nuttyworks.npc_destinations.plugins;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class Plugin_Manager {
    private HashMap<String, DestinationsAddon> pluginRegistration;
    private DestinationsAddon                  timePlugin         = null;
    private DestinationsPlugin                 destRef;

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
        return new ArrayList<>(pluginRegistration.values());
    }

    public DestinationsAddon getPluginByName(String actionName) {
        return pluginRegistration.get(actionName.toUpperCase());
    }

    public DestinationsAddon getTimePlugin() { return timePlugin; }
    
}
