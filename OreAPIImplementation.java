/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2018
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;

import Reika.CondensedOres.API.CondensedOreAPI;
import Reika.CondensedOres.API.OreEntryBase;
import Reika.CondensedOres.Control.OreEntry;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;


public class OreAPIImplementation extends CondensedOreAPI {

	private final HashMap<BlockKey, Boolean> canGenerate = new HashMap();

	public void resetGenCache() {
		canGenerate.clear();
	}

	@Override
	public Collection<OreEntryBase> getGeneratorsFor(Block b) {
		return this.getGeneratorsFor(b, OreDictionary.WILDCARD_VALUE);
	}

	@Override
	public Collection<OreEntryBase> getGeneratorsFor(Block b, int meta) {
		Collection<OreEntryBase> li = new ArrayList();
		BlockKey bk = new BlockKey(b, meta);
		for (OreEntry e : CondensedOreConfig.instance.getOres()) {
			if (e.getBlockTypes().contains(bk)) {
				li.add(e);
			}
		}
		return li;
	}

	@Override
	public boolean doesBlockGenerate(Block b, int meta) {
		BlockKey bk = new BlockKey(b, meta);
		Boolean flag = canGenerate.get(bk);
		if (flag == null) {
			flag = !this.getGeneratorsFor(b, meta).isEmpty();
			canGenerate.put(bk, flag);
		}
		return flag.booleanValue();
	}

	@Override
	public Collection<OreEntryBase> getAllGenerators() {
		return (Collection)CondensedOreConfig.instance.getOres();
	}

	@Override
	public OreEntryBase getGeneratorByName(String s) {
		return CondensedOreConfig.instance.getOre(s);
	}

}
