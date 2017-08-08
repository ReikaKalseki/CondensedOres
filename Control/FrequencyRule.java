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

import java.util.Random;

import Reika.CondensedOres.CondensedOreOptions;
import Reika.CondensedOres.CondensedOres;


public class FrequencyRule {

	private final String oreName;
	private final double chunkGenChance;
	private final double veinsPerChunk;

	public FrequencyRule(String n, double v, double c) {
		oreName = n;
		if (v < 1)
			throw new IllegalArgumentException("Invalid vein frequency setting for '"+n+"': # of Veins per generated chunk must be at least one!");
		if (c <= 0)
			throw new IllegalArgumentException("Invalid vein frequency setting for '"+n+"': chunk generation chance must be more than zero!");
		if (c < 1 && v > 1)
			CondensedOres.logger.log("Warning: Frequency Settings for '"+n+"' have <100% chance of spawning a vein yet a count of more than one vein per chunk. Though this works, this is usually an error.");
		float f = CondensedOreOptions.FREQUENCY.getFloat();
		if (f != 1) {
			double vold = v;
			double cold = c;
			if (f > 1) {
				if (c >= 1) {
					v *= f;
				}
				else {
					if (c*f <= 1) {
						c *= f;
					}
					else {
						f *= c;
						c = 1;
						v = f;
					}
				}
			}
			else if (v*f >= 1) {
				v *= f;
			}
			else if (c <= 1 && v == 1) {
				c *= f;
			}
			else {
				f *= v;
				v = 1;
				c = f;
			}
			CondensedOres.logger.debug(String.format("Modified frequency rule by factor %.3f: VeinsPerChunk: %.3f => %.3f; ChunkGenChance: %.3f => %.3f", f, vold, v, cold, c));
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
		return String.format("%.3fx @ %.3f%%", veinsPerChunk, chunkGenChance*100);
	}

}
