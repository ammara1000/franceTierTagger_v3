package nerd.amara;

import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FontConstants {
    public static final Identifier FONT_REGULAR_ID = Identifier.of("frtl", "lol");
    public static final Identifier FONT_SMALL_ID = Identifier.of("frtl", "lol_small");
    public static final Identifier FONT_OLD_ID = Identifier.of("frtl", "lol_old");

    public static final StyleSpriteSource.Font FONT_REGULAR = new StyleSpriteSource.Font(FONT_REGULAR_ID);
    public static final StyleSpriteSource.Font FONT_SMALL = new StyleSpriteSource.Font(FONT_SMALL_ID);
    public static final StyleSpriteSource.Font FONT_OLD = new StyleSpriteSource.Font(FONT_OLD_ID);

    public static StyleSpriteSource.Font getActiveFont(ModConfig config) {
        if (config.smallIcons) return FONT_SMALL;
        if (config.oldIcons) return FONT_OLD;
        return FONT_REGULAR;
    }

    public static boolean hasTierFont(Text text) {
        if (text == null) return false;
        if (isTierFont(text.getStyle())) return true;
        for (Text sibling : text.getSiblings()) {
            if (hasTierFont(sibling)) return true;
        }
        return false;
    }

    private static boolean isTierFont(Style style) {
        if (style == null) return false;
        StyleSpriteSource source = style.getFont();
        if (source instanceof StyleSpriteSource.Font font) {
            return isTierFontId(font.id());
        }
        return false;
    }

    public static boolean isTierFontId(Identifier id) {
        if (id == null) return false;
        return id.equals(FONT_REGULAR_ID) || id.equals(FONT_SMALL_ID) || id.equals(FONT_OLD_ID);
    }
}
