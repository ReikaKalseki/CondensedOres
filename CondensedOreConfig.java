/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
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
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Libraries.World.ReikaBiomeHelper;


public class CondensedOreConfig {

	public static final CondensedOreConfig instance = new CondensedOreConfig();

	private final ArrayList<OreEntry> entries = new ArrayList();
	private final HashMap<String, LuaBlock> rawData = new HashMap();

	private LuaBlock block = new LuaBlock("top", null);

	//Formatted, parsed, and used like Factorio prototypes with param1 = val1, param2 = {subparam2_1=subval2_1,subparam2_2=subval2_2} & inheritance
	private CondensedOreConfig() {
		LuaBlock base = new LuaBlock("base", null);
		base.data.put("type", "base");
		base.data.put("sprinkleMix", "false");
		base.data.put("veinSize", "10");
		//base.data.put("generate", "true");
		LuaBlock height = new LuaBlock("heightRule", base);
		height.data.put("minHeight", "0");
		height.data.put("maxHeight", "64");
		height.data.put("variation", "linear");
		LuaBlock freq = new LuaBlock("veinFrequency", base);
		freq.data.put("veinsPerChunk", "8");
		freq.data.put("chunkGenChance", "1");
		LuaBlock spawn = new LuaBlock("spawnBlock", base);
		LuaBlock sub = new LuaBlock("-", spawn);
		sub.data.put("block", "minecraft:stone");
		LuaBlock dim = new LuaBlock("dimensionRules", base);
		dim.data.put("combination", "or");
		LuaBlock biome = new LuaBlock("biomeRules", base);
		biome.data.put("combination", "or");
		LuaBlock neighbor = new LuaBlock("proximityRules", base);
		biome.data.put("strict", "false");
		LuaBlock ores = new LuaBlock("blocks", base);
		rawData.put("base", base);
	}

