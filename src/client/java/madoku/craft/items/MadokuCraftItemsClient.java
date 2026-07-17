package madoku.craft.items;

import madoku.craft.items.item.system.MadokuItem;
import madoku.craft.items.network.ItemProfileSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MadokuCraftItemsClient implements ClientModInitializer {
	private static boolean configuredItemMetadataApplied;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (configuredItemMetadataApplied || client.level == null) {
				return;
			}
			configuredItemMetadataApplied = true;
			MadokuItem.applyConfiguredItemMetadata();
		});
		ClientPlayNetworking.registerGlobalReceiver(ItemProfileSyncPayload.TYPE, (payload, context) ->
			context.client().execute(() -> MadokuItem.applySynchronizedProfiles(payload.snapshot()))
		);
	}
}

