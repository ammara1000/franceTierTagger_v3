package nerd.amara;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import nerd.amara.tiers.GamemodeType;

@Config(name = "francetiers_tagger")
public class ModConfig implements ConfigData {
    
    @ConfigEntry.Category("general")
    public boolean enabled = true;

    @ConfigEntry.Category("general")
    public boolean checkForUpdates = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public GamemodeType gamemode = GamemodeType.ALL;

    @ConfigEntry.Category("display")
    public boolean showInNametag = true;

    @ConfigEntry.Category("display")
    public boolean showInTabList = true;

    @ConfigEntry.Category("display")
    public boolean showKitIcon = true;

    @ConfigEntry.Category("display")
    public boolean showParticles = true;

    @ConfigEntry.Category("display")
    public boolean smallIcons = false;

    @ConfigEntry.Category("display")
    public boolean oldIcons = false;

    @ConfigEntry.Category("display")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TierPosition tierPosition = TierPosition.RIGHT;

    public enum TierPosition {
        LEFT("Left"),
        RIGHT("Right");

        private final String displayName;

        TierPosition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public void validatePostLoad() {
        if (gamemode == null) {
            gamemode = GamemodeType.ALL;
        }
        if (tierPosition == null) {
            tierPosition = TierPosition.RIGHT;
        }
    }
}
