package nerd.amara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.FontConstants;
import nerd.amara.ModConfig;
import nerd.amara.RequestManager;
import nerd.amara.ShowedTier;
import nerd.amara.PlayerInfoCache;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {

    @ModifyReturnValue(method = "getPlayerName", at = @At("RETURN"))
    private Text francetiers$modifyPlayerName(Text original, PlayerListEntry entry) {
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
                net.minecraft.text.MutableText nameText = net.minecraft.text.Text.empty();
                
                net.minecraft.text.MutableText tierComponent = net.minecraft.text.Text.literal(prefix).styled(s -> s.withColor(Formatting.WHITE).withFont(FontConstants.getActiveFont(config)));
                
                if (config.tierPosition == ModConfig.TierPosition.RIGHT) {
                    nameText.append(original != null ? original : net.minecraft.text.Text.literal(pseudo));
                    nameText.append(net.minecraft.text.Text.literal(" "));
                    nameText.append(tierComponent);
                } else {
                    nameText.append(tierComponent);
                    nameText.append(net.minecraft.text.Text.literal(" "));
                    nameText.append(original != null ? original : net.minecraft.text.Text.literal(pseudo));
                }
                
                return nameText;
            }
        }
        
        return original != null ? original : Text.literal(pseudo);
    }
}
