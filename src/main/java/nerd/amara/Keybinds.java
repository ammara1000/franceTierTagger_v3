package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;


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
                    }
                }
            }
        });
    }
}