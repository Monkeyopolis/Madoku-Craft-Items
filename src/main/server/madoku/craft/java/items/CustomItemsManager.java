package madoku.craft.java.items;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.food.FoodProperties;

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
	private static final Identifier BOWL_OF_MELON_ID = Identifier.fromNamespaceAndPath(
		"madoku-craft",
		"bowl-of-melon"
	);

	public static final Item SWEET_BERRY_COOKIE = register(SWEET_BERRY_COOKIE_ID);
	public static final Item COOKED_TROPICAL_FISH = register(COOKED_TROPICAL_FISH_ID);
	public static final Item BOWL_OF_MELON = register(
		BOWL_OF_MELON_ID,
		new Item.Properties()
			.food(new FoodProperties(6, 0.0F, false))
			.usingConvertsTo(Items.BOWL)
	);
	private static boolean initialized;

	private CustomItemsManager() { }

	public static void initialize() {
		if (initialized) return;
		initialized = true;
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
			output.accept(SWEET_BERRY_COOKIE);
			output.accept(COOKED_TROPICAL_FISH);
			output.accept(BOWL_OF_MELON);
		});
	}

	private static Item register(Identifier itemId) {
		return register(itemId, new Item.Properties());
	}

	private static Item register(Identifier itemId, Item.Properties properties) {
		return Registry.register(
			BuiltInRegistries.ITEM,
			itemId,
			new Item(properties.setId(ResourceKey.create(Registries.ITEM, itemId)))
		);
	}
}
