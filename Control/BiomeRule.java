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
import java.util.HashMap;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;


public abstract class BiomeRule {

	private static final HashMap<String, Class<? extends BiomeRule>> typeMap = new HashMap();

	private final String identifier;

	public BiomeRule(String s) {
		identifier = s;
		/*
		if (typeMap.containsKey(s) && typeMap.get(s) != this.getClass())
			throw new IllegalStateException("Conflicting Biome Rule IDs: "+s);
		typeMap.put(s, this.getClass());
		 */
	}

	public abstract boolean isBiomeValid(BiomeGenBase b);
	/*
	static { //init class map
		new BiomeExclusion(null);
		new BiomeWhitelist(null);
		new BiomeDictionaryRequirement(null);
		new BiomeDictionaryExclusion(null);
	}
	 */
	/*
	public static BiomeRule construct(LuaBlock b) throws Exception {
		String ty = b.getString("type");
		Class c = typeMap.get(ty);
		if (c == null)
			throw new IllegalArgumentException("Invalid biome rule type: '"+ty+"'");
		Constructor[] cst = c.getDeclaredConstructors();
		for (int i = 0; i < cst.length; i++) {
			Constructor cs = cst[i];
			if (cs.getParameterTypes().length > 0) {
				BiomeRule br = cs.newInstance(initargs);
			}
		}
	}
	 */
	public static class BiomeExclusion extends BiomeRule {

		private final BiomeGenBase biome;

		public BiomeExclusion(BiomeGenBase b) {
			super("exclude");
			biome = b;
		}

		@Override
		public boolean isBiomeValid(BiomeGenBase b) {
			return b != biome;
		}

		@Override
		public String toString() {
			return "Biome != ["+biome.biomeID+"] "+biome.biomeName;
		}

	}

	public static class BiomeWhitelist extends BiomeRule {

		private final BiomeGenBase biome;

		public BiomeWhitelist(BiomeGenBase b) {
			super("include");
			biome = b;
		}

		@Override
		public boolean isBiomeValid(BiomeGenBase b) {
			return b == biome;
		}

		@Override
		public String toString() {
			return "Biome == ["+biome.biomeID+"] "+biome.biomeName;
		}

	}

	public static class BiomeDictionaryRequirement extends BiomeRule {

		private final BiomeDictionary.Type type;

		public BiomeDictionaryRequirement(BiomeDictionary.Type type) {
			super("dictionary-require");
			this.type = type;
		}

		@Override
		public boolean isBiomeValid(BiomeGenBase b) {
			return BiomeDictionary.isBiomeOfType(b, type);
		}

		@Override
		public String toString() {
			return "Dictionary == "+type.name();
		}

	}

	public static class BiomeDictionaryExclusion extends BiomeRule {

		private final BiomeDictionary.Type type;

		public BiomeDictionaryExclusion(BiomeDictionary.Type type) {
			super("dictionary-exclude");
			this.type = type;
		}

		@Override
		public boolean isBiomeValid(BiomeGenBase b) {
			return !BiomeDictionary.isBiomeOfType(b, type);
		}

		@Override
		public String toString() {
			return "Dictionary != "+type.name();
		}

	}

	public static class BiomeRuleset {

		private final ArrayList<BiomeRule> rules = new ArrayList();

		private boolean combineAND;

		public BiomeRuleset(String s) {
			this.setCombineMode(s);
		}

		public BiomeRuleset addRule(BiomeRule r) {
			rules.add(r);
			return this;
		}

		private BiomeRuleset setCombineMode(String s) {
			combineAND = s.equalsIgnoreCase("and");
			return this;
		}

		public boolean isValidBiome(World world, int chunkX, int chunkZ) {
			if (rules.isEmpty())
				return true;
			BiomeGenBase b = world.getBiomeGenForCoords(chunkX, chunkZ);
			for (BiomeRule r : rules) {
				if (r.isBiomeValid(b)) {
					if (!combineAND)
						return true;
				}
				else {
					if (combineAND)
						return false;
				}
			}
			return combineAND;
		}

		@Override
		public String toString() {
			if (rules.isEmpty())
				return "[ANY]";
			String join = combineAND ? " AND " : " OR ";
			StringBuilder sb = new StringBuilder();
			for (BiomeRule r : rules) {
				sb.append("[");
				sb.append(r);
				sb.append("]");
				sb.append(join);
			}
			return sb.toString().substring(0, sb.length()-join.length());
		}

	}

}
