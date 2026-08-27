package nerd.amara.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.Francetiers_tagger;
import nerd.amara.ModConfig;
import nerd.amara.PlayerInfoCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "onTrackedDataSet", at = @At("RETURN"))
    private void onTrackedDataSetInject(TrackedData<?> data, CallbackInfo ci) {
        if ((Object) this instanceof DisplayEntity.TextDisplayEntity textDisplay) {
            Entity vehicle = textDisplay.getVehicle();
            if (vehicle instanceof PlayerEntity player) {
                ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                if (config.showInNametag) {
                    String pseudo = player.getName().getString();
                    PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
                    if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
                        String currentText = textDisplay.getText().getString();
                        String suffix = nerd.amara.ShowedTier.showed_tier(cached.info);
                        if (currentText.contains(pseudo) && suffix != null && !suffix.isEmpty() && !currentText.contains(suffix)) {
                            Francetiers_tagger.setTextInTextDisplay(cached.info, textDisplay, pseudo);
                        }
                    }
                }
            }
        }
    }
}
