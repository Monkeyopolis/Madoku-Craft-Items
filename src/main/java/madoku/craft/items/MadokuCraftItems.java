package madoku.craft.items;

import madoku.craft.config.StaticJsonSystem;
import madoku.craft.debug.MadokuDebug;
import madoku.craft.items.item.system.MadokuItem;
import madoku.craft.items.itemstack.system.MadokuItemStack;
import madoku.craft.items.rarity.MadokuRarity;
import madoku.craft.items.smelting.system.MadokuSmeltingManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MadokuCraftItems implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-items";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		StaticJsonSystem.initialize();
		MadokuSmeltingManager.initialize();
		MadokuDebug.initialize();
		MadokuItem.initialize();
		MadokuRarity.initialize();
		MadokuItemStack.initialize();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MadokuItemStack.reset();
			MadokuItemStack.loadPersistedData(server);
			MadokuItem.onServerStarted();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuItemStack.savePersistedData(server);
			MadokuItemStack.reset();
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			MadokuItemStack.autosavePersistedData(server);
		});
		LOGGER.info("Initialized {} item, stack, and rarity systems", MOD_ID);
	}
}
