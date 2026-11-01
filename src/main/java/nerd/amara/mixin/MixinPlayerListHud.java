package nerd.amara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.FontConstants;
import nerd.amara.ModConfig;
import nerd.amara.RequestManager;
import nerd.amara.ShowedTier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import nerd.amara.PlayerInfoCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTabOverlay.class)
public class MixinPlayerListHud {

    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    private Component francetiers$modifyPlayerName(Component original, PlayerInfo entry) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.enabled || !config.showInTabList) {
            return original;
        }

        String pseudo = entry.getProfile().name();
        if (pseudo == null) return original;

        RequestManager.fetchPlayerInfo(pseudo, (info) -> {}, (status) -> {});

        PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
        if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
            String prefix = ShowedTier.showed_tier(cached.info, config);
            if (prefix != null && !prefix.isEmpty()) {
                net.minecraft.network.chat.MutableComponent nameText = net.minecraft.network.chat.Component.empty();
                
                net.minecraft.network.chat.MutableComponent tierComponent = net.minecraft.network.chat.Component.literal(prefix).withStyle(s -> s.withColor(ChatFormatting.WHITE).withFont(FontConstants.getActiveFont(config)));
                
                if (config.tierPosition == ModConfig.TierPosition.RIGHT) {
                    nameText.append(original != null ? original : net.minecraft.network.chat.Component.literal(pseudo));
                    nameText.append(net.minecraft.network.chat.Component.literal(" "));
                    nameText.append(tierComponent);
                } else {
                    nameText.append(tierComponent);
                    nameText.append(net.minecraft.network.chat.Component.literal(" "));
                    nameText.append(original != null ? original : net.minecraft.network.chat.Component.literal(pseudo));
                }
                
                return nameText;
            }
        }
        
        return original != null ? original : Component.literal(pseudo);
    }
}
