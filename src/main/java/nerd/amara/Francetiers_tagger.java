package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Francetiers_tagger implements ClientModInitializer {
	public static final String MOD_ID = "francetiers_tagger";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static String web_url="https://old.francetiers.fr/search_player.php?pseudo=";;

	@Override
	public void onInitializeClient() {
		Keybinds.RedgisterKeybinds();
		ConfigManager.load();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandManager.registerCommands(dispatcher);
		});
		String lol = "\u0000";
		//PlayerInfo info_ = Http.getJson("https://tierlistmc.fr/search_player.php?pseudo="+"amara1000", PlayerInfo.class);
		//if (info_ != null) {
		//	System.out.println(ShowedTier.showed_tier(info_));
		//}
		ClientEntityEvents.ENTITY_LOAD.register(((entity, clientWorld) ->{
			if (entity instanceof PlayerEntity){
				if (((TierModifier) entity).getSuffix()==null){
					new Thread(() -> {
						//System.out.println(entity.getName().getString());
						PlayerInfo info = Http.getJson(web_url+entity.getName().getString(), PlayerInfo.class);
						if (info != null) {
							((TierModifier)entity).setSuffix(ShowedTier.showed_tier(info));
						}
					}).start();
				}
				//((TierModifier)entity).setSuffix(" (lol)");
			}
		}));

		LOGGER.info("france tiers tagger initialized");
	}
}