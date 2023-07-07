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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.CondensedOres.Control.OreEntry;
import Reika.CondensedOres.Control.ProximityRule;
import Reika.DragonAPI.Exception.UnreachableCodeException;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Effects.LightningBolt;
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Interfaces.Subgenerator;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;


public class CondensedOreVein extends WorldGenerator implements Subgenerator {

	private final OreEntry ore;
	private final WeightedRandom<BlockKey> blocks;
	private final int veinSizeMin;
	private final int veinSizeMax;

	public Block target;
	public ProximityRule proximity;

	private BlockKey currentToPlace;
	private int veinSize;

	public CondensedOreVein(OreEntry o, WeightedRandom<BlockKey> blocks, int min, int max) {
		ore = o;
		this.blocks = blocks;
		veinSizeMin = min;
		veinSizeMax = max;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		blocks.setSeed(rand.nextLong());
		currentToPlace = this.getRandomOre(rand);
		veinSize = ReikaRandomHelper.getRandomBetween(veinSizeMin, veinSizeMax, rand);
		switch(ore.shape) {
			case VANILLA:
				return this.generateVanillaStyle(world, rand, x, y, z);
			case TENDRIL:
				return this.generateTendril(world, rand, x, y, z);
			case STAR:
				return this.generateStarVein(world, rand, x, y, z);
			default:
				throw new UnreachableCodeException(ore.shape);
		}
	}

	private boolean generateStarVein(World world, Random rand, int x, int y, int z) {
		int n = 0;
		float ang1 = rand.nextFloat()*360;
		float ang2 = (float)ReikaRandomHelper.getRandomPlusMinus(ang1+180D, 90, rand);
		double len = veinSize/1.5D;
		ArrayList<DecimalPosition> ends = new ArrayList();
		int endCount = ReikaRandomHelper.getRandomBetween(4, 9, rand);
		for (int i = 0; i < endCount; i++) {
			double[] angs = ReikaPhysicsHelper.polarToCartesianFast(1, rand.nextDouble()*360, rand.nextDouble()*360);
			ends.add(new DecimalPosition(angs[0], angs[1], angs[2]));
		}
		HashSet<Coordinate> cells = new HashSet();
		double maxR = Math.max(0.67, veinSize/32D);
		for (DecimalPosition pos : ends) {
			double len2 = len*ReikaRandomHelper.getRandomBetween(0.7, 1, rand);
			for (double d = 0; d <= len2; d += 0.5) {
				double f = Math.pow(1D-Math.max(0, (d-3)/len2), 1.2);
				double r = maxR*f;
				double tr = r+0.2;
				tr *= tr;
				double dx = x+pos.xCoord*d;
				double dy = y+pos.yCoord*d;
				double dz = z+pos.zCoord*d;
				int ir = MathHelper.ceiling_double_int(r);
				for (int i = -ir; i <= ir; i++) {
					for (int j = -ir; j <= ir; j++) {
						for (int k = -ir; k <= ir; k++) {
							if (i*i+j*j+k*k <= tr)
								cells.add(new Coordinate(dx+i, dy+j, dz+k));
						}
					}
				}
			}
		}
		for (Coordinate c : cells) {
			if (this.tryPlaceBlock(world, c.xCoord, c.yCoord, c.zCoord, rand))
				n++;
		}
		if (n > 0)
			ore.onVeinGenerated(world, Vec3.createVectorHelper(x, y, z));
		return n > 0;
	}

	private boolean generateTendril(World world, Random rand, int x, int y, int z) {
		int n = 0;
		float ang1 = rand.nextFloat()*360;
		float ang2 = (float)ReikaRandomHelper.getRandomPlusMinus(ang1+180D, 90, rand);
		double len = veinSize/2.5D;
		DecimalPosition pos1 = new DecimalPosition(x+len*MathHelper.cos(ang1), ReikaRandomHelper.getRandomPlusMinus(y, 10), z+len*MathHelper.sin(ang1));
		DecimalPosition pos2 = new DecimalPosition(x+len*MathHelper.cos(ang2), ReikaRandomHelper.getRandomPlusMinus(y, 10), z+len*MathHelper.sin(ang2));
		LightningBolt lb = new LightningBolt(pos1, pos2, 5).scaleVariance(3).maximize();
		HashSet<Coordinate> cells = new HashSet();
		List<DecimalPosition> li = lb.spline(SplineType.CENTRIPETAL, 18);
		double maxR = Math.max(1, veinSize/15D);
		for (int idx = 0; idx < li.size(); idx++) {
			DecimalPosition pos = li.get(idx);
			Coordinate c = pos.getCoordinate();
			double f0 = idx/(double)li.size();
			double f = Math.min(f0, 1D-f0)*2;
			double r = maxR*f;
			double tr = r+0.2;
			tr *= tr;
			int ir = MathHelper.ceiling_double_int(r);
			for (int i = -ir; i <= ir; i++) {
				for (int j = -ir; j <= ir; j++) {
					for (int k = -ir; k <= ir; k++) {
						if (i*i+j*j+k*k <= tr)
							cells.add(c.offset(i, j, k));
					}
				}
			}
		}
		for (Coordinate c : cells) {/*
		for (int i = 0; i < veinSize && !cells.isEmpty(); i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, cells);
			cells.remove(c);*/
			if (this.tryPlaceBlock(world, c.xCoord, c.yCoord, c.zCoord, rand))
				n++;
		}
		if (n > 0)
			ore.onVeinGenerated(world, Vec3.createVectorHelper(x, y, z));
		return n > 0;
	}

	private boolean generateVanillaStyle(World world, Random rand, int x, int y, int z) {
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
									if (this.tryPlaceBlock(world, dx, dy, dz, rand)) {
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

		if (n > 0) {
			center.xCoord /= n;
			center.yCoord /= n;
			center.zCoord /= n;
			ore.onVeinGenerated(world, center);
		}

		return n > 0;
	}

	private boolean tryPlaceBlock(World world, int dx, int dy, int dz, Random rand) {
		if (world.getBlock(dx, dy, dz).isReplaceableOreGen(world, dx, dy, dz, target)) {
			if (!proximity.strictProximity || proximity.isLocationValid(world, dx, dy, dz)) {

				if (ore.sprinkleOre)
					currentToPlace = this.getRandomOre(rand);

				world.setBlock(dx, dy, dz, currentToPlace.blockID, currentToPlace.metadata, 2);
				return true;
			}
		}
		return false;
	}

	private BlockKey getRandomOre(Random rand) {
		return blocks.getRandomEntry();
	}

	@Override
	public Object getParentGenerator() {
		return ore;
	}

}
