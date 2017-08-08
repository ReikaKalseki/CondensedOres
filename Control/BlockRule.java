/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres.Control;

import net.minecraft.block.Block;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;

@Deprecated
public class BlockRule {

	private final BlockKey block;

	public BlockRule(Block b) {
		this(b, -1);
	}

	public BlockRule(Block b, int meta) {
		block = new BlockKey(b, meta);
	}

	public boolean match(Block b, int meta) {
		return block.match(b, meta);
	}

}
