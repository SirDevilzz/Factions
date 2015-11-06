package com.massivecraft.factions.cmd;

import java.util.ArrayList;
import java.util.List;

import com.massivecraft.factions.Perm;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.cmd.type.TypeMPerm;
import com.massivecraft.factions.cmd.type.TypeRel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;
import com.massivecraft.massivecore.command.type.primitive.TypeBoolean;
import com.massivecraft.massivecore.util.Txt;

public class CmdFactionsPermSet extends FactionsCommand
{
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public CmdFactionsPermSet()
	{
		// Aliases
		this.addAliases("set");
		
		// Parameters
		this.addParameter(TypeMPerm.get(), "perm");
		this.addParameter(TypeRel.get(), "relation");
		this.addParameter(TypeBoolean.getYes(), "yes/no");
		this.addParameter(TypeFaction.get(), "faction", "you");
		
		// Requirements
		this.addRequirements(RequirementHasPerm.get(Perm.PERM_SET.node));
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public void perform() throws MassiveException
	{
		// Args
		MPerm perm = this.readArg();
		Rel rel = this.readArg();
		Boolean value = this.readArg();
		Faction faction = this.readArg(msenderFaction);
		
		// Do the sender have the right to change perms for this faction?
		if ( ! MPerm.getPermPerms().has(msender, faction, true)) return;
		
		// Is this perm editable?
		if ( ! msender.isUsingAdminMode() && ! perm.isEditable())
		{
			msg("<b>The perm <h>%s <b>is not editable.", perm.getName());
			return;
		}
		
		// No change
		if (faction.getPermitted(perm).contains(rel) == value)
		{
			msg("%s <i>already has %s <i>set to %s <i>for %s<i>.", faction.describeTo(msender), perm.getDesc(true, false), Txt.parse(value ? "<g>YES" : "<b>NOO"), rel.getColor() + rel.getDescPlayerMany());
			return;
		}
		
		// Apply
		faction.setRelationPermitted(perm, rel, value);
		
		// The following is to make sure the leader always has the right to change perms if that is our goal.
		if (perm == MPerm.getPermPerms() && MPerm.getPermPerms().getStandard().contains(Rel.LEADER))
		{
			faction.setRelationPermitted(MPerm.getPermPerms(), Rel.LEADER, true);
		}
		
		// Create messages
		List<String> messages = new ArrayList<String>();
		
		// Inform sender
		messages.add(Txt.titleize("Perm for " + faction.describeTo(msender, true)));
		messages.add(MPerm.getStateHeaders());
		messages.add(Txt.parse(perm.getStateInfo(faction.getPermitted(perm), true)));
		message(messages);
		
		// Inform faction (their message is slighly different)
		List<MPlayer> recipients = faction.getMPlayers();
		recipients.remove(msender);
		
		for (MPlayer recipient : recipients)
		{
			messages.add(0, Txt.parse("<h>%s <i>set a perm for <h>%s<i>.", msender.describeTo(recipient, true), faction.describeTo(recipient, true)));
			recipient.message(messages);
		}
	}
	
}