package nerd.amara;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "francetiers_tagger")
public class ModConfig implements ConfigData {
    
    @ConfigEntry.Category("general")
    public boolean enabled = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Gamemode gamemode = Gamemode.ALL;

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

    public enum Gamemode {
        ALL("All"),
        CRYSTAL("Crystal"),
        SWORD("Sword"),
        UHC("UHC"),
        POT("Pot"),
        NETHPOT("NethPot"),
        SMP("SMP"),
        AXE("Axe"),
        DIASMP("DiaSMP"),
        MACE("Mace"),
        MOD_OFF("Mod Off");

        private final String displayName;

        Gamemode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
