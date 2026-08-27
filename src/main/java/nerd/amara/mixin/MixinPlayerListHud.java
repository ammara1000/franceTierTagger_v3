package nerd.amara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.ModConfig;
import nerd.amara.RequestManager;
import nerd.amara.ShowedTier;
import nerd.amara.PlayerInfoCache;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {
    @Unique
    private static final net.minecraft.text.StyleSpriteSource.Font FRTL_FONT = new net.minecraft.text.StyleSpriteSource.Font(Identifier.of("frtl", "lol"));
    @Unique
    private static final net.minecraft.text.StyleSpriteSource.Font SMALL_FRTL_FONT = new net.minecraft.text.StyleSpriteSource.Font(Identifier.of("frtl", "lol_small"));

    @ModifyReturnValue(method = "getPlayerName", at = @At("RETURN"))
    public Text modifyPlayerName(Text original, PlayerListEntry entry) {
        ModConfig config = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.showInTabList) {
            return original;
        }

        String pseudo = entry.getProfile().name();
        if (pseudo == null) return original;

        RequestManager.fetchPlayerInfo(pseudo, (info) -> {}, (status) -> {});

        PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
        if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
            String prefix = ShowedTier.showed_tier(cached.info);
            if (prefix != null && !prefix.isEmpty()) {
                net.minecraft.text.MutableText nameText = net.minecraft.text.Text.empty();
                
                net.minecraft.text.MutableText tierComponent = net.minecraft.text.Text.literal(prefix).styled(s -> s.withColor(Formatting.WHITE).withFont(config.smallIcons ? SMALL_FRTL_FONT : FRTL_FONT));
                
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

