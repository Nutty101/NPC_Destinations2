package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest.v2;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import org.apache.commons.lang.math.NumberUtils;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

import java.util.UUID;

public class Condition_CurrentLocation extends Condition {
    private UUID destUUID;
    private int destID;
    private int targetNPC;

    public Condition_CurrentLocation(Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        //<npcid> <loc#>

        if (instruction.size() < 2) {
            throw new InstructionParseException("Not enough arguments");
        }
        if (NumberUtils.isNumber(instruction.getPart(1)) && NumberUtils.isNumber(instruction.getPart(2))) {
            targetNPC = Integer.parseInt(instruction.getPart(1));
            destID = Integer.parseInt(instruction.getPart(2));
            return;
        } else if (NumberUtils.isNumber(instruction.getPart(1)) && !NumberUtils.isNumber(instruction.getPart(2))) {
            targetNPC = Integer.parseInt(instruction.getPart(1));
            if (instruction.getPart(2).matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                destID = -1;
                destUUID = UUID.fromString(instruction.getPart(2));
                return;
            }
        }

        throw new InstructionParseException("Values should be numeric (NPCID) (LOC# / OR LocationGUID)");
    }

    @Override
    protected Boolean execute(String playerID) throws QuestRuntimeException {
        //Validate that the NPC exists
        NPC npc = CitizensAPI.getNPCRegistry().getById(targetNPC);
        if (npc == null) {
            // specified number doesn't exist.
            BetonQuest_Plugin.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin.destRef, "destinations", "Console_Messages.betonquest_error", "Condition_CurrentLoc references invalid NPC ID " + targetNPC);
            return false;
        }

        NPCDestinationsTrait trait = null;
        if (!npc.hasTrait(NPCDestinationsTrait.class)) {
            BetonQuest_Plugin.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin.destRef, "destinations", "Console_Messages.betonquest_error", "Condition_CurrentLoc references NPC (" + targetNPC + "), but lacks the NPCDestination trait.");
            return false;
        } else
            trait = npc.getTrait(NPCDestinationsTrait.class);

        if (destID > -1) {
            if (destID >= trait.NPCLocations.size()) {
                BetonQuest_Plugin.destRef.getMessageManager.consoleMessage(BetonQuest_Plugin.destRef, "destinations", "Console_Messages.betonquest_error", "Condition_CurrentLoc references NPC (" + targetNPC + ") but is missing location (" + destID + ")");
                return false;
            }

            return trait.NPCLocations.get(destID).destination.toString().equals(trait.currentLocation.destination.toString());
        } else return trait.currentLocation.LocationIdent.toString().equalsIgnoreCase(destUUID.toString());
    }
}
