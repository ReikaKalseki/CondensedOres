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

import Reika.DragonAPI.Interfaces.Configuration.DecimalConfig;


public enum CondensedOreOptions implements DecimalConfig {

	FREQUENCY("Global Frequency Multiplier", 1F),
	SIZE("Global Size Multiplier", 1F);

	private String label;
	private boolean defaultState;
	private int defaultValue;
	private float defaultFloat;
	private String defaultString;
	private Class type;
	private boolean enforcing = false;

	public static final CondensedOreOptions[] optionList = values();

	private CondensedOreOptions(String l, boolean d) {
		label = l;
		defaultState = d;
		type = boolean.class;
	}

	private CondensedOreOptions(String l, boolean d, boolean tag) {
		this(l, d);
		enforcing = true;
	}

	private CondensedOreOptions(String l, int d) {
		label = l;
		defaultValue = d;
		type = int.class;
	}

	private CondensedOreOptions(String l, float d) {
		label = l;
		defaultFloat = d;
		type = float.class;
	}

	private CondensedOreOptions(String l, String d) {
		label = l;
		defaultString = d;
		type = String.class;
	}

	public boolean isBoolean() {
		return type == boolean.class;
	}

	public boolean isNumeric() {
		return type == int.class;
	}

	public boolean isDecimal() {
		return type == float.class;
	}

	public boolean isString() {
		return type == String.class;
	}

	public Class getPropertyType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public boolean getState() {
		return (Boolean)CondensedOres.config.getControl(this.ordinal());
	}

	public int getValue() {
		return (Integer)CondensedOres.config.getControl(this.ordinal());
	}

	public float getFloat() {
		return (Float)CondensedOres.config.getControl(this.ordinal());
	}

	public String getString() {
		return (String)CondensedOres.config.getControl(this.ordinal());
	}

	public String getDefaultString() {
		return defaultString;
	}

	public boolean isDummiedOut() {
		return type == null;
	}

	public boolean getDefaultState() {
		return defaultState;
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public float getDefaultFloat() {
		return defaultFloat;
	}

	@Override
	public boolean isEnforcingDefaults() {
		return enforcing;
	}

	@Override
	public boolean shouldLoad() {
		return true;
	}

}
