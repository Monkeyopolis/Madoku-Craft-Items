package madoku.craft.java.items;

import java.util.List;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Food-category profiles and predicates for configurable item foods. */
public final class CategoriesFoodManager {
	private CategoriesFoodManager() { }

	public static boolean matches(Item item, Set<Item> configuredItems) {
		return item != null && configuredItems != null && configuredItems.contains(item);
	}

	public static boolean matches(ItemStack stack, Set<Item> configuredItems) {
		return stack != null && !stack.isEmpty() && matches(stack.getItem(), configuredItems);
	}

	public record StatusEffect(String effect, int duration, float chance) { }

	public record FoodProfile(int nutrition, List<StatusEffect> statusEffects) {
		public FoodProfile {
			statusEffects = statusEffects == null ? List.of() : List.copyOf(statusEffects);
		}
	}
}
