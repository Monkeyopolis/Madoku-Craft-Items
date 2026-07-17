package madoku.craft.items.network;

import madoku.craft.items.item.system.MadokuItem;
import madoku.craft.api.sync.SyncPlayerManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class ItemProfileSync {
	private static boolean initialized = false;

	private ItemProfileSync() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		PayloadTypeRegistry.clientboundPlay().register(ItemProfileSyncPayload.TYPE, ItemProfileSyncPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SyncPlayerManager.send(
				handler.player,
				new ItemProfileSyncPayload(MadokuItem.createClientSyncSnapshot())
			);
		});
		initialized = true;
	}
}
