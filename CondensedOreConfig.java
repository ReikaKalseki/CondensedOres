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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import Reika.CondensedOres.API.CondensedOreAPI;
import Reika.CondensedOres.Control.BiomeRule;
import Reika.CondensedOres.Control.BiomeRule.BiomeDictionaryExclusion;
import Reika.CondensedOres.Control.BiomeRule.BiomeDictionaryRequirement;
import Reika.CondensedOres.Control.BiomeRule.BiomeExclusion;
import Reika.CondensedOres.Control.BiomeRule.BiomeRuleset;
import Reika.CondensedOres.Control.BiomeRule.BiomeWhitelist;
import Reika.CondensedOres.Control.DimensionRule;
import Reika.CondensedOres.Control.DimensionRule.DimensionExclusion;
import Reika.CondensedOres.Control.DimensionRule.DimensionRuleset;
import Reika.CondensedOres.Control.DimensionRule.DimensionWhitelist;
import Reika.CondensedOres.Control.FrequencyRule;
import Reika.CondensedOres.Control.HeightRule;
import Reika.CondensedOres.Control.OreEntry;
import Reika.CondensedOres.Control.ProximityRule;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Instantiable.IO.LuaBlock.LuaBlockDatabase;
import Reika.DragonAPI.Libraries.World.ReikaBiomeHelper;


public class CondensedOreConfig {

	public static final CondensedOreConfig instance = new CondensedOreConfig();

	private LuaBlockDatabase data;

	private final HashMap<String, OreEntry> entries = new HashMap();

	//Formatted, parsed, and used like Factorio prototypes with param1 = val1, param2 = {subparam2_1=subval2_1,subparam2_2=subval2_2} & inheritance
	private CondensedOreConfig() {
		data = new LuaBlockDatabase();
		OreLuaBlock base = new OreLuaBlock("base", null, data);
		base.putData("type", "base");
		base.putData("sprinkleMix", "false");
		base.putData("retrogen", "false");
		base.putData("veinSize", "10");
		//base.putData("generate", "true");
		OreLuaBlock height = new OreLuaBlock("heightRule", base, data);
		height.putData("minHeight", "0");
		height.putData("maxHeight", "64");
		height.putData("variation", "linear");
		OreLuaBlock freq = new OreLuaBlock("veinFrequency", base, data);
		freq.putData("veinsPerChunk", "8");
		freq.putData("chunkGenChance", "1");
		OreLuaBlock spawn = new OreLuaBlock("spawnBlock", base, data);
		OreLuaBlock sub = new OreLuaBlock("-", spawn, data);
		sub.putData("block", "minecraft:stone");
		OreLuaBlock dim = new OreLuaBlock("dimensionRules", base, data);
		dim.putData("combination", "or");
		OreLuaBlock biome = new OreLuaBlock("biomeRules", base, data);
		biome.putData("combination", "or");
		OreLuaBlock neighbor = new OreLuaBlock("proximityRules", base, data);
		biome.putData("strict", "false");
		OreLuaBlock ores = new OreLuaBlock("blocks", base, data);
		data.addBlock("base", base);
	}

	/** Returns the number of entries that ERRORED, not loaded! */
	public int loadConfigs() {
		int ret = 0;
		this.reset();
		CondensedOres.logger.log("Loading configs.");
		String sg = this.getSaveFolder();
		File f = new File(sg); //parent dir
		if (f.exists()) {
			this.loadFiles(f);
			ret += this.parseConfigs();

			CondensedOres.logger.log("Configs loaded.");
		}
		else {
			try {
				f.mkdirs();
			}
			catch (Exception e) {
				e.printStackTrace();
				CondensedOres.logger.logError("Could not create ore config folder!");
			}
		}
		try {
			this.createBaseFile(f);
		}
		catch (IOException e) {
			e.printStackTrace();
			CondensedOres.logger.logError("Could not create base data file!");
		}
		return ret;
	}

