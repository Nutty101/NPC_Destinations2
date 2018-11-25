package net.livecar.nuttyworks.npc_destinations.utilities;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

public class Utilities {

    private DestinationsPlugin destRef = null;

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

    public static boolean isNumeric(String value) {
        value = value.replaceAll("-", "").replaceAll("\\.", "");
        if (value.trim().equals(""))
            return false;
        return (StringUtils.isNumeric(value));
    }
}
