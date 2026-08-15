package nerd.amara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import nerd.amara.TierModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity implements TierModifier {
    @Shadow public abstract void remove(Entity.RemovalReason reason);

    @Unique
    private static final StyleSpriteSource.Font FRTL_FONT = new StyleSpriteSource.Font(Identifier.of("frtl", "lol"));

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
    public Text lol(Text original) {
        if (suffix != null && me.shedaniel.autoconfig.AutoConfig.getConfigHolder(nerd.amara.ModConfig.class).getConfig().showInNametag) {
            return original.copy().append(Text.literal(suffix).styled(s -> s.withColor(Formatting.WHITE).withFont(FRTL_FONT)));
        }
        return original;
    }
}