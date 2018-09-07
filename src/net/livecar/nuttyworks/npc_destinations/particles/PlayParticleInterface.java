package net.livecar.nuttyworks.npc_destinations.particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PlayParticleInterface {

    void PlayOutHeartParticle(Location partLocation, Player player);
    void PlayOutParticle(Location partLocation, Player player, SupportedParticles particle);
}
