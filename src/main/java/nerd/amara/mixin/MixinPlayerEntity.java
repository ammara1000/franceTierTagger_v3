package nerd.amara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.FontConstants;
import nerd.amara.ModConfig;
import nerd.amara.TierModifier;
import nerd.amara.TierSuffixCache;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
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
    private Text francetiers$modifyDisplayName(Text original) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.enabled || !config.showInNametag) {
            return original;
        }

        String pseudo = ((PlayerEntity) (Object) this).getName().getString();
        String currentSuffix = TierSuffixCache.getSuffix(pseudo);
        if (currentSuffix == null || currentSuffix.isEmpty()) {
            currentSuffix = suffix;
        }
        if (currentSuffix == null || currentSuffix.isEmpty()) {
            return original;
        }

        Text suffixText = Text.literal(currentSuffix).styled(s -> s.withColor(Formatting.WHITE).withFont(FontConstants.getActiveFont(config)));

        if (config.tierPosition == ModConfig.TierPosition.LEFT) {
            return Text.empty().append(suffixText.copy()).append(Text.literal(" ")).append(original.copy());
        } else {
            return Text.empty().append(original.copy()).append(Text.literal(" ")).append(suffixText);
        }
    }
}
