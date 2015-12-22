/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres;

import net.minecraft.command.ICommandSender;
import Reika.DragonAPI.Command.DragonCommandBase;


public class ReloadOreConfigCommand extends DragonCommandBase {

	@Override
	public void processCommand(ICommandSender ics, String[] args) {
		CondensedOreConfig.instance.loadConfigs();
		this.sendChatToSender(ics, "Ore config reloaded, "+CondensedOreConfig.instance.getOres().size()+" entries loaded.");
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
