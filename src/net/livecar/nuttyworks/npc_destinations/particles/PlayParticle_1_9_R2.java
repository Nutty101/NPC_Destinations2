package net.livecar.nuttyworks.npc_destinations.particles;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;

public class PlayParticle_1_9_R2 implements PlayParticleInterface {

    public void PlayOutHeartParticle(Location partLocation, Player player) {

        EnumParticle part = EnumParticle.HEART;
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(part, false, (float) partLocation.getX(), (float) partLocation.getY(), (float) partLocation.getZ(), 0, 1, 0, (float) 0.1, 1, null);

        // part.toString(), (float) partLocation.getX(), (float)
        // partLocation.getY(), (float) partLocation.getZ(), 0, 1, 0, 0.1, 1
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void PlayOutParticle(Location partLocation, Player player, SupportedParticles particle) {

        EnumParticle part = EnumParticle.valueOf(particle.toString());
        if (part == null)
            return;

        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(part, false, (float) partLocation.getX(), (float) partLocation.getY(), (float) partLocation.getZ(), 0, 1, 0, (float) 0.1, 1, null);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