	private void createBaseFile(File f) throws IOException {
		File out = new File(f, "base.lua");
		if (out.exists())
			out.delete();
		out.createNewFile();
		ArrayList<String> li = data.getBlock("base").writeToStrings();
		ReikaFileReader.writeLinesToFile(out, li, true);
	}

	private void reset() {
		((OreAPIImplementation)CondensedOreAPI.instance).resetGenCache();
		LuaBlock base = data.getBlock("base");
		data = new LuaBlockDatabase();
		entries.clear();
		data.addBlock("base", base);
	}

	private void loadFiles(File parent) {
		ArrayList<File> files = ReikaFileReader.getAllFilesInFolder(parent, ".lua", ".ini", ".cfg", ".txt", ".yml");
		for (File f : files) {
			if (!f.getName().equals("base.lua"))
				data.loadFromFile(f);
		}
	}

	private int parseConfigs() {
		int ret = 0;
		int skip = 0;
		LuaBlock root = data.getRootBlock();
		for (LuaBlock b : root.getChildren()) {
			try {
				String type = b.getString("type");
				data.addBlock(type, b);
				OreEntry ore = this.parseEntry(type, b);
				ore.build();
				if (!ore.isEmpty()) {
					CondensedOres.logger.debug("Loaded ore prototype:\n"+ore);
					entries.put(type, ore);
				}
				else {
					CondensedOres.logger.log("Ore prototype '"+ore.displayName+"' not loaded; no ores found.");
					skip++;
				}
			}
			catch (Exception e) {
				CondensedOres.logger.logError("Could not parse config section "+b.getString("type")+": ");
				e.printStackTrace();
				ret++;
			}
		}
		CondensedOres.logger.log("All config entries parsed; "+skip+" skipped.");
		return ret;
	}

	private OreEntry parseEntry(String type, LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		String name = b.getString("name");
		int size = b.getInt("veinSize");
		boolean spr = b.getBoolean("sprinkleMix");
		boolean retro = b.getBoolean("retrogen");

		LuaBlock height = b.getChild("heightRule");
		HeightRule h = new HeightRule(height.getInt("minHeight"), height.getInt("maxHeight"), height.getString("variation"));

		LuaBlock freq = b.getChild("veinFrequency");
		FrequencyRule f = new FrequencyRule(name, freq.getDouble("veinsPerChunk"), freq.getDouble("chunkGenChance"));

		LuaBlock biome = b.getChild("biomeRules");
		BiomeRuleset br = new BiomeRuleset(biome.getString("combination"));
		for (LuaBlock sub : biome.getChildren()) {
			br.addRule(this.parseBiomeRule(sub));
		}

		LuaBlock dimension = b.getChild("dimensionRules");
		DimensionRuleset dim = new DimensionRuleset(dimension.getString("combination"));
		for (LuaBlock sub : dimension.getChildren()) {
			dim.addRule(this.parseDimensionRule(sub));
		}

		LuaBlock prox = b.getChild("proximityRules");
		ProximityRule p = new ProximityRule(prox.getBoolean("strict"));
		for (LuaBlock sub : prox.getChildren()) {
			Collection<BlockKey> keys = this.parseBlocks(sub);
			for (BlockKey bk : keys) {
				p.addBlock(bk);
			}
		}

		OreEntry ore = new OreEntry(type, name, size, spr, retro, h, f, dim, br, p);

		LuaBlock set = b.getChild("blockSet");
		if (set != null) {
			WeightedRandom<String> wr = set.asWeightedRandom();
			for (String s : wr.getValues()) {
				BlockKey bk = this.parseBlockKey(s);
				ore.addBlock(bk, wr.getWeight(s));
			}
		}
		else {
			LuaBlock blocks = b.getChild("blocks");
			for (String s : blocks.getDataValues()) {
				BlockKey bk = this.parseBlockKey(s);
				if (bk != null)
					ore.addBlock(bk, 10);
			}
		}

		LuaBlock spawn = b.getChild("spawnBlock");
		for (LuaBlock sub : spawn.getChildren()) {
			Collection<BlockKey> keys = this.parseBlocks(sub);
			for (BlockKey bk : keys) {
				ore.addBlockRule(bk);
			}
		}
		return ore;
	}

