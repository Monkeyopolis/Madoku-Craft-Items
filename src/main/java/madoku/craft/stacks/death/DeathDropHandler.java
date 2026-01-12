package madoku.craft.stacks.death;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import madoku.craft.API.system.MadokuDeathSystem;
import madoku.craft.API.system.MadokuDeathSystem.PlayerDeathContext;
import madoku.craft.API.system.MadokuSavingSystem;
import madoku.craft.API.system.MadokuSavingSystem.MadokuData;
import madoku.craft.stacks.config.StackingConfig;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

public final class DeathDropHandler {
	private static final String DATA_ID = "madoku-craft-stacks-death-drops";
	private static final String KEPT_STACKS_KEY = "keptStacks";
	private static final String ENTRY_SLOT_KEY = "slot";
	private static final String ENTRY_STACK_KEY = "stack";

	private static final Map<UUID, List<KeptStack>> KEPT_STACKS = new ConcurrentHashMap<>();
	private static MadokuData data;
	private static MinecraftServer dataServer;

	private DeathDropHandler() {
	}

	public static void init() {
		MadokuDeathSystem.init();
		MadokuDeathSystem.registerRespawn(DeathDropHandler::onRespawn);
	}

	public static boolean handleDrop(PlayerInventory inventory) {
		if (!StackingConfig.isDeathDropEnabled()) {
			return false;
		}

		PlayerEntity player = inventory.player;
		if (player == null || player.getEntityWorld().isClient()) {
			return false;
		}
		if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) {
			return false;
		}
		MinecraftServer server = serverWorld.getServer();
		if (server == null) {
			return false;
		}
		ensureData(server);

		List<Integer> slots = collectStacks(inventory);
		if (slots.isEmpty()) {
			return true;
		}

		int dropCount = calculateDropCount(slots.size(), StackingConfig.getDeathDropStackPercent());
		shuffleSlots(slots, player.getRandom());

		List<KeptStack> kept = new ArrayList<>(slots.size() - dropCount);
		for (int i = 0; i < slots.size(); i++) {
			int slot = slots.get(i);
			ItemStack stack = inventory.getStack(slot);
			if (stack.isEmpty()) {
				continue;
			}

			if (i < dropCount) {
				DeathDropTag.mark(stack);
				ItemEntity dropped = player.dropItem(stack, true, false);
				if (dropped != null) {
					DeathDropTag.mark(dropped.getStack());
				}
			} else {
				kept.add(new KeptStack(slot, stack.copy()));
			}

			inventory.setStack(slot, ItemStack.EMPTY);
		}

		if (kept.isEmpty()) {
			KEPT_STACKS.remove(player.getUuid());
		} else {
			KEPT_STACKS.put(player.getUuid(), kept);
		}

