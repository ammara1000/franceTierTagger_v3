package nerd.amara;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class FontConstants {
    public static final Identifier FONT_REGULAR_ID = Identifier.fromNamespaceAndPath("frtl", "lol");
    public static final Identifier FONT_SMALL_ID = Identifier.fromNamespaceAndPath("frtl", "lol_small");
    public static final Identifier FONT_OLD_ID = Identifier.fromNamespaceAndPath("frtl", "lol_old");
    public static final Identifier FONT_OLD_SMALL_ID = Identifier.fromNamespaceAndPath("frtl", "lol_old_small");

    public static final FontDescription.Resource FONT_REGULAR = new FontDescription.Resource(FONT_REGULAR_ID);
    public static final FontDescription.Resource FONT_SMALL = new FontDescription.Resource(FONT_SMALL_ID);
    public static final FontDescription.Resource FONT_OLD = new FontDescription.Resource(FONT_OLD_ID);
    public static final FontDescription.Resource FONT_OLD_SMALL = new FontDescription.Resource(FONT_OLD_SMALL_ID);

    public static FontDescription.Resource getActiveFont(ModConfig config) {
        if (config == null) return FONT_REGULAR;
        if (config.oldIcons) {
            return config.smallIcons ? FONT_OLD_SMALL : FONT_OLD;
        }
        return config.smallIcons ? FONT_SMALL : FONT_REGULAR;
    }

    public static boolean hasTierFont(Component text) {
        if (text == null) return false;
        if (isTierFont(text.getStyle())) return true;
        for (Component sibling : text.getSiblings()) {
            if (hasTierFont(sibling)) return true;
        }
        return false;
    }

    private static boolean isTierFont(Style style) {
        if (style == null) return false;
        FontDescription source = style.getFont();
        if (source instanceof FontDescription.Resource font) {
            return isTierFontId(font.id());
        }
        return false;
    }

    public static boolean isTierFontId(Identifier id) {
        if (id == null) return false;
        return id.equals(FONT_REGULAR_ID) || id.equals(FONT_SMALL_ID) || id.equals(FONT_OLD_ID) || id.equals(FONT_OLD_SMALL_ID);
    }
}
