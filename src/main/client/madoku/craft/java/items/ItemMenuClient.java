package madoku.craft.java.items;

import madoku.craft.java.core.menu.MenuAPIManager;
import madoku.craft.java.core.menu.MenuEntry;
import madoku.craft.java.items.ItemPayloadAPIManager.OpenItemMenuPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;

/** Registers the Items route in Core's main menu. */
public final class ItemMenuClient {
	private static boolean initialized;

	private ItemMenuClient() { }

	public static void initialize() {
		if (initialized) return;
		MenuScreens.register(ItemMenuManager.ITEM_MENU, ItemMenuScreen::new);
		MenuAPIManager.registerEntry(new MenuEntry(
			"items",
			"menu.madoku-craft.items",
			texture("madoku-menu/main-menu/items-button.png"),
			texture("madoku-menu/main-menu/items-button-highlighted.png"),
			40,
			client -> {
				if (client != null && client.player != null) ClientPlayNetworking.send(new OpenItemMenuPayload());
			}
		));
		initialized = true;
	}

	private static Identifier texture(String path) {
		return Identifier.fromNamespaceAndPath("madoku-craft", "textures/" + path);
	}
}
