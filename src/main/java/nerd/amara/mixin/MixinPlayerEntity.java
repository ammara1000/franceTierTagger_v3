package nerd.amara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.FontConstants;
import nerd.amara.ModConfig;
import nerd.amara.TierModifier;
import nerd.amara.TierSuffixCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class MixinPlayerEntity implements TierModifier {

    @Unique
    private String suffix = null;

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void setSuffix(String value) {
        this.suffix = value;
    }

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Component francetiers$modifyDisplayName(Component original) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.enabled || !config.showInNametag) {
            return original;
        }

        String pseudo = ((Player) (Object) this).getName().getString();
        String currentSuffix = TierSuffixCache.getSuffix(pseudo);
        if (currentSuffix == null || currentSuffix.isEmpty()) {
            currentSuffix = suffix;
        }
        if (currentSuffix == null || currentSuffix.isEmpty()) {
            return original;
        }

        Component suffixText = Component.literal(currentSuffix).withStyle(s -> s.withColor(ChatFormatting.WHITE).withFont(FontConstants.getActiveFont(config)));

        if (config.tierPosition == ModConfig.TierPosition.LEFT) {
            return Component.empty().append(suffixText.copy()).append(Component.literal(" ")).append(original.copy());
        } else {
            return Component.empty().append(original.copy()).append(Component.literal(" ")).append(suffixText);
        }
    }
}