	public void loadConfigs() {
		this.reset();
		CondensedOres.logger.log("Loading configs.");
		String sg = this.getSaveFolder();
		File f = new File(sg); //parent dir
		if (f.exists()) {
			this.loadFiles(f);
			this.parseConfigs();

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
	}

	private void reset() {
		block = new LuaBlock("top", null);
		entries.clear();
		LuaBlock base = rawData.get("base");
		rawData.clear();
		rawData.put("base", base);
	}

	private void loadFiles(File parent) {
		ArrayList<File> files = ReikaFileReader.getAllFilesInFolder(parent, ".lua", ".ini", ".cfg", ".txt", ".yml");
		for (File f : files) {
			this.loadFile(f);
		}
	}

	private void loadFile(File f) {
		ArrayList<String> li = ReikaFileReader.getFileAsLines(f, false);
		ArrayList<ArrayList<String>> data = new ArrayList();
		for (String s : li) {
			s = this.cleanString(s);
			if (s.contains("{")) {
				if (s.endsWith(" = {"))
					s = s.substring(0, s.length()-4);
				block = new LuaBlock(s, block);
			}
			else if (s.contains("}")) {
				block = block.parent;
			}

			if (!s.equals("{") && !s.equals("}") && !s.equals(block.name)) {
				s = s.replaceAll("\"", "");
				String[] parts = s.split("=");
				if (parts.length == 2)
					block.data.put(parts[0].substring(0, parts[0].length()-1), parts[1].substring(1));
				else
					block.data.put(String.valueOf(block.data.size()), s);
			}
		}
	}

	private String cleanString(String s) {
		s = s.replaceAll("\t", "");
		if (s.contains("--")) {
			s = s.substring(0, s.indexOf("--"));
		}
		if (s.contains("//")) {
			s = s.substring(0, s.indexOf("//"));
		}
		if (s.length() > 0) {
			while (s.charAt(s.length()-1) == ' ')
				s = s.substring(0, s.length()-1);
		}
		return s;
	}

	private void parseConfigs() {
		block = block.getTopParent();
		for (LuaBlock b : block.children.values()) {
			try {
				rawData.put(b.getString("type"), b);
				OreEntry ore = this.parseEntry(b);
				ore.build();
				if (!ore.isEmpty()) {
					CondensedOres.logger.debug("Loaded ore prototype:\n"+ore);
					entries.add(ore);
				}
				else {
					CondensedOres.logger.log("Ore prototype '"+ore.displayName+"' not loaded; no ores found.");
				}
			}
			catch (Exception e) {
				CondensedOres.logger.logError("Could not parse config section "+b.data.get("type")+": ");
				e.printStackTrace();
			}
		}
		CondensedOres.logger.log("All config entries parsed.");
	}

	private OreEntry parseEntry(LuaBlock b) throws NumberFormatException, IllegalArgumentException, IllegalStateException {
		String name = b.getString("name");
		int size = b.getInt("veinSize");
		boolean spr = b.getBoolean("sprinkleMix");

		LuaBlock height = b.getChild("heightRule");
		HeightRule h = new HeightRule(height.getInt("minHeight"), height.getInt("maxHeight"), height.getString("variation"));

		LuaBlock freq = b.getChild("veinFrequency");
		FrequencyRule f = new FrequencyRule(freq.getDouble("veinsPerChunk"), freq.getDouble("chunkGenChance"));

		LuaBlock biome = b.getChild("biomeRules");
		BiomeRuleset br = new BiomeRuleset(biome.getString("combination"));
		for (LuaBlock sub : biome.children.values()) {
			br.addRule(this.parseBiomeRule(sub));
		}

		LuaBlock dimension = b.getChild("dimensionRules");
		DimensionRuleset dim = new DimensionRuleset(dimension.getString("combination"));
		for (LuaBlock sub : dimension.children.values()) {
			dim.addRule(this.parseDimensionRule(sub));
		}

		LuaBlock prox = b.getChild("proximityRules");
		ProximityRule p = new ProximityRule(prox.getBoolean("strict"));
		for (LuaBlock sub : prox.children.values()) {
			Collection<BlockKey> keys = this.parseBlocks(sub);
			for (BlockKey bk : keys) {
				p.addBlock(bk);
			}
		}

		OreEntry ore = new OreEntry(name, size, spr, h, f, dim, br, p);

		LuaBlock blocks = b.getChild("blocks");
		for (String s : blocks.data.values()) {
			BlockKey bk = this.parseBlockKey(s);
			if (bk != null)
				ore.addBlock(bk);
		}

		LuaBlock spawn = b.getChild("spawnBlock");
		for (LuaBlock sub : spawn.children.values()) {
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
			LuaBlock metas = b.children.get("metadata"); //do not use inherit, use direct call so will return null if unspecified
			Collection<Integer> c = new HashSet();
			if (metas != null) {
				for (String val : metas.data.values()) {
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
					if (!sub.data.containsKey("biomeID"))
						throw new NumberFormatException();
					int id = sub.getInt("biomeID");
					return new BiomeExclusion(BiomeGenBase.biomeList[id]);
				}
				catch (NumberFormatException e) {
					return new BiomeExclusion(ReikaBiomeHelper.getBiomeByName(sub.getString("biomeName")));
				}
			case "include":
				try {
					if (!sub.data.containsKey("biomeID"))
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
		return Collections.unmodifiableCollection(entries);
	}

	private static final class LuaBlock {

		private final String name;
		private final LuaBlock parent;
		private final HashMap<String, LuaBlock> children = new HashMap();
		private final HashMap<String, String> data = new HashMap();

		private static final HashSet<String> requiredElements = new HashSet();

		static {
			requiredElements.add("type");
			requiredElements.add("inherit");
			requiredElements.add("name");
			requiredElements.add("blocks");

			//Subprops
			requiredElements.add("block");
			requiredElements.add("biomeID");
			requiredElements.add("biomeName");
		}

		private LuaBlock(String n, LuaBlock lb) {
			if (n.equals("{"))
				n = Integer.toHexString(System.identityHashCode(this));
			name = n;
			parent = lb;
			if (parent != null)
				parent.children.put(name, this);
		}

		private LuaBlock getTopParent() {
			LuaBlock lb = this;
			while (lb.parent != null) {
				lb = lb.parent;
			}
			return lb;
		}

		public double getDouble(String key) {
			return Double.parseDouble(this.getString(key));
		}

		public boolean getBoolean(String key) {
			return Boolean.parseBoolean(this.getString(key));
		}

		public int getInt(String key) {
			return Integer.parseInt(this.getString(key));
		}

		public String getString(String key) {
			if (data.containsKey(key))
				return data.get(key);
			if (!this.canInherit(key))
				throw new IllegalArgumentException("Missing key '"+key+"' for '"+name+"'");
			return this.inherit(key);
		}

		private String inherit(String key) {
			LuaBlock b = this;
			Collection<String> steps = new ArrayList();
			LuaBlock orig = b;
			while (!b.data.containsKey("inherit") && b.parent != null) {
				steps.add(b.name);
				b = b.parent;
			}
			String inherit = b.data.get("inherit");
			if (inherit == null)
				return "[NULL KEY INHERIT]";
			LuaBlock lb = instance.rawData.get(inherit);
			if (lb == null)
				return "[NULL INHERIT]";
			for (String s : steps) {
				if (lb.children.containsKey(s))
					lb = lb.children.get(s);
				else
					throw new IllegalStateException("'"+orig.parent.name+"/"+orig.name+"' tried to inherit property '"+key+"', but could not.");
			}
			return lb.data.containsKey(key) ? lb.getString(key) : "[NULL DATA]";
		}

		private boolean canInherit(String key) {
			return /*!requiredElements.contains(name) && */!requiredElements.contains(key);
		}

		private LuaBlock getChild(String key) {
			return children.containsKey(key) ? children.get(key) : this.inheritChild(key);
		}

		private LuaBlock inheritChild(String key) {
			LuaBlock b = this;
			Collection<String> steps = new ArrayList();
			while (!b.data.containsKey("inherit") && b.parent != null) {
				steps.add(b.name);
				b = b.parent;
			}
			String inherit = b.data.get("inherit");
			if (inherit == null)
				return null;
			LuaBlock lb = instance.rawData.get(inherit);
			if (lb == null)
				return null;
			for (String s : steps) {
				lb = lb.children.get(s);
			}
			return lb.children.containsKey(key) ? lb.children.get(key) : null;
		}

		@Override
		public String toString() {
			return this.toString(0);
		}

		private String toString(int indent) {
			StringBuilder sb = new StringBuilder();

			sb.append("\n");
			sb.append(this.getIndent("----", indent)+"-------------"+name+"-------------\n");
			sb.append(this.getIndent("====", indent)+"=============DATA=============\n");
			for (String s : data.keySet()) {
				String val = data.get(s);
				sb.append(this.getIndent("\t", indent)+s+"="+val);
				sb.append("\n");
			}
			if (!children.isEmpty()) {
				sb.append("\n");
				sb.append(this.getIndent("====", indent)+"=============CHILDREN=============\n");
				for (LuaBlock lb : children.values()) {
					sb.append(lb.toString(indent+1));
				}
			}
			sb.append(this.getIndent("----", indent)+"---------------------------------------\n");
			sb.append("\n");
			sb.append("\n");

			return sb.toString();
		}

		private String getIndent(String rpt, int idt) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < idt; i++) {
				sb.append(rpt);
			}
			return sb.toString();
		}

	}

}
