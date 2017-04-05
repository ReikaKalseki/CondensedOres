{
	type = "documentation" --An internal ID; must be unique
	inherit = "base" --The parent; anything not explicitly specified will be inherited, possibly recursively; consult base.lua to see the base values (base.lua is not loaded)
	name = "Name" --A display name
	sprinkleMix = true --If multiple ore blocks are specified, should it generate one per vein, or mix them all into every vein (sprinkle mix)?
	retrogen = false --Should this ore rerun generation on chunks as they are loaded?
	blocks = { --the list of all blocks that count as this ore type; one per line, formatted like Minetweaker would use them; any and all can generate. Note that they need not actually be ores; you can make it generate anything you like except TileEntities
		"ModID:Name:Metadata"
	}
	heightRule = { --The height spawning mechanics; vanilla is linear with min of 0 and max based on the ore type (see MC wiki)
		minHeight = 0 --The lower bound of the distribution; no ores of this type will spawn below this
		maxHeight = 32 --The upper bound of the distribution; no ores of this type will spawn above this
		variation = "linear" --Possible values: Linear (rand.nextInt(), uniform distribution); Normal (normal/bell curve distribution); Pyramid (triangular distribution, linear falloff from a max at mid-height)
	}
	veinFrequency = { --Spawning frequencies; typically, if veinsPerChunk is more than one, chunkGenChance is 1, but this is not required (you can have few chunks with ore, but have those be very ore-dense)
		veinsPerChunk = 12 --The number of times to try generating a vein per chunk; must be >= 1
		chunkGenChance = 1 --The chance of even trying to operate on a chunk; can be decimals, but must be <= 1
	} --The effective number of veins per chunk is the product of these two numbers
	veinSize = 8 --The vanilla veinSize property; this is not the number of ore blocks in a vein; that is a more complex nonlinear relationship. Vanilla coal has a size of 16, while iron has a size of 8. Any size is permitted, but note that sizes over 100 can mean veins the size of villages, yielding 5k+ ore.
	spawnBlock = { --The block types to spawn in
		{
			block = "minecraft:stone" --vanilla stone
		}
		{
			block = "artifice:rock"
			metadata = { --metadatas can be specified, in case of mods using the same block ID for multiple things
				0
				1
				2
				3
			}
		}
	}
	biomeRules = { --Any biome restrictions on the ore; if unspecified, it ignores biome
		combination = "and" --whether to apply these rules with a logical AND or a logical OR (all or any satisfied)
		{
			type = "include" --Require it have this biome type
			biomeID = 0 --With this biomeID
		}
		{
			type = "exclude" --Disallow this biome type
			biomeName = "Rainbow Forest" --Can use names instead of IDs for types
		}
		{
			type = "dictionary-require" --Require a specific Forge BiomeDictionary tag
			name = "forest" --see Forge for a list of all types, or consult this http://i.imgur.com/I7wbd2b.png
		}
		{
			type = "dictionary-exclude" --Require that a specific Forge BiomeDict tag not be present
			name = "nether"
		}
	}
	dimensionRules = { --Any dimension restrictions on the ore; if unspecified, it ignores dimension
		combination = "or" --same as biome rules; unless using only blacklists, this is rarely AND in practice since rules work such that adding a second either does nothing or stops all generation (cannot satisfy two whitelists simultaneously)
		{
			type = "whitelist" --Allow this dimensionID
			dimensionID = 0
		}
		{
			type = "blacklist" --Disallow this dimensionID
			dimensionID = -1
		}
	}
	proximityRules = { --A check for ores that need to generate near something else (like lava for sulfur)
		strict = false --If strict, every ore block in a vein is run through this check; otherwise, only the starting block is; can substantially reduce ore quantitiy and add some worldgen load, but makes every ore block obey this rule
		{
			block = "minecraft:water" --any block type as before
			metadata = { --metadata options also exist
				0
			}
		}
	}
}
{
	type = "copper"
	inherit = "base"
	name = "Copper Ore"
	blocks = {
		"Forestry:ore:2"
		"ElectriCraft:ore:0"
		"TConstruct:ore:5"
	}
	heightRule = {
		minHeight = 0
		maxHeight = 32
		variation = "linear" //example comment
	}
	veinFrequency = {
		veinsPerChunk = 12
		chunkGenChance = 1
	}
	veinSize = 8 --another example comment
	spawnBlock = {
		{
			block = "minecraft:stone"
		}
		{
			block = "artifice:rock"
			metadata = {
				0
				1
				3
				8
			}
		}
	}
	biomeRules = {
		combination = "and"
		{
			type = "exclude"
			biomeID = 7
		}
		{
			type = "exclude"
			biomeName = "Swampland"
		}
		{
			type = "dictionary-require"
			name = "sandy"
		}
		{
			type = "dictionary-exclude"
			name = "nether"
		}
	}
	dimensionRules = {
		combination = "or"
		{
			type = "whitelist"
			dimensionID = 7
		}
		{
			type = "whitelist"
			dimensionID = -100
		}
	}
}
{
	type = "tin"
	inherit = "base"
	name = "Tin Ore"
	sprinkleMix = true
	blocks = {
		"Forestry:ore:1"
		"ElectriCraft:ore:2"
		"IC2:ore:3"
	}
	heightRule = {
		minHeight = 32
		maxHeight = 60
		variation = "normal"
	}
	veinFrequency = {
		veinsPerChunk = 6
		chunkGenChance = 0.875
	}
	veinSize = 12
	spawnBlock = {
		{
			block = "chisel:rock"
			metadata = {
				0
				5
				15
			}
		}
	}
	proximityRules = {
		{
			block = "minecraft:lava" --lava source blocks
			metadata = {
				0
			}
		}
	}
	biomeRules = {
		combination = "or"
		{
			type = "include"
			biomeID = 0
		}
		{
			type = "dictionary-require"
			name = "river"
		}
	}
	dimensionRules = {
		combination = "and"
		{
			type = "blacklist"
			dimensionID = 60
		}
	}
}
{
	type = "quartz" --inherits most of its properties
	inherit = "base"
	name = "Quartz Ore"
	blocks = {
		"minecraft:quartz"
	}
	heightRule = {
		maxHeight = 128
		variation = "pyramid"
	}
	veinSize = 24
	dimensionRules = {
		combination = "and"
		{
			type = "whitelist"
			dimensionID = -1 //can only gen in Nether, but requires stone... :P
		}
	}
}