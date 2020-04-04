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

import java.util.HashMap;
import java.util.Random;

import net.minecraft.util.MathHelper;


public class HeightRule {

	public final int minY;
	public final int maxY;
	public final Distribution distribution;
	private final int range;
	private final int midpoint;

	public HeightRule(int y1, int y2, String type) {
		minY = y1;
		maxY = y2;
		if (minY > maxY)
			throw new IllegalArgumentException("Invalid distribution setting: maxY must be greater or equal to minY!");
		range = y2-y1;
		midpoint = y1+range/2;
		distribution = Distribution.map.get(type);
		if (distribution == null)
			throw new IllegalArgumentException("Invalid distribution setting '"+type+"'");
	}

	public final int getRandomizedY(Random rand) {
		return distribution.getRandomizedY(minY, maxY, midpoint, range, rand);
	}

	@Override
	public String toString() {
		return distribution.name()+" ["+minY+"-"+maxY+"]";
	}

	public static enum Distribution {
		LINEAR("linear"),
		NORMAL("normal"),
		PYRAMID("pyramid");

		private final String name;

		private static final HashMap<String, Distribution> map = new HashMap();

		private Distribution(String s) {
			name = s;
		}

		private final int getRandomizedY(int y1, int y2, int mid, int range, Random rand) {
			switch(this) {
				case LINEAR:
					return y1+rand.nextInt(1+y2-y1);
				case NORMAL:
					return MathHelper.clamp_int((int)Math.round(mid+(range/2D)*rand.nextGaussian()), y1, y2);
				case PYRAMID:
					double F = range/(double)(mid-y1);
					double val = rand.nextDouble();
					if (val < F) {
						return (int)(y1+Math.sqrt(val*(mid-y1)*range));
					}
					else {
						return (int)(mid-Math.sqrt((1-val)*(mid-y1)*(mid-y2)));
					}
			}
			return -1;
		}

		public double getNormalizedChanceAt(int y1, int y2, int y) {
			switch(this) {
				case LINEAR:
					return 1;
				case NORMAL:
					return -1; //I have no f$%^ing idea
				case PYRAMID:
					double ctr = (y1+y2)/2D;
					double range = y2-y1;
					double diff = Math.abs(y-ctr);
					double f = diff*2/range;
					return 1-f;
			}
			return 0;
		}

		static {
			for (int i = 0; i < values().length; i++) {
				map.put(values()[i].name, values()[i]);
			}
		}
	}

}
