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

import java.util.Random;

import Reika.CondensedOres.CondensedOreOptions;


public class FrequencyRule {

	private final double chunkGenChance;
	private final double veinsPerChunk;

	public FrequencyRule(double v, double c) {
		if (v <= 0)
			throw new IllegalArgumentException("Invalid vein frequency setting: # of Veins per generated chunk must be more than zero!");
		if (c <= 0)
			throw new IllegalArgumentException("Invalid vein frequency setting: chunk generation chance must be more than zero!");
		float f = CondensedOreOptions.FREQUENCY.getFloat();
		if (f != 1) {
			if (f > 1 || v >= 1F/f) {
				v *= f;
			}
			else {
				f *= v;
				v = 1;
				c = f;
			}
		}
		chunkGenChance = c;
		veinsPerChunk = v;
	}

	public boolean generate(int chunkX, int chunkZ, Random rand) {
		return rand.nextDouble() < chunkGenChance;
	}

	public int getVeinCount(int chunkX, int chunkZ, Random rand) {
		int base = (int)veinsPerChunk;
		double c = veinsPerChunk-base;
		if (rand.nextDouble() < c)
			base++;
		return base;
	}

	@Override
	public String toString() {
		return String.format("%.3fx @ %.3f%%", veinsPerChunk, chunkGenChance);
	}

}
