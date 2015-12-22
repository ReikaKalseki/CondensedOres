/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres.Control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.world.World;
import Reika.CondensedOres.CondensedOreOptions;
import Reika.CondensedOres.CondensedOreVein;
import Reika.CondensedOres.Control.BiomeRule.BiomeRuleset;
import Reika.CondensedOres.Control.DimensionRule.DimensionRuleset;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;


public class OreEntry {

	private final ArrayList<BlockKey> oreBlocks = new ArrayList();

	private final DimensionRuleset dimensionRules;
	private final BiomeRuleset biomeRules;

	private final ProximityRule neighbors;

	private final HashSet<BlockKey> blockRules = new HashSet();

	private final FrequencyRule frequency;
	private final HeightRule height;

	public final String displayName;
	private final int veinSize;

	private CondensedOreVein vein;

	public final boolean sprinkleOre;

	public OreEntry(String n, int size, boolean spr, HeightRule h, FrequencyRule f, DimensionRuleset dim, BiomeRuleset b, ProximityRule p) {
		displayName = n;
		veinSize = (int)(size*CondensedOreOptions.SIZE.getFloat());
		frequency = f;
		height = h;
		dimensionRules = dim;
		biomeRules = b;
		neighbors = p;
		sprinkleOre = spr;
	}
	/*
	public OreEntry addBlock(Block b) {
		return this.addBlock(b, 0);
	}

	public OreEntry addBlock(Block b, int meta) {
		return this.addBlock(new BlockKey(b, meta));
	}
	 */
	public OreEntry addBlock(BlockKey bk) {
		if (!oreBlocks.contains(bk))
			oreBlocks.add(bk);
		return this;
	}

	public OreEntry addBiomeRule(BiomeRule b) {
		biomeRules.addRule(b);
		return this;
	}

	public OreEntry addDimensionRule(DimensionRule b) {
		dimensionRules.addRule(b);
		return this;
	}

	public OreEntry addBlockRule(BlockKey b) {
		blockRules.add(b);
		return this;
	}

	public OreEntry build() {
		vein = new CondensedOreVein(this, oreBlocks, veinSize);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: "+displayName+"\n");
		sb.append("Mix: "+(sprinkleOre ? "Sprinkle" : "One Per Vein")+"\n");
		sb.append("Vein Size: "+veinSize+"\n");
		sb.append("Blocks: "+oreBlocks+"\n");
		sb.append("Height: "+height+"\n");
		sb.append("Frequency: "+frequency+"\n");
		sb.append("Biomes: "+biomeRules+"\n");
		sb.append("Dimensions: "+dimensionRules+"\n");
		sb.append("Spawn Blocks: "+blockRules+"\n");
		sb.append("ProximityRules: "+neighbors+"\n");
		return sb.toString();
	}

	public void generate(World world, int chunkX, int chunkZ, Random random) {
		chunkX *= 16;
		chunkZ *= 16;
		if (dimensionRules.isValidDimension(world)) {
			if (biomeRules.isValidBiome(world, chunkX, chunkZ)) {
				if (frequency.generate(chunkX, chunkZ, random)) {
					int n = frequency.getVeinCount(chunkX, chunkZ, random);
					for (int i = 0; i < n; i++) {
						int x = chunkX + random.nextInt(16);
						int z = chunkZ + random.nextInt(16);
						int y = height.getRandomizedY(random);
						this.placeVein(world, x, y, z, random);
					}
				}
			}
		}
	}

	private boolean placeVein(World world, int x, int y, int z, Random random) {
		BlockKey at = BlockKey.getAt(world, x, y, z);
		if (blockRules.contains(at)) {
			if (neighbors.isLocationValid(world, x, y, z)) {
				vein.target = at.blockID;
				vein.proximity = neighbors;
				return vein.generate(world, random, x, y, z);
			}
		}
		return false;
	}

	public boolean isEmpty() {
		return oreBlocks.isEmpty();
	}

}
