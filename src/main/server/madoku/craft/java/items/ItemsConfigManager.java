package madoku.craft.java.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import madoku.craft.java.core.json.JSONFormatAPIManager;
import madoku.craft.java.core.json.JSONAPIManager;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ItemsConfigManager {
	public static final String FIELD_ITEM_ID = "item-id";
	public static final String FIELD_ITEM_UPGRADE = "item-upgrade";
	public static final String FIELD_INGREDIENTS = "ingredients";
	public static final String FIELD_INGREDIENT_ITEM_ID = "item-id";
	public static final String FIELD_BASE_COST = "base-cost";
	public static final String FIELD_ITEM_LEVELS = "item-levels";
	public static final String FIELD_STARTING_LEVEL = "starting-level";
	public static final String FIELD_MAXIMUM_LEVEL = "maximum-level";
	public static final String FIELD_ARMOR_CATEGORY = "armor-category";
	public static final String FIELD_TOOL_CATEGORY = "tool-category";
	public static final String FIELD_WEAPON_CATEGORY = "weapon-category";
	public static final String FIELD_FOOD_CATEGORY = "food-category";
	public static final String FIELD_FUEL_CATEGORY = "fuel-category";
	public static final String FIELD_OTHER_CATEGORY = "other-category";
	public static final String FIELD_CATEGORY_ENABLED = "enabled";
	public static final String FIELD_CATEGORY = "categories";
	public static final String FIELD_STACK = "stack";
	public static final String STACK_SINGLE = "single";
	public static final String STACK_MULTI = "multi";
	public static final int MAX_UPGRADE_INGREDIENTS = 3;

	public static final String CATEGORY_FUEL = "fuel";
	public static final String CATEGORY_OTHER = "other";
	public static final String CATEGORY_TOOL = "tool";
	public static final String CATEGORY_WEAPON = "weapon";
	public static final String CATEGORY_ARMOR = "armor";
	public static final String CATEGORY_FOOD = "food";

	public static final String FIELD_FUEL_TICKS = "fuel-ticks";
	public static final String FIELD_NUTRITION = "nutrition";
	public static final String FIELD_STATUS_EFFECTS = "status-effects";
	public static final String FIELD_EFFECT = "effect";
	public static final String FIELD_DURATION = "duration";
	public static final String FIELD_CHANCE = "chance";
	public static final String FIELD_DURABILITY = "durability";
	public static final String FIELD_ATTACK_DAMAGE = "attack-damage";
	public static final String FIELD_ATTACK_SPEED = "attack-speed";
	public static final String FIELD_MINING_SPEED = "mining-speed";
	public static final String FIELD_MATERIAL_LEVEL = "material-level";
	public static final String FIELD_ARMOR = "armor";
	public static final String FIELD_ARMOR_TOUGHNESS = "armor-toughness";
	public static final String FIELD_REACH_MIN = "reach-min";
	public static final String FIELD_REACH_MAX = "reach-max";

	public static final int TOOL_INT_UNSET = -1;
	public static final double TOOL_DOUBLE_UNSET = -1.0d;

	private ItemsConfigManager() {
	}

	public static void initialize() { }

	public static void reset() { }

	public static JsonObject buildCategoryFeatureDefaults() {
		return JSONFormatAPIManager.object()
			.object(FIELD_ITEM_LEVELS, levels -> levels
				.put(FIELD_CATEGORY_ENABLED, true)
				.put(FIELD_STARTING_LEVEL, 1)
				.put(FIELD_MAXIMUM_LEVEL, 5))
			.object(FIELD_ARMOR_CATEGORY, category -> category.put(FIELD_CATEGORY_ENABLED, true))
			.object(FIELD_TOOL_CATEGORY, category -> category.put(FIELD_CATEGORY_ENABLED, true))
			.object(FIELD_WEAPON_CATEGORY, category -> category.put(FIELD_CATEGORY_ENABLED, true))
			.object(FIELD_FOOD_CATEGORY, category -> category.put(FIELD_CATEGORY_ENABLED, true))
			.object(FIELD_FUEL_CATEGORY, category -> category.put(FIELD_CATEGORY_ENABLED, true))
			.object(FIELD_OTHER_CATEGORY, category -> category.put(FIELD_CATEGORY_ENABLED, true))
			.build();
	}

	public static Map<String, JsonObject> buildDefaultFuelFileDefaults() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry : buildDefaultFuelTicks().entrySet()) {
			String itemId = entry.getKey();
			String fileKey = fileKeyFromItemId(itemId);
			if (fileKey.isBlank()) {
				continue;
			}
			defaults.put(fileKey, buildFuelItemDefaults(itemId, entry.getValue(), defaultStackForItem(itemId)));
		}
		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultOtherFileDefaults() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		for (String itemId : buildDefaultOtherItems().keySet()) {
			String fileKey = fileKeyFromItemId(itemId);
			if (fileKey.isBlank()) {
				continue;
			}
			defaults.put(fileKey, buildOtherItemDefaults(itemId, defaultStackForItem(itemId)));
		}
		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultFoodFileDefaults() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		String driedKelp = "minecraft:dried_kelp";
		defaults.put(fileKeyFromItemId(driedKelp), buildDefaultFoodItem(
			driedKelp,
			1,
			defaultStackForItem(driedKelp),
			new DefaultFoodEffect("speed", 15, 0.05D),
			new DefaultFoodEffect("haste", 15, 0.05D),
			new DefaultFoodEffect("strength", 15, 0.05D),
			new DefaultFoodEffect("regeneration", 15, 0.05D),
			new DefaultFoodEffect("resistance", 15, 0.05D),
			new DefaultFoodEffect("conduit-power", 15, 0.05D),
			new DefaultFoodEffect("health-boost", 15, 0.05D),
			new DefaultFoodEffect("dolphins-grace", 15, 0.05D)
		));

		String sweetBerries = "minecraft:sweet_berries";
		defaults.put(fileKeyFromItemId(sweetBerries), buildDefaultFoodItem(
			sweetBerries,
			1,
			defaultStackForItem(sweetBerries),
			new DefaultFoodEffect("speed", 15, 0.5D)
		));

		String sweetBerryCookie = "madoku-craft:sweet-berry-cookie";
		defaults.put(fileKeyFromItemId(sweetBerryCookie), buildDefaultFoodItem(
			sweetBerryCookie,
			4,
			defaultStackForItem(sweetBerryCookie),
			new DefaultFoodEffect("speed", 30, 1.0D)
		));

		String glowBerries = "minecraft:glow_berries";
		defaults.put(fileKeyFromItemId(glowBerries), buildDefaultFoodItem(
			glowBerries,
			1,
			defaultStackForItem(glowBerries),
			new DefaultFoodEffect("night-vision", 15, 0.5D)
		));

		String beetroot = "minecraft:beetroot";
		defaults.put(fileKeyFromItemId(beetroot), buildDefaultFoodItem(
			beetroot,
			1,
			defaultStackForItem(beetroot),
			new DefaultFoodEffect("strength", 15, 0.5D)
		));

		String beetrootSoup = "minecraft:beetroot_soup";
		defaults.put(fileKeyFromItemId(beetrootSoup), buildDefaultFoodItem(
			beetrootSoup,
			6,
			defaultStackForItem(beetrootSoup),
			new DefaultFoodEffect("strength", 30, 1.0D)
		));

		String bread = "minecraft:bread";
		defaults.put(fileKeyFromItemId(bread), buildDefaultFoodItem(
			bread,
			6,
			defaultStackForItem(bread)
		));

		String melonSlice = "minecraft:melon_slice";
		defaults.put(fileKeyFromItemId(melonSlice), buildDefaultFoodItem(
			melonSlice,
			2,
			defaultStackForItem(melonSlice),
			new DefaultFoodEffect("regeneration", 15, 0.5D)
		));

		String mushroomStew = "minecraft:mushroom_stew";
		defaults.put(fileKeyFromItemId(mushroomStew), buildDefaultFoodItem(
			mushroomStew,
			4,
			defaultStackForItem(mushroomStew),
			new DefaultFoodEffect("haste", 30, 1.0D)
		));

		String potato = "minecraft:potato";
		defaults.put(fileKeyFromItemId(potato), buildDefaultFoodItem(
			potato,
			2,
			defaultStackForItem(potato)
		));

		String chicken = "minecraft:chicken";
		defaults.put(fileKeyFromItemId(chicken), buildDefaultFoodItem(
			chicken,
			4,
			defaultStackForItem(chicken)
		));

		String cod = "minecraft:cod";
		defaults.put(fileKeyFromItemId(cod), buildDefaultFoodItem(
			cod,
			3,
			defaultStackForItem(cod)
		));

		String cookedBeef = "minecraft:cooked_beef";
		defaults.put(fileKeyFromItemId(cookedBeef), buildDefaultFoodItem(
			cookedBeef,
			7,
			defaultStackForItem(cookedBeef)
		));

		String cookedChicken = "minecraft:cooked_chicken";
		defaults.put(fileKeyFromItemId(cookedChicken), buildDefaultFoodItem(
			cookedChicken,
			7,
			defaultStackForItem(cookedChicken)
		));

		String cookedCod = "minecraft:cooked_cod";
		defaults.put(fileKeyFromItemId(cookedCod), buildDefaultFoodItem(
			cookedCod,
			6,
			defaultStackForItem(cookedCod)
		));

		String cookedMutton = "minecraft:cooked_mutton";
		defaults.put(fileKeyFromItemId(cookedMutton), buildDefaultFoodItem(
			cookedMutton,
			7,
			defaultStackForItem(cookedMutton)
		));

		String cookedPorkchop = "minecraft:cooked_porkchop";
		defaults.put(fileKeyFromItemId(cookedPorkchop), buildDefaultFoodItem(
			cookedPorkchop,
			7,
			defaultStackForItem(cookedPorkchop),
			new DefaultFoodEffect("health-boost", 30, 1.0D)
		));

		String cookedPotato = "minecraft:cooked_potato";
		defaults.put(fileKeyFromItemId(cookedPotato), buildDefaultFoodItem(
			cookedPotato,
			5,
			defaultStackForItem(cookedPotato)
		));

		String cookedRabbit = "minecraft:cooked_rabbit";
		defaults.put(fileKeyFromItemId(cookedRabbit), buildDefaultFoodItem(
			cookedRabbit,
			7,
			defaultStackForItem(cookedRabbit)
		));

		String cookedSalmon = "minecraft:cooked_salmon";
		defaults.put(fileKeyFromItemId(cookedSalmon), buildDefaultFoodItem(
			cookedSalmon,
			6,
			defaultStackForItem(cookedSalmon)
		));

		String mutton = "minecraft:mutton";
		defaults.put(fileKeyFromItemId(mutton), buildDefaultFoodItem(
			mutton,
			4,
			defaultStackForItem(mutton)
		));

		String pufferfish = "minecraft:pufferfish";
		defaults.put(fileKeyFromItemId(pufferfish), buildDefaultFoodItem(
			pufferfish,
			4,
			defaultStackForItem(pufferfish),
			new DefaultFoodEffect("nausea", 20, 1.0D),
			new DefaultFoodEffect("weakness", 20, 0.7D),
			new DefaultFoodEffect("poison", 20, 0.4D)
		));

		String rottenFlesh = "minecraft:rotten_flesh";
		defaults.put(fileKeyFromItemId(rottenFlesh), buildDefaultFoodItem(
			rottenFlesh,
			2,
			defaultStackForItem(rottenFlesh),
			new DefaultFoodEffect("nausea", 10, 0.8D),
			new DefaultFoodEffect("weakness", 10, 0.5D),
			new DefaultFoodEffect("poison", 10, 0.2D)
		));

		String spiderEye = "minecraft:spider_eye";
		defaults.put(fileKeyFromItemId(spiderEye), buildDefaultFoodItem(
			spiderEye,
			3,
			defaultStackForItem(spiderEye),
			new DefaultFoodEffect("nausea", 15, 0.9D),
			new DefaultFoodEffect("weakness", 15, 0.6D),
			new DefaultFoodEffect("poison", 15, 0.3D)
		));

		String tropicalFish = "minecraft:tropical_fish";
		defaults.put(fileKeyFromItemId(tropicalFish), buildDefaultFoodItem(
			tropicalFish,
			3,
			defaultStackForItem(tropicalFish),
			new DefaultFoodEffect("conduit-power", 15, 0.5D)
		));

		String cookedTropicalFish = "madoku-craft:cooked-tropical-fish";
		defaults.put(fileKeyFromItemId(cookedTropicalFish), buildDefaultFoodItem(
			cookedTropicalFish,
			7,
			defaultStackForItem(cookedTropicalFish),
			new DefaultFoodEffect("conduit-power", 30, 1.0D)
		));

		String apple = "minecraft:apple";
		defaults.put(fileKeyFromItemId(apple), buildDefaultFoodItem(
			apple,
			3,
			defaultStackForItem(apple)
		));

		String beef = "minecraft:beef";
		defaults.put(fileKeyFromItemId(beef), buildDefaultFoodItem(
			beef,
			4,
			defaultStackForItem(beef)
		));

		String carrot = "minecraft:carrot";
		defaults.put(fileKeyFromItemId(carrot), buildDefaultFoodItem(
			carrot,
			2,
			defaultStackForItem(carrot)
		));

		String chorusFruit = "minecraft:chorus_fruit";
		defaults.put(fileKeyFromItemId(chorusFruit), buildDefaultFoodItem(
			chorusFruit,
			4,
			defaultStackForItem(chorusFruit)
		));

		String cookie = "minecraft:cookie";
		defaults.put(fileKeyFromItemId(cookie), buildDefaultFoodItem(
			cookie,
			3,
			defaultStackForItem(cookie)
		));

		String enchantedGoldenApple = "minecraft:enchanted_golden_apple";
		defaults.put(fileKeyFromItemId(enchantedGoldenApple), buildDefaultFoodItem(
			enchantedGoldenApple,
			7,
			defaultStackForItem(enchantedGoldenApple),
			new DefaultFoodEffect("speed", 120, 1.0D),
			new DefaultFoodEffect("haste", 120, 1.0D),
			new DefaultFoodEffect("strength", 120, 1.0D),
			new DefaultFoodEffect("regeneration", 120, 1.0D),
			new DefaultFoodEffect("resistance", 120, 1.0D),
			new DefaultFoodEffect("health-boost", 120, 1.0D)
		));

		String goldenApple = "minecraft:golden_apple";
		defaults.put(fileKeyFromItemId(goldenApple), buildDefaultFoodItem(
			goldenApple,
			5,
			defaultStackForItem(goldenApple),
			new DefaultFoodEffect("speed", 30, 1.0D),
			new DefaultFoodEffect("haste", 30, 1.0D),
			new DefaultFoodEffect("regeneration", 30, 1.0D)
		));

		String goldenCarrot = "minecraft:golden_carrot";
		defaults.put(fileKeyFromItemId(goldenCarrot), buildDefaultFoodItem(
			goldenCarrot,
			6,
			defaultStackForItem(goldenCarrot)
		));

		String honeyBottle = "minecraft:honey_bottle";
		defaults.put(fileKeyFromItemId(honeyBottle), buildDefaultFoodItem(
			honeyBottle,
			5,
			defaultStackForItem(honeyBottle),
			new DefaultFoodEffect("absorption", 120, 1.0D)
		));

		String poisonousPotato = "minecraft:poisonous_potato";
		defaults.put(fileKeyFromItemId(poisonousPotato), buildDefaultFoodItem(
			poisonousPotato,
			5,
			defaultStackForItem(poisonousPotato),
			new DefaultFoodEffect("nausea", 30, 0.2D),
			new DefaultFoodEffect("weakness", 30, 0.5D),
			new DefaultFoodEffect("poison", 30, 0.8D)
		));

		String porkchop = "minecraft:porkchop";
		defaults.put(fileKeyFromItemId(porkchop), buildDefaultFoodItem(
			porkchop,
			4,
			defaultStackForItem(porkchop),
			new DefaultFoodEffect("health-boost", 15, 0.5D)
		));

		String rabbit = "minecraft:rabbit";
		defaults.put(fileKeyFromItemId(rabbit), buildDefaultFoodItem(
			rabbit,
			4,
			defaultStackForItem(rabbit)
		));

		String pumpkinPie = "minecraft:pumpkin_pie";
		defaults.put(fileKeyFromItemId(pumpkinPie), buildDefaultFoodItem(
			pumpkinPie,
			7,
			defaultStackForItem(pumpkinPie),
			new DefaultFoodEffect("resistance", 30, 1.0D)
		));

		String rabbitStew = "minecraft:rabbit_stew";
		defaults.put(fileKeyFromItemId(rabbitStew), buildDefaultFoodItem(
			rabbitStew,
			15,
			defaultStackForItem(rabbitStew)
		));

		String salmon = "minecraft:salmon";
		defaults.put(fileKeyFromItemId(salmon), buildDefaultFoodItem(
			salmon,
			3,
			defaultStackForItem(salmon)
		));
		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultToolFileDefaults() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		for (Map.Entry<String, JsonObject> entry : buildDefaultToolItemProfiles().entrySet()) {
			String fileKey = fileKeyFromItemId(entry.getKey());
			if (fileKey.isBlank()) {
				continue;
			}
			defaults.put(fileKey, entry.getValue());
		}
		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultToolsCategoryFileDefaults() {
		return new LinkedHashMap<>(buildDefaultToolFileDefaults());
	}

	public static Map<String, JsonObject> buildDefaultWeaponFileDefaults() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		for (Map.Entry<String, JsonObject> entry : buildDefaultWeaponItemProfiles().entrySet()) {
			String fileKey = fileKeyFromItemId(entry.getKey());
			if (!fileKey.isBlank()) defaults.put(fileKey, entry.getValue());
		}
		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultArmorFileDefaults() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		for (Map.Entry<String, JsonObject> entry : buildDefaultArmorItemProfiles().entrySet()) {
			String fileKey = fileKeyFromItemId(entry.getKey());
			if (fileKey.isBlank()) {
				continue;
			}
			defaults.put(fileKey, entry.getValue());
		}
		return defaults;
	}

	public static JsonObject buildFuelItemDefaults(String itemId, double fuelTicks) {
		return buildFuelItemDefaults(itemId, fuelTicks, defaultStackForItem(itemId));
	}

	public static JsonObject buildFuelItemDefaults(String itemId, double fuelTicks, String stackValue) {
		return JSONFormatAPIManager.object()
			.putAll(buildBaseDefaults(itemId, stackValue, CATEGORY_FUEL))
			.put(FIELD_FUEL_TICKS, fuelTicks)
			.build();
	}

	public static JsonObject buildOtherItemDefaults(String itemId, String stackValue) {
		return buildBaseDefaults(itemId, stackValue, CATEGORY_OTHER);
	}

	public static JsonObject buildFoodItemDefaults(String itemId, int nutrition, String stackValue) {
		return JSONFormatAPIManager.object()
			.putAll(buildBaseDefaults(itemId, stackValue, CATEGORY_FOOD))
			.put(FIELD_NUTRITION, Math.max(0, nutrition))
			.array(FIELD_STATUS_EFFECTS, ignored -> { })
			.build();
	}

	private static JsonObject buildDefaultFoodItem(
		String itemId,
		int nutrition,
		String stackValue,
		DefaultFoodEffect... effects
	) {
		JsonObject root = buildFoodItemDefaults(itemId, nutrition, stackValue);
		JsonArray statusEffects = root.getAsJsonArray(FIELD_STATUS_EFFECTS);
		if (effects != null) {
			for (DefaultFoodEffect effect : effects) {
				if (effect == null || effect.effect() == null || effect.effect().isBlank()) {
					continue;
				}
				JsonObject statusEffect = new JsonObject();
				statusEffect.addProperty(FIELD_EFFECT, effect.effect());
				statusEffect.addProperty(FIELD_DURATION, Math.max(0, effect.duration()));
				statusEffect.addProperty(FIELD_CHANCE, Math.max(0.0D, Math.min(1.0D, effect.chance())));
				statusEffects.add(statusEffect);
			}
		}
		return root;
	}

	public static JsonObject buildFoodItemDefaults(
		String itemId,
		int nutrition,
		String stackValue,
		String[] effects,
		int duration,
		double chance
	) {
		JsonObject root = buildFoodItemDefaults(itemId, nutrition, stackValue);
		JsonArray statusEffects = root.getAsJsonArray(FIELD_STATUS_EFFECTS);
		if (effects == null) {
			return root;
		}
		for (String effect : effects) {
			if (effect == null || effect.isBlank()) {
				continue;
			}
			JsonObject statusEffect = new JsonObject();
			statusEffect.addProperty(FIELD_EFFECT, effect);
			statusEffect.addProperty(FIELD_DURATION, Math.max(0, duration));
			statusEffect.addProperty(FIELD_CHANCE, Math.max(0.0D, Math.min(1.0D, chance)));
			statusEffects.add(statusEffect);
		}
		return root;
	}

	public static JsonObject buildToolItemDefaults(String itemId) {
		return buildToolItemDefaults(
			itemId,
			TOOL_INT_UNSET,
			TOOL_DOUBLE_UNSET,
			TOOL_DOUBLE_UNSET,
			TOOL_DOUBLE_UNSET,
			TOOL_INT_UNSET,
			STACK_SINGLE
		);
	}

	public static JsonObject buildToolItemDefaults(
		String itemId,
		int durability,
		double attackDamage,
		double attackSpeed,
		double miningSpeed,
		int materialLevel,
		String stackValue
	) {
		return JSONFormatAPIManager.object()
			.putAll(buildBaseDefaults(itemId, stackValue, CATEGORY_TOOL))
			.put(FIELD_DURABILITY, durability)
			.put(FIELD_ATTACK_DAMAGE, attackDamage)
			.put(FIELD_ATTACK_SPEED, attackSpeed)
			.put(FIELD_MINING_SPEED, miningSpeed)
			.put(FIELD_MATERIAL_LEVEL, materialLevel)
			.build();
	}

	public static JsonObject buildWeaponItemDefaults(String itemId) {
		return buildWeaponItemDefaults(itemId, TOOL_INT_UNSET, TOOL_DOUBLE_UNSET, TOOL_DOUBLE_UNSET,
			TOOL_DOUBLE_UNSET, TOOL_INT_UNSET, STACK_SINGLE);
	}

	public static JsonObject buildWeaponItemDefaults(
		String itemId,
		int durability,
		double attackDamage,
		double attackSpeed,
		double miningSpeed,
		int materialLevel,
		String stackValue
	) {
		return JSONFormatAPIManager.object()
			.putAll(buildBaseDefaults(itemId, stackValue, CATEGORY_WEAPON))
			.put(FIELD_DURABILITY, durability)
			.put(FIELD_ATTACK_DAMAGE, attackDamage)
			.put(FIELD_ATTACK_SPEED, attackSpeed)
			.put(FIELD_MINING_SPEED, miningSpeed)
			.put(FIELD_MATERIAL_LEVEL, materialLevel)
			.build();
	}

	public static JsonObject buildSpearItemDefaults(String itemId) {
		return buildSpearItemDefaults(
			itemId,
			TOOL_INT_UNSET,
			TOOL_DOUBLE_UNSET,
			TOOL_DOUBLE_UNSET,
			TOOL_INT_UNSET,
			1.0d,
			4.5d,
			STACK_SINGLE
		);
	}

	public static JsonObject buildSpearItemDefaults(
		String itemId,
		int durability,
		double attackDamage,
		double attackSpeed,
		int materialLevel,
		double reachMin,
		double reachMax,
		String stackValue
	) {
		return JSONFormatAPIManager.object()
			.putAll(buildBaseDefaults(itemId, stackValue, CATEGORY_WEAPON))
			.put(FIELD_DURABILITY, durability)
			.put(FIELD_ATTACK_DAMAGE, attackDamage)
			.put(FIELD_ATTACK_SPEED, attackSpeed)
			.put(FIELD_MATERIAL_LEVEL, materialLevel)
			.put(FIELD_REACH_MIN, reachMin)
			.put(FIELD_REACH_MAX, reachMax)
			.build();
	}

	public static JsonObject buildArmorItemDefaults(String itemId) {
		return buildArmorItemDefaults(
			itemId,
			TOOL_INT_UNSET,
			TOOL_DOUBLE_UNSET,
			TOOL_DOUBLE_UNSET,
			STACK_SINGLE
		);
	}

	public static JsonObject buildArmorItemDefaults(
		String itemId,
		int durability,
		double armor,
		double armorToughness,
		String stackValue
	) {
		return JSONFormatAPIManager.object()
			.putAll(buildBaseDefaults(itemId, stackValue, CATEGORY_ARMOR))
			.put(FIELD_DURABILITY, durability)
			.put(FIELD_ARMOR, armor)
			.put(FIELD_ARMOR_TOUGHNESS, armorToughness)
			.build();
	}

	public static JsonObject buildBaseDefaults(String itemId, String stackValue, String... categories) {
		return JSONFormatAPIManager.object()
			.put(FIELD_ITEM_ID, JSONAPIManager.normalizeRegistryIdentifierForJson(itemId))
			.put(FIELD_CATEGORY, buildCategoryArray(categories))
			.put(FIELD_STACK, normalizeStackValue(stackValue))
			.object(FIELD_ITEM_UPGRADE, upgrade -> upgrade.array(FIELD_INGREDIENTS, ignored -> { }))
			.build();
	}

	private static JsonArray buildCategoryArray(String... categories) {
		JSONFormatAPIManager.ArrayBuilder categoryArray = JSONFormatAPIManager.array();
		if (categories == null || categories.length == 0) {
			categoryArray.add(CATEGORY_OTHER);
			return categoryArray.build();
		}

		Map<String, Boolean> normalizedCategories = new LinkedHashMap<>();
		for (String category : categories) {
			if (category == null) {
				continue;
			}
			String normalizedCategory = normalizeCategoryValue(category);
			if (normalizedCategory.isEmpty()) {
				continue;
			}
			normalizedCategories.put(normalizedCategory, true);
		}

		if (normalizedCategories.isEmpty()) {
			categoryArray.add(CATEGORY_OTHER);
			return categoryArray.build();
		}

		for (String category : normalizedCategories.keySet()) categoryArray.add(category);

		return categoryArray.build();
	}

	private static String normalizeCategoryValue(String rawCategoryValue) {
		return rawCategoryValue == null ? "" : rawCategoryValue.trim().toLowerCase(Locale.ROOT);
	}

	private static String defaultStackForItem(String itemId) {
		if ("minecraft:lava_bucket".equals(itemId)
			|| "minecraft:water_bucket".equals(itemId)
			|| "minecraft:milk_bucket".equals(itemId)
			|| "minecraft:powder_snow_bucket".equals(itemId)) {
			return STACK_SINGLE;
		}
		return STACK_MULTI;
	}

	public static String normalizeStackValue(String rawStackValue) {
		String normalized = rawStackValue == null ? "" : rawStackValue.trim().toLowerCase(Locale.ROOT);
		if (STACK_SINGLE.equals(normalized)) {
			return STACK_SINGLE;
		}
		return STACK_MULTI;
	}

	private static String fileKeyFromItemId(String itemId) {
		if (itemId == null) {
			return "";
		}
		String normalized = itemId.trim();
		if (normalized.isEmpty()) {
			return "";
		}
		int separator = normalized.indexOf(':');
		if (separator < 0 || separator >= normalized.length() - 1) {
			return normalized.toLowerCase().replace('_', '-');
		}
		return normalized.substring(separator + 1).toLowerCase().replace('_', '-');
	}

	public static Map<String, Double> buildDefaultFuelTicks() {
		Map<String, Double> defaults = new LinkedHashMap<>();
		defaults.put("minecraft:lava_bucket", 86400.0);
		defaults.put("minecraft:coal_block", 19200.0);
		defaults.put("minecraft:magma_block", 12800.0);
		defaults.put("minecraft:dried_kelp_block", 4800.0);
		defaults.put("minecraft:blaze_rod", 3600.0);
		defaults.put("minecraft:coal", 2400.0);
		defaults.put("minecraft:charcoal", 1600.0);
		defaults.put("minecraft:mangrove_roots", 800.0);
		defaults.put("minecraft:muddy_mangrove_roots", 800.0);
		defaults.put("minecraft:crimson_stem", 800.0);
		defaults.put("minecraft:warped_stem", 800.0);
		defaults.put("minecraft:stripped_crimson_stem", 800.0);
		defaults.put("minecraft:stripped_warped_stem", 800.0);
		defaults.put("minecraft:oak_log", 600.0);
		defaults.put("minecraft:spruce_log", 600.0);
		defaults.put("minecraft:birch_log", 600.0);
		defaults.put("minecraft:jungle_log", 600.0);
		defaults.put("minecraft:acacia_log", 600.0);
		defaults.put("minecraft:cherry_log", 600.0);
		defaults.put("minecraft:dark_oak_log", 600.0);
		defaults.put("minecraft:mangrove_log", 600.0);
		defaults.put("minecraft:pale_oak_log", 600.0);
		defaults.put("minecraft:stripped_oak_log", 600.0);
		defaults.put("minecraft:stripped_spruce_log", 600.0);
		defaults.put("minecraft:stripped_birch_log", 600.0);
		defaults.put("minecraft:stripped_jungle_log", 600.0);
		defaults.put("minecraft:stripped_acacia_log", 600.0);
		defaults.put("minecraft:stripped_cherry_log", 600.0);
		defaults.put("minecraft:stripped_dark_oak_log", 600.0);
		defaults.put("minecraft:stripped_mangrove_log", 600.0);
		defaults.put("minecraft:stripped_pale_oak_log", 600.0);
		defaults.put("minecraft:oak_wood", 600.0);
		defaults.put("minecraft:spruce_wood", 600.0);
		defaults.put("minecraft:birch_wood", 600.0);
		defaults.put("minecraft:jungle_wood", 600.0);
		defaults.put("minecraft:acacia_wood", 600.0);
		defaults.put("minecraft:cherry_wood", 600.0);
		defaults.put("minecraft:pale_oak_wood", 600.0);
		defaults.put("minecraft:dark_oak_wood", 600.0);
		defaults.put("minecraft:mangrove_wood", 600.0);
		defaults.put("minecraft:stripped_oak_wood", 600.0);
		defaults.put("minecraft:stripped_spruce_wood", 600.0);
		defaults.put("minecraft:stripped_birch_wood", 600.0);
		defaults.put("minecraft:stripped_jungle_wood", 600.0);
		defaults.put("minecraft:stripped_acacia_wood", 600.0);
		defaults.put("minecraft:stripped_cherry_wood", 600.0);
		defaults.put("minecraft:stripped_pale_oak_wood", 600.0);
		defaults.put("minecraft:stripped_dark_oak_wood", 600.0);
		defaults.put("minecraft:stripped_mangrove_wood", 600.0);
		defaults.put("minecraft:stripped_bamboo_block", 450.0);
		defaults.put("minecraft:bamboo_block", 450.0);
		defaults.put("minecraft:crimson_planks", 400.0);
		defaults.put("minecraft:warped_planks", 400.0);
		defaults.put("minecraft:oak_planks", 300.0);
		defaults.put("minecraft:spruce_planks", 300.0);
		defaults.put("minecraft:birch_planks", 300.0);
		defaults.put("minecraft:jungle_planks", 300.0);
		defaults.put("minecraft:acacia_planks", 300.0);
		defaults.put("minecraft:cherry_planks", 300.0);
		defaults.put("minecraft:dark_oak_planks", 300.0);
		defaults.put("minecraft:mangrove_planks", 300.0);
		defaults.put("minecraft:bamboo_planks", 300.0);
		defaults.put("minecraft:pale_oak_planks", 300.0);
		defaults.put("minecraft:leaf_litter", 200.0);
		defaults.put("minecraft:crimson_fungus", 200.0);
		defaults.put("minecraft:warped_fungus", 200.0);
		defaults.put("minecraft:oak_sapling", 150.0);
		defaults.put("minecraft:spruce_sapling", 150.0);
		defaults.put("minecraft:birch_sapling", 150.0);
		defaults.put("minecraft:jungle_sapling", 150.0);
		defaults.put("minecraft:acacia_sapling", 150.0);
		defaults.put("minecraft:dark_oak_sapling", 150.0);
		defaults.put("minecraft:mangrove_propagule", 150.0);
		defaults.put("minecraft:cherry_sapling", 150.0);
		defaults.put("minecraft:pale_oak_sapling", 150.0);
		defaults.put("minecraft:stick", 100.0);
		defaults.put("minecraft:bamboo", 50.0);
		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultToolItemProfiles() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();

		String[] materials = {"wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"};
		int[] durability = {64, 160, 320, 640, 960, 1600, 2048};
		int[] materialLevel = {0, 1, 2, 2, 3, 3, 4};
		double[] pickAndShovelDamage = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0};

		for (int index = 0; index < materials.length; index++) {
			String prefix = "minecraft:" + materials[index] + "_";
			int itemDurability = durability[index];
			int itemLevel = materialLevel[index];
			double attackStep = index;
			double miningSpeed = 2.0 + (index * 1.5);

			defaults.put(
				prefix + "axe",
				buildAxeItemDefaults(prefix + "axe", itemDurability, 8.0 + attackStep, 0.8 + (index * 0.025), miningSpeed, itemLevel, STACK_SINGLE)
			);
			defaults.put(
				prefix + "pickaxe",
				buildToolItemDefaults(prefix + "pickaxe", itemDurability, pickAndShovelDamage[index], 1.2 + (index * 0.05), miningSpeed, itemLevel, STACK_SINGLE)
			);
			defaults.put(
				prefix + "shovel",
				buildToolItemDefaults(prefix + "shovel", itemDurability, pickAndShovelDamage[index], 1.2 + (index * 0.05), miningSpeed, itemLevel, STACK_SINGLE)
			);
			defaults.put(
				prefix + "hoe",
				buildToolItemDefaults(prefix + "hoe", itemDurability, pickAndShovelDamage[index], 1.2 + (index * 0.05), miningSpeed, itemLevel, STACK_SINGLE)
			);
		}

		defaults.put(
			"minecraft:shears",
			buildToolItemDefaults("minecraft:shears", 512, TOOL_DOUBLE_UNSET, TOOL_DOUBLE_UNSET, TOOL_DOUBLE_UNSET, TOOL_INT_UNSET, STACK_SINGLE)
		);
		defaults.put(
			"minecraft:flint_and_steel",
			buildToolItemDefaults("minecraft:flint_and_steel", 256, TOOL_DOUBLE_UNSET, TOOL_DOUBLE_UNSET, TOOL_DOUBLE_UNSET, TOOL_INT_UNSET, STACK_SINGLE)
		);
		defaults.put(
			"minecraft:fishing_rod",
			buildToolItemDefaults("minecraft:fishing_rod", 128, TOOL_DOUBLE_UNSET, TOOL_DOUBLE_UNSET, TOOL_DOUBLE_UNSET, TOOL_INT_UNSET, STACK_SINGLE)
		);
		return defaults;
	}

	public static JsonObject buildAxeItemDefaults(
		String itemId,
		int durability,
		double attackDamage,
		double attackSpeed,
		double miningSpeed,
		int materialLevel,
		String stackValue
	) {
		return JSONFormatAPIManager.object()
			.putAll(buildBaseDefaults(itemId, stackValue, CATEGORY_TOOL, CATEGORY_WEAPON))
			.put(FIELD_DURABILITY, durability)
			.put(FIELD_ATTACK_DAMAGE, attackDamage)
			.put(FIELD_ATTACK_SPEED, attackSpeed)
			.put(FIELD_MINING_SPEED, miningSpeed)
			.put(FIELD_MATERIAL_LEVEL, materialLevel)
			.build();
	}

	public static Map<String, JsonObject> buildDefaultWeaponItemProfiles() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		String[] materials = {"wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"};
		int[] durability = {64, 160, 320, 640, 960, 1600, 2048};
		int[] materialLevel = {0, 1, 2, 2, 3, 3, 4};
		for (int index = 0; index < materials.length; index++) {
			String itemId = "minecraft:" + materials[index] + "_sword";
			defaults.put(itemId, buildWeaponItemDefaults(
				itemId,
				durability[index],
				6.0 + (index * 0.75),
				1.6 + (index * 0.075),
				TOOL_DOUBLE_UNSET,
				materialLevel[index],
				STACK_SINGLE
			));
		}
		defaults.putAll(buildDefaultSpearItemProfiles());
		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultSpearItemProfiles() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		String[] materials = {"wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"};
		int[] durability = {64, 160, 320, 640, 960, 1600, 2048};
		int[] materialLevel = {0, 1, 2, 2, 3, 3, 4};

		for (int index = 0; index < materials.length; index++) {
			String itemId = "minecraft:" + materials[index] + "_spear";
			defaults.put(
				itemId,
				buildSpearItemDefaults(
					itemId,
					durability[index],
					4.0 + (index * 0.5),
					1.2 + (index * 0.05),
					materialLevel[index],
					1.0d,
					4.5d,
					STACK_SINGLE
				)
			);
		}

		return defaults;
	}

	public static Map<String, JsonObject> buildDefaultArmorItemProfiles() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		String[] materials = {"leather", "chainmail", "copper", "iron", "golden", "diamond", "netherite"};
		int[] durability = {64, 160, 320, 640, 960, 1600, 2048};
		double[] armor = {0.6, 0.75, 0.9, 1.05, 1.2, 1.35, 1.5};
		double[] toughness = {0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
		String[] pieces = {"helmet", "chestplate", "leggings", "boots"};

		for (int materialIndex = 0; materialIndex < materials.length; materialIndex++) {
			String material = materials[materialIndex];
			for (String piece : pieces) {
				String itemId = "minecraft:" + material + "_" + piece;
				defaults.put(
					itemId,
					buildArmorItemDefaults(
						itemId,
						durability[materialIndex],
						armor[materialIndex],
						toughness[materialIndex],
						STACK_SINGLE
					)
				);
			}
		}
		return defaults;
	}

	public static Map<String, Boolean> buildDefaultOtherItems() {
		Map<String, Boolean> defaults = new LinkedHashMap<>();
		defaults.put("minecraft:bone_meal", true);
		defaults.put("minecraft:water_bucket", true);
		defaults.put("minecraft:milk_bucket", true);
		defaults.put("minecraft:powder_snow_bucket", true);
		return defaults;
	}

	private record DefaultFoodEffect(String effect, int duration, double chance) { }

}


