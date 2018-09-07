package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest;

import java.util.UUID;

import org.apache.commons.lang.math.NumberUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.Condition;

public class Condition_DistanceToLocation_V1_9 extends Condition
{
	private UUID destUUID;
	private int destID;
	private int destDistance;
	private int targetNPC;

	public Condition_DistanceToLocation_V1_9(Instruction instruction) throws InstructionParseException
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
			destDistance = Integer.parseInt(instruction.getPart(3));
			return;
		} else if (NumberUtils.isNumber(instruction.getPart(1)) && !NumberUtils.isNumber(instruction.getPart(2)))
		{
			targetNPC = Integer.parseInt(instruction.getPart(1));
			destDistance = Integer.parseInt(instruction.getPart(3));
			if (instruction.getPart(2).matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))
			{
				destID = -1;
				destUUID = UUID.fromString(instruction.getPart(2));
				return;
			}
		}

		throw new InstructionParseException("Values should be numeric (NPCID) (LOC# / OR LocationGUID)(Distance)" );
	}

	public boolean check(String playerID)
	{
		//Validate that the NPC exists
		NPC npc = CitizensAPI.getNPCRegistry().getById(targetNPC);
		if (npc == null) {
			// specified number doesn't exist.
		    BetonQuest_Plugin_V1_9.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin_V1_9.destRef,"destinations","Console_Messages.betonquest_error","Condition_CurrentLoc references invalid NPC ID " + targetNPC);
			return false;
		}

		NPCDestinationsTrait trait = null;
		if (!npc.hasTrait(NPCDestinationsTrait.class)) {
		    BetonQuest_Plugin_V1_9.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin_V1_9.destRef,"destinations","Console_Messages.betonquest_error","Condition_CurrentLoc references NPC (" + targetNPC + "), but lacks the NPCDestination trait.");
			return false;
		} else
			trait = npc.getTrait(NPCDestinationsTrait.class);

		if (destID > -1)
		{
			if (destID >= trait.NPCLocations.size())
			{
			    BetonQuest_Plugin_V1_9.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin_V1_9.destRef,"destinations","Console_Messages.betonquest_error","Condition_CurrentLoc references NPC (" + targetNPC + ") but is missing location (" + destID + ")");
				return false;
			}

			//if (trait.NPCLocations.get(destID).destination.toString().equals(trait.currentLocation.destination.toString()))
			//{
            return trait.NPCLocations.get(destID).destination.distance(npc.getEntity().getLocation()) <= destDistance;
            //}
		} 
		for (Destination_Setting destLoc : trait.NPCLocations)
		{
		    
			if (destLoc.LocationIdent.toString().equalsIgnoreCase(destUUID.toString()) && destLoc.destination.distance(npc.getEntity().getLocation()) <= destDistance)
			{
				return true;
			}
		}
		return false;
	}
}