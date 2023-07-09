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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.CondensedOres.CondensedOreVein;
import Reika.DragonAPI.Exception.UnreachableCodeException;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Effects.LightningBolt;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Instantiable.Math.Noise.Simplex3DGenerator;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;

public class ShapeRule {

	public final VeinShape shape;
	public final OreRuleGenerator generator;

	public ShapeRule() throws Exception {
		this(null);
	}

	public ShapeRule(LuaBlock lb) throws Exception {
		VeinShape s = VeinShape.VANILLA;
		if (lb != null)
			s = VeinShape.valueOf(lb.getString("shape").toUpperCase(Locale.ENGLISH));
		shape = s;
		generator = s.createGenerator();
		generator.setData(lb);
	}

	@Override
	public String toString() {
		return shape.name()+" "+generator.toString();
	}

	public static enum VeinShape {
		VANILLA,
		TENDRIL,
		STAR,
		NOISE,
		;

		private OreRuleGenerator createGenerator() {
			switch(this) {
				case VANILLA:
					return new VanillaOreGenerator();
				case TENDRIL:
					return new TendrilOreGenerator();
				case STAR:
					return new StarOreGenerator();
				case NOISE:
					return new SimplexWebGenerator();
				default:
					throw new UnreachableCodeException(this);

			}
		}

		public boolean allowRandomOffset() {
			switch(this) {
				case NOISE:
					return false;
				default:
					return true;
			}
		}
	}

	public static abstract class OreRuleGenerator {

		public abstract boolean generateAt(World world, Random rand, int x, int y, int z, CondensedOreVein vein, int veinSize);

		protected abstract void setData(LuaBlock lb) throws Exception;

		@Override
		public abstract String toString();

	}

	private static class VanillaOreGenerator extends OreRuleGenerator {

		@Override
		public boolean generateAt(World world, Random rand, int x, int y, int z, CondensedOreVein vein, int veinSize) {
			float f = rand.nextFloat() * (float)Math.PI;
			double xvar_pos = x + 8 + MathHelper.sin(f) * veinSize / 8F;
			double xvar_neg = x + 8 - MathHelper.sin(f) * veinSize / 8F;
			double zvar_pos = z + 8 + MathHelper.cos(f) * veinSize / 8F;
			double zvar_neg = z + 8 - MathHelper.cos(f) * veinSize / 8F;
			double ypos_1 = y + rand.nextInt(3) - 2;
			double ypos_2 = y + rand.nextInt(3) - 2;

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
										vein.tryPlaceBlock(world, dx, dy, dz, rand);
									}
								}
							}
						}
					}
				}
			}

			return true;
		}

		@Override
		protected void setData(LuaBlock lb) throws Exception {

		}

		@Override
		public String toString() {
			return "Vanilla";
		}
	}

	private static class TendrilOreGenerator extends OreRuleGenerator {

		@Override
		public boolean generateAt(World world, Random rand, int x, int y, int z, CondensedOreVein vein, int veinSize) {
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
			for (Coordinate c : cells) {
				vein.tryPlaceBlock(world, c.xCoord, c.yCoord, c.zCoord, rand);
			}
			return true;
		}

		@Override
		protected void setData(LuaBlock lb) throws Exception {

		}

		@Override
		public String toString() {
			return "Tendril";
		}
	}

	private static class StarOreGenerator extends OreRuleGenerator {

		private BlockKey centerSpecial;

		@Override
		public boolean generateAt(World world, Random rand, int x, int y, int z, CondensedOreVein vein, int veinSize) {
			float ang1 = rand.nextFloat()*360;
			float ang2 = (float)ReikaRandomHelper.getRandomPlusMinus(ang1+180D, 90, rand);
			double len = veinSize/1.8D;
			ArrayList<DecimalPosition> ends = new ArrayList();
			int endCount = ReikaRandomHelper.getRandomBetween(4, 9, rand);
			for (int i = 0; i < endCount; i++) {
				double[] angs = ReikaPhysicsHelper.polarToCartesianFast(1, rand.nextDouble()*360, rand.nextDouble()*360);
				ends.add(new DecimalPosition(angs[0], angs[1], angs[2]));
			}
			HashMap<Coordinate, Boolean> cells = new HashMap();
			double maxR = Math.max(0.67, veinSize/27D);
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
								double dist = i*i+j*j+k*k;
								if (dist <= tr) {
									Coordinate c = new Coordinate(dx+i, dy+j, dz+k);
									boolean canCenter = tr > 1 && dist <= Math.max(tr*0.5, tr-1.5) && d < 3.5;
									Boolean get = cells.get(c);
									if (get != null)
										canCenter |= get.booleanValue();
									//canCenter = c.getTaxicabDistanceTo(x, y, z) <= 3;
									cells.put(c, canCenter);
								}
							}
						}
					}
				}
			}
			for (Entry<Coordinate, Boolean> e : cells.entrySet()) {
				Coordinate c = e.getKey();
				if (centerSpecial != null && e.getValue())
					vein.tryPlaceBlock(world, c.xCoord, c.yCoord, c.zCoord, rand, centerSpecial);
				else
					vein.tryPlaceBlock(world, c.xCoord, c.yCoord, c.zCoord, rand);
			}
			return true;
		}

		@Override
		protected void setData(LuaBlock lb) throws Exception {
			if (lb.containsKey("specialBlock")) {
				centerSpecial = BlockKey.fromItem(CustomRecipeList.parseItemString(lb.getString("specialBlock"), null, false));
			}
		}

		@Override
		public String toString() {
			return "Star with center "+centerSpecial;
		}
	}

	private static class SimplexWebGenerator extends OreRuleGenerator {

		private double threshold;
		private long seedOffset;

		private Simplex3DGenerator gen;

		@Override
		public boolean generateAt(World world, Random rand, int x, int y, int z, CondensedOreVein vein, int veinSize) {
			this.setSeed(world, rand);
			int chunkX = x >> 4;
									int chunkZ = z >> 4;
							x = chunkX << 4;
							z = chunkZ << 4;
							for (int dy = vein.ore.height.minY; dy <= vein.ore.height.maxY; dy++) {
								for (int i = 0; i < 16; i++) {
									for (int k = 0; k < 16; k++) {
										int dx = x+i;
										int dz = z+k;
										double val = gen.getValue(dx, dy, dz);
										if (Math.abs(val) <= threshold*0.05)
											vein.tryPlaceBlock(world, dx, dy, dz, rand);
									}
								}
							}
							return true;
		}

		private void setSeed(World world, Random rand) {
			if (gen == null || gen.seed != world.getSeed()) {
				gen = (Simplex3DGenerator)new Simplex3DGenerator(world.getSeed()^seedOffset).setFrequency(0.1);
			}
		}

		@Override
		protected void setData(LuaBlock lb) throws Exception {
			threshold = lb.getDouble("threshold");
			seedOffset = lb.containsKey("seedOffset") ? lb.getLong("seedOffset") : 0;
		}

		@Override
		public String toString() {
			return "Noise with threshold "+threshold;
		}
	}

}