		saveData(server);
		return true;
	}

	private static void onRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive, PlayerDeathContext context) {
		if (newPlayer == null || !(newPlayer.getEntityWorld() instanceof ServerWorld serverWorld)) {
			return;
		}
		MinecraftServer server = serverWorld.getServer();
		if (server == null) {
			return;
		}
		ensureData(server);
		List<KeptStack> kept = KEPT_STACKS.remove(newPlayer.getUuid());
		if (kept == null || kept.isEmpty()) {
			return;
		}

		PlayerInventory inventory = newPlayer.getInventory();
		for (KeptStack entry : kept) {
			ItemStack stack = entry.stack();
			if (stack.isEmpty()) {
				continue;
			}

			int slot = entry.slot();
			if (slot >= 0 && slot < inventory.size()) {
				ItemStack existing = inventory.getStack(slot);
				if (existing.isEmpty()) {
					inventory.setStack(slot, stack);
					continue;
				}
			}

			if (!inventory.insertStack(stack)) {
				DeathDropTag.mark(stack);
				ItemEntity dropped = newPlayer.dropItem(stack, true, false);
				if (dropped != null) {
					DeathDropTag.mark(dropped.getStack());
				}
			}
		}

		inventory.markDirty();
		saveData(server);
	}

	private static List<Integer> collectStacks(PlayerInventory inventory) {
		int size = inventory.size();
		List<Integer> slots = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			if (!inventory.getStack(i).isEmpty()) {
				slots.add(i);
			}
		}
		return slots;
	}

	private static void ensureData(MinecraftServer server) {
		if (data == null || dataServer != server) {
			dataServer = server;
			JsonObject defaults = new JsonObject();
			defaults.add(KEPT_STACKS_KEY, new JsonObject());
			data = MadokuSavingSystem.loadForWorld(server, DATA_ID, defaults);
			loadFromData(server, data);
		}
	}

	private static void loadFromData(MinecraftServer server, MadokuData data) {
		KEPT_STACKS.clear();
		if (data == null) {
			return;
		}

		JsonObject root = data.getRoot();
		if (root == null || !root.has(KEPT_STACKS_KEY) || !root.get(KEPT_STACKS_KEY).isJsonObject()) {
			return;
		}

		JsonObject keptRoot = root.getAsJsonObject(KEPT_STACKS_KEY);
		RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager());
		for (Map.Entry<String, JsonElement> entry : keptRoot.entrySet()) {
			UUID playerId;
			try {
				playerId = UUID.fromString(entry.getKey());
			} catch (IllegalArgumentException ignored) {
				continue;
			}

			if (!entry.getValue().isJsonArray()) {
				continue;
			}

			JsonArray stacks = entry.getValue().getAsJsonArray();
			List<KeptStack> kept = new ArrayList<>(stacks.size());
			for (JsonElement element : stacks) {
				if (!element.isJsonObject()) {
					continue;
				}

				JsonObject stackEntry = element.getAsJsonObject();
				if (!stackEntry.has(ENTRY_SLOT_KEY) || !stackEntry.has(ENTRY_STACK_KEY)) {
					continue;
				}

				int slot = stackEntry.get(ENTRY_SLOT_KEY).getAsInt();
				JsonElement stackElement = stackEntry.get(ENTRY_STACK_KEY);
				DataResult<ItemStack> parsed = ItemStack.CODEC.parse(ops, stackElement);
				ItemStack stack = parsed.result().orElse(ItemStack.EMPTY);
				if (!stack.isEmpty()) {
					kept.add(new KeptStack(slot, stack));
				}
			}

			if (!kept.isEmpty()) {
				KEPT_STACKS.put(playerId, kept);
			}
		}
	}

	private static void saveData(MinecraftServer server) {
		if (data == null || dataServer != server) {
			return;
		}

		JsonObject root = data.getRoot();
		if (root == null) {
			return;
		}

		JsonObject keptRoot = new JsonObject();
		RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager());
		for (Map.Entry<UUID, List<KeptStack>> entry : KEPT_STACKS.entrySet()) {
			List<KeptStack> kept = entry.getValue();
			if (kept == null || kept.isEmpty()) {
				continue;
			}

			JsonArray stacks = new JsonArray();
			for (KeptStack keptStack : kept) {
				ItemStack stack = keptStack.stack();
				if (stack.isEmpty()) {
					continue;
				}

				DataResult<JsonElement> encoded = ItemStack.CODEC.encodeStart(ops, stack);
				JsonElement stackElement = encoded.result().orElse(null);
				if (stackElement == null) {
					continue;
				}

				JsonObject stackEntry = new JsonObject();
				stackEntry.addProperty(ENTRY_SLOT_KEY, keptStack.slot());
				stackEntry.add(ENTRY_STACK_KEY, stackElement);
				stacks.add(stackEntry);
			}

			if (!stacks.isEmpty()) {
				keptRoot.add(entry.getKey().toString(), stacks);
			}
		}

		root.add(KEPT_STACKS_KEY, keptRoot);
		data.save();
	}

	private static int calculateDropCount(int stackCount, int percent) {
		float ratio = percent / 100.0f;
		int dropCount = Math.round(stackCount * ratio);
		if (dropCount < 0) {
			return 0;
		}
		return Math.min(dropCount, stackCount);
	}

	private static void shuffleSlots(List<Integer> slots, Random random) {
		for (int i = slots.size() - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			if (i != j) {
				int tmp = slots.get(i);
				slots.set(i, slots.get(j));
				slots.set(j, tmp);
			}
		}
	}

	private record KeptStack(int slot, ItemStack stack) {
	}
}
