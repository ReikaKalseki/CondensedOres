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