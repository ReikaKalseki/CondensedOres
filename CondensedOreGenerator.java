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

import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import Reika.CondensedOres.Control.OreEntry;
import Reika.DragonAPI.Interfaces.RetroactiveGenerator;



public class CondensedOreGenerator implements RetroactiveGenerator {

	public static final CondensedOreGenerator instance = new CondensedOreGenerator();

	private CondensedOreGenerator() {

	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		List<OreEntry> c = CondensedOreConfig.instance.getOresSorted();
		for (OreEntry ore : c) {
			try {
				ore.generate(world, chunkX, chunkZ, random);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to generate ore entry "+ore.ID+"; it was likely defined incorrectly!", e);
			}
		}
	}

	@Override
	public boolean canGenerateAt(World world, int chunkX, int chunkZ) {
		return true;
	}

	@Override
	public String getIDString() {
		return "CondensedOres";
	}

}
