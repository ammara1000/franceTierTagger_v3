package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Francetiers_tagger implements ClientModInitializer {
	public static final String MOD_ID = "francetiers_tagger";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static String web_url="https://francetiers.fr/search_playerV2.php?pseudo=";

	@Override
	public void onInitializeClient() {
		Keybinds.registerKeybinds();
		ConfigManager.load();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandManager.registerCommands(dispatcher);
		});

		ClientEntityEvents.ENTITY_LOAD.register(((entity, clientWorld) -> {
			if (entity instanceof PlayerEntity) {
				if (((TierModifier) entity).getSuffix() == null) {
					String pseudo = entity.getName().getString();
					RequestManager.fetchPlayerInfo(pseudo,
							(PlayerInfo info) -> ((TierModifier) entity).setSuffix(ShowedTier.showed_tier(info)),
							(Http.Status status) -> LOGGER.warn("Impossible de récupérer le tier de {} ({})", pseudo, status));
				}
			}
		}));

		LOGGER.info("france tiers tagger initialized");
	}
}