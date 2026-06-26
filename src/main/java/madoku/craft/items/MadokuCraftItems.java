package madoku.craft.items;

import madoku.craft.clock.MadokuTicks;
import madoku.craft.config.JsonManagerSystem;
import madoku.craft.debug.MadokuDebug;
import madoku.craft.items.item.system.MadokuItem;
import madoku.craft.items.itemstack.system.MadokuItemStack;
import madoku.craft.items.network.ItemProfileSync;
import madoku.craft.items.rarity.MadokuRarity;
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
		JsonManagerSystem.initialize();
		MadokuDebug.initialize();
		MadokuItem.initialize();
		ItemProfileSync.initialize();
		MadokuRarity.initialize();
		MadokuItemStack.initialize();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MadokuTicks.reset();
			MadokuItem.reset();
			MadokuItemStack.reset();
			MadokuItemStack.loadPersistedData(server);
			MadokuItem.onServerStarted(server);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuItemStack.savePersistedData(server);
			MadokuItem.reset();
			MadokuItemStack.reset();
			MadokuTicks.reset();
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			MadokuTicks.tickGameplay();
			MadokuItem.onServerTick(server);
			MadokuItemStack.autosavePersistedData(server);
		});
		LOGGER.info("Initialized {} item, stack, and rarity systems", MOD_ID);
	}
}
