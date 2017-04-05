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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import Reika.CondensedOres.CondensedOreOptions;
import Reika.CondensedOres.CondensedOreVein;
import Reika.CondensedOres.Control.BiomeRule.BiomeRuleset;
import Reika.CondensedOres.Control.DimensionRule.DimensionRuleset;
import Reika.DragonAPI.Auxiliary.Trackers.RetroGenController;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Interfaces.RetroactiveGenerator;


public class OreEntry implements RetroactiveGenerator {

	private final ArrayList<BlockKey> oreBlocks = new ArrayList();

	private final DimensionRuleset dimensionRules;
	private final BiomeRuleset biomeRules;

	private final ProximityRule neighbors;

	private final HashSet<BlockKey> blockRules = new HashSet();

	private final FrequencyRule frequency;
	private final HeightRule height;

	public final String ID;
	public final String displayName;
	private final int veinSize;

	private CondensedOreVein vein;

	public final boolean sprinkleOre;
	public final boolean doRetrogen;

	public OreEntry(String id, String n, int size, boolean spr, boolean retro, HeightRule h, FrequencyRule f, DimensionRuleset dim, BiomeRuleset b, ProximityRule p) {
		ID = id;
		displayName = n;
		veinSize = (int)(size*CondensedOreOptions.SIZE.getFloat());
		frequency = f;
		height = h;
		dimensionRules = dim;
		biomeRules = b;
		neighbors = p;
		sprinkleOre = spr;
		doRetrogen = retro;
		if (doRetrogen)
			RetroGenController.instance.addRetroGenerator(this, 0);
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
		sb.append("Proximity Rules: "+neighbors+"\n");
		return sb.toString();
	}

	public void generate(World world, int chunkX, int chunkZ, Random random) {
		chunkX *= 16;
		chunkZ *= 16;
		boolean flag = false;
		if (frequency.generate(chunkX, chunkZ, random)) {
			if (dimensionRules.isValidDimension(world)) {
				if (biomeRules.isValidBiome(world, chunkX, chunkZ)) {
					int n = frequency.getVeinCount(chunkX, chunkZ, random);
					for (int i = 0; i < n; i++) {
						int x = chunkX + random.nextInt(16);
						int z = chunkZ + random.nextInt(16);
						int y = height.getRandomizedY(random);
						flag = this.placeVein(world, x, y, z, random);
					}
				}
			}
		}
		//else {
		//
		//}
		//if (flag) {
		//	CondensedOres.logger.debug("Generated "+this+" @ "+chunkX+","+chunkZ);
		//}
	}

	private boolean placeVein(World world, int x, int y, int z, Random random) {
		BlockKey at = BlockKey.getAt(world, x, y, z);
		if (blockRules.contains(at)) {
			if (neighbors.isLocationValid(world, x, y, z)) {
				vein.target = at.blockID;
				vein.proximity = neighbors;
				if (vein.generate(world, random, x, y, z)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isEmpty() {
		return oreBlocks.isEmpty();
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		this.generate(world, chunkX, chunkZ, random);
	}

	@Override
	public boolean canGenerateAt(World world, int chunkX, int chunkZ) {
		return true;
	}

	@Override
	public String getIDString() {
		return "CondensedOresPrototype_"+ID;
	}

}
