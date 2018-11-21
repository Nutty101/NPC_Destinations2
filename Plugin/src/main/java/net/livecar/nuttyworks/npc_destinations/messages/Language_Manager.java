package net.livecar.nuttyworks.npc_destinations.messages;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

public class Language_Manager {
    public HashMap<String, FileConfiguration> languageStorage = new HashMap<String, FileConfiguration>();
    private DestinationsPlugin                destRef         = null;

    public Language_Manager(DestinationsPlugin storageRef) {
        destRef = storageRef;
    }

    public void loadLanguages() {
        loadLanguages(false);
    }

    public void loadLanguages(boolean silent) {
        if (languageStorage == null)
            languageStorage = new HashMap<String, FileConfiguration>();
        languageStorage.clear();

        File[] languageFiles = destRef.languagePath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".yml");
            }
        });

        for (File ymlFile : languageFiles) {
            FileConfiguration oConfig = destRef.getUtilitiesClass.loadConfiguration(ymlFile);
            if (oConfig == null) {
                destRef.getMessageManager.logToConsole(destRef, "Problem loading language file (" + ymlFile.getName().toLowerCase().replace(".yml", "") + ")");
            } else {
                languageStorage.put(ymlFile.getName().toLowerCase().replace(".yml", ""), oConfig);
                if (!silent) {
                    // destRef.getMessageManager.consoleMessage(destRef,"destinations","console_messages.plugin_langloaded",ymlFile.getName().toLowerCase().replace(".yml","")
                    // );
                }

            }
        }
    }
}
