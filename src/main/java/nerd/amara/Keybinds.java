package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;


public class Keybinds {
    private static KeyBinding change_gamemode;
    static List<String> gamemodes = List.of(
            "All",
            "Crystal",
            "Sword",
            "UHC",
            "Pot",
            "NethPot",
            "SMP",
            "Axe",
            "DiaSMP",
            "Mace",
            "Mod Off"
    );
    public static void RedgisterKeybinds(){
        change_gamemode=KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mod.change_gamemode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KeyBinding.Category.create(Identifier.of("francetierstagger"))
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (change_gamemode.wasPressed()){
                ModConfig config = ConfigManager.getConfig();
                String actual_gamemode=config.gamemode;
                Integer index = gamemodes.indexOf(actual_gamemode);
                index++;
                if (index > 10){
                    index=0;
                }
                String new_gamemode=gamemodes.get(index);
                config.gamemode=new_gamemode;
                ConfigManager.save();
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Gamemode Selected: ").append(Text.literal(new_gamemode).styled(s->s.withColor(Formatting.AQUA))),true);
                ClientWorld world = MinecraftClient.getInstance().world;

                if (world != null) {
                    for (AbstractClientPlayerEntity player : world.getPlayers()) {
                        new Thread(() -> {
                            PlayerInfo info = Http.getJson(Francetiers_tagger.web_url+player.getName().getString(), PlayerInfo.class);
                            if (info != null) {
                                ((TierModifier)player).setSuffix(ShowedTier.showed_tier(info));
                                //player.sendMessage(Text.literal(ShowedTier.showed_tier(info)),false);//----
                            }
                        }).start();
                    }
                }
            }
        });
    }
}
