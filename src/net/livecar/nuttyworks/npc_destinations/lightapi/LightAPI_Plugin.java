package net.livecar.nuttyworks.npc_destinations.lightapi;

import org.bukkit.Location;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightAPI_Plugin {
    @SuppressWarnings("unused")
    private DestinationsPlugin destRef = null;

    public LightAPI_Plugin(DestinationsPlugin storageRef) {
        destRef = storageRef;

    }

    public void CreateLight(Location oLoc, int lightLevel) {
        LightAPI.createLight(oLoc, lightLevel, true);
        for (ChunkInfo info : LightAPI.collectChunks(oLoc)) {
            LightAPI.updateChunk(info);
        }
    }

    public void DeleteLight(Location oLoc) {
        try {
            LightAPI.deleteLight(oLoc, true);
        } catch (Exception err) {
        }

        for (ChunkInfo info : LightAPI.collectChunks(oLoc)) {
            LightAPI.updateChunk(info);
        }
    }
}
