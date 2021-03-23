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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.CondensedOres.Control.OreEntry;
import Reika.CondensedOres.Control.ProximityRule;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Interfaces.Subgenerator;


public class CondensedOreVein extends WorldGenerator implements Subgenerator {

	private final OreEntry ore;
	private final WeightedRandom<BlockKey> blocks;
	private final int veinSize;

	public Block target;
	public ProximityRule proximity;

	public CondensedOreVein(OreEntry o, WeightedRandom<BlockKey> blocks, int number) {
		ore = o;
		this.blocks = blocks;
		veinSize = number;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		blocks.setSeed(rand.nextLong());
		BlockKey bk = this.getRandomOre(rand);

		float f = rand.nextFloat() * (float)Math.PI;
		double xvar_pos = x + 8 + MathHelper.sin(f) * veinSize / 8F;
		double xvar_neg = x + 8 - MathHelper.sin(f) * veinSize / 8F;
		double zvar_pos = z + 8 + MathHelper.cos(f) * veinSize / 8F;
		double zvar_neg = z + 8 - MathHelper.cos(f) * veinSize / 8F;
		double ypos_1 = y + rand.nextInt(3) - 2;
		double ypos_2 = y + rand.nextInt(3) - 2;

		Vec3 center = Vec3.createVectorHelper(0, 0, 0);
		int n = 0;

		for (int l = 0; l <= veinSize; l++ ) {
			double wx = xvar_pos + (xvar_neg - xvar_pos) * l / veinSize;
			double wy = ypos_1 + (ypos_2 - ypos_1) * l / veinSize;
			double wz = zvar_pos + (zvar_neg - zvar_pos) * l / veinSize;
			double rl = rand.nextDouble() * veinSize / 16D;
			double r1 = (MathHelper.sin(l * (float)Math.PI / veinSize) + 1F) * rl + 1D;
			double r2 = (MathHelper.sin(l * (float)Math.PI / veinSize) + 1F) * rl + 1D;

			int mx = MathHelper.floor_double(wx - r1 / 2D);
			int my = MathHelper.floor_double(wy - r2 / 2D);
			int mz = MathHelper.floor_double(wz - r1 / 2D);
			int px = MathHelper.floor_double(wx + r1 / 2D);
			int py = MathHelper.floor_double(wy + r2 / 2D);
			int pz = MathHelper.floor_double(wz + r1 / 2D);

			for (int dx = mx; dx <= px; dx++ ) {
				double ox = (dx + 0.5D - wx) / (r1 / 2D);

				if (ox * ox < 1D) {
					for (int dy = my; dy <= py; dy++ ) {
						double oy = (dy + 0.5D - wy) / (r2 / 2D);

						if (ox * ox + oy * oy < 1D) {
							for (int dz = mz; dz <= pz; dz++ ) {
								double oz = (dz + 0.5D - wz) / (r1 / 2D);

								if (ox * ox + oy * oy + oz * oz < 1D) {
									if (world.getBlock(dx, dy, dz).isReplaceableOreGen(world, dx, dy, dz, target)) {
										if (!proximity.strictProximity || proximity.isLocationValid(world, dx, dy, dz)) {

											if (ore.sprinkleOre)
												bk = this.getRandomOre(rand);

											world.setBlock(dx, dy, dz, bk.blockID, bk.metadata, 2);
											n++;
											center.xCoord += dx;
											center.yCoord += dy;
											center.zCoord += dz;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (n > 0) {
			center.xCoord /= n;
			center.yCoord /= n;
			center.zCoord /= n;
			ore.onVeinGenerated(world, center);
		}

		return true;
	}

	private BlockKey getRandomOre(Random rand) {
		return blocks.getRandomEntry();
	}

	@Override
	public Object getParentGenerator() {
		return ore;
	}

}
