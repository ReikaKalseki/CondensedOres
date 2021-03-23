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

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;

import Reika.CondensedOres.CondensedOreOptions;
import Reika.CondensedOres.CondensedOreVein;
import Reika.CondensedOres.API.OreEntryBase;
import Reika.CondensedOres.API.VeinGenerationEvent;
import Reika.CondensedOres.Control.BiomeRule.BiomeRuleset;
import Reika.CondensedOres.Control.DimensionRule.DimensionRuleset;
import Reika.DragonAPI.Auxiliary.Trackers.RetroGenController;
import Reika.DragonAPI.Auxiliary.Trackers.WorldgenProfiler;
import Reika.DragonAPI.Auxiliary.Trackers.WorldgenProfiler.WorldProfilerParent;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Interfaces.RetroactiveGenerator;
import Reika.DragonAPI.Interfaces.Registry.OreType;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;
import Reika.DragonAPI.ModRegistry.ModOreList;


public final class OreEntry extends OreEntryBase implements RetroactiveGenerator, WorldProfilerParent, Comparable<OreEntry> {

	private final WeightedRandom<BlockKey> oreBlocks = new WeightedRandom();

	private final DimensionRuleset dimensionRules;
	private final BiomeRuleset biomeRules;

	private final ProximityRule neighbors;

	private final HashSet<BlockKey> blockRules = new HashSet();

	public final FrequencyRule frequency;
	public final HeightRule height;

	public final int veinSize;

	private CondensedOreVein vein;

	public final boolean sprinkleOre;
	public final boolean doRetrogen;
	public final int sortOrder;

	public OreEntry(String id, String n, int size, int order, boolean spr, boolean retro, HeightRule h, FrequencyRule f, DimensionRuleset dim, BiomeRuleset b, ProximityRule p) {
		super(id, n);
		veinSize = (int)(size*CondensedOreOptions.SIZE.getFloat());
		frequency = f;
		height = h;
		dimensionRules = dim;
		biomeRules = b;
		neighbors = p;
		sprinkleOre = spr;
		doRetrogen = retro;
		sortOrder = order;
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
	public OreEntry addBlock(BlockKey bk, double wt) {
		oreBlocks.addEntry(bk, wt);
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
		WorldgenProfiler.registerGeneratorAsSubGenerator(this, vein);
		return this;
	}

	public Set<BlockKey> getBlockTypes() {
		return Collections.unmodifiableSet(oreBlocks.getValues());
	}

	@Override
	public String info() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: "+displayName+"\n");
		sb.append("Mix: "+(sprinkleOre ? "Sprinkle" : "One Per Vein")+"\n");
		sb.append("Vein Size: "+veinSize+"\n");
		sb.append("Blocks: "+oreBlocks.size()+" -> "+ReikaJavaLibrary.makeSortedListFromCollection(oreBlocks.getValues(), BlockKey.class)+"\n");
		sb.append("Height: "+height+"\n");
		sb.append("Frequency: "+frequency+"\n");
		sb.append("Biomes: "+biomeRules+"\n");
		sb.append("Dimensions: "+dimensionRules+"\n");
		sb.append("Spawn Blocks: "+String.valueOf(blockRules).replace(":-1", "")+"\n");
		sb.append("Proximity Rules: "+neighbors+"\n");
		return sb.toString();
	}

	@Override
	public String toString() {
		return this.info();
	}

	public void generate(World world, int chunkX, int chunkZ, Random random) {
		chunkX *= 16;
		chunkZ *= 16;
		boolean flag = false;
		if (frequency.generate(chunkX, chunkZ, random)) {
			if (this.isValidDimension(world)) {
				if (this.isValidBiome(world, chunkX, chunkZ)) {
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

	@Override
	public boolean isValidDimension(World world) {
		return dimensionRules.isValidDimension(world);
	}

	@Override
	public boolean isValidBiome(World world, int x, int z) {
		return biomeRules.isValidBiome(world, x, z);
	}

	private boolean placeVein(World world, int x, int y, int z, Random random) {
		this.startProfiling(world, x, z);
		BlockKey at = BlockKey.getAt(world, x, y, z);
		if (blockRules.contains(at)) {
			if (neighbors.isLocationValid(world, x, y, z)) {
				vein.target = at.blockID;
				vein.proximity = neighbors;
				if (vein.generate(world, random, x, y, z)) {
					this.finishProfiling(world, x, z);
					return true;
				}
			}
		}
		this.finishProfiling(world, x, z);
		return false;
	}

	private void startProfiling(World world, int x, int z) {
		if (WorldgenProfiler.profilingEnabled())
			WorldgenProfiler.startGenerator(world, this.getWorldgenProfilerID(), x >> 4, z >> 4);
	}

	private void finishProfiling(World world, int x, int z) {
		if (WorldgenProfiler.profilingEnabled())
			WorldgenProfiler.onRunGenerator(world, this.getWorldgenProfilerID(), x >> 4, z >> 4);
	}

	public final String getWorldgenProfilerID() {
		return "Condensed Ore "+ID;
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

	@Override
	public int compareTo(OreEntry o) {
		return Integer.compare(sortOrder, o.sortOrder);
	}

	public int getRenderColor() {
		OreType ore = this.getOreEntry();
		return ore != null ? ore.getDisplayColor() : 0xffD47EFF;
	}

	private OreType getOreEntry() {
		OreType ore = null;
		for (BlockKey bk : oreBlocks.getValues()) {
			ore = ReikaOreHelper.getFromVanillaOre(bk.blockID);
			if (ore != null)
				break;
			ore = ModOreList.getModOreFromOre(bk.blockID, bk.metadata);
			if (ore != null)
				break;
		}
		return ore;
	}

	public int getEnumIndex() {
		OreType ore = this.getOreEntry();
		return ore instanceof ReikaOreHelper ? ((ReikaOreHelper)ore).ordinal() : (ore instanceof ModOreList ? ((ModOreList)ore).ordinal()+100 : Integer.MAX_VALUE);
	}

	public void onVeinGenerated(World world, Vec3 center) {
		MinecraftForge.EVENT_BUS.post(new VeinGenerationEvent(world, this, center));
	}

}
