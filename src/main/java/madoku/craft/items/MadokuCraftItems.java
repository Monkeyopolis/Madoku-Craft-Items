package madoku.craft.items;

import madoku.craft.api.rarity.MadokuRarityManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MadokuCraftItems implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-items";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MadokuItemsManager.initialize();
		MadokuRarityManager.setRarityItemPredicate(ItemsCategoriesManager::isRarityCategoryItem);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			// The API resets shared runtime state before each server; restore its rarity runtime.
			MadokuRarityManager.initialize();
			MadokuItemsManager.reset();
			MadokuItemsManager.onServerStarted(server);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuItemsManager.reset();
		});
		LOGGER.info("Initialized {} item and stack systems", MOD_ID);
	}
}
