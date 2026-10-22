package madoku.craft.java.items;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** Registers Items-owned custom item types. */
public final class CustomItemsManager {
	private static final Identifier SWEET_BERRY_COOKIE_ID = Identifier.fromNamespaceAndPath(
		"madoku-craft",
		"sweet-berry-cookie"
	);
	private static final Identifier COOKED_TROPICAL_FISH_ID = Identifier.fromNamespaceAndPath(
		"madoku-craft",
		"cooked-tropical-fish"
	);

	public static final Item SWEET_BERRY_COOKIE = register(SWEET_BERRY_COOKIE_ID);
	public static final Item COOKED_TROPICAL_FISH = register(COOKED_TROPICAL_FISH_ID);
	private static boolean initialized;

	private CustomItemsManager() { }

	public static void initialize() {
		if (initialized) return;
		initialized = true;
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
			output.accept(SWEET_BERRY_COOKIE);
			output.accept(COOKED_TROPICAL_FISH);
		});
	}

	private static Item register(Identifier itemId) {
		return Registry.register(
			BuiltInRegistries.ITEM,
			itemId,
			new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, itemId)))
		);
	}
}
