package nerd.amara;

import nerd.amara.tiers.GamemodeType;
import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;


import static nerd.amara.Francetiers_tagger.setTextInTextDisplay;

import com.mojang.blaze3d.platform.InputConstants;

public class Keybinds {
    private static KeyMapping change_gamemode;
    private static KeyMapping open_tier_screen;
    private static KeyMapping auto_detect_kit;

    public static void registerKeybinds() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.parse("francetierstagger"));

        change_gamemode = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mod.change_gamemode",
                InputConstants.Type.KEYBOARD,
                InputConstants.UNKNOWN.getValue(),
                category
        ));
        open_tier_screen = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mod.open_tier_screen",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K,
                category
        ));
        auto_detect_kit = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mod.auto_detect_kit",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_Y,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (auto_detect_kit.consumeClick()) {
                InventoryChecker.checkInventory(client, true);
            }

            while (change_gamemode.consumeClick()) {
                me.shedaniel.autoconfig.ConfigHolder<ModConfig> holder = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ModConfig.class);
                ModConfig config = holder.getConfig();
                GamemodeType[] values = GamemodeType.values();
                int nextIndex = (config.gamemode.ordinal() + 1) % values.length;
                config.gamemode = values[nextIndex];
                holder.save();
                refreshAllPlayerTags();
                if (client.player != null) {
                    client.player.sendOverlayMessage(Component.translatable("francetiers_tagger.message.gamemode_selected", Component.literal(config.gamemode.getDisplayName()).withStyle(s -> s.withColor(ChatFormatting.AQUA))));
                }
            }

            while (open_tier_screen.consumeClick()) {
                ClientLevel world = client.level;
                if (world != null && client.player != null) {
                    AbstractClientPlayer closestPlayer = null;
                    double closestDistance = 50.0 * 50.0;
                    for (AbstractClientPlayer player : world.players()) {
                        if (player == client.player) continue;
                        double distance = player.distanceToSqr(client.player);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestPlayer = player;
                        }
                    }
                    if (closestPlayer != null) {
                        String pseudo = closestPlayer.getName().getString();
                        RequestManager.fetchPlayerInfo(pseudo, (PlayerInfo info) -> {
                            client.execute(() -> {
                                client.gui.setScreen(new TierScreen(info));
                            });
                        }, null);
                    }
                }
            }
        });
    }

    public static void refreshAllPlayerTags() {
        TierSuffixCache.clear();
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
            for (AbstractClientPlayer player : world.players()) {
                String pseudo = player.getName().getString();
                RequestManager.fetchPlayerInfo(pseudo,
                        (PlayerInfo info) -> {
                            ((TierModifier) player).setSuffix(ShowedTier.showed_tier(info));
                            if (!player.getPassengers().isEmpty()) {
                                for (Entity i : player.getPassengers()) {
                                    if (i instanceof Display.TextDisplay textDisplay) {
                                        if (textDisplay.getText().getString().contains(pseudo)) {
                                            setTextInTextDisplay(info, textDisplay, pseudo);
                                        }
                                    }
                                }
                            }
                        },
                        null);
            }
        }
    }
}
