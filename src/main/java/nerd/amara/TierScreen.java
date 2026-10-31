package nerd.amara;

import nerd.amara.tiers.GamemodeType;
import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.TierRank;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TierScreen extends Screen {

    private final PlayerInfo playerInfo;
    private Identifier playerAvatarId;
    private boolean avatarLoaded = false;
    
    private static final Set<String> REGISTERED_AVATARS = new HashSet<>();

    public TierScreen(PlayerInfo playerInfo) {
        super(Text.translatable("francetiers_tagger.screen.profile_title", playerInfo.pseudo));
        this.playerInfo = playerInfo;
        
        CompletableFuture.runAsync(() -> {
            try {
                String urlStr = "https://mc-heads.net/body/" + playerInfo.pseudo + "/54";
                byte[] data = Http.getBytes(urlStr, Duration.ofSeconds(5));
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                    NativeImage image = NativeImage.read(bais);
                    MinecraftClient.getInstance().execute(() -> {
                        String avatarStr = "avatar_" + playerInfo.pseudo.toLowerCase(Locale.ROOT);
                        this.playerAvatarId = Identifier.of("tier", avatarStr);
                        if (!REGISTERED_AVATARS.contains(avatarStr)) {
                            NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> avatarStr, image);
                            texture.upload();
                            MinecraftClient.getInstance().getTextureManager().registerTexture(this.playerAvatarId, texture);
                            REGISTERED_AVATARS.add(avatarStr);
                        } else {
                            image.close();
                        }
                        this.avatarLoaded = true;
                    });
                }
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(net.minecraft.screen.ScreenTexts.DONE, button -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        
        int windowWidth = 340;
        int windowHeight = 210;
        int startX = (this.width - windowWidth) / 2;
        int startY = (this.height - windowHeight) / 2;

        drawRoundedRect(context, startX, startY, windowWidth, windowHeight, 0xFF111219);
        drawRoundedRectBorder(context, startX, startY, windowWidth, windowHeight, 0xFF252636);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int windowWidth = 340;
        int windowHeight = 210;
        int startX = (this.width - windowWidth) / 2;
        int startY = (this.height - windowHeight) / 2;

        int gridCenterX = startX + 210;
        
        if (this.avatarLoaded && this.playerAvatarId != null) {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, this.playerAvatarId, startX + 23, startY + 40, 0f, 0f, 54, 120, 54, 120);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("francetiers_tagger.screen.loading"), startX + 50, startY + 100, 0xFFAAAAAA);
        }

        Text titleText = Text.translatable("francetiers_tagger.screen.profile_title", playerInfo.pseudo);
        int titleWidth = this.textRenderer.getWidth(titleText);
        
        if (playerInfo.region != null && !playerInfo.region.isEmpty()) {
            String regionText = playerInfo.region.toUpperCase(Locale.ROOT);
            int regionWidth = this.textRenderer.getWidth(regionText);
            int badgeWidth = regionWidth + 8;
            int badgeHeight = 12;
            int totalWidth = titleWidth + 4 + badgeWidth;
            
            int titleStartX = this.width / 2 - totalWidth / 2;
            context.drawTextWithShadow(this.textRenderer, titleText, titleStartX, startY + 10, 0xFFFFFFFF);
            
            int badgeStartX = titleStartX + titleWidth + 4;
            int badgeY = startY + 9;
            
            int regionColor = switch (regionText) {
                case "EU" -> 0xFF599C4B;
                case "NA" -> 0xFF9F3A44;
                case "AS" -> 0xFFE09F3E;
                case "SA" -> 0xFF3E80E0;
                case "AF" -> 0xFFA0522D;
                case "OC" -> 0xFF800080;
                case "ME" -> 0xFFD4A017;
                default -> 0xFF555555;
            };
            
            drawRoundedRect(context, badgeStartX, badgeY, badgeWidth, badgeHeight, regionColor);
            context.drawText(this.textRenderer, regionText, badgeStartX + 4, badgeY + 2, 0xFFFFFFFF, false);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, titleText, this.width / 2, startY + 10, 0xFFFFFFFF);
        }
        
        String rankStr = playerInfo.global_rank != null ? "#" + playerInfo.global_rank : "-";
        String ptsStr = playerInfo.total_points != null ? playerInfo.total_points : "0";
        
        context.drawTextWithShadow(this.textRenderer, Text.translatable("francetiers_tagger.screen.global_rank", rankStr), startX + 15, startY + windowHeight - 20, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("francetiers_tagger.screen.points", ptsStr), startX + windowWidth - 75, startY + windowHeight - 20, 0xFF55FF55);

        String[] row1 = {"Dia SMP", "Pot", "Nethpot", "SMP"};
        String[] row2 = {"Sword", "Vanilla", "Axe", "UHC"};
        String[] row3 = {"Mace"};
        
        int gridY = startY + 45;
        drawRow(context, row1, gridCenterX, gridY);
        drawRow(context, row2, gridCenterX, gridY + 45);
        drawRow(context, row3, gridCenterX, gridY + 90);
    }

    private void drawRow(DrawContext context, String[] modes, int centerX, int y) {
        int spacing = 50;
        int totalWidth = (modes.length - 1) * spacing;
        int startX = centerX - totalWidth / 2;
        
        for (int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            int x = startX + i * spacing;
            GamemodeType modeType = GamemodeType.fromName(mode);
            
            String tierText = "-";
            if (playerInfo.tiers != null) {
                for (Map.Entry<String, nerd.amara.tiers.Tier> entry : playerInfo.tiers.entrySet()) {
                    if (GamemodeType.fromName(entry.getKey()) == modeType) {
                        tierText = entry.getValue().tier;
                        break;
                    }
                }
            }
            
            Text iconText = modeType.getIconText();
            int iconWidth = this.textRenderer.getWidth(iconText);
            
            context.drawText(this.textRenderer, iconText, x - iconWidth / 2, y, 0xFFFFFFFF, false);
            
            TierRank rank = TierRank.fromCode(tierText);
            Text badgeText;
            if (rank == TierRank.NA) {
                badgeText = Text.literal("-");
            } else {
                String iconChar = modeType.getGamemodeChar(rank.getLevel());
                if (!iconChar.isEmpty()) {
                    badgeText = Text.empty()
                        .append(Text.literal(iconChar).setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(FontConstants.FONT_SMALL_ID))))
                        .append(Text.literal(" " + tierText));
                } else {
                    badgeText = Text.literal(tierText);
                }
            }
            
            drawBadge(context, x, y + 16, badgeText, rank.getColor());
        }
    }

    private void drawBadge(DrawContext context, int centerX, int y, Text text, int color) {
        int textWidth = this.textRenderer.getWidth(text);
        int badgeWidth = Math.max(34, textWidth + 12);
        int badgeHeight = 14;
        int startX = centerX - badgeWidth / 2;
        
        drawRoundedRect(context, startX, y, badgeWidth, badgeHeight, color | 0xFF000000);
        context.drawText(this.textRenderer, text, centerX - textWidth / 2, y + 3, 0xFFFFFFFF, false);
    }

    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    private void drawRoundedRectBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + 1, color);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
