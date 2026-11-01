package nerd.amara;

import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Francetiers_tagger implements ClientModInitializer {
	public static final String MOD_ID = "francetiers_tagger";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static String web_url = "https://francetiers.fr/search_playerV2.php?pseudo=";
	public static CopyOnWriteArrayList<Display.TextDisplay> TextDisplayList = new CopyOnWriteArrayList<>();
	public static ConcurrentHashMap<Display.TextDisplay, Component> OriginalTextMap = new ConcurrentHashMap<>();

	@Override
	public void onInitializeClient() {
		Keybinds.registerKeybinds();
		AutoConfig.register(ModConfig.class, me.shedaniel.autoconfig.serializer.GsonConfigSerializer::new);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandManager.registerCommands(dispatcher);
		});

		UpdateChecker.checkAsync();

		HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
			if (Minecraft.getInstance().screen == null) {
				UpdateNotification.triggerPendingIfAny();
				UpdateNotification.render(drawContext, -1, -1);
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			int btnW = UpdateNotification.getWidth();
			int btnX = scaledWidth - btnW - 8;
			Button updateBtn = Button.builder(Component.empty(), b -> {
				if (UpdateNotification.isVisible()) {
					UpdateNotification.openModrinth();
				}
			}).bounds(btnX, 8, btnW, UpdateNotification.HEIGHT).build();
			updateBtn.setAlpha(0f);
			Screens.getButtons(screen).add(updateBtn);

			ScreenEvents.afterRender(screen).register((scr, drawContext, mouseX, mouseY, delta) -> {
				int curW = UpdateNotification.getWidth();
				updateBtn.setX(UpdateNotification.getX());
				updateBtn.setY(UpdateNotification.getY());
				updateBtn.setWidth(curW);
				updateBtn.visible = UpdateNotification.isVisible();
				UpdateNotification.render(drawContext, mouseX, mouseY);
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			TextDisplayList.clear();
			OriginalTextMap.clear();
			PlayerInfoCache.clear();
			TierSuffixCache.clear();
		});

		AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener((manager, data) -> {
			TierSuffixCache.clear();
			if (Minecraft.getInstance().level != null) {
				for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
					if (entity instanceof Player player) {
						String pseudo = player.getName().getString();
						PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
						if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
							String newSuffix = ShowedTier.showed_tier(cached.info, data);
							((TierModifier) player).setSuffix(newSuffix);
							TierSuffixCache.putSuffix(pseudo, newSuffix);
							for (Entity passenger : player.getPassengers()) {
								if (passenger instanceof Display.TextDisplay textDisplay) {
									updateTextDisplay(textDisplay);
								}
							}
						}
					}
				}
			}
			return InteractionResult.SUCCESS;
		});

		ClientEntityEvents.ENTITY_LOAD.register(((entity, clientWorld) -> {
			if (entity instanceof Player player) {
				String pseudo = player.getName().getString();
				RequestManager.fetchPlayerInfo(pseudo,
						(PlayerInfo info) -> {
							String suffix = ShowedTier.showed_tier(info);
							((TierModifier) player).setSuffix(suffix);
							TierSuffixCache.putSuffix(pseudo, suffix);
							for (Entity passenger : player.getPassengers()) {
								if (passenger instanceof Display.TextDisplay textDisplay) {
									TextDisplayList.addIfAbsent(textDisplay);
									updateTextDisplay(textDisplay);
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
			} else if (entity instanceof Display.TextDisplay textDisplay) {
				Entity vehicle = textDisplay.getVehicle();
				if (vehicle instanceof Player player) {
					TextDisplayList.addIfAbsent(textDisplay);
					updateTextDisplay(textDisplay);
				}
			}
		}));

		ClientEntityEvents.ENTITY_UNLOAD.register((entity, clientWorld) -> {
			if (entity instanceof Display.TextDisplay textDisplay) {
				TextDisplayList.remove(textDisplay);
				OriginalTextMap.remove(textDisplay);
			}
		});

		ClientTickEvents.END_WORLD_TICK.register(world -> {
			for (Display.TextDisplay textDisplay : TextDisplayList) {
				if (textDisplay.isRemoved()) {
					TextDisplayList.remove(textDisplay);
					OriginalTextMap.remove(textDisplay);
					continue;
				}
				if (!FontConstants.hasTierFont(textDisplay.getText())) {
					OriginalTextMap.put(textDisplay, textDisplay.getText());
					updateTextDisplay(textDisplay);
				}
			}

			ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
			if (!config.enabled || !config.showParticles) return;

			for (Player player : world.players()) {
				if (player.isInvisible()) continue;

				String pseudo = player.getName().getString();
				PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
				if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
					Tier tierObj = ShowedTier.getDisplayedTierObj(cached.info, config);
					if (tierObj != null && tierObj.tier != null) {
						if (tierObj.tier.equals("HT1") || tierObj.tier.equals("RHT1")) {
							if (Math.random() < 0.2) {
								world.addParticle(ParticleTypes.END_ROD, 
									player.getX() + (Math.random() - 0.5) * 1.5, 
									player.getY() + Math.random() * 2.0, 
									player.getZ() + (Math.random() - 0.5) * 1.5, 
									0.0D, 0.05D, 0.0D);
							}
						} else if (tierObj.tier.equals("LT1") || tierObj.tier.equals("RLT1")) {
							if (Math.random() < 0.1) {
								world.addParticle(ParticleTypes.ENCHANT, 
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

	public static void updateTextDisplay(Display.TextDisplay textDisplay) {
		if (textDisplay.isRemoved()) {
			TextDisplayList.remove(textDisplay);
			OriginalTextMap.remove(textDisplay);
			return;
		}
		Entity vehicle = textDisplay.getVehicle();
		if (vehicle instanceof Player player) {
			String pseudo = player.getName().getString();
			if (textDisplay.getText().getString().contains(pseudo)) {
				PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
				if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
					setTextInTextDisplay(cached.info, textDisplay, pseudo);
				}
			}
		}
	}

	public static Component setSuffixTextDisplay(Component original, String suffix, String pseudo){
		ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
		if (!config.enabled || !config.showInNametag || suffix == null || suffix.isEmpty()) {
			return original;
		}

		Component suffixText = Component.literal(suffix).withStyle(s -> s.withColor(ChatFormatting.WHITE).withFont(FontConstants.getActiveFont(config)));

		if (config.tierPosition == ModConfig.TierPosition.LEFT) {
			return Component.empty().append(suffixText.copy()).append(Component.literal(" ")).append(original.copy());
		} else {
			return Component.empty().append(original.copy()).append(Component.literal(" ")).append(suffixText);
		}
	}

	public static void setTextInTextDisplay(PlayerInfo info, Display.TextDisplay textDisplay, String pseudo){
		Component original = OriginalTextMap.get(textDisplay);
		if (original == null) {
			original = textDisplay.getText();
			if (!FontConstants.hasTierFont(original)) {
				OriginalTextMap.put(textDisplay, original);
			}
		}
		textDisplay.setText(setSuffixTextDisplay(original, ShowedTier.showed_tier(info), pseudo));
	}
}
