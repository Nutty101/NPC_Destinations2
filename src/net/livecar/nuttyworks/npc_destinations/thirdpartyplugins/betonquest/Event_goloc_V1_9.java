package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait.en_RequestedAction;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.QuestEvent;

public class Event_goloc_V1_9 extends QuestEvent
{
	private UUID destUUID;
	private int destID;
	private int targetNPC;
	private int duration;
	
	public Event_goloc_V1_9(Instruction instruction) throws InstructionParseException
	{
		super(instruction);
		//<npcid> <loc#> 

		if (instruction.size() < 3)
		{
			throw new InstructionParseException("Not enough arguments");
		}
		if (NumberUtils.isNumber(instruction.getPart(1)) && NumberUtils.isNumber(instruction.getPart(2)))
		{
			targetNPC = Integer.parseInt(instruction.getPart(1));
			destID = Integer.parseInt(instruction.getPart(2));
			duration = Integer.parseInt(instruction.getPart(3))*1000;
			return;
		}
		if (NumberUtils.isNumber(instruction.getPart(1)) && NumberUtils.isNumber(instruction.getPart(2)) && NumberUtils.isNumber(instruction.getPart(3)))
		{
			duration = Integer.parseInt(instruction.getPart(3))*1000;
			targetNPC = Integer.parseInt(instruction.getPart(1));
			destID = Integer.parseInt(instruction.getPart(2));
			return;
		} else if (NumberUtils.isNumber(instruction.getPart(1)) && !NumberUtils.isNumber(instruction.getPart(2)) && NumberUtils.isNumber(instruction.getPart(3)))
		{
			duration = Integer.parseInt(instruction.getPart(3))*1000;
			targetNPC = Integer.parseInt(instruction.getPart(1));
			if (instruction.getPart(2).matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))
			{
				destID = -1;
				destUUID = UUID.fromString(instruction.getPart(2));
				return;
			}
		}
		throw new InstructionParseException("Values should be numeric (NPCID) (LOC# / OR LocationGUID) (DURATION)" );
	}

    @Override
    public void run(String playerID) 
    {
    	//Validate that the NPC exists
    	NPC npc = CitizensAPI.getNPCRegistry().getById(targetNPC);
		if (npc == null) {
			// specified number doesn't exist.
		    BetonQuest_Plugin_V1_9.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin_V1_9.destRef,"destinations","Console_Messages.betonquest_error","Event_GoLocation references invalid NPC ID " + targetNPC);
			return;
		}
		
		NPCDestinationsTrait trait = null;
		if (!npc.hasTrait(NPCDestinationsTrait.class)) {
		    BetonQuest_Plugin_V1_9.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin_V1_9.destRef,"destinations","Console_Messages.betonquest_error","Event_GoLocation references NPC (" + targetNPC + "), but lacks the NPCDestination trait.");
		} else
			trait = npc.getTrait(NPCDestinationsTrait.class);

		Destination_Setting newDest = null;
		if (destID > -1)
		{
			if (destID > trait.NPCLocations.size())
			{
			    BetonQuest_Plugin_V1_9.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin_V1_9.destRef,"destinations","Console_Messages.betonquest_error","Event_GoLocation references NPC (" + targetNPC + ") but is missing location (" + destID + ")");
				return;
			}
			newDest = trait.NPCLocations.get(destID);
		} else {
			for (Destination_Setting destLoc : trait.NPCLocations)
			{
				if (destLoc.LocationIdent.toString().equalsIgnoreCase(destUUID.toString()))
				{
					newDest = destLoc;
					break;
				}
			}
		}
		if (newDest == null)
			return;
		
		npc.getNavigator().cancelNavigation();
		trait.clearPendingDestinations();
		trait.lastResult = "Forced location";
		trait.setLocation = newDest;
		trait.currentLocation = newDest;
		trait.locationLockUntil = new java.util.Date(System.currentTimeMillis()+duration);
		trait.lastPositionChange = new Date();
		trait.setRequestedAction(en_RequestedAction.SET_LOCATION);
		return;
    }
}
