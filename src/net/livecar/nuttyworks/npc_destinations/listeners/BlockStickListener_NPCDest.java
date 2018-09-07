package net.livecar.nuttyworks.npc_destinations.listeners;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockStickListener_NPCDest implements org.bukkit.event.Listener {
    private PlayerInteractEvent lastClickEvent;

    private DestinationsPlugin  destRef = null;

    public BlockStickListener_NPCDest(DestinationsPlugin storageRef) {
        destRef = storageRef;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (destRef.getMCUtils.getMainHand(player) == null)
                return;
            if (destRef.getMCUtils.getMainHand(player).getType() == Material.STICK && destRef.getMCUtils.getMainHand(player).getItemMeta().getDisplayName() != null && destRef.getMCUtils.getMainHand(player).getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor
                    .translateAlternateColorCodes('&', "&eNPCDestinations &2[&fBlockStick&2]"))) {
                if (lastClickEvent == null) {
                    lastClickEvent = event;
                } else if (lastClickEvent.getPlayer().equals(event.getPlayer()) && lastClickEvent.getClickedBlock().equals(event.getClickedBlock())) {
                    // Return and ignore this event.
                    event.setCancelled(true);
                    return;
                }
                lastClickEvent = event;

                NPC npc = destRef.getCitizensPlugin.getNPCSelector().getSelected(player);
                if (npc == null) {
                    destRef.getMessageManager.sendMessage("destinations", player, "messages.invalid_npc");
                    event.setCancelled(true);
                    return;
                } else {
                    NPCDestinationsTrait trait = null;
                    if (npc != null) {
                        if (!npc.hasTrait(NPCDestinationsTrait.class)) {
                            destRef.getMessageManager.sendMessage("destinations", player, "messages.invalid_npc");
                            event.setCancelled(true);
                            return;
                        } else
                            trait = npc.getTrait(NPCDestinationsTrait.class);
                        if (!player.isSneaking()) {
                            if (!trait.AllowedPathBlocks.contains(event.getClickedBlock().getType())) {
                                trait.AllowedPathBlocks.add(event.getClickedBlock().getType());
                                destRef.getMessageManager.sendMessage("destinations", player, "messages.commands_addblock_added", event.getClickedBlock().getType());
                            } else {
                                destRef.getMessageManager.sendMessage("destinations", player, "messages.commands_addblock_exists", event.getClickedBlock().getType());
                            }
                        } else {
                            if (trait.AllowedPathBlocks.size() > 0) {
                                if (trait.AllowedPathBlocks.contains(event.getClickedBlock().getType())) {
                                    trait.AllowedPathBlocks.remove(event.getClickedBlock().getType());
                                    destRef.getMessageManager.sendMessage("destinations", player, "messages.commands_removeblock_removed", event.getClickedBlock().getType());
                                } else {
                                    destRef.getMessageManager.sendMessage("destinations", player, "messages.commands_removeblock_notinlist", event.getClickedBlock().getType());
                                }
                            } else {
                                destRef.getMessageManager.sendMessage("destinations", player, "messages.commands_removeblock_notinlist", event.getClickedBlock().getType());
                            }
                        }
                    }
                    event.setCancelled(true);
                    return;

                }
            }
        }
    }
}