	private Collection<BlockKey> parseBlocks(LuaBlock b) {
		Collection<BlockKey> blocks = new HashSet();
		Block block = Block.getBlockFromName(b.getString("block"));
		if (block != null) {
			LuaBlock metas = b.getChild("metadata"); //do not use inherit, use direct call so will return null if unspecified
			Collection<Integer> c = new HashSet();
			if (metas != null) {
				for (String val : metas.getDataValues()) {
					int m = Integer.parseInt(val);
					c.add(m);
				}
			}
			else {
				c.add(-1);
			}
			for (int m : c) {
				blocks.add(new BlockKey(block, m));
			}
		}
		return blocks;
	}

	private BiomeRule parseBiomeRule(LuaBlock sub) {
		String type = sub.getString("type");
		switch(type) {
			case "exclude":
				try {
					if (!sub.containsKey("biomeID"))
						throw new NumberFormatException();
					int id = sub.getInt("biomeID");
					return new BiomeExclusion(BiomeGenBase.biomeList[id]);
				}
				catch (NumberFormatException e) {
					return new BiomeExclusion(ReikaBiomeHelper.getBiomeByName(sub.getString("biomeName")));
				}
			case "include":
				try {
					if (!sub.containsKey("biomeID"))
						throw new NumberFormatException();
					int id = sub.getInt("biomeID");
					return new BiomeWhitelist(BiomeGenBase.biomeList[id]);
				}
				catch (NumberFormatException e) {
					return new BiomeWhitelist(ReikaBiomeHelper.getBiomeByName(sub.getString("biomeName")));
				}
			case "dictionary-require":
				return new BiomeDictionaryRequirement(BiomeDictionary.Type.valueOf(sub.getString("name").toUpperCase()));
			case "dictionary-exclude":
				return new BiomeDictionaryExclusion(BiomeDictionary.Type.valueOf(sub.getString("name").toUpperCase()));
			default:
				throw new IllegalArgumentException("Invalid biome rule type '"+type+"'");
		}
	}

	private DimensionRule parseDimensionRule(LuaBlock sub) {
		String type = sub.getString("type");
		switch(type) {
			case "blacklist":
				return new DimensionExclusion(sub.getInt("dimensionID"));
			case "whitelist":
				return new DimensionWhitelist(sub.getInt("dimensionID"));
			default:
				throw new IllegalArgumentException("Invalid dimension rule type '"+type+"'");
		}
	}

	private BlockKey parseBlockKey(String s) {
		String[] parts = s.split(":");
		if (parts.length < 2)
			throw new IllegalArgumentException("Malformed Block Name/Namespace: "+s);
		int meta = 0;
		if (parts.length == 3)
			meta = Integer.parseInt(parts[2]);
		Block b = Block.getBlockFromName(parts[0]+":"+parts[1]);
		return b != null ? new BlockKey(b, meta) : null;
	}

	/*
	private ArrayList<String> addOrGetSection(ArrayList<ArrayList<String>> li, int idx) {
		while (idx >= li.size())
			li.add(new ArrayList());
		return li.get(idx);
	}
	/*
	private void pushAndLoadSection() {
		sections.add(section);
		section = new ArrayList();
	}
	 */

	private final String getSaveFolder() {
		return CondensedOres.config.getConfigFolder().getAbsolutePath()+"/CondensedOres_Files/";
	}

	public Collection<OreEntry> getOres() {
		return Collections.unmodifiableCollection(entries.values());
	}

	public OreEntry getOre(String name) {
		return entries.get(name);
	}

	private static class OreLuaBlock extends LuaBlock {

		protected OreLuaBlock(String n, LuaBlock lb, LuaBlockDatabase db) {
			super(n, lb, db);

			requiredElements.add("inherit");
			requiredElements.add("name");
			requiredElements.add("blocks");

			//Subprops
			requiredElements.add("block");
			requiredElements.add("biomeID");
			requiredElements.add("biomeName");
			requiredElements.add("dimensionID");
		}

	}

}
