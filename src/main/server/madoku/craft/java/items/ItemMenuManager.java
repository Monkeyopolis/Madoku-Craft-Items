package madoku.craft.java.items;

import madoku.craft.java.items.ItemPayloadAPIManager.OpenItemMenuPayload;
import madoku.craft.java.items.ItemPayloadAPIManager.UpgradeItemPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Registers and opens the dedicated item-level upgrade menu. */
public final class ItemMenuManager {
	public static final MenuType<ItemMenu> ITEM_MENU = Registry.register(
		BuiltInRegistries.MENU,
		Identifier.fromNamespaceAndPath("madoku-craft", "item_menu"),
		new MenuType<>(ItemMenu::new, FeatureFlags.VANILLA_SET)
	);

	private static boolean initialized;

	private ItemMenuManager() { }

	public static void initialize() {
		if (initialized) return;
		PayloadTypeRegistry.serverboundPlay().register(OpenItemMenuPayload.TYPE, OpenItemMenuPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(UpgradeItemPayload.TYPE, UpgradeItemPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenItemMenuPayload.TYPE, (payload, context) ->
			context.player().openMenu(new SimpleMenuProvider(
				(containerId, inventory, player) -> new ItemMenu(containerId, inventory),
				Component.translatable("menu.madoku-craft.items.title")
			))
		);
		ServerPlayNetworking.registerGlobalReceiver(UpgradeItemPayload.TYPE, (payload, context) -> {
			if (context.player().containerMenu instanceof ItemMenu itemMenu) itemMenu.upgrade(context.player());
		});
		initialized = true;
	}
}
