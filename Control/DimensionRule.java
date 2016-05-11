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

import net.minecraft.world.World;


public abstract class DimensionRule {

	public DimensionRule() {

	}

	public abstract boolean isDimensionValid(int dim);


	public static class DimensionExclusion extends DimensionRule {

		private final int dimension;

		public DimensionExclusion(int dim) {
			dimension = dim;
		}

		@Override
		public boolean isDimensionValid(int dim) {
			return dim != dimension;
		}

		@Override
		public String toString() {
			return "DimID != "+dimension;
		}

	}

	public static class DimensionWhitelist extends DimensionRule {

		private final int dimension;

		public DimensionWhitelist(int dim) {
			dimension = dim;
		}

		@Override
		public boolean isDimensionValid(int dim) {
			return dim == dimension;
		}

		@Override
		public String toString() {
			return "DimID == "+dimension;
		}

	}

	public static class DimensionRuleset {

		private final ArrayList<DimensionRule> rules = new ArrayList();

		private boolean combineAND;

		public DimensionRuleset(String s) {
			this.setCombineMode(s);
		}

		public DimensionRuleset addRule(DimensionRule r) {
			rules.add(r);
			return this;
		}

		private DimensionRuleset setCombineMode(String s) {
			combineAND = s.equalsIgnoreCase("and");
			return this;
		}

		public boolean isValidDimension(World world) {
			if (rules.isEmpty())
				return true;
			int dim = world.provider.dimensionId;
			for (DimensionRule r : rules) {
				if (r.isDimensionValid(dim)) {
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
			for (DimensionRule r : rules) {
				sb.append("[");
				sb.append(r);
				sb.append("]");
				sb.append(join);
			}
			return sb.toString().substring(0, sb.length()-join.length());
		}

	}

}
