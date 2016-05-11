/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres.Control;

import java.util.HashSet;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;


public class ProximityRule {

	private final HashSet<BlockKey> blocks = new HashSet();
	public final boolean strictProximity;

	public ProximityRule(boolean strict) {
		strictProximity = strict;
	}

	public ProximityRule addBlock(BlockKey bk) {
		blocks.add(bk);
		return this;
	}

	public boolean isLocationValid(World world, int x, int y, int z) {
		if (blocks.isEmpty())
			return true;
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			BlockKey bk = BlockKey.getAt(world, dx, dy, dz);
			if (blocks.contains(bk))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return blocks.isEmpty() ? "None" : (strictProximity ? "[Strict]" : "[General]")+" Next to: "+blocks;
	}

}
