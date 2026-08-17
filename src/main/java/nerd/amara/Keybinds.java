package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;


import java.util.List;

import static nerd.amara.Francetiers_tagger.setTextInTextDisplay;


public class Keybinds {
    private static KeyBinding change_gamemode;
    public static void registerKeybinds(){
        change_gamemode=KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mod.change_gamemode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KeyBinding.Category.create(Identifier.of("francetierstagger"))
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (change_gamemode.wasPressed()){
                me.shedaniel.autoconfig.ConfigHolder<ModConfig> holder = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ModConfig.class);
                ModConfig config = holder.getConfig();
                ModConfig.Gamemode[] values = ModConfig.Gamemode.values();
                int nextIndex = (config.gamemode.ordinal() + 1) % values.length;
                config.gamemode = values[nextIndex];
                holder.save();
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Gamemode Selected: ").append(Text.literal(config.gamemode.getDisplayName()).styled(s->s.withColor(Formatting.AQUA))),true);
                ClientWorld world = MinecraftClient.getInstance().world;

                if (world != null) {
                    for (AbstractClientPlayerEntity player : world.getPlayers()) {
                        String pseudo = player.getName().getString();
                        RequestManager.fetchPlayerInfo(pseudo,
                                (PlayerInfo info) -> ((TierModifier) player).setSuffix(ShowedTier.showed_tier(info)),
                                null);
                        if (!player.getPassengerList().isEmpty()){
                            for (Entity i : player.getPassengerList()){
                                if (i instanceof DisplayEntity.TextDisplayEntity){
                                    DisplayEntity.TextDisplayEntity textDisplay = (DisplayEntity.TextDisplayEntity) i;
                                    if (textDisplay.getText().getString().contains(pseudo)){
                                        RequestManager.fetchPlayerInfo(pseudo,
                                                (PlayerInfo info) -> setTextInTextDisplay(info,textDisplay, pseudo),
                                                (Http.Status status) -> {
                                                    if (status == Http.Status.NOT_FOUND) {
                                                        Francetiers_tagger.LOGGER.debug("{} n'est pas classé sur FranceTiers", pseudo);
                                                    } else {
                                                        Francetiers_tagger.LOGGER.warn("Impossible de récupérer le tier de {} ({})", pseudo, status);
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}