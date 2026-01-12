package madoku.craft.stacks.gamerule;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.rule.GameRules;

public final class KeepInventoryEnforcer {
	private static boolean initialized;

	private KeepInventoryEnforcer() {
	}

	public static void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		ServerLifecycleEvents.SERVER_STARTED.register(KeepInventoryEnforcer::enforce);
	}

	private static void enforce(MinecraftServer server) {
		if (server == null) {
			return;
		}

		GameRules rules = server.getOverworld().getGameRules();
		if (Boolean.TRUE.equals(rules.getValue(GameRules.KEEP_INVENTORY))) {
			rules.setValue(GameRules.KEEP_INVENTORY, false, server);
		}
	}
}
