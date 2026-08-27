package nerd.amara;

import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

public class Francetiers_tagger implements ClientModInitializer {
	public static final String MOD_ID = "francetiers_tagger";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static String web_url="https://francetiers.fr/search_playerV2.php?pseudo=";
	public static ArrayList<DisplayEntity.TextDisplayEntity> TextDisplayList = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		Keybinds.registerKeybinds();
		me.shedaniel.autoconfig.AutoConfig.register(ModConfig.class, me.shedaniel.autoconfig.serializer.GsonConfigSerializer::new);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandManager.registerCommands(dispatcher);
		});

		AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener((manager, data) -> {
			if (net.minecraft.client.MinecraftClient.getInstance().world != null) {
				for (Entity entity : net.minecraft.client.MinecraftClient.getInstance().world.getEntities()) {
					if (entity instanceof PlayerEntity player) {
						String pseudo = player.getName().getString();
						PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
						if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
							((TierModifier) player).setSuffix(ShowedTier.showed_tier(cached.info));
							for (Entity passenger : player.getPassengerList()) {
								if (passenger instanceof DisplayEntity.TextDisplayEntity textDisplay) {
									if (textDisplay.getText().getString().contains(pseudo)) {
										setTextInTextDisplay(cached.info, textDisplay, pseudo);
									}
								}
							}
						}
					}
				}
			}
			return net.minecraft.util.ActionResult.SUCCESS;
		});

		ClientEntityEvents.ENTITY_LOAD.register(((entity, clientWorld) -> {
			if (entity instanceof PlayerEntity) {
				if (((TierModifier) entity).getSuffix() == null) {
					String pseudo = entity.getName().getString();
					RequestManager.fetchPlayerInfo(pseudo,
							(PlayerInfo info) -> {
								((TierModifier) entity).setSuffix(ShowedTier.showed_tier(info));
								for (Entity passenger : entity.getPassengerList()) {
									if (passenger instanceof DisplayEntity.TextDisplayEntity textDisplay) {
										TextDisplayList.add(textDisplay);
										if (textDisplay.getText().getString().contains(pseudo)) {
											setTextInTextDisplay(info, textDisplay, pseudo);
										}
									}
								}
							},
							(Http.Status status) -> {
								if (status == Http.Status.NOT_FOUND) {
									LOGGER.debug("{} n'est pas classé sur FranceTiers", pseudo);
								} else {
									LOGGER.warn("Impossible de récupérer le tier de {} ({})", pseudo, status);
								}
							});
				}
			} else if (entity instanceof DisplayEntity.TextDisplayEntity textDisplay) {
				Entity vehicle = textDisplay.getVehicle();
				if (vehicle instanceof PlayerEntity player) {
					String pseudo = player.getName().getString();
					if (textDisplay.getText().getString().contains(pseudo)) {
						RequestManager.fetchPlayerInfo(pseudo,
								(PlayerInfo info) -> setTextInTextDisplay(info, textDisplay, pseudo),
								(Http.Status status) -> {
									if (status == Http.Status.NOT_FOUND) {
										LOGGER.debug("{} n'est pas classé sur FranceTiers", pseudo);
									} else {
										LOGGER.warn("Impossible de récupérer le tier de {} ({})", pseudo, status);
									}
								});
					}
				}
			}
		}));

		ClientTickEvents.END_WORLD_TICK.register((world)->{
			for (DisplayEntity.TextDisplayEntity textDisplay : TextDisplayList){
				if (!textDisplay.getText().toString().contains("frtl")){
					Entity vehicle = textDisplay.getVehicle();
					if (vehicle instanceof PlayerEntity player) {
						String pseudo = player.getName().getString();
						if (textDisplay.getText().getString().contains(pseudo)) {
							PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
							if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
								setTextInTextDisplay(cached.info, textDisplay, pseudo);
							}
						}
					}
				}
			}
		});

		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_WORLD_TICK.register(clientWorld -> {
			ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
			if (!config.showParticles) return;

			for (PlayerEntity player : clientWorld.getPlayers()) {
				if (player.isInvisible()) continue;

				String pseudo = player.getName().getString();
				PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
				if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
					nerd.amara.tiers.Tier tierObj = ShowedTier.getDisplayedTierObj(cached.info);
					if (tierObj != null && tierObj.tier != null) {
						if (tierObj.tier.equals("HT1") || tierObj.tier.equals("RHT1")) {
							if (Math.random() < 0.2) {
								clientWorld.addParticleClient(net.minecraft.particle.ParticleTypes.END_ROD, 
									player.getX() + (Math.random() - 0.5) * 1.5, 
									player.getY() + Math.random() * 2.0, 
									player.getZ() + (Math.random() - 0.5) * 1.5, 
									0.0D, 0.05D, 0.0D);
							}
						} else if (tierObj.tier.equals("LT1") || tierObj.tier.equals("RLT1")) {
							if (Math.random() < 0.1) {
								clientWorld.addParticleClient(net.minecraft.particle.ParticleTypes.ENCHANT, 
									player.getX() + (Math.random() - 0.5) * 1.2, 
									player.getY() + Math.random() * 2.0, 
									player.getZ() + (Math.random() - 0.5) * 1.2, 
									0.0D, 0.1D, 0.0D);
							}
						}
					}
				}
			}
		});

		LOGGER.info("france tiers tagger initialized");
	}

	private static final StyleSpriteSource.Font FRTL_FONT = new StyleSpriteSource.Font(Identifier.of("frtl", "lol"));
	private static final StyleSpriteSource.Font SMALL_FRTL_FONT = new StyleSpriteSource.Font(Identifier.of("frtl", "lol_small"));

	public static Text setSuffixTextDisplay(Text original, String suffix, String pseudo){
		ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
		if (config.showInNametag && suffix != null) {
			if (!Objects.equals(suffix, "")) {
				Text suffixText;
				if (config.smallIcons) {
					suffixText = Text.literal(suffix).styled(s -> s.withColor(Formatting.WHITE).withFont(SMALL_FRTL_FONT));
				} else {
					suffixText = Text.literal(suffix).styled(s -> s.withColor(Formatting.WHITE).withFont(FRTL_FONT));
				}

				if (config.tierPosition == ModConfig.TierPosition.LEFT) {
					return Text.empty().append(suffixText.copy()).append(Text.literal(" ")).append(original.copy());
				} else {
					return Text.empty().append(original.copy()).append(Text.literal(" ")).append(suffixText);
				}
			}
		}
		if (original.getString().contains(pseudo)) {
			return Text.literal(pseudo);
		}
		return original;
	}
	public static void setTextInTextDisplay(PlayerInfo info, DisplayEntity.TextDisplayEntity textDisplay, String pseudo){
		textDisplay.setText(setSuffixTextDisplay(textDisplay.getText(),ShowedTier.showed_tier(info),pseudo));
	}
}