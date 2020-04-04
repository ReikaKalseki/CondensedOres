/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import Reika.DragonAPI.Command.DragonCommandBase;


public class ReloadOreConfigCommand extends DragonCommandBase {

	@Override
	public void processCommand(ICommandSender ics, String[] args) {
		CondensedOres.config.reload();
		int errored = CondensedOreConfig.instance.loadConfigs();
		this.sendChatToSender(ics, "Ore config reloaded, "+CondensedOreConfig.instance.getOres().size()+" entries loaded.");
		if (errored > 0)
			this.sendChatToSender(ics, EnumChatFormatting.RED.toString()+errored+" entries errored.");
	}

	@Override
	public String getCommandString() {
		return "reloadoreconfig";
	}

	@Override
	protected boolean isAdminOnly() {
		return true;
	}

}
