/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CondensedOres;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.RetroGenController;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.IO.ReikaFileReader;
import Reika.DragonAPI.Instantiable.IO.ControlledConfig;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;


@Mod( modid = "CondensedOres", name="CondensedOres", acceptableRemoteVersions="*", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")
public class CondensedOres extends DragonAPIMod {

	@Instance("CondensedOres")
	public static CondensedOres instance = new CondensedOres();

	public static ModLogger logger;

	public static final ControlledConfig config = new ControlledConfig(instance, CondensedOreOptions.optionList, null);

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.startTiming(LoadPhase.PRELOAD);
		this.verifyInstallation();

		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);

		logger = new ModLogger(instance, false);
		if (DragonOptions.FILELOG.getState())
			logger.setOutput("**_Loading_Log.log");

		//CondensedOreConfig.instance.loadConfigs(); //for initial testing

		if (!this.isSource())
			this.makeExampleFile(config.getConfigFolder());

		this.basicSetup(evt);
		this.finishTiming();
	}

	private void makeExampleFile(File folder) {
		try {
			File dest = new File(folder, "CondensedOres_example.lua");
			InputStream in = ReikaFileReader.getFileInsideJar(this.getModFile(), "Reika/CondensedOres/example.lua");
			File src = ReikaFileReader.createFileFromStream(in);
			FileUtils.copyFile(src, dest);
		}
		catch (Exception e) {
			logger.logError("Could not make example file.");
			e.printStackTrace();
		}
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);
		RetroGenController.instance.addHybridGenerator(CondensedOreGenerator.instance, Integer.MAX_VALUE, false);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);
		CondensedOreConfig.instance.loadConfigs();
		this.finishTiming();
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent evt) {

	}

	@EventHandler
	public void start(FMLServerStartingEvent evt) {

	}

	@EventHandler
	public void registerCommands(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new ReloadOreConfigCommand());
	}

	@Override
	public String getDisplayName() {
		return "Condensed Ores";
	}

	@Override
	public String getModAuthorName() {
		return "Reika";
	}

	@Override
	public URL getDocumentationSite() {
		return DragonAPICore.getReikaForumPage();
	}

	@Override
	public String getWiki() {
		return null;
	}

	@Override
	public String getUpdateCheckURL() {
		return CommandableUpdateChecker.reikaURL;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}

	@Override
	public File getConfigFolder() {
		return config.getConfigFolder();
	}

}
