package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait.en_RequestedAction;
import org.apache.commons.lang.math.NumberUtils;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.api.QuestEvent;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event_goloc extends QuestEvent {
	private UUID destUUID;
	private int destID;
	private int targetNPC;
	private int duration;
	
	public Event_goloc(Instruction instruction) throws InstructionParseException {
		super(instruction,true);
		//<npcid> <loc#>
		
		if (instruction.size() < 3) {
			throw new InstructionParseException("Not enough arguments");
		}
		if (NumberUtils.isNumber(instruction.getPart(1)) && NumberUtils.isNumber(instruction.getPart(2))) {
			targetNPC = Integer.parseInt(instruction.getPart(1));
			destID = Integer.parseInt(instruction.getPart(2));
			duration = Integer.parseInt(instruction.getPart(3)) * 1000;
			return;
		}
		if (NumberUtils.isNumber(instruction.getPart(1)) && NumberUtils.isNumber(instruction.getPart(2)) && NumberUtils.isNumber(instruction.getPart(3))) {
			duration = Integer.parseInt(instruction.getPart(3)) * 1000;
			targetNPC = Integer.parseInt(instruction.getPart(1));
			destID = Integer.parseInt(instruction.getPart(2));
			return;
		} else if (NumberUtils.isNumber(instruction.getPart(1)) && !NumberUtils.isNumber(instruction.getPart(2)) && NumberUtils.isNumber(instruction.getPart(3))) {
			duration = Integer.parseInt(instruction.getPart(3)) * 1000;
			targetNPC = Integer.parseInt(instruction.getPart(1));
			if (instruction.getPart(2).matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
				destID = -1;
				destUUID = UUID.fromString(instruction.getPart(2));
				return;
			}
		}
		throw new InstructionParseException("Values should be numeric (NPCID) (LOC# / OR LocationGUID) (DURATION)");
	}
	
	@Override
	protected Void execute(String playerID) throws IllegalStateException, QuestRuntimeException {
		//Validate that the NPC exists
		NPC npc = CitizensAPI.getNPCRegistry().getById(targetNPC);
		if (npc == null) {
			// specified number doesn't exist.
			BetonQuest_Plugin.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin.destRef, "destinations", "Console_Messages.betonquest_error", "Event_GoLocation references invalid NPC ID " + targetNPC);
			return null;
		}
		
		NPCDestinationsTrait trait = null;
		if (!npc.hasTrait(NPCDestinationsTrait.class)) {
			BetonQuest_Plugin.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin.destRef, "destinations", "Console_Messages.betonquest_error", "Event_GoLocation references NPC (" + targetNPC + "), but lacks the NPCDestination trait.");
		} else
			trait = npc.getTrait(NPCDestinationsTrait.class);
		
		Destination_Setting newDest = null;
		if (destID > -1) {
			if (destID > trait.NPCLocations.size()) {
				BetonQuest_Plugin.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin.destRef, "destinations", "Console_Messages.betonquest_error", "Event_GoLocation references NPC (" + targetNPC + ") but is missing location (" + destID + ")");
				return null;
			}
			newDest = trait.NPCLocations.get(destID);
		} else {
			for (Destination_Setting destLoc : trait.NPCLocations) {
				if (destLoc.LocationIdent.toString().equalsIgnoreCase(destUUID.toString())) {
					newDest = destLoc;
					break;
				}
			}
		}
		if (newDest == null)
			return null;
		
		npc.getNavigator().cancelNavigation();
		trait.clearPendingDestinations();
		trait.lastResult = "Forced location";
		trait.setLocation = newDest;
		trait.currentLocation = newDest;
		trait.setLocationLockUntil(duration);
		trait.lastPositionChange = LocalDateTime.now();
		trait.setRequestedAction(en_RequestedAction.SET_LOCATION);
		return null;
	}
}
