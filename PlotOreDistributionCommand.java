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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import Reika.CondensedOres.Control.OreEntry;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Command.DragonClientCommand;


public class PlotOreDistributionCommand extends DragonClientCommand {

	private static double maxGlobalValue;

	@Override
	public void processCommand(ICommandSender ics, String[] args) {
		maxGlobalValue = 0;
		ArrayList<OreLevel>[] levels = new ArrayList[256];

		ArrayList<OreEntry> ores = new ArrayList(CondensedOreConfig.instance.getOres());

		Iterator<OreEntry> it = ores.iterator();
		while (it.hasNext()) {
			OreEntry e = it.next();
			if (!e.isValidDimension(Minecraft.getMinecraft().theWorld))
				it.remove();
		}

		Collections.sort(ores, new Comparator<OreEntry>() {

			@Override
			public int compare(OreEntry o1, OreEntry o2) {
				return Integer.compare(o1.getEnumIndex(), o2.getEnumIndex());
			}

		});

		for (OreEntry e : ores) {
			for (int y = e.height.minY; y <= e.height.maxY; y++) {
				if (levels[y] == null)
					levels[y] = new ArrayList();
				levels[y].add(new OreLevel(e, y));
			}
		}

		int width = 2048;//512;
		int heightPerBar = 8;
		BufferedImage img = new BufferedImage(width, heightPerBar*256, BufferedImage.TYPE_INT_ARGB);

		CombinedLevel[] layers = new CombinedLevel[256];

		for (int i = 0; i < 256; i++) {
			int y0 = img.getHeight()-(i+1)*heightPerBar;
			for (int x = 0; x < width; x++) {
				for (int y = y0; y < y0+heightPerBar; y++) {
					int clr = (x/4+y/4)%2 == 0 ? 0xffcfcfcf : 0xffe0e0e0;
					img.setRGB(x, y, clr);
				}
			}
			if (levels[i] != null) {
				CombinedLevel layer = new CombinedLevel(i);
				layers[i] = layer;
				for (OreLevel lvl : levels[i]) {
					layer.addEntry(lvl);
				}
				OreEntry[] arr = layer.calculateStack(width);
				for (int x = 0; x < width; x++) {
					if (arr[x] == null) {
						continue;
					}
					int clr = 0xff000000 | arr[x].getRenderColor();
					for (int y = y0; y < y0+heightPerBar; y++) {
						if (y == y0 && i == 64 && (x/8)%2 == 0)
							clr = 0xff000000;
						else if (y == y0 && i < 4 && (x/8)%4 == 0)
							clr = 0xff404040;
						img.setRGB(x, y, clr);
					}
				}
			}
		}

		Graphics graphics = img.getGraphics();
		Font ft = graphics.getFont();
		graphics.setFont(new Font(ft.getName(), ft.getStyle(), (int)(ft.getSize()*1.2D)));
		graphics.setColor(new Color(0xff000000));
		int textSize = graphics.getFont().getSize();

		for (int i = 0; i < 256; i++) {
			int y0 = img.getHeight()-(i+1)*heightPerBar;
			if (levels[i] != null) {
				CombinedLevel layer = layers[i];
				if (i%16 == 4)
					layer.addLabels(graphics, width, y0+heightPerBar/2, textSize);
			}
		}

		graphics.dispose();

		try {
			File f = new File(DragonAPICore.getMinecraftDirectory(), "OrePlot/"+System.nanoTime()+".png");
			if (f.exists())
				f.delete();
			f.getParentFile().mkdirs();
			f.createNewFile();
			ImageIO.write(img, "png", f);
			this.sendChatToSender(ics, EnumChatFormatting.GREEN+"Plotted "+ores.size()+" ores.");
		}
		catch (Exception e) {
			this.sendChatToSender(ics, EnumChatFormatting.RED+"Could not write file: "+e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public String getCommandString() {
		return "plotoreconfig";
	}

	private static class CombinedLevel {

		private final int yLevel;

		private final TreeMap<Double, OreLevel> barStack = new TreeMap();
		private final HashMap<OreLevel, Double> minValues = new HashMap();
		private final HashMap<OreLevel, Double> maxValues = new HashMap();
		private double maxValue;

		private CombinedLevel(int y) {
			yLevel = y;
		}

		public void addLabels(Graphics graphics, int width, int y, int fontSize) {
			for (OreLevel l : barStack.values()) {
				double pos = this.getCenter(l)*width/maxGlobalValue;
				String s = l.ore.displayName;
				double w = graphics.getFontMetrics().stringWidth(s);
				graphics.drawString(s, (int)(pos-w/2D), y);
			}
		}

		private double getCenter(OreLevel ol) {
			return (minValues.get(ol)+maxValues.get(ol))/2D;
		}

		private void addEntry(OreLevel ol) {
			Entry<Double, OreLevel> last = barStack.lastEntry();
			double at = maxValue;
			barStack.put(at, ol);
			maxValue += ol.getValue();
			maxGlobalValue = Math.max(maxGlobalValue, maxValue);
			minValues.put(ol, at);
			maxValues.put(ol, maxValue);
		}

		private OreEntry[] calculateStack(int px) {
			OreEntry[] ret = new OreEntry[px];
			double scale = maxValue/maxGlobalValue;
			for (int p = 0; p < px; p++) {
				double d = p*maxValue/px/scale;
				if (d <= maxValue)
					ret[p] = barStack.floorEntry(d).getValue().ore;
			}
			return ret;
		}

	}

	private static class OreLevel {

		private final OreEntry ore;
		private final int yLevel;

		private OreLevel(OreEntry e, int y) {
			ore = e;
			yLevel = y;
		}

		public double getValue() {
			return ore.height.distribution.getNormalizedChanceAt(ore.height.minY, ore.height.maxY, yLevel)*ore.frequency.veinsPerChunk*ore.frequency.chunkGenChance;
		}

	}

}
