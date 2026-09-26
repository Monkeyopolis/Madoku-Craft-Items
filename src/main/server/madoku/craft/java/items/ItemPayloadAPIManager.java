package madoku.craft.java.items;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Public payload contracts used by the Madoku Items menu integration. */
public final class ItemPayloadAPIManager {
	public record OpenItemMenuPayload() implements CustomPacketPayload {
		public static final Type<OpenItemMenuPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("madoku-craft", "open_item_menu"));
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenItemMenuPayload> CODEC = StreamCodec.unit(new OpenItemMenuPayload());
		@Override public Type<OpenItemMenuPayload> type() { return TYPE; }
	}

	public record UpgradeItemPayload() implements CustomPacketPayload {
		public static final Type<UpgradeItemPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("madoku-craft", "upgrade_item"));
		public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeItemPayload> CODEC = StreamCodec.unit(new UpgradeItemPayload());
		@Override public Type<UpgradeItemPayload> type() { return TYPE; }
	}

	private ItemPayloadAPIManager() { }
}
