package madoku.craft.items;

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
			ItemsCategoriesManager.applyConfiguredItemMetadata();
		});
		ClientPlayNetworking.registerGlobalReceiver(ItemsPayloadManager.TYPE, (payload, context) ->
			context.client().execute(() -> ItemsCategoriesManager.applySynchronizedProfiles(payload.snapshot()))
		);
	}
}
