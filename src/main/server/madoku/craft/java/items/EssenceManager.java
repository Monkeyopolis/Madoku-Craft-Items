package madoku.craft.java.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;

/** Compatibility facade for the shared Core Essence item. */
public final class EssenceManager {
	public static final Item ESSENCE = madoku.craft.java.core.essence.EssenceManager.ESSENCE;

	private EssenceManager() {
	}

	public static void initialize() {
		madoku.craft.java.core.essence.EssenceManager.initialize();
	}

	static ServerPlayer resolveDirectPlayer(DamageSource damageSource) {
		return madoku.craft.java.core.essence.EssenceManager.resolveDirectPlayer(damageSource);
	}
}
