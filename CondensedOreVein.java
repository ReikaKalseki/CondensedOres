/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import Reika.CondensedOres.Control.OreEntry;
import Reika.CondensedOres.Control.ProximityRule;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;


public class CondensedOreVein extends WorldGenerator {

	private final OreEntry ore;
	private final ArrayList<BlockKey> blocks;
	private final int veinSize;

	public Block target;
	public ProximityRule proximity;

	public CondensedOreVein(OreEntry o, ArrayList<BlockKey> blocks, int number) {
		ore = o;
		this.blocks = blocks;
		veinSize = number;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		BlockKey bk = this.getRandomOre(rand);

		float f = rand.nextFloat() * (float)Math.PI;
		double xvar_pos = x + 8 + MathHelper.sin(f) * veinSize / 8F;
		double xvar_neg = x + 8 - MathHelper.sin(f) * veinSize / 8F;
		double zvar_pos = z + 8 + MathHelper.cos(f) * veinSize / 8F;
		double zvar_neg = z + 8 - MathHelper.cos(f) * veinSize / 8F;
		double ypos_1 = y + rand.nextInt(3) - 2;
		double ypos_2 = y + rand.nextInt(3) - 2;

		for (int l = 0; l <= veinSize; l++ ) {
			double d6 = xvar_pos + (xvar_neg - xvar_pos) * l / veinSize;
			double d7 = ypos_1 + (ypos_2 - ypos_1) * l / veinSize;
			double d8 = zvar_pos + (zvar_neg - zvar_pos) * l / veinSize;
			double d9 = rand.nextDouble() * veinSize / 16D;
			double d10 = (MathHelper.sin(l * (float)Math.PI / veinSize) + 1F) * d9 + 1D;
			double d11 = (MathHelper.sin(l * (float)Math.PI / veinSize) + 1F) * d9 + 1D;

			int i1 = MathHelper.floor_double(d6 - d10 / 2D);
			int j1 = MathHelper.floor_double(d7 - d11 / 2D);
			int k1 = MathHelper.floor_double(d8 - d10 / 2D);
			int l1 = MathHelper.floor_double(d6 + d10 / 2D);
			int i2 = MathHelper.floor_double(d7 + d11 / 2D);
			int j2 = MathHelper.floor_double(d8 + d10 / 2D);

			for (int k2 = i1; k2 <= l1; k2++ ) {
				double d12 = (k2 + 0.5D - d6) / (d10 / 2D);

				if (d12 * d12 < 1D) {
					for (int l2 = j1; l2 <= i2; l2++ ) {
						double d13 = (l2 + 0.5D - d7) / (d11 / 2D);

						if (d12 * d12 + d13 * d13 < 1D) {
							for (int i3 = k1; i3 <= j2; i3++ ) {
								double d14 = (i3 + 0.5D - d8) / (d10 / 2D);

								if (d12 * d12 + d13 * d13 + d14 * d14 < 1D) {
									if (world.getBlock(k2, l2, i3).isReplaceableOreGen(world, k2, l2, i3, target)) {
										if (!proximity.strictProximity || proximity.isLocationValid(world, k2, l2, i3)) {

											if (ore.sprinkleOre)
												bk = this.getRandomOre(rand);

											world.setBlock(k2, l2, i3, bk.blockID, bk.metadata, 2);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	private BlockKey getRandomOre(Random rand) {
		return blocks.get(rand.nextInt(blocks.size()));
	}

}
