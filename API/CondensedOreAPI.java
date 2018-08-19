/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2018
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres.API;

import java.util.ArrayList;

import net.minecraft.block.Block;


public abstract class CondensedOreAPI {

	public static CondensedOreAPI instance;

	/** These are not quite as performant. Do not use in lieu of doesGenerate. */
	public abstract ArrayList<OreEntryBase> getGeneratorsFor(Block b);
	/** These are not quite as performant. Do not use in lieu of doesGenerate. */
	public abstract ArrayList<OreEntryBase> getGeneratorsFor(Block b, int meta);

	/** Call this as much as you like; it is optimized. */
	public abstract boolean doesBlockGenerate(Block b, int meta);

}
