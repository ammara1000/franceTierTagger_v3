package nerd.amara.tiers;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public enum GamemodeType {
    ALL("All", -1, "", null),
    MACE("Mace", 0, "\uF007", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    SMP("SMP", 1, "\uF004", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    UHC("UHC", 2, "\uF001", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    POT("Pot", 3, "\uF002", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    CRYSTAL("Crystal", 4, "\uF000", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    SWORD("Sword", 5, "\uF005", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    DIASMP("DiaSMP", 6, "\uF007", Identifier.fromNamespaceAndPath("frtl", "gamemodes/subtiers")),
    NETHPOT("NethPot", 7, "\uF003", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    AXE("Axe", 8, "\uF006", Identifier.fromNamespaceAndPath("frtl", "gamemodes/mctiers")),
    MOD_OFF("Mod Off", -1, "", null);

    private final String displayName;
    private final int offset;
    private final String iconChar;
    private final Identifier iconFontId;

    GamemodeType(String displayName, int offset, String iconChar, Identifier iconFontId) {
        this.displayName = displayName;
        this.offset = offset;
        this.iconChar = iconChar;
        this.iconFontId = iconFontId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getOffset() {
        return offset;
    }

    public String getIconChar() {
        return iconChar;
    }

    public Identifier getIconFontId() {
        return iconFontId;
    }

    public String getGamemodeChar(int level) {
        if (offset == -1) {
            return "";
        }
        int base = switch (level) {
            case 5 -> 0xEC00;
            case 4 -> 0xEC10;
            case 3 -> 0xEC20;
            case 2 -> 0xEC30;
            case 1 -> 0xEC40;
            default -> 0xEF00;
        };
        return String.valueOf((char) (base + offset));
    }

    public Component getIconText() {
        return getIconText(false);
    }

    public Component getIconText(boolean old) {
        if (iconFontId == null || iconChar.isEmpty()) {
            return Component.empty();
        }
        Identifier fontId = iconFontId;
        if (old) {
            fontId = this == DIASMP
                ? Identifier.fromNamespaceAndPath("frtl", "gamemodes/old_subtiers")
                : Identifier.fromNamespaceAndPath("frtl", "gamemodes/old");
        }
        return Component.literal(iconChar).setStyle(Style.EMPTY.withFont(new FontDescription.Resource(fontId)).withColor(0xFFFFFFFF));
    }

    public static GamemodeType fromName(String name) {
        if (name == null || name.isEmpty()) {
            return ALL;
        }
        String clean = name.toLowerCase(Locale.ROOT).replaceAll("[\\s_-]+", "");
        if (clean.equals("vanilla") || clean.equals("crystal")) {
            return CRYSTAL;
        }
        if (clean.equals("diasmp") || clean.equals("diamond") || clean.equals("diamondsmp")) {
            return DIASMP;
        }
        if (clean.equals("netheritepot") || clean.equals("nethpot")) {
            return NETHPOT;
        }
        for (GamemodeType type : values()) {
            String targetClean = type.displayName.toLowerCase(Locale.ROOT).replaceAll("[\\s_-]+", "");
            if (clean.equals(targetClean)) {
                return type;
            }
        }
        return ALL;
    }
}
