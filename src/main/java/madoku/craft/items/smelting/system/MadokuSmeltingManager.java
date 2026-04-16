package madoku.craft.items.smelting.system;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import madoku.craft.config.DynamicJsonSystem;
import madoku.craft.config.StaticJsonSystem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MadokuSmeltingManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(MadokuSmeltingManager.class);

	private static final String SMELTING_CONFIG_FOLDER_NAME = "madoku-craft-smelting";
	private static final String SMELTING_CONFIG_FILE_NAME = "smelting";
	private static final String FURNACES_DIRECTORY_NAME = "madoku-furnaces";

	private static final String FIELD_BLOCK_ID = "block_id";
	private static final String FIELD_BLOCK_ENTITY_ID = "block_entity_id";
	private static final String FIELD_RECIPE_TYPE_ID = "recipe_type_id";
	private static final String FIELD_SMELTING_SPEED = "smeltingSpeed";
	private static final String FIELD_FUEL_EFFICIENCY = "fuelEfficiency";
	private static final String FIELD_ADDITIONAL_INPUTS = "additional_inputs";

	private static final MadokuSmeltingConfig configuration = new MadokuSmeltingConfig();
	private static Map<RecipeType<?>, Set<Item>> additionalInputsByRecipeType = Map.of();
	private static Map<BlockEntityType<?>, Set<Item>> additionalInputsByBlockEntityType = Map.of();

	private MadokuSmeltingManager() {
	}

	public static void initialize() {
		JsonObject smeltingDefaults = MadokuSmeltingConfig.buildSmeltingDefaults();
		try {
			Path directory = StaticJsonSystem.getOrCreateGlobalSystemDirectory(SMELTING_CONFIG_FOLDER_NAME);
			Path smeltingFile = resolveJsonFile(directory, SMELTING_CONFIG_FILE_NAME);

			JsonObject smeltingRoot = StaticJsonSystem.ensureManagedFile(smeltingFile, smeltingDefaults);
			boolean smeltingChanged = configuration.updateSmelting(smeltingRoot);
			if (smeltingChanged) {
				StaticJsonSystem.writeManagedFile(smeltingFile, smeltingRoot, smeltingDefaults);
			}

			FurnaceRules rules = loadFurnaceRules(directory);
			additionalInputsByRecipeType = Map.copyOf(rules.additionalInputsByRecipeType());
			additionalInputsByBlockEntityType = Map.copyOf(rules.additionalInputsByBlockEntityType());
		} catch (IOException | RuntimeException exception) {
			configuration.resetToDefaults();
			additionalInputsByRecipeType = Map.of();
			additionalInputsByBlockEntityType = Map.of();
			LOGGER.error("Failed to load MadokuSmelting config; using defaults.", exception);
		}
	}

	public static boolean isEnabled() {
		return configuration.enableFeature;
	}

	public static boolean shouldWrapRecipeType(RecipeType<?> recipeType) {
		return recipeType != null && recipeType != RecipeType.SMELTING;
	}

	public static boolean isAdditionalInput(RecipeType<?> recipeType, ItemStack stack) {
		if (!isEnabled() || recipeType == null || stack == null || stack.isEmpty()) {
			return false;
		}
		Set<Item> items = additionalInputsByRecipeType.get(recipeType);
		return items != null && items.contains(stack.getItem());
	}

	public static boolean isAdditionalInput(BlockEntityType<?> blockEntityType, RecipeType<?> recipeType, ItemStack stack) {
		if (!isEnabled() || blockEntityType == null || stack == null || stack.isEmpty()) {
			return false;
		}
		Set<Item> items = additionalInputsByBlockEntityType.get(blockEntityType);
		if (items != null && items.contains(stack.getItem())) {
			return true;
		}
		return isAdditionalInput(recipeType, stack);
	}

	private static FurnaceRules loadFurnaceRules(Path smeltingRootDirectory) throws IOException {
		Path furnacesFolder = smeltingRootDirectory.resolve(FURNACES_DIRECTORY_NAME);
		Map<String, JsonObject> defaultFiles = buildDefaultFurnaceFiles();
		Map<String, JsonObject> loadedFiles = DynamicJsonSystem.ensureManagedFolder(
			furnacesFolder,
			defaultFiles,
			ignored -> buildGenericFurnaceDefaults(),
			(fileKey, sourceRoot) -> defaultFiles.containsKey(fileKey) || isSupportedFurnaceDefinition(sourceRoot),
			null
		);

		Map<RecipeType<?>, Set<Item>> byRecipeType = new LinkedHashMap<>();
		Map<BlockEntityType<?>, Set<Item>> byBlockEntityType = new LinkedHashMap<>();

		for (Map.Entry<String, JsonObject> entry : loadedFiles.entrySet()) {
			String fileKey = entry.getKey();
			JsonObject root = entry.getValue();
			JsonObject defaults = defaultFiles.getOrDefault(fileKey, buildGenericFurnaceDefaults());
			boolean changed = normalizeFurnaceDefinition(root, defaults);

			if (!isSupportedFurnaceDefinition(root)) {
				if (defaultFiles.containsKey(fileKey)) {
					root = defaults.deepCopy();
					changed = true;
				} else {
					Files.deleteIfExists(furnacesFolder.resolve(fileKey + ".json"));
					continue;
				}
			}

			List<String> additionalInputs = normalizeAdditionalInputs(root.get(FIELD_ADDITIONAL_INPUTS), List.of());
			if (changed) {
				DynamicJsonSystem.writeManagedFile(
					furnacesFolder.resolve(fileKey + ".json"),
					root,
					defaults,
					null
				);
			}

			RecipeType<?> recipeType = resolveRecipeType(readString(root, FIELD_RECIPE_TYPE_ID, ""));
			if (recipeType != null) {
				byRecipeType.computeIfAbsent(recipeType, ignored -> new LinkedHashSet<>()).addAll(buildItemSet(additionalInputs));
			}

			BlockEntityType<?> blockEntityType = resolveBlockEntityType(readString(root, FIELD_BLOCK_ENTITY_ID, ""));
			if (blockEntityType != null) {
				byBlockEntityType.computeIfAbsent(blockEntityType, ignored -> new LinkedHashSet<>()).addAll(buildItemSet(additionalInputs));
			}
		}

		return new FurnaceRules(copyItemMap(byRecipeType), copyItemMap(byBlockEntityType));
	}

	private static <K> Map<K, Set<Item>> copyItemMap(Map<K, Set<Item>> source) {
		Map<K, Set<Item>> copy = new LinkedHashMap<>();
		for (Map.Entry<K, Set<Item>> entry : source.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				copy.put(entry.getKey(), Set.copyOf(entry.getValue()));
			}
		}
		return copy;
	}

	private static boolean normalizeFurnaceDefinition(JsonObject root, JsonObject defaults) {
		boolean changed = false;
		changed |= setString(root, FIELD_BLOCK_ID, normalizeBlockId(readString(root, FIELD_BLOCK_ID, readString(defaults, FIELD_BLOCK_ID, "")), readString(defaults, FIELD_BLOCK_ID, "")));
		changed |= setString(root, FIELD_BLOCK_ENTITY_ID, normalizeBlockEntityId(readString(root, FIELD_BLOCK_ENTITY_ID, readString(defaults, FIELD_BLOCK_ENTITY_ID, "")), readString(defaults, FIELD_BLOCK_ENTITY_ID, "")));
		changed |= setString(root, FIELD_RECIPE_TYPE_ID, normalizeRecipeTypeId(readString(root, FIELD_RECIPE_TYPE_ID, readString(defaults, FIELD_RECIPE_TYPE_ID, "")), readString(defaults, FIELD_RECIPE_TYPE_ID, "")));
		changed |= setDouble(root, FIELD_SMELTING_SPEED, readDouble(root, FIELD_SMELTING_SPEED, readDouble(defaults, FIELD_SMELTING_SPEED, 200.0)));
		changed |= setDouble(root, FIELD_FUEL_EFFICIENCY, readDouble(root, FIELD_FUEL_EFFICIENCY, readDouble(defaults, FIELD_FUEL_EFFICIENCY, 1.0)));
		changed |= setArray(root, FIELD_ADDITIONAL_INPUTS, normalizeAdditionalInputs(root.get(FIELD_ADDITIONAL_INPUTS), normalizeAdditionalInputs(defaults.get(FIELD_ADDITIONAL_INPUTS), List.of())));
		return changed;
	}

	private static boolean isSupportedFurnaceDefinition(JsonObject root) {
		if (root == null) {
			return false;
		}
		return resolveBlockId(readString(root, FIELD_BLOCK_ID, "")) != null
			&& resolveBlockEntityType(readString(root, FIELD_BLOCK_ENTITY_ID, "")) != null
			&& resolveRecipeType(readString(root, FIELD_RECIPE_TYPE_ID, "")) != null;
	}

	private static Map<String, JsonObject> buildDefaultFurnaceFiles() {
		Map<String, JsonObject> defaults = new LinkedHashMap<>();
		defaults.put("furnace", buildFurnaceDefaultsObject("minecraft:furnace", "minecraft:furnace", "minecraft:smelting", 120.0, 1.0, List.of()));
		defaults.put("smoker", buildFurnaceDefaultsObject("minecraft:smoker", "minecraft:smoker", "minecraft:smoking", 80.0, 1.5, MadokuSmeltingConfig.buildDefaultSmokerAdditionalInputs()));
		defaults.put("blast_furnace", buildFurnaceDefaultsObject("minecraft:blast_furnace", "minecraft:blast_furnace", "minecraft:blasting", 80.0, 1.5, MadokuSmeltingConfig.buildDefaultBlastAdditionalInputs()));
		return defaults;
	}

	private static JsonObject buildGenericFurnaceDefaults() {
		return buildFurnaceDefaultsObject("", "", "", 200.0, 1.0, List.of());
	}

	private static JsonObject buildFurnaceDefaultsObject(String blockId, String blockEntityId, String recipeTypeId, double smeltingSpeed, double fuelEfficiency, List<String> additionalInputs) {
		JsonObject root = new JsonObject();
		root.addProperty(FIELD_BLOCK_ID, blockId);
		root.addProperty(FIELD_BLOCK_ENTITY_ID, blockEntityId);
		root.addProperty(FIELD_RECIPE_TYPE_ID, recipeTypeId);
		root.addProperty(FIELD_SMELTING_SPEED, smeltingSpeed);
		root.addProperty(FIELD_FUEL_EFFICIENCY, fuelEfficiency);
		root.add(FIELD_ADDITIONAL_INPUTS, toJsonArray(additionalInputs));
		return root;
	}

	private static List<String> normalizeAdditionalInputs(JsonElement element, List<String> fallback) {
		if (!(element instanceof JsonArray array)) {
			return new ArrayList<>(fallback);
		}
		Set<String> normalized = new LinkedHashSet<>();
		for (JsonElement value : array) {
			if (value instanceof JsonPrimitive primitive && primitive.isString()) {
				String itemId = resolveItemId(primitive.getAsString());
				if (itemId != null) {
					normalized.add(itemId);
				}
			}
		}
		return new ArrayList<>(normalized);
	}

	private static Set<Item> buildItemSet(List<String> entries) {
		Set<Item> items = new LinkedHashSet<>();
		for (String entry : entries) {
			Item item = resolveItem(entry);
			if (item != null) {
				items.add(item);
			}
		}
		return items;
	}

	private static JsonArray toJsonArray(List<String> values) {
		JsonArray array = new JsonArray();
		for (String value : values) {
			array.add(value);
		}
		return array;
	}

	private static String normalizeBlockId(String value, String fallback) {
		String resolved = resolveBlockId(value);
		return resolved != null ? resolved : safe(resolveBlockId(fallback));
	}

	private static String normalizeBlockEntityId(String value, String fallback) {
		String resolved = resolveBlockEntityTypeId(value);
		return resolved != null ? resolved : safe(resolveBlockEntityTypeId(fallback));
	}

	private static String normalizeRecipeTypeId(String value, String fallback) {
		String resolved = resolveRecipeTypeId(value);
		return resolved != null ? resolved : safe(resolveRecipeTypeId(fallback));
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}

	private static String resolveBlockId(String value) {
		Identifier id = Identifier.tryParse(value == null ? "" : value.trim());
		if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
			return null;
		}
		return id.toString();
	}

	private static String resolveBlockEntityTypeId(String value) {
		Identifier id = Identifier.tryParse(value == null ? "" : value.trim());
		if (id == null || !BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(id)) {
			return null;
		}
		return id.toString();
	}

	private static String resolveRecipeTypeId(String value) {
		Identifier id = Identifier.tryParse(value == null ? "" : value.trim());
		if (id == null || !BuiltInRegistries.RECIPE_TYPE.containsKey(id)) {
			return null;
		}
		return id.toString();
	}

	private static String resolveItemId(String value) {
		Identifier id = Identifier.tryParse(value == null ? "" : value.trim());
		if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
			return null;
		}
		return id.toString();
	}

	private static RecipeType<?> resolveRecipeType(String value) {
		String id = resolveRecipeTypeId(value);
		return id == null ? null : BuiltInRegistries.RECIPE_TYPE.getValue(Identifier.tryParse(id));
	}

	private static BlockEntityType<?> resolveBlockEntityType(String value) {
		String id = resolveBlockEntityTypeId(value);
		return id == null ? null : BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(Identifier.tryParse(id));
	}

	private static Item resolveItem(String value) {
		String id = resolveItemId(value);
		return id == null ? null : BuiltInRegistries.ITEM.getValue(Identifier.tryParse(id));
	}

	private static String readString(JsonObject root, String key, String fallback) {
		if (root == null) {
			return fallback;
		}
		JsonElement element = root.get(key);
		return element instanceof JsonPrimitive primitive && primitive.isString() ? primitive.getAsString() : fallback;
	}

	private static double readDouble(JsonObject root, String key, double fallback) {
		if (root == null) {
			return fallback;
		}
		JsonElement element = root.get(key);
		return element instanceof JsonPrimitive primitive && primitive.isNumber() ? primitive.getAsDouble() : fallback;
	}

	private static boolean setString(JsonObject root, String key, String value) {
		String safeValue = value == null ? "" : value;
		JsonElement element = root.get(key);
		if (element instanceof JsonPrimitive primitive && primitive.isString() && safeValue.equals(primitive.getAsString())) {
			return false;
		}
		root.addProperty(key, safeValue);
		return true;
	}

	private static boolean setDouble(JsonObject root, String key, double value) {
		JsonElement element = root.get(key);
		if (element instanceof JsonPrimitive primitive && primitive.isNumber() && Double.compare(primitive.getAsDouble(), value) == 0) {
			return false;
		}
		root.addProperty(key, value);
		return true;
	}

	private static boolean setArray(JsonObject root, String key, List<String> values) {
		JsonArray replacement = toJsonArray(values);
		JsonElement element = root.get(key);
		if (element instanceof JsonArray existing && existing.equals(replacement)) {
			return false;
		}
		root.add(key, replacement);
		return true;
	}

	private static Path resolveJsonFile(Path directory, String fileName) {
		String normalized = fileName == null ? "" : fileName.trim();
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("Config file name must not be blank.");
		}
		if (!normalized.endsWith(".json")) {
			normalized = normalized + ".json";
		}
		return directory.resolve(normalized);
	}

	private record FurnaceRules(
		Map<RecipeType<?>, Set<Item>> additionalInputsByRecipeType,
		Map<BlockEntityType<?>, Set<Item>> additionalInputsByBlockEntityType
	) {
	}
}
