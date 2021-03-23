package Reika.CondensedOres.API;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.CondensedOres.Control.OreEntry;

import cpw.mods.fml.common.eventhandler.Event;


public class VeinGenerationEvent extends Event {

	public final World world;

	public final OreEntryBase oreType;

	public final double veinCenterX;
	public final double veinCenterY;
	public final double veinCenterZ;

	public VeinGenerationEvent(World world, OreEntry e, Vec3 ctr) {
		oreType = e;

		this.world = world;

		veinCenterX = ctr.xCoord;
		veinCenterY = ctr.yCoord;
		veinCenterZ = ctr.zCoord;
	}

}
