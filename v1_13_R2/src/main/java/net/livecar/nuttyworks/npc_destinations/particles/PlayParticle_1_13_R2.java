package net.livecar.nuttyworks.npc_destinations.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class PlayParticle_1_13_R2 implements PlayParticleInterface {

    public void PlayOutHeartParticle(Location partLocation, Player player) {

        player.spawnParticle(Particle.HEART, partLocation.clone().add(0, 1, 0), 1);
    }

    public void PlayOutParticle(Location partLocation, Player player, SupportedParticles particle) {

        Particle part = Particle.valueOf(particle.toString());
        if (part == null)
            return;

        player.spawnParticle(part, partLocation.clone().add(0, 1, 0), 1);
    }
}
