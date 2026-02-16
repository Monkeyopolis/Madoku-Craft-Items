package madoku.craft.stacks.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import madoku.craft.API.system.MadokuJSONSystem;
import madoku.craft.API.system.MadokuJSONSystem.ManagedJSON;
import madoku.craft.stacks.MadokuCraftStacks;

public final class StackingConfig {
	public static final int DEFAULT_STACK_LIMIT = 128;
	public static final int MAX_STACK_CAP = 999;
	private static final String ENABLE_FEATURE_KEY = "enableFeature";
	private static final String STACK_LIMIT_KEY = "customStackAmount";
	private static final String LEGACY_STACK_LIMIT_KEY = "maxStackSize";
	private static final String DEATH_DROP_ENABLED_KEY = "deathDropEnabled";
	private static final String DEATH_DROP_STACK_PERCENT_KEY = "deathDropStackPercent";
	private static final String DEATH_DROP_DESPAWN_MINUTES_KEY = "deathDropDespawnMinutes";
	private static final String JSON_FOLDER_ID = "Stacks";
	private static final String JSON_FILE_ID = "madoku_craft_stacks";

	private static ManagedJSON feature;
	private static boolean enabled = true;
	private static int stackLimit = DEFAULT_STACK_LIMIT;
	private static boolean deathDropEnabled = true;
	private static int deathDropStackPercent = 50;
	private static int deathDropDespawnMinutes = 15;

	private StackingConfig() {
	}

	public static void init() {
		JsonObject defaults = new JsonObject();
		defaults.addProperty(ENABLE_FEATURE_KEY, true);
		defaults.addProperty(STACK_LIMIT_KEY, DEFAULT_STACK_LIMIT);
		defaults.addProperty(DEATH_DROP_ENABLED_KEY, true);
		defaults.addProperty(DEATH_DROP_STACK_PERCENT_KEY, 50);
		defaults.addProperty(DEATH_DROP_DESPAWN_MINUTES_KEY, 15);

		feature = MadokuJSONSystem.load(JSON_FOLDER_ID, JSON_FILE_ID, defaults);
		JsonObject root = feature.getRoot();
		enabled = readEnabled(root);

		int configured = readStackLimit(root);
		stackLimit = clamp(configured);

		boolean updated = false;
		if (!hasBoolean(root, ENABLE_FEATURE_KEY)) {
			root.addProperty(ENABLE_FEATURE_KEY, enabled);
			updated = true;
		}

		if (stackLimit != configured || !hasNumber(root, STACK_LIMIT_KEY)) {
			root.addProperty(STACK_LIMIT_KEY, stackLimit);
			updated = true;
		}

		if (root != null && root.has(LEGACY_STACK_LIMIT_KEY)) {
			root.remove(LEGACY_STACK_LIMIT_KEY);
			updated = true;
		}

		deathDropEnabled = readDeathDropEnabled(root);
		int configuredDropPercent = readDeathDropPercent(root);
		deathDropStackPercent = clampPercent(configuredDropPercent);
		int configuredDespawnMinutes = readDeathDropDespawnMinutes(root);
		deathDropDespawnMinutes = clampMinutes(configuredDespawnMinutes);

		if (!hasBoolean(root, DEATH_DROP_ENABLED_KEY)) {
			root.addProperty(DEATH_DROP_ENABLED_KEY, deathDropEnabled);
			updated = true;
		}

		if (deathDropStackPercent != configuredDropPercent || !hasNumber(root, DEATH_DROP_STACK_PERCENT_KEY)) {
			root.addProperty(DEATH_DROP_STACK_PERCENT_KEY, deathDropStackPercent);
			updated = true;
		}

		if (deathDropDespawnMinutes != configuredDespawnMinutes || !hasNumber(root, DEATH_DROP_DESPAWN_MINUTES_KEY)) {
			root.addProperty(DEATH_DROP_DESPAWN_MINUTES_KEY, deathDropDespawnMinutes);
			updated = true;
		}

		if (updated) {
			feature.save();
		}

		MadokuCraftStacks.LOGGER.info("Stack limit feature is {}", enabled ? "enabled" : "disabled");
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static int getStackLimit() {
		return stackLimit;
	}

	public static boolean isDeathDropEnabled() {
		return deathDropEnabled;
	}

	public static int getDeathDropStackPercent() {
		return deathDropStackPercent;
	}

	public static int getDeathDropDespawnMinutes() {
		return deathDropDespawnMinutes;
	}

	private static int readStackLimit(JsonObject root) {
		if (root == null) {
			return DEFAULT_STACK_LIMIT;
		}

		JsonElement value = root.get(STACK_LIMIT_KEY);
		if (value == null) {
			value = root.get(LEGACY_STACK_LIMIT_KEY);
		}

		if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
			return value.getAsInt();
		}

		return DEFAULT_STACK_LIMIT;
	}

	private static boolean readEnabled(JsonObject root) {
		if (root == null) {
			return true;
		}

		JsonElement value = root.get(ENABLE_FEATURE_KEY);
		if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
			return value.getAsBoolean();
		}

		return true;
	}

	private static boolean readDeathDropEnabled(JsonObject root) {
		if (root == null) {
			return true;
		}

		JsonElement value = root.get(DEATH_DROP_ENABLED_KEY);
		if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
			return value.getAsBoolean();
		}

		return true;
	}

	private static int readDeathDropPercent(JsonObject root) {
		if (root == null) {
			return 50;
		}

		JsonElement value = root.get(DEATH_DROP_STACK_PERCENT_KEY);
		if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
			return value.getAsInt();
		}

		return 50;
	}

	private static int readDeathDropDespawnMinutes(JsonObject root) {
		if (root == null) {
			return 15;
		}

		JsonElement value = root.get(DEATH_DROP_DESPAWN_MINUTES_KEY);
		if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
			return value.getAsInt();
		}

		return 15;
	}

	private static boolean hasBoolean(JsonObject root, String key) {
		if (root == null) {
			return false;
		}
		JsonElement value = root.get(key);
		return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
	}

	private static boolean hasNumber(JsonObject root, String key) {
		if (root == null) {
			return false;
		}
		JsonElement value = root.get(key);
		return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
	}

	private static int clamp(int value) {
		if (value < 1) {
			return 1;
		}
		if (value > MAX_STACK_CAP) {
			return MAX_STACK_CAP;
		}
		return value;
	}

	private static int clampPercent(int value) {
		if (value < 0) {
			return 0;
		}
		if (value > 100) {
			return 100;
		}
		return value;
	}

	private static int clampMinutes(int value) {
		if (value < 1) {
			return 1;
		}
		if (value > 60) {
			return 60;
		}
		return value;
	}
}
