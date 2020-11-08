package net.livecar.nuttyworks.npc_destinations.utilities;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;

public class Utilities {

    private DestinationsPlugin destRef;

    public Utilities(DestinationsPlugin storageRef) {
        destRef = storageRef;
    }

    public YamlConfiguration loadConfiguration(File file) {
        Validate.notNull(file, "File cannot be null");

        YamlConfiguration config = new YamlConfiguration();

        InputStream inputStream = null;
        Reader inputStreamReader = null;
        try {
            inputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            config.load(inputStreamReader);
            inputStreamReader.close();
            inputStream.close();
            return config;
        } catch (InvalidConfigurationException | IOException ex) {
            destRef.getMessageManager.debugMessage(Level.SEVERE, "Utilities.loadConfiguration()|InvalidConfigurationException(" + file.getName() + ")|" + ex.getMessage());
        }

        if (inputStreamReader != null)
            try {
                inputStreamReader.close();
            } catch (IOException e) {
            }

        if (inputStream != null)
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        return null;
    }

    public static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    public boolean containsField(Object object, String fieldName) {
        return Arrays.stream(object.getClass().getFields())
                .anyMatch(f -> f.getName().equals(fieldName));
    }

}
