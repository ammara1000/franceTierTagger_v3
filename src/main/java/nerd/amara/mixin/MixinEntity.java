package nerd.amara.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.Francetiers_tagger;
import nerd.amara.ModConfig;
import nerd.amara.PlayerInfoCache;
import nerd.amara.ShowedTier;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("RETURN"))
    private void francetiers$onTrackedDataSet(EntityDataAccessor<?> data, CallbackInfo ci) {
        if (!((Object) this instanceof Display.TextDisplay textDisplay)) {
            return;
        }
        Entity vehicle = textDisplay.getVehicle();
        if (vehicle instanceof Player player) {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            if (config.enabled && config.showInNametag) {
                String pseudo = player.getName().getString();
                PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
                if (cached != null && cached.result == PlayerInfoCache.Result.FOUND) {
                    String currentText = textDisplay.getText().getString();
                    String suffix = ShowedTier.showed_tier(cached.info, config);
                    if (currentText.contains(pseudo) && suffix != null && !suffix.isEmpty() && !currentText.contains(suffix)) {
                        Francetiers_tagger.setTextInTextDisplay(cached.info, textDisplay, pseudo);
                    }
                }
            }
        }
    }
}
