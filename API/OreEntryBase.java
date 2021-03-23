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

import net.minecraft.world.World;


public abstract class OreEntryBase {

	public final String ID;
	public final String displayName;

	protected OreEntryBase(String id, String n) {
		ID = id;
		displayName = n;
	}

	public abstract boolean isValidDimension(World world);

	public abstract boolean isValidBiome(World world, int x, int z);

	/** Provides a multiline string detailing the parameters of this ore generator entry. */
	public abstract String info();

}
