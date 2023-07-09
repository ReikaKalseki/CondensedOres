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
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import Reika.CondensedOres.Control.OreEntry;
import Reika.CondensedOres.Control.ProximityRule;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Interfaces.Subgenerator;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;


public class CondensedOreVein extends WorldGenerator implements Subgenerator {

	public final OreEntry ore;
	private final WeightedRandom<BlockKey> blocks;
	private final int veinSizeMin;
	private final int veinSizeMax;

	public Block target;
	public ProximityRule proximity;

	private BlockKey currentToPlace;

	private int placedCount;
	private Vec3 placeCenterSum;

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
		placedCount = 0;
		placeCenterSum = Vec3.createVectorHelper(0, 0, 0);
		if (ore.shape.generator.generateAt(world, rand, x, y, z, this, ReikaRandomHelper.getRandomBetween(veinSizeMin, veinSizeMax, rand)) && placedCount > 0) {
			placeCenterSum.xCoord /= placedCount;
			placeCenterSum.yCoord /= placedCount;
			placeCenterSum.zCoord /= placedCount;
			ore.onVeinGenerated(world, placeCenterSum);
			return true;
		}
		return false;
	}

	public boolean tryPlaceBlock(World world, int dx, int dy, int dz, Random rand) {
		if (ore.sprinkleOre)
			currentToPlace = this.getRandomOre(rand);
		return this.tryPlaceBlock(world, dx, dy, dz, rand, currentToPlace);
	}

	public boolean tryPlaceBlock(World world, int dx, int dy, int dz, Random rand, BlockKey bk) {
		if (world.getBlock(dx, dy, dz).isReplaceableOreGen(world, dx, dy, dz, target)) {
			if (!proximity.strictProximity || proximity.isLocationValid(world, dx, dy, dz)) {
				if (world.setBlock(dx, dy, dz, bk.blockID, bk.metadata, 2)) {
					placeCenterSum.xCoord += dx;
					placeCenterSum.yCoord += dy;
					placeCenterSum.zCoord += dz;
					placedCount++;
				}
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
